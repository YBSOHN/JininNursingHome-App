package com.estimote.proximity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LocationMetaAdapter extends BaseAdapter {
    private Context mContext = null;
    private int layout = 0;
    private ArrayList<LocationMeta> data = null;
    private LayoutInflater inflater = null;

    public LocationMetaAdapter(Context c, int l, ArrayList<LocationMeta> d) {
        this.mContext = c;
        this.layout = l;
        this.data = d;
        this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        if (convertView == null) {
            convertView = inflater.inflate(this.layout, parent, false);
        }

        TextView nameView = (TextView) convertView.findViewById(R.id.et_name);

        nameView.setText(data.get(position).name);

        return convertView;
    }
}
