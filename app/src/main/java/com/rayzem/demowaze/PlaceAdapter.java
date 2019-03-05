package com.rayzem.demowaze;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rayzem.demowaze.model.PlaceBO;

import java.util.ArrayList;

class PlaceAdapter extends ArrayAdapter<PlaceBO>{

    static class ViewHolder {
        TextView tv_name;
        TextView tv_location;
    }

    public PlaceAdapter(Context context, int resource) {
        super(context, resource);
    }


    public PlaceAdapter(Context context, ArrayList<PlaceBO> data) {
        super(context, 0, data);
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_place, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tv_name = convertView.findViewById(R.id.tv_name);
            viewHolder.tv_location = convertView.findViewById(R.id.tv_location);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        PlaceBO placeBO = getItem(position);
        viewHolder.tv_name.setText(placeBO.getName());
        viewHolder.tv_location.setText(placeBO.getLocation());
        return convertView;
    }
}
