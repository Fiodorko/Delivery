package com.example.fiodorko.delivery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DeliveryOptions extends Activity {

    public static String date;

    private final int SCAN_REQUEST = 0;
    private final int SIGN_REQUEST = 1;
    private final int CANCEL_REQUEST = 2;
    private final int DELAY_REQUEST = 3;

    private String TAG = "Options";

    private ImageButton cancelButton;
    private ImageButton holdButton;
    private ImageButton signButton;

    private ListView listView;
    public Delivery delivery;
    private boolean scann_completed = false;

    Intent resultIntent = new Intent();

    private static final int REQUEST_CODE_QR_SCAN = 101;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_options);

        delivery = (Delivery) getIntent().getParcelableExtra("parcel_data");

        Log.d(TAG, ""+ delivery.getColor() + " Spravna: " + Color.CYAN );

        ImageView image = (ImageView)findViewById(R.id.image);
        image.setImageResource(R.drawable.ic_delivery_package_big);
        image.setColorFilter( delivery.getColor(), PorterDuff.Mode.SRC_ATOP );


        TextView recipient = findViewById(R.id.recipient);
        recipient.setText(getString(R.string.recipient) + delivery.getRecipient());

        TextView address = findViewById(R.id.address);

        address.setText(getString(R.string.address) +
                delivery.getAddress().split(",")[0] +
                delivery.getAddress().split(",")[1]);

        TextView phone = findViewById(R.id.phone);
        phone.setText(getString(R.string.phone) +
                delivery.getPhone());

        TextView date = (TextView)findViewById(R.id.date);
        date.setText(getString(R.string.date) +
                delivery.getDate());

        TextView status = (TextView)findViewById(R.id.status);
        phone.setText(getString(R.string.random));

        cancelButton = findViewById(R.id.cancel_button);
        holdButton = findViewById(R.id.hold_button);
        signButton = findViewById(R.id.sign_button);

        listView = findViewById(R.id.itemListView);
        listView.setAdapter(new ItemListAdapter(this, delivery.getContent()));

        if(!delivery.isFirst())
        {
            cancelButton.setEnabled(false);
            holdButton.setEnabled(false);
        }

        signButton.setEnabled(scann_completed);

        ImageButton scanButton = findViewById(R.id.scan_button);


    }

    public void sign(View v){
        Intent intent = new Intent(this, SignatureActivity.class);
        intent.putExtra("id", delivery.getId());
        startActivityForResult(intent, SIGN_REQUEST);
    }

    public void delay(View v)
    {
        DateDialog d = new DateDialog(this, this, delivery);
        d.show(getFragmentManager(), "timePicker");
        Log.d("Date", d.getDate());
    }

    public void cancel(View v)
    {
        CancelDialog d = new CancelDialog(this, delivery);
        d.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode)
        {
            case SIGN_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri uri = null;
                    if (data != null) {
                        delivery.setStatus("Doručená");
                        signButton.setImageResource(R.drawable.ic_completed_icon);
                        signButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v) {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("id", delivery.getId());
                                resultIntent.putExtra("delivery", delivery);
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            }
                        });

                        cancelButton.setEnabled(false);
                        holdButton.setEnabled(false);
                    }
                }
                break;

                case SCAN_REQUEST:
                    if (resultCode == RESULT_OK) {
                       // String contents = data.getStringExtra("data");
                        boolean completed = true;
                        for(Item item : delivery.getContent())
                        {
                            if(item.getId().equals(data.getStringExtra("data"))) item.setCompleted(true);
                            if(!item.isCompleted()) completed = false;
                        }

                        if(completed)
                        {
                            scann_completed = true;
                            signButton.setEnabled(true);
                        }


                        resultIntent.putExtra("id", delivery.getId());
                        resultIntent.putExtra("delivery", delivery);
                        setResult(Activity.RESULT_CANCELED, resultIntent);
                        listView.setAdapter(new ItemListAdapter(this, delivery.getContent()));
                    }
                    if(resultCode == RESULT_CANCELED){
                        //handle cancel
                    }
                    break;

        }

    }

    public void scan(View v)
    {
        try {
            Intent intent = new Intent(this, Scanner.class);
            startActivityForResult(intent, SCAN_REQUEST);
        } catch (Exception e) {

        }
    }







}
