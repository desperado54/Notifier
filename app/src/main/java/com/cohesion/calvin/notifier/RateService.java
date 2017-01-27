package com.cohesion.calvin.notifier;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class RateService extends IntentService {

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_REFRESHED = 1;
    public static final int STATUS_ERROR = 2;

    private static final String TAG = "RateService";

    private AlarmManager alarmManager;

    private PendingIntent pendingIntent;

    public RateService() {
        super(RateService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service Started!");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String url = intent.getStringExtra("url");

        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(url)) {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);

            try {
                ArrayList<String> result = fetchRate(url + "?" + System.currentTimeMillis());

                if (result != null) {
                    bundle.putStringArrayList("result", result);
                    //bundle.pu
                    receiver.send(STATUS_REFRESHED, bundle);
                }
            } catch (Exception e) {
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
            }
        }

        alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        pendingIntent = PendingIntent.getService(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.add(Calendar.SECOND, 15);

        alarmManager.set (
                AlarmManager.RTC_WAKEUP,
                time.getTimeInMillis(),
                pendingIntent);

        this.stopSelf();
    }

    private ArrayList<String> fetchRate(String requestUrl) throws IOException, SAXException, ParserConfigurationException {
        ArrayList<String> list = new ArrayList<>();

        URL url = new URL(requestUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");

        int statusCode = urlConnection.getResponseCode();

        StringBuilder sb = new StringBuilder();
        if (statusCode == 200) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
        } else {
            throw new IOException("Failed to fetch data!!");
        }

        SAXParserFactory saxPF = SAXParserFactory.newInstance();
        SAXParser saxP = saxPF.newSAXParser();
        XMLReader xmlR = saxP.getXMLReader();
        XMLHandler xmlHandler = new XMLHandler();
        xmlR.setContentHandler(xmlHandler);

        xmlR.parse(new InputSource(new ByteArrayInputStream(sb.toString().getBytes("utf-8"))));

        List<Rate> rateList = xmlHandler.rateList;

        for (Rate rate : rateList) {
            String cp = rate.getCp();
            Double bid = rate.bid;
            Rate.Cache.put(cp, bid);
            list.add(String.format("%s,%s", cp, bid.toString()));
        }

        return list;
    }

}