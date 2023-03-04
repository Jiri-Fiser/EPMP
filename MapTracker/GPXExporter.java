package cz.ujep.ki.mapy2022;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class GPXExporter {
    public void export(Writer out, List<LatLngAlt> positions) throws IOException {
            out.write("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\">\n" +
                    "  <metadata></metadata>\n" +
                    "  <trk><name>Track</name>\n" +
                    "    <trkseg>");

            for(LatLngAlt pos : positions) {
                Date date = new Date(pos.time);

                SimpleDateFormat sdf;
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("Z"));
                String text = sdf.format(date);
                out.write(String.format("<trkpt lat=\"%f\" lon=\"%f\">\n", pos.latitude, pos.longitude) +
                        String.format(" <ele>%f</ele> <time>%s</time>\n",pos.altitude, text ) +
                        " </trkpt>");
            }
            out.write("</trkseg>\n" +
                    "  </trk>\n" +
                    "</gpx>");
    }
}
