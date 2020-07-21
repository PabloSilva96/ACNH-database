package com.example.proyecto;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class CustomListAdapter extends BaseAdapter {
    Context context;
    ArrayList<ListItems> fishList;


    public CustomListAdapter(Context context, ArrayList<ListItems> list) {

        this.context = context;
        fishList = list;
    }


    @Override
    public int getCount() {

        return fishList.size();
    }

    @Override
    public Object getItem(int position) {

        return fishList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {

        // o do holder e porque os listview reciclan as vistas entonces cando se usaba o scroll non se monstraban os datos correctamente
        ViewHolder holder;

        ListItems listItems = fishList.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_entry, null);

            holder = new ViewHolder();

            //instanciar os listview
            holder.name= convertView.findViewById(R.id.name);
            holder.location= convertView.findViewById(R.id.location);
            holder.size= convertView.findViewById(R.id.size);
            holder.price= convertView.findViewById(R.id.price);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder) convertView.getTag();
        }
        // coller os valores de ListItems e metelos no listview
        holder.name.setText(listItems.getName());
        holder.location.setText(listItems.getLocation());
        holder.size.setText(listItems.getSize());
        holder.price.setText(String.valueOf(listItems.getPrice()));

        // cambiar color
        if (listItems.getCaptured()==1){
            holder.name.setTextColor(Color.GREEN);
        }else{
            holder.name.setTextColor(Color.WHITE);
        }

        return convertView;
    }
    static class ViewHolder{
        TextView name;
        TextView location;
        TextView size;
        TextView price;

    }

    public void updateCapture(int position,String table){
        ListItems listItems = fishList.get(position);
        DatabaseAccess databaseAccess=DatabaseAccess.getInstance(context.getApplicationContext());
        databaseAccess.open();
        if(listItems.getCaptured()== 0) {
            //marcar como capturado
            databaseAccess.capture(table,listItems.getName(),1);
            // si solo o cambiara na base de datos non se actualizaria no programa hasta reinicialo
            listItems.setCaptured(1);
            notifyDataSetChanged();

        } else if (listItems.getCaptured()== 1){
            databaseAccess.capture(table,listItems.getName(), 0);
            listItems.setCaptured(0);
            notifyDataSetChanged();
        }

    }
}