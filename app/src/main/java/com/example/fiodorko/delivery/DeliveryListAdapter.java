package com.example.fiodorko.delivery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Zobrazuje objednávky v liste
 */
public class DeliveryListAdapter extends BaseAdapter {


    private Context context;
    private List<Delivery> deliveryList;

    DeliveryListAdapter(Context mContext, List<Delivery> mDeliveryList) {
        context = mContext;
        deliveryList = mDeliveryList;
        for (Delivery delivery : deliveryList) {
            delivery.setFirst(false);
        }
    }

    private class ViewHolder {
        ImageButton detailButton;
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

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        @SuppressLint("ViewHolder") View v = View.inflate(context, R.layout.delivery_detail_row, null);

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
        holder.detailButton = v.findViewById(R.id.detail_button);
        holder.detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });


        ImageView image = v.findViewById(R.id.image);
        TextView address = v.findViewById(R.id.address);
        TextView duration = v.findViewById(R.id.duration);
        TextView distance = v.findViewById(R.id.distance);


//        SimpleDateFormat date = new SimpleDateFormat("hh.mm.ss", Locale.ENGLISH);
//        date.setTimeZone(TimeZone.getDefault());


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
            int hours = (int) time / 3600;
            time -= hours * 3600;
            int minutes = (int) time / 60;
            time -= minutes * 60;

            duration.setText(
                    context.getString(R.string.totalDuration) + " "
                            + String.format("%02d", hours) + ":"
                            + String.format("%02d", minutes) + ":"
                            + String.format("%02.2f", time) + "");

            distance.setText(context.getString(R.string.totalDistance) + " " + String.format("%.2f", deliveryList.get(position).getRoad().mLength) + "km");
        }

        v.setTag(deliveryList.get(position).getId());

        return v;
    }
}
