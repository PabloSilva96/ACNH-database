package com.example.proyecto;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DatabaseAccess instance;
    Cursor c = null;

    // e privado para que non se poidan crear objetos fora de esta clase
    private DatabaseAccess(Context context){
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context){
        if(instance == null){
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    //abrir conexion ca base
    public void open(){
        this.db=openHelper.getWritableDatabase();
    }
    //cerrar
    public void close(){
        if(db !=null){
            this.db.close();
        }
    }

    //consultas aqui
    public Cursor fishnow(String month, String hour){

        c = db.rawQuery("select name, location, size, price, captured from fish where Month like'%"+month+"%' and TimeStart <= "+hour+" and ((TimeStop > "+hour+") or (TimeStop < "+hour+" and TimeStop < TimeStart)) ",new String[]{});

        return c;
    }
    public Cursor bugsnow(String month, String hour){

        c = db.rawQuery("select name, location, price, captured from bugs where Month like'%"+month+"%' and TimeStart <= "+hour+" and ((TimeStop > "+hour+") or (TimeStop < "+hour+" and TimeStop < TimeStart)) ",new String[]{});

        return c;
    }

    public void capture(String table, String name, int captured){
        db.execSQL("update "+table+" set captured = "+captured+" where name = '"+name+"'");
    }

    public Cursor spinnerNames(String table){

        c = db.rawQuery("select name from "+table+"",new String[]{});

        return c;
    }
    public Cursor spinnerLocations(String table){

        c = db.rawQuery("select distinct location from "+table+"",new String[]{});

        return c;
    }

    public Cursor searchResult(String table, String name, String location, String month, int TimeStart, int TimeStop, int price1, int price2, int captured){

        c = db.rawQuery("select name, location, price, captured from "+table+" where name like '%"+name+"%' and location like '%"+location+"%' and Month like '%"+month+"%' and TimeStart >= "+TimeStart+" and TimeStop < "+TimeStop+" and (captured = "+captured+" or captured = 0) and price between "+price1+" and "+price2+" ",new String[]{});

        return c;

    }


}
