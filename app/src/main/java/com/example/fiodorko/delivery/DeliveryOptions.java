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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DeliveryOptions extends Activity {

    private int SIGN_REQUEST = 1;

    private String TAG = "Options";

    private ImageButton cancelButton;
    private ImageButton holdButton;
    private ImageButton signButton;
    Delivery delivery;

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



        if(!delivery.isFirst())
        {
            cancelButton.setEnabled(false);
            holdButton.setEnabled(false);
            signButton.setEnabled(false);
        }

    }

    public void sign(View v){
        Intent intent = new Intent(this, SignatureActivity.class);
        intent.putExtra("id", delivery.getId());
        startActivityForResult(intent, SIGN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGN_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri uri = null;
                if (data != null) {
                    signButton.setImageResource(R.drawable.ic_completed_icon);
                    signButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("id", delivery.getId());
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                    });

                    cancelButton.setEnabled(false);
                    holdButton.setEnabled(false);
                }
            }
        }
    }




}
