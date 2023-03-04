package cz.ujep.ki.mapy2022;

import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class DrawLocationCallback extends LocationCallback {
    MapsActivity activity;
    ArrayList<LatLngAlt> positions;
    ArrayList<Polyline> lines = new ArrayList<>();
    RequestQueue queue = null;


    public DrawLocationCallback(MapsActivity activity) {
        this.activity = activity;
        positions = new ArrayList<>();

        queue =  Volley.newRequestQueue(activity);

        Runnable periodicUpdate = new Runnable() {
            @Override
            public void run() {
                activity.handler.postDelayed(this, 5 * 1000);
                getElevation();
            }
        };        //Iterator<LatLng> it = positions.stream().map(LatLngAlt::toLatLng).iterator();
        //line = map.addPolyline(new PolylineOptions().addAll(() -> it));
        activity.handler.postDelayed(periodicUpdate, 5*1000);
    }

    private void getElevation() {
        StringBuilder posllist = new StringBuilder();
        int poscount = 0;
        for(LatLngAlt p: positions) {
            if(!p.isAltitude()) {
                if(poscount > 0)
                    posllist.append("|");
                posllist.append(p.latitude);
                posllist.append(",");
                posllist.append(p.longitude);
                poscount++;
            }
        }

        if (poscount == 0)
            return;
        String url = "https://api.open-elevation.com/api/v1/lookup?locations=" + posllist.toString();
        Log.d("elevation url", url);
        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d("response thread", new Long(Thread.currentThread().getId()).toString());
                        Log.d("elavation response", response);
                        updateAltitudes(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("elevation error", error.getMessage());
                    }
                });
        queue.add(req);
    }

    void updateAltitudes(String response) {
        int updated = 0;
        try {
            JSONObject root = new JSONObject(response);
            JSONArray rs = root.getJSONArray("results");
            for(int i=0; i<rs.length(); i++) {
                JSONObject item = rs.getJSONObject(i);
                double latitude = item.getDouble("latitude");
                double longitude = item.getDouble("longitude");
                double altitude = item.getDouble("elevation");
                for(LatLngAlt p: positions) {
                    LatLngAlt altp = new LatLngAlt(latitude, longitude);
                    if (!p.isAltitude() && p.distance(altp) < 20) {
                        Log.d("altitude set", p.distance(altp) + "");
                        p.altitude = altitude;
                        updated++;
                    }
                }
            }
            if(updated > 0) {
                updatePolylines();
            }
        } catch (JSONException e) {
            Log.e("JSON", e.getMessage());
        }

    }

    static float sigmoid(float x) {
        return (float)(Math.exp(x)/(Math.exp(x)+1.0));
    }

    int inclinationToColor(LatLngAlt p1, LatLngAlt p2) {
        double x = p1.distance(p2);
        double y = p2.altitude -p1.altitude;
        float incl = (float)(100 * y / x);
        if(incl >= 0 && incl < 8) {
            return Color.HSVToColor(new float[]{0, incl / 8.0f, 1.0f});
        }
        if(incl < 0 && incl > -8) {
            return Color.HSVToColor(new float[]{120, Math.abs(incl) / 8.0f, 1.0f});
        }
        if (incl > 8) {
            float hue = 360.0f - 60.0f * 2.0f*(sigmoid(incl-8) - 0.5f);
            return Color.HSVToColor(new float[]{hue, 1.0f, 1.0f});
        }
        else {
            float hue = 120.0f + 60.0f * 2.0f * (sigmoid(-incl-8) - 0.5f);
            return Color.HSVToColor(new float[]{hue, 1.0f, 1.0f});
        }
    }

   @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onLocationResult(@NonNull LocationResult locationResult) {
        GoogleMap map = activity.mMap;
        if (activity.mMap == null)
                return;
        for (Location loc: locationResult.getLocations()) {
            LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
            positions.add(new LatLngAlt(loc, System.currentTimeMillis()));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
        }

        updatePolylines();
    }

    void updatePolylines() {
        for(int i=0; i<positions.size() - 1; i++) {
            if(i < lines.size() && lines.get(i).getColor() != Color.BLACK)
                continue;

            int c;
            if(positions.get(i).isAltitude() && positions.get(i+1).isAltitude())
                c = inclinationToColor(positions.get(i), positions.get(i+1));
            else
                c = Color.BLACK;
            Polyline newLine = activity.mMap.addPolyline(new PolylineOptions()
                    .add(positions.get(i).toLatLng())
                    .add(positions.get(i+1).toLatLng())
                    .color(c));
            if(i < lines.size()) {
                lines.get(i).remove();
                lines.set(i, newLine);
            }
            else {
                lines.add(newLine);
            }
        }
    }

    public String export() throws IOException {
        try (StringWriter writer = new StringWriter()) {
            GPXExporter exporter = new GPXExporter();
            exporter.export(writer, this.positions);
            return writer.toString();
        }
    }
}

class LatLngAlt {
        public double latitude;
        public double longitude;
        public double altitude;
        public long time;

        public LatLngAlt(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = Double.NaN;
        }

        public LatLngAlt(Location loc, long time) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            altitude = Double.NaN;
            this.time = time;
        }

        public double distance(LatLngAlt other) {
            double dpphi = this.latitude - other.latitude;
            double dlambda = this.longitude - other.longitude;
            double phim = (this.latitude + other.latitude) / 2.0;
            double c_dlambda = Math.cos(Math.PI * phim / 180.0)*dlambda;
            double R = 111100;
            return R * Math.sqrt(dpphi*dpphi + c_dlambda*c_dlambda);
        }

        public boolean isAltitude() {
            return !Double.isNaN(altitude);
        }

        public LatLng toLatLng() {
            return new LatLng(latitude, longitude);
        }
}

