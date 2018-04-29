package com.example.fiodorko.delivery;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLParser {

    private XmlPullParser parser;
    private Geocoder geocoder;
    private Context ctx;

    XMLParser(InputStream paInput, Context ctx) {
        this.geocoder = new Geocoder(ctx);
        this.ctx = ctx;
        try {
            XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
            parser = pullParserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(paInput, null);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Delivery> parseXML() throws XmlPullParserException, IOException {
        ArrayList<Delivery> deliveries = null;
        ArrayList<Item> content = null;
        int eventType = parser.getEventType();
        Delivery delivery;
        String address = null, recipient = null, date = null, phone = null;

        int id = 0;
        GeoPoint location = null;


        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name;
            delivery = new Delivery(recipient, address, date, phone, id, location, content, ctx);
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    deliveries = new ArrayList();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals("delivery"))
                    {
                        id = Integer.parseInt(parser.getAttributeValue(null, "id"));
                    }
                    else if (delivery != null)
                    {
                        if (name.equals("recipient"))
                        {
                            recipient = parser.nextText();
                        }
                        else if (name.equals("address"))
                        {
                            address = parser.nextText();
                            List<Address> addresses;
                            addresses = geocoder.getFromLocationName(address,1);
                            if(!addresses.isEmpty())location = new GeoPoint(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                        }
                        else if (name.equals("date"))
                        {
                            date =  parser.nextText();
                        }
                        else if(name.equals("phone"))
                        {
                            phone = parser.nextText();
                        }
                        else if(name.equals("content"))
                        {
                            content = new ArrayList<>();
                        }
                        else if(content != null)
                        {
                            if(name.equals("item"))
                            {
                                content.add(new Item(parser.nextText(), false));
                            }
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("delivery") && delivery != null && location != null) {
                        deliveries.add(delivery);
                    }
                    else
                        {
                            Toast.makeText(ctx, "Adresa" + address + "Nebola nájdená", Toast.LENGTH_SHORT).show();
                        }
            }
            eventType = parser.next();
        }

        return deliveries;

    }
}
