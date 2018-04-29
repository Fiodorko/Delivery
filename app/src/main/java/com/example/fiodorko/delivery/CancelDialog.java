package com.example.fiodorko.delivery;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class CancelDialog extends Dialog implements
        android.view.View.OnClickListener, AdapterView.OnItemSelectedListener{

    public Activity activity;
    public Button confirm, cancel;
    Spinner reasonSpinner;
    Delivery delivery;
    String reason = "";

    public CancelDialog(Activity activity, Delivery delivery) {
        super(activity);
        this.delivery = delivery;
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cancel_dialog);
        confirm = findViewById(R.id.confirm_cancel);
        cancel = findViewById(R.id.cancel_cancel);
        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);
        reasonSpinner = findViewById(R.id.reason_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                activity, R.array.reasons, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(R.layout.dropdown_item);

        reasonSpinner.setAdapter(adapter);
        reasonSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_cancel:
                if(reason.equals("Dôvod"))
                {
                    Toast.makeText(this.getContext(), "Zadajte dôvod zrušenia", Toast.LENGTH_SHORT).show();
                } else
                    {
                        Intent resultIntent = new Intent();
                        delivery.setStatus("Zrušená z dôvodu:" + reason);
                        resultIntent.putExtra("id", delivery.getId());
                        resultIntent.putExtra("delivery", delivery);
                        activity.setResult(Activity.RESULT_OK, resultIntent);
                        activity.finish();
                    }
                break;
            case R.id.cancel_cancel:
                dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        reason = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
