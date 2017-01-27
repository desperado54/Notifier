package com.cohesion.calvin.notifier;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements RateResultReceiver.Receiver{

    //private Map<String, Double> WATCHLIST =  Collections.synchronizedMap(new HashMap<String, Double>());
    private Map<String, Rate> WATCHLIST =  new HashMap<>();
    private RateResultReceiver receiver;
    private TextView textView;
    private Spinner cpSpinner;
    private EditText rateTxt;
    private Button setBtn;

    private static final String TAG = "MainActivity";

//    private Uri notification;
    private MediaPlayer mp;
    NotificationManager nfManager;

    static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        mp = MediaPlayer.create(getApplicationContext(), notification);
        nfManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        cpSpinner = (Spinner) findViewById(R.id.spinner);
        rateTxt = (EditText) findViewById(R.id.rate);
        setBtn = (Button) findViewById(R.id.setbtn);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Rate.ObserveList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cpSpinner.setAdapter(adapter);

        cpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView parent, View view, int pos, long id) {
                String cp = parent.getItemAtPosition(pos).toString();
                Double d = Rate.Cache.get(cp);
                if(d != null) {
                    String s = d.toString();
                    if (rateTxt.requestFocus()) {
                        rateTxt.setText(s);
                        rateTxt.setSelection(s.length() - 3, s.length());
//                        InputMethodManager imm = (InputMethodManager)
//                                getSystemService(Context.INPUT_METHOD_SERVICE);
//                        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView parent) {

            }
        });

        setBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                String cp = cpSpinner.getSelectedItem().toString();
                double d = 0.0d;
                try {
                    d = Double.parseDouble(rateTxt.getText().toString().trim());
                } catch(Exception e)
                {
                    ;
                }
                if(!cp.isEmpty() && d != 0) {
                    Double current = Rate.Cache.get(cp);
                    if(current != null) {
                        Short trend = (short)(current - d > 0 ? (byte)-1 : (byte)1);
                        Rate r = new Rate();
                        r.setBid(d);
                        r.trend = trend;
                        r.cp = cp;
                        WATCHLIST.put(cp, r);

                    }
                } else {
                    WATCHLIST.remove(cp);
                }
            }
        });

        final Button clearBtn = (Button) findViewById(R.id.clearbtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                WATCHLIST.clear();
            }
        });

        receiver = new RateResultReceiver(new Handler());
        receiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, RateService.class);

        intent.putExtra("url", "http://rates.fxcm.com/RatesXML");
        intent.putExtra("receiver", receiver);

        startService(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case RateService.STATUS_RUNNING:

                setProgressBarIndeterminateVisibility(true);
                break;
            case RateService.STATUS_REFRESHED:
                setProgressBarIndeterminateVisibility(false);

                ArrayList<String> result = resultData.getStringArrayList("result");
                StringBuilder txt = new StringBuilder();
                StringBuilder nfTxt = new StringBuilder();
                for (String info : result) {
                    String[] ss = info.split(",");
                    String cp = ss[0];
                    double currBid = new Double(ss[1]);
                    if(WATCHLIST.keySet().contains(ss[0])) {
                        Double watchBid = WATCHLIST.get(ss[0]).getBid();
                        short trend = WATCHLIST.get(ss[0]).getTrend();
                        if(currBid > watchBid && trend == 1 || currBid < watchBid && trend == -1) {
                            //mp.start();
                            nfTxt.append(String.format("%s: %s $$$$$\n", cp, currBid));
                            txt.append(String.format("%s    %s    %s    $$$$$\n", cp, currBid, watchBid));
                        } else {
                            txt.append(String.format("%s    %s    %s\n", cp, currBid, watchBid));
                        }
                    } else {
                        txt.append(String.format("%s    %s \n", cp, currBid));
                    }
                }
                if(nfTxt.length() > 0) {
                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.notification)
                                    .setLights(Color.MAGENTA, 500, 500)
                                    .setVibrate(new long[] {500, 500, 500})
                                    .setContentTitle("Price hit expectation.")
                                    .setContentText(nfTxt.toString())
                                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

                    nfManager.notify(NOTIFICATION_ID, builder.build());
                }
                textView.setText(txt.toString());

                break;
            case RateService.STATUS_ERROR:
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopTimer();
    }

 }
