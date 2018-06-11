package com.example.fiodorko.delivery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Dialóg na zadanie nového dátumu doručenia objednávky
 */
@SuppressLint("ValidFragment")
public class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    Button confirm, cancel;
    Context ctx;
    Activity activity;
    int day;
    int month;
    int year;
    Delivery delivery;

    public DateDialog(@NonNull Context context, Activity activity, Delivery delivery) {
        this.activity = activity;
        ctx = context;
        this.delivery = delivery;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Intent resultIntent = new Intent();
        delivery.setDate(getDate());
        resultIntent.putExtra("id", delivery.getId());
        resultIntent.putExtra("delivery", delivery);
        activity.setResult(Activity.RESULT_OK, resultIntent);
        activity.finish();
    }

    public String getDate() {
        return "" + day + "." + month + "." + year;
    }
}


