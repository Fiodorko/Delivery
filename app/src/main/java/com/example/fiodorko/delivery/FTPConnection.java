package com.example.fiodorko.delivery;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class FTPConnection {

    public static void DownloadFiles(String url, Context ctx){

        try {
            URL u = new URL(url);
            InputStream is = u.openStream();



            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[1024];
            int length;

            FileOutputStream fos = new FileOutputStream(new File(ctx.getFilesDir(), "test.xml" ));
            while ((length = dis.read(buffer))>0) {
                fos.write(buffer, 0, length);
            }

        } catch (MalformedURLException mue) {
            Log.e("SYNC getUpdate", "malformed url error", mue);
        } catch (IOException ioe) {
            Log.e("SYNC getUpdate", "io error", ioe);
        } catch (SecurityException se) {
            Log.e("SYNC getUpdate", "security error", se);
        }
    }
}



