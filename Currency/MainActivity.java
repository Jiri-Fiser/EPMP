package cz.ujep.ki.currency2022;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity  {
    ListView view;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.cview);

        cursor = getCursor();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.item,
                cursor,
                new String[]{CurrencyContentProvider.CODE,
                             CurrencyContentProvider.NAME,
                             CurrencyContentProvider.COUNTRY,
                             CurrencyContentProvider.AMOUNT,
                             CurrencyContentProvider.RATE},
                new int[]{R.id.code, R.id.name, R.id.country, R.id.amount, R.id.rate});
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                cursor.moveToPosition(position);
                Bundle b = new Bundle();
                addStringColumn(b, CurrencyContentProvider.CODE);
                addStringColumn(b, CurrencyContentProvider.NAME);
                addStringColumn(b, CurrencyContentProvider.COUNTRY);
                addIntColumn(b, CurrencyContentProvider.AMOUNT);
                addDoubleColumn(b, CurrencyContentProvider.RATE);

                Intent intent = new Intent(MainActivity.this, CalculatorActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    void addStringColumn(Bundle b, String column) {
        int index = cursor.getColumnIndex(column);
        b.putString(column, cursor.getString(index));
    }

    void addIntColumn(Bundle b, String column) {
        int index = cursor.getColumnIndex(column);
        b.putInt(column, cursor.getInt(index));
    }

    void addDoubleColumn(Bundle b, String column) {
        int index = cursor.getColumnIndex(column);
        b.putDouble(column, cursor.getDouble(index));
    }

    private Cursor getCursor() {
        Cursor c = managedQuery(Uri.parse(CurrencyContentProvider.CONTENT_URI),
                new String[]{CurrencyContentProvider._ID,
                             CurrencyContentProvider.CODE,
                             CurrencyContentProvider.NAME,
                             CurrencyContentProvider.COUNTRY,
                             CurrencyContentProvider.AMOUNT,
                             CurrencyContentProvider.RATE}, null, null,
                CurrencyContentProvider.NAME + " ASC");
        return c;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.update:
                Intent intent = new Intent(this, UpdateService.class);
                startService(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}