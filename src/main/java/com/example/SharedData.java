package com.example;

import com.example.model.*;

import java.util.List;
import java.util.Map;

public class SharedData {

    public Date currentTime;
    public List<Kcube> kcubes;
    public Map<Date, List<Penalty>> penalties;
    public Map<Date, List<KLog>> logs;
    public Map<Date, List<Reservation>> reservationList;

    private static SharedData instance;
    private SharedData() {}
    public static SharedData getInstance() {
        if (instance == null) {
            instance = new SharedData();
        }
        return instance;
    }
}
