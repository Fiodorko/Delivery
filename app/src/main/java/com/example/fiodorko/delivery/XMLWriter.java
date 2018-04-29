package com.example.fiodorko.delivery;

import android.os.Message;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class XMLWriter {

    public static String writeXml(ArrayList<Delivery> deliveries){
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");

        try {

            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.text("\n");
            serializer.startTag("", "report");
            serializer.attribute("", "id", formatter.format(Date.parse(Calendar.getInstance().getTime().toString())));


            for (Delivery delivery: deliveries){
                serializer.text("\n");
                serializer.text("\t");

                serializer.startTag("", "delivery");
                serializer.attribute("", "id", String.valueOf(delivery.getId()));
                serializer.text("\n");
                serializer.text("\t");
                serializer.text("\t");
                serializer.startTag("", "recipient");
                serializer.text(delivery.getRecipient());
                serializer.endTag("", "recipient");
                serializer.text("\n");
                serializer.text("\t");
                serializer.text("\t");

                serializer.startTag("", "address");
                serializer.text(delivery.getAddress());
                serializer.endTag("", "address");
                serializer.text("\n");
                serializer.text("\t");
                serializer.text("\t");

                serializer.startTag("", "date");
                serializer.text(delivery.getDate());
                serializer.endTag("", "date");
                serializer.text("\n");
                serializer.text("\t");
                serializer.text("\t");

                serializer.startTag("", "status");
                serializer.text(delivery.getStatus());
                serializer.endTag("", "status");
                serializer.text("\n");
                serializer.text("\t");


                serializer.endTag("", "delivery");
            }
            serializer.endTag("", "report");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
