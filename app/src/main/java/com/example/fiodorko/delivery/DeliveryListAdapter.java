package com.example.fiodorko.delivery;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.media.Image;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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
        for (Delivery delivery: deliveryList) {
            delivery.setFirst(false);
        }
    }

    private class ViewHolder {
        public ImageButton detailButton;
    }

    @Override
    public int getCount() {
        return deliveryList.size();
    }

    @Override
    public Delivery getItem(int position)
    {
        return deliveryList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View v = View.inflate(context, R.layout.delivery_detail_row, null);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 1);
            }
        });

        deliveryList.get(0).setFirst(true);


        ViewHolder holder = new ViewHolder();
        holder.detailButton = (ImageButton) v.findViewById(R.id.detail_button);
        holder.detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });



        GeoPoint location = deliveryList.get(position).getLocation();
        ImageView image = (ImageView) v.findViewById(R.id.image);
        TextView address = (TextView)v.findViewById(R.id.address);
        TextView duration = (TextView)v.findViewById(R.id.duration);
        TextView distance = (TextView)v.findViewById(R.id.distance);


        SimpleDateFormat date = new SimpleDateFormat("HH.mm.ss.SSS");
        date.setTimeZone(TimeZone.getDefault());


        image.setImageResource(R.drawable.ic_delivery_package);
        image.setColorFilter( deliveryList.get(position).getColor(), PorterDuff.Mode.SRC_ATOP );
        address.setText("Adresa: " + deliveryList.get(position).getAddress());
        duration.setText("Odhadovaný čas " + date.format((long)deliveryList.get(position).getDuration()*1000 ));
        distance.setText("Vzdialenosť: " + deliveryList.get(position).getDistance() + "Km");


        v.setTag(deliveryList.get(position).getId());


        return v;
    }
}
