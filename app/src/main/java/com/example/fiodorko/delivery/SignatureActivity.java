package com.example.fiodorko.delivery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.IOException;
import java.io.OutputStream;

public class SignatureActivity extends Activity {


    private int SAVE_REQUEST = 1;
    private Button confirmButton;
    private Button resetButton;
    private SignaturePad mSignaturePad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        confirmButton = findViewById(R.id.confirmButton);
        resetButton = findViewById(R.id.resetButton);

        mSignaturePad = findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {

            @Override
            public void onStartSigning() {
                //Event triggered when the pad is touched
            }

            @Override
            public void onSigned() {
                confirmButton.setEnabled(true);
                resetButton.setEnabled(true);
            }

            @Override
            public void onClear() {
                confirmButton.setEnabled(false);
                resetButton.setEnabled(false);
            }
        });

    }

    public void confirm(View v)
    {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT , Uri.parse("/sdcard/Download/deliveries"));
        intent.putExtra(Intent.EXTRA_TITLE, "DeliveryID:" + getIntent().getIntExtra("id", 0));
        intent.setType("image/jpeg");

        startActivityForResult(intent, SAVE_REQUEST);
    }

    public void reset(View v)
    {
        mSignaturePad.clear();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SAVE_REQUEST) {

            if (resultCode == RESULT_OK) {
                Uri uri = null;
                if (data != null) {
                    try {
                        Bitmap bitmap = mSignaturePad.getSignatureBitmap();
                        OutputStream stream = getContentResolver().openOutputStream(data.getData());
                        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(newBitmap);
                        canvas.drawColor(Color.BLACK);
                        canvas.drawBitmap(bitmap, 0, 0, null);
                        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                        stream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.setResult(RESULT_OK, this.getIntent());
                    finish();
                }
            }
        }
    }





}
