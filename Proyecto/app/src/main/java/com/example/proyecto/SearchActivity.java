package com.example.proyecto;

import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    public String table = "fish";
    DatabaseAccess databaseAccess;
    Spinner slocations, smonths, shours, sprices;
    CheckBox capturedCheck;
    AutoCompleteTextView actv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        // abrir conexion ca base de datos
        databaseAccess=DatabaseAccess.getInstance(getApplicationContext());
        databaseAccess.open();
        fillStaticSpinners();
        fillLocationsSpinner();
        fillAutoCompleteTextView();

        Switch tableSwitch = (Switch) findViewById(R.id.switch1);
        tableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    table ="bugs";
                } else {
                   table = "fish";
                }
                fillLocationsSpinner();
                fillAutoCompleteTextView();
            }
        });

        capturedCheck = findViewById(R.id.capturedCheckbox);

    }
    public void fillStaticSpinners(){
        // rellenar os spinners con arrays predefinidos
        smonths= (Spinner) findViewById(R.id.spinner_month);
        ArrayAdapter<CharSequence> madapter = ArrayAdapter.createFromResource(this,
                R.array.months_array , android.R.layout.simple_spinner_item);
        madapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        smonths.setAdapter(madapter);

        shours= (Spinner) findViewById(R.id.spinner_hour);
        ArrayAdapter<CharSequence> hadapter = ArrayAdapter.createFromResource(this,
                R.array.hours_array , android.R.layout.simple_spinner_item);
        hadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shours.setAdapter(hadapter);

        sprices= (Spinner) findViewById(R.id.spinner_price);
        ArrayAdapter<CharSequence> padapter = ArrayAdapter.createFromResource(this,
                R.array.prices_array , android.R.layout.simple_spinner_item);
        padapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprices.setAdapter(padapter);
    }

    public void fillLocationsSpinner(){

        slocations = (Spinner) findViewById(R.id.spinner_location);
        ArrayList<String> locationsList = new ArrayList<>();
        locationsList.clear();
        locationsList.add("Any location");
        // teñen que ser 2 cursores para que non se repitan as locations
        Cursor cursor2 = databaseAccess.spinnerLocations(table);
        if(cursor2.moveToFirst()){
            while (cursor2.moveToNext()){
                locationsList.add(cursor2.getString(0));
            }
        }

        ArrayAdapter<String> locationsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, locationsList);

        locationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        slocations.setAdapter(locationsAdapter);

    }
    public void fillAutoCompleteTextView(){
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        ArrayList<String> namesList = new ArrayList<>();
        namesList.clear();
        namesList.add("Any name");
        Cursor cursor = databaseAccess.spinnerNames(table);
        if(cursor.moveToFirst()){
            while (cursor.moveToNext()){
                namesList.add(cursor.getString(0));
            }
        }
        ArrayAdapter<String> actvadapter = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item, namesList);

        actv.setAdapter(actvadapter);

        actv.setThreshold(1);//que sala o autocompletar a partir de un caracter
        // como quero que salan as opcions sin poñer nincun caracter fixen esto
        actv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                actv.showDropDown();
            }
        });
    }

    public void fillList(View view){
        // sacar os datos e formatealos para a consulta

        String name = actv.getText().toString();

        if (name.contains("Any")){
            name = "";
        }

        String location = slocations.getSelectedItem().toString();
        if (location.contains("Any")){
            location = "";
        }

        String month = smonths.getSelectedItem().toString();
        if (month.contains("Any")){
            month = "";
        }

        String hour = shours.getSelectedItem().toString();
        if (hour.contains("Any")){
            hour = "0-24";
        }
        int TimeStart = Integer.parseInt(hour.split("\\-")[0]);
        int TimeStop = Integer.parseInt(hour.split("\\-")[1]);

        String price = sprices.getSelectedItem().toString();
        if (price.contains("Any")){
            price = "0-15000";
        }
        int price1 = Integer.parseInt(price.split("\\-")[0]);
        int price2 = Integer.parseInt(price.split("\\-")[1]);

        int captured;
        if(capturedCheck.isChecked()){
            captured = 1;
        }else{
            captured = 0;
        }

        // rellenar e mostrar o listview
        // como aqui solo hay un listview que sobreescribo con distintos datos e non uso a columna size esta mellor refactorizado que na main
        ListView resultlistview = findViewById(R.id.ResultList);

        Cursor cursor = databaseAccess.searchResult(table, name, location, month, TimeStart, TimeStop, price1, price2, captured);

        ArrayList<ListItems> resultList = new ArrayList<ListItems>();
        resultList.clear();

        while (cursor.moveToNext()){
            ListItems listItems = new ListItems();
            listItems.setName(cursor.getString(0));
            listItems.setLocation(cursor.getString(1));
            listItems.setPrice(cursor.getInt(2));
            listItems.setCaptured(cursor.getInt(3));
            resultList.add(listItems);
        }

        cursor.close();

        final CustomListAdapter adapter = new CustomListAdapter(this, resultList);

        resultlistview.setAdapter(adapter);

        resultlistview.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent , View view , int position , long id) {
                adapter.updateCapture(position,table);

            }
        }) ;


    }

}
