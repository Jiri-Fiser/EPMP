package cz.ujep.ki.currency2022;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


public class UpdateService extends IntentService {
    Handler handler;
    private static final String uri =
            "https://www.cnb.cz/cs/financni_trhy/devizovy_trh/kurzy_devizoveho_trhu/denni_kurz.xml";

    public UpdateService() {
        super("UpdateService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        URL url;
        InputStream input;

        try {
            url = new URL(uri);
            input = url.openConnection().getInputStream();
        } catch (MalformedURLException e) {
            Log.e("Update service", "Malformed URL");
            return;
        } catch (IOException e) {
            //TODO: toast for users
            Log.e("Update service", "IO Exception " + e.getMessage());
            return;
        }

        XmlPullParser parser = Xml.newPullParser();
        ContentResolver resolver = getContentResolver();
        int updated = 0;
        int inserted = 0;

        try {
            parser.setInput(input, null);
            int eventType = parser.getEventType(); //načtení první konstrukce
            while(eventType != XmlPullParser.END_DOCUMENT) { //dokud není konec dokumentu
                if(eventType == XmlPullParser.START_TAG && //pro každý element "řádek"
                       parser.getName().equals("radek")) {
//vytvoříme kontejner pro db. řád
                    ContentValues val = new ContentValues();
//a vložíme do něj jednotlivé sloupce
                    putAttrString(parser, "kod", val, CurrencyContentProvider.CODE);
                    putAttrString(parser, "mena", val, CurrencyContentProvider.NAME);
                    putAttrInt(parser, "mnozstvi", val, CurrencyContentProvider.AMOUNT);
                    putAttrDouble(parser, "kurz", val, CurrencyContentProvider.RATE);
                    putAttrString(parser, "zeme", val, CurrencyContentProvider.COUNTRY);
//pokusíme se ho updatovat
                    Uri contentUri = Uri.parse(CurrencyContentProvider.CONTENT_URI);
                    int cols = resolver.update(
                            contentUri, val, CurrencyContentProvider.CODE + "=?",
                            new String[]{val.getAsString(CurrencyContentProvider.CODE)});
                    if(cols == 0) { //nepodaří − li se to, tak jej vložíme (insert)
                        resolver.insert(contentUri, val);
                        inserted++;
                    }
                    else {
                        updated++;
                    }
                }
            eventType = parser.next(); //přejdeme na další XML konstrukc
        } //konec cyklu přes prvky XML
    } catch (XmlPullParserException e) {
        Log.e("Update service", "Malformed XML");
        return;
    } catch (IOException e) {
        Log.e("Update service", "Malformed XML");
        return;
    }
        final String toastText = "Done.  Inserted: " + inserted + ". Updated: " + updated;
        handler.post(new Runnable() { //toast from non − GUI thread
            @Override
            public void run() {
                Toast toast = Toast.makeText(getBaseContext(), toastText, Toast.LENGTH_SHORT);
                toast.show();
            }}); //konec volání metody post
    }

    private static void putAttrString(XmlPullParser parser, String attrName,
                                      ContentValues cv, String colName) {
        cv.put(colName, parser.getAttributeValue("", attrName));
    }
    private static void putAttrInt(XmlPullParser parser, String attrName,
                                   ContentValues cv, String colName) {
        cv.put(colName, Integer.parseInt(parser.getAttributeValue("", attrName)));
    }
    private static void putAttrDouble(XmlPullParser parser, String attrName,
                                      ContentValues cv, String colName) {
        String svalue = parser.getAttributeValue("", attrName);
        if (svalue.contains(",")) { //the source uses "," as decimal point
            svalue = svalue.replace(',', '.');
        }
        cv.put(colName, Double.parseDouble(svalue));
    }
}

