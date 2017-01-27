package com.cohesion.calvin.notifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Calvin He on 6/27/2015.
 */
public class Rate {

    public static List<String> ObserveList;
    static {
        String[] ss = {
                "EURUSD","GBPUSD","USDCAD","AUDUSD",//"AUDNZD",
                "NZDUSD","AUDCAD","USDCHF","EURGBP",
                "EURAUD","EURCAD","GBPAUD","EURJPY",
                "USDJPY","AUDJPY","GBPJPY","NZDJPY"
        };
        //Arrays.sort(ss);
        ObserveList = Arrays.asList(ss);
    }

    public static Map<String, Double> Cache = new HashMap<>();

    public String cp;
    public double bid;
    public short trend;

    public short getTrend() {
        return trend;
    }

    public void setTrend(short trend) {
        this.trend = trend;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

}
