package com.example.fiodorko.delivery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Zobrazuje položky objednávku v liste
 */
public class ItemListAdapter extends BaseAdapter {

    private Context context;
    private List<Item> itemList;

    ItemListAdapter(Context mContext, List<Item> mItemList) {
        context = mContext;
        itemList = mItemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View v = View.inflate(context, R.layout.item_detail_row, null);

        TextView identifier = v.findViewById(R.id.identifier);

        ImageView completed = v.findViewById(R.id.completed);

        if (itemList.get(position).isCompleted()) completed.setVisibility(View.VISIBLE);

        identifier.setText(itemList.get(position).getId());

        return v;
    }
}
