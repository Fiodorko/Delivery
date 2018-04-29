package com.example.fiodorko.delivery;

import android.annotation.SuppressLint;
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
import java.util.Locale;
import java.util.TimeZone;

public class DeliveryListAdapter extends BaseAdapter {


    private Context context;
    private List<Delivery> deliveryList;

    public DeliveryListAdapter(Context mContext, List<Delivery> mDeliveryList) {
        context = mContext;
        deliveryList = mDeliveryList;
        for (Delivery delivery : deliveryList) {
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
    public Delivery getItem(int position) {
        return deliveryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View v = View.inflate(context, R.layout.delivery_detail_row, null);

        deliveryList.get(0).setFirst(true);

        Delivery actual = deliveryList.get(position);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 1);
            }
        });

        if (actual.getId() == -1) v.findViewById(R.id.detail_button).setVisibility(View.INVISIBLE);

        ViewHolder holder = new ViewHolder();
        holder.detailButton = (ImageButton) v.findViewById(R.id.detail_button);
        holder.detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });


        GeoPoint location = actual.getLocation();
        ImageView image = (ImageView) v.findViewById(R.id.image);
        TextView address = (TextView) v.findViewById(R.id.address);
        TextView duration = (TextView) v.findViewById(R.id.duration);
        TextView distance = (TextView) v.findViewById(R.id.distance);


        SimpleDateFormat date = new SimpleDateFormat("hh.mm.ss", Locale.ENGLISH);
        date.setTimeZone(TimeZone.getDefault());


        if (actual.getId() == -1) {
            image.setImageResource(R.drawable.ic_storage);
            image.setColorFilter(actual.getColor(), PorterDuff.Mode.SRC_ATOP);
        } else {
            image.setImageResource(R.drawable.ic_delivery_package);
            image.setColorFilter(actual.getColor(), PorterDuff.Mode.SRC_ATOP);
            address.setText(context.getString(R.string.address) + actual.getAddress());
        }

        if (actual.getRoad() != null) {
            double time = actual.getRoad().mDuration;
            int hours = (int)time/3600;
            time -= hours*3600;
            int minutes = (int)time/60;
            time -= minutes*60;

            duration.setText(
                    context.getString(R.string.totalDuration) + " "
                            + String.format("%02d", hours) + ":"
                            + String.format("%02d", minutes) + ":"
                            + String.format("%02.2f", time) + "");

            distance.setText(context.getString(R.string.totalDistance) + " " + String.format("%.2f",deliveryList.get(position).getRoad().mLength) + "km");
        }

        v.setTag(deliveryList.get(position).getId());

        return v;
    }
}
