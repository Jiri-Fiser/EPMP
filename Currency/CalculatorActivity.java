package cz.ujep.ki.currency2022;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class CalculatorActivity extends AppCompatActivity {

    private EditText fcAmount;
    private EditText hcAmount;
    private TextView fcCode;
    private Spinner tax;
    private TextView cTitle;

    private double rate;
    private double taxrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        cTitle = findViewById(R.id.cTitle);
        fcAmount = (EditText)findViewById(R.id.fcAmount);
        hcAmount = (EditText)findViewById(R.id.hcAmount);
        fcCode = (TextView)findViewById(R.id.fcLabel);
        tax = (Spinner)findViewById(R.id.tax);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        cTitle.setText(b.getString(CurrencyContentProvider.NAME) + " (" +
                b.getString(CurrencyContentProvider.COUNTRY) + ")");
        fcCode.setText(b.getString(CurrencyContentProvider.CODE) + ": ");
        fcAmount.setText("1");
        fcAmount.requestFocus();

        rate = b.getDouble(CurrencyContentProvider.RATE) / b.getInt(CurrencyContentProvider.AMOUNT);
        taxrate = 1.0;

        refresh(fcAmount, hcAmount, rate * taxrate);

        fcAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(fcAmount.hasFocus()) {
                    refresh(fcAmount, hcAmount, rate * taxrate);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        hcAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(hcAmount.hasFocus()) {
                    refresh(hcAmount, fcAmount, 1.0 / (rate * taxrate));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.taxes,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tax.setAdapter(adapter);
        tax.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String taxString = (String) tax.getItemAtPosition(position);
                int inttax = Integer.parseInt(taxString.substring(0, taxString.length() - 1));
                taxrate = 1.0 + inttax/100.0;

                if(hcAmount.hasFocus()) {
                    refresh(hcAmount, fcAmount, 1.0/(rate * taxrate));
                }
                if(fcAmount.hasFocus()) {
                    refresh(fcAmount, hcAmount, rate * taxrate);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void refresh(EditText source, EditText target, double rate) {
        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
        double c;
        String sourceText = source.getText().toString();
        if (!sourceText.isEmpty()) {
            try {
                c = format.parse(sourceText).doubleValue();
            } catch (ParseException e) {
                c = 0.0;
            }
        }
        else
            c = 0.0;

        double targetAmount = rate * c;
        target.setText(format.format(targetAmount));
    }
}