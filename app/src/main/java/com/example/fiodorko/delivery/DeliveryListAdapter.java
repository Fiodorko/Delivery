package com.example.fiodorko.delivery;

import android.content.Context;
import android.icu.util.Calendar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class DeliveryListAdapter extends BaseAdapter{

    private Context context;
    private List<Delivery> deliveryList;

    public DeliveryListAdapter(Context mContext, List<Delivery> mDeliveryList) {
        context = mContext;
        deliveryList = mDeliveryList;
    }

    @Override
    public int getCount() {
        return deliveryList.size();
    }

    @Override
    public Delivery getItem(int position) { return deliveryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(context, R.layout.delivery_detail_row, null);
        //TextView recipient = (TextView)v.findViewById(R.id.recipient);
//        TextView address = (TextView)v.findViewById(R.id.address);
//        TextView date = (TextView)v.findViewById(R.id.date);
//        TextView phone = (TextView)v.findViewById(R.id.phone);

        GeoPoint location = deliveryList.get(position).getLocation();
        ImageView image = (ImageView) v.findViewById(R.id.image);
        TextView address = (TextView)v.findViewById(R.id.address);
        TextView duration = (TextView)v.findViewById(R.id.duration);
        TextView distance = (TextView)v.findViewById(R.id.distance);


        SimpleDateFormat date = new SimpleDateFormat("HH.mm.ss.SSS");
        date.setTimeZone(TimeZone.getDefault());


        image.setImageResource(R.drawable.package_img);
        image.setBackgroundColor(deliveryList.get(position).getColor());
        address.setText("Adresa: " + deliveryList.get(position).getAddress());
        duration.setText("Odhadovaný čas " + date.format((long)deliveryList.get(position).getDuration()*1000 ));
        distance.setText("Vzdialenosť: " + deliveryList.get(position).getDistance() + "Km");

        //recipient.setText("Adresát: " + deliveryList.get(position).getRecipient());
//        address.setText("Adresa: " + deliveryList.get(position).getAddress());
//        date.setText("Dátum: " + deliveryList.get(position).getDate());
//        phone.setText("Telefónny kontakt: " + deliveryList.get(position).getPhone() + " ID" + deliveryList.get(position).getId());

        v.setTag(deliveryList.get(position).getId());


        return v;
    }
}
