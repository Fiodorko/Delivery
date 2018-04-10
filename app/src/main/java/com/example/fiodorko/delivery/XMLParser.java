package com.example.fiodorko.delivery;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

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
        int eventType = parser.getEventType();
        Delivery delivery = null;
        String address = null, recipient = null, date = null, phone = null;
        int id = 0;
        GeoPoint location = null;


        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name;
            delivery = new Delivery(recipient, address, date, phone, id, location, ctx);

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
                            location = new GeoPoint(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                        }
                        else if (name.equals("date"))
                        {

                            date =  parser.nextText();
                        }
                        else if(name.equals("phone"))
                        {
                            phone = parser.nextText();
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("delivery") && delivery != null) {
                        deliveries.add(delivery);
                    }
            }
            eventType = parser.next();
        }

        return deliveries;

    }
}
