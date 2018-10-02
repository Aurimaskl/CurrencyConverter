package com.example.android.currencyconverter;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText amount;
    private Spinner convertFrom;
    private Spinner convertTo;
    private Button convertButton;
    private TextView result1;
    private TextView result2;
    private TextView result3;
    private TextView valueEurTv;
    private TextView valueUsdTv;
    private TextView valueJpyTv;
    private SharedPreferences prefs;
    private Double currency_from_value;
    private String base_URL = "http://api.evp.lt/currency/commercial/exchange/"; //q=EUR_USD&compact=y
    private AQuery aq;
    double valueEur;
    double valueUsd;
    double valueJpy;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        amount = (EditText) findViewById(R.id.amountToConvert);
        convertFrom = (Spinner) findViewById(R.id.convertFrom);
        convertTo = (Spinner) findViewById(R.id.convertTo);
        convertButton = (Button) findViewById(R.id.convertButton);
        result1 = (TextView) findViewById(R.id.result1);
        result2 = (TextView) findViewById(R.id.result2);
        result3 = (TextView) findViewById(R.id.result3);
        valueEurTv = (TextView) findViewById(R.id.valueEur);
        valueUsdTv = (TextView) findViewById(R.id.valueUsd);
        valueJpyTv = (TextView) findViewById(R.id.valueJpy);
        prefs = getSharedPreferences("PREFS", 0);
        aq = new AQuery(this);
        Button reset = (Button) findViewById(R.id.clear);
        // make switch button for switching currencies
//        Button switchCurrencies = (Button) findViewById(R.id.switchButton);
        valueEur = Double.valueOf(prefs.getString("EURValue", "1000"));
        valueUsd = Double.valueOf(prefs.getString("USDValue", "0"));
        valueJpy = Double.valueOf(prefs.getString("JPYValue", "0"));
        valueEurTv.setText(prefs.getString("EURValue", "1000") + " EUR");
        valueUsdTv.setText(prefs.getString("USDValue", "0") + " USD");
        valueJpyTv.setText(prefs.getString("JPYValue", "0") + " JPY");
        reset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                result1.setText("");
                result2.setText("");
                amount.setText("");

            }
        });


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currency_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        convertFrom.setAdapter(adapter);
        convertTo.setAdapter(adapter);
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (amount.getText().toString().length() < 1) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.enter_value), Toast.LENGTH_SHORT).show();
                } else {

                    currency_from_value = Double.valueOf(amount.getText().toString());
                    double x = 0;
                    switch (convertFrom.getSelectedItem().toString()) {
                        case ("EUR"):
                            x = valueEur;
                            break;
                        case ("USD"):
                            x = valueUsd;
                            break;
                        case ("JPY"):
                            x = valueJpy;
                            break;
                    }
                    String convert_from_currency = String.valueOf(convertFrom.getSelectedItem());
                    String convert_to_currency = String.valueOf(convertTo.getSelectedItem());
                    double y = 1;
                    if (prefs.getInt(getResources().getString(R.string.triesLeft), 5) <= 0)
                        y = 1.007;
                    if (x - currency_from_value * y > 0 && !convert_from_currency.equals(convert_to_currency)) {
                        String url = base_URL + currency_from_value.toString() + "-" + convert_from_currency + "/" + convert_to_currency + "/latest";

                        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {

                            @SuppressLint("DefaultLocale")
                            @Override
                            public void callback(String url, JSONObject json, AjaxStatus status) {

                                if (json != null) {

                                    try {
                                        Double answer = json.getDouble("amount");


                                        Log.d("Stored in memory", url);

//                                    double answer = currency_from_value * amount;

                                        String stringResult1;
                                        String stringResult2;
                                        String stringResult3;
                                        stringResult1 = getResources().getString(R.string.conversion_from) + amount.getText().toString() + " " + convertFrom.getSelectedItem().toString() + getResources().getString(R.string.to);
                                        stringResult2 = String.format("%.2f", answer) + " " + convertTo.getSelectedItem().toString() + ".";
                                        stringResult3 = getResources().getString(R.string.commission) + " - " + applyCommissions(currency_from_value, answer) + " EUR ";
                                        result1.setText(stringResult1);
                                        result2.setText(stringResult2);
                                        result3.setText(stringResult3);
                                        //No internet implementation
                                        SharedPreferences.Editor editor = prefs.edit();
                                        //coefficient for offline conversion
                                        Double coefficient = (answer / currency_from_value);
                                        editor.putString(convertFrom.getSelectedItem().toString() + convertTo.getSelectedItem().toString(), coefficient.toString());
                                        editor.apply();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    //what happens if there is no internet
                                    Log.e("~~~ no Internet ~~~", "~~~~~~ no Internet ~~~~~~");

                                    //retrieval back of string from currencyFrom+currencyTo, will be unique rate for every conversion, in case that 1 jpy won't be equal to 1 usd when converting from eur
                                    String value_from_memory = prefs.getString(convertFrom.getSelectedItem().toString() + convertTo.getSelectedItem().toString(), "");

                                    if (value_from_memory.equals("")) {

                                        Toast.makeText(MainActivity.this, getResources().getString(R.string.run_no_internet), Toast.LENGTH_LONG).show();

                                    } else {
                                        Double preanswer = Double.parseDouble(value_from_memory);
                                        //get, calc, put ans
                                        Double answer = currency_from_value * preanswer;
                                        String stringResult1;
                                        String stringResult2;
                                        String stringResult3;
                                        stringResult1 = getResources().getString(R.string.conversion_from) + amount.getText().toString() + " " + convertFrom.getSelectedItem().toString() + getResources().getString(R.string.to);
                                        stringResult2 = String.format("%.2f", answer) + " " + convertTo.getSelectedItem().toString() + ".";
                                        stringResult3 = getResources().getString(R.string.commission) + " - " + applyCommissions(currency_from_value, answer) + " EUR ";
                                        result1.setText(stringResult1);
                                        result2.setText(stringResult2);
                                        result3.setText(stringResult3);
                                    }

                                }

                            }

                        });

                    } else
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.insufficient), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private String applyCommissions(double currencyFromValue, double currencyToValue) {
        SharedPreferences.Editor editor = prefs.edit();
        String answer;
        int timesLeft = prefs.getInt(getResources().getString(R.string.triesLeft), 5);
        double currentCurrency = 0;
        double com;
        switch (convertFrom.getSelectedItem().toString()) {
            case ("EUR"):
                currentCurrency = valueEur;
                break;
            case ("USD"):
                currentCurrency = valueUsd;
                break;
            case ("JPY"):
                currentCurrency = valueJpy;
                break;
        }

        if (timesLeft > 0) {
            currentCurrency = currentCurrency - currencyFromValue;
            timesLeft--;
            //timesLeft = timesLeft -1;
            answer = "0";
            editor.putInt(getResources().getString(R.string.triesLeft), timesLeft);
            editor.apply();
        } else {
            com = (currencyFromValue * 0.007);
            currentCurrency = (currentCurrency - currencyFromValue) - com;
            answer = String.format("%.2f", com);
        }
        switch (convertFrom.getSelectedItem().toString()) {
            case ("EUR"):
                valueEur = currentCurrency;
                String vk = new DecimalFormat("##.##").format(valueEur);
                valueEurTv.setText(vk + " EUR");
                break;
            case ("USD"):
                valueUsd = currentCurrency;
                String vk1 = new DecimalFormat("##.##").format(valueUsd);
                valueUsdTv.setText(vk1 + " USD");
                break;
            case ("JPY"):
                valueJpy = currentCurrency;
                String vk2 = new DecimalFormat("##.##").format(valueJpy);
                valueJpyTv.setText(vk2 + " JPY");
                break;
        }
        switch (convertTo.getSelectedItem().toString()) {
            case ("EUR"):
                valueEur = valueEur + currencyToValue;
                String mk = new DecimalFormat("##.##").format(valueEur);
                valueEurTv.setText(mk + " EUR");
                break;
            case ("USD"):
                valueUsd = valueUsd + currencyToValue;
                String mk1 = new DecimalFormat("##.##").format(valueUsd);
                valueUsdTv.setText(mk1 + " USD");
                break;
            case ("JPY"):
                valueJpy = valueJpy + currencyToValue;
                String mk2 = new DecimalFormat("##.##").format(valueJpy);
                valueJpyTv.setText(mk2 + " JPY");
                break;
        }
        return answer;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("EURValue", String.valueOf(valueEur));
        editor.putString("USDValue", String.valueOf(valueUsd));
        editor.putString("JPYValue", String.valueOf(valueJpy));
        editor.apply();
    }
}

