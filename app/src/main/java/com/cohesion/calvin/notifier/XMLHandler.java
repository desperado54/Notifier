package com.cohesion.calvin.notifier;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Calvin He on 6/27/2015.
 */
public class XMLHandler extends DefaultHandler {
    public static List<Rate> rateList = null;

    String elementValue = null;
    Boolean elementOn = false;
    Rate rate = null;
    String cp;

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        elementOn = true;

        if (localName.equals("Rates")) {
            rateList = new ArrayList<>();
        } else if (localName.equals("Rate")) {
            cp = attributes.getValue("Symbol");
            if(Rate.ObserveList.contains(cp)) {
                rate = new Rate();
                rate.setCp(cp);
            }
        }
    }

    /**
     * This will be called when the tags of the XML end.
     **/
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        elementOn = false;

        if(!Rate.ObserveList.contains(cp))
            return;

        if (localName.equalsIgnoreCase("Bid")) {
            double bid = 0.0d;
            try {
                bid = Double.parseDouble(elementValue);
            } catch (NumberFormatException e) {

            }
            rate.setBid(bid);
        }
        else if (localName.equalsIgnoreCase("Rate"))
            rateList.add(rate);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {

        if (elementOn) {
            elementValue = new String(ch, start, length);
            elementOn = false;
        }

    }

}
