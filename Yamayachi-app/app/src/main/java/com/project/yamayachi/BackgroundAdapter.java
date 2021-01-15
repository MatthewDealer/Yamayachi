package com.project.yamayachi;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BackgroundAdapter extends BaseAdapter {

    private Context context;
    private int[] maps;
    LayoutInflater inflater;


    public BackgroundAdapter(Context e, int[] bgs){
        this.context = e;
        this.maps = bgs;
    }

    @Override
    public int getCount() {
        return maps.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if( inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.row_item, null);

        ImageView im = convertView.findViewById(R.id.bg_select);
        TextView tx = convertView.findViewById(R.id.bg_text);

        im.setImageResource(maps[position]);
        tx.setText("background "+ position);

        return  convertView;
    }
}
