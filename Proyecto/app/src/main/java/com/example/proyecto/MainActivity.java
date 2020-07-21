package com.example.proyecto;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public String month;
    public String hour;
    ListView fishlistview;
    ListView bugslistview;
    Intent svc;
    Boolean playing = false;
    String table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        // dialogo a primeira vez que se ejecuta
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);

        if (firstStart) {
            showStartDialog();
        }

        // toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // fechas
        getTime();

        // elementos en pantalla
        fishlistview = findViewById(R.id.fish);
        bugslistview = findViewById(R.id.bugs);

        // abrir conexion ca base de datos
        DatabaseAccess databaseAccess=DatabaseAccess.getInstance(getApplicationContext());
        databaseAccess.open();

        //peixes
        Cursor cursor = databaseAccess.fishnow(month,hour);

        ArrayList<ListItems> fishList = new ArrayList<ListItems>();
        fishList.clear();

        if(cursor.moveToFirst()){
            while (cursor.moveToNext()){
                ListItems listItems = new ListItems();
                listItems.setName(cursor.getString(0));
                listItems.setLocation(cursor.getString(1));
                listItems.setSize(cursor.getString(2));
                listItems.setPrice(cursor.getInt(3));
                listItems.setCaptured(cursor.getInt(4));
                fishList.add(listItems);
            }
        }
        cursor.close();

        final CustomListAdapter adapter = new CustomListAdapter(this, fishList);

        fishlistview.setAdapter(adapter);

        fishlistview.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent , View view , int position , long id) {
                table= "fish";
                adapter.updateCapture(position,table);
            }
        }) ;

        // bichos
        Cursor cursor2 = databaseAccess.bugsnow(month,hour);

        ArrayList<ListItems> bugsList = new ArrayList<ListItems>();
        bugsList.clear();

        if(cursor2.moveToFirst()){
            while (cursor2.moveToNext()){
                ListItems listItems = new ListItems();
                listItems.setName(cursor2.getString(0));
                listItems.setLocation(cursor2.getString(1));
                listItems.setPrice(cursor2.getInt(2));
                listItems.setCaptured(cursor2.getInt(3));
                bugsList.add(listItems);
            }
        }
        cursor2.close();

        final CustomListAdapter adapter2 = new CustomListAdapter(this, bugsList);

        bugslistview.setAdapter(adapter2);

        bugslistview.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent , View view , int position , long id) {
                table="bugs";
                adapter2.updateCapture(position, table);
            }
        }) ;

        //cerrar
        databaseAccess.close();

    }
    private void showStartDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("This app requires access to the internal storage of the phone")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }
    private void getTime(){
        DateTimeFormatter monthformat = DateTimeFormatter.ofPattern("MMMM");
        monthformat = monthformat.withLocale( Locale.US ); // pasa do idioma do dispositivo a inglés
        DateTimeFormatter hourformat = DateTimeFormatter.ofPattern("H");
        LocalDateTime now = LocalDateTime.now();
        month = monthformat.format(now);
        hour = hourformat.format(now);
    }



    public void launchSearchActivity(){
        Intent miIntent = new Intent(this, SearchActivity.class);
        startActivity(miIntent);
    }
    // inflar o menu
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    // cando se clica unha opcion do menu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.mute:
                if(!playing){
                    svc=new Intent(this, BackgroundSoundService.class);
                    startService(svc);
                    playing = true;
                    item.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_lock_silent_mode_off));
                }else if (playing){
                    svc=new Intent(this, BackgroundSoundService.class);
                    stopService(svc);
                    playing = false;
                    item.setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_lock_silent_mode));
                }

                return true;

            case R.id.app_bar_search:

                launchSearchActivity();

                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }



    // cando se pulsa o boton de retroceso
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(playing){
            stopService(svc);
        }

    }
    // cando se blockea a pantalla
    @Override
    protected void onPause() {
        super.onPause();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isInteractive();
        }
        if (!isScreenOn && playing) {
            stopService(svc);
        }

    }
}
