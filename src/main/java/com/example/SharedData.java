package com.example;

import com.example.model.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SharedData {

    public Date currentTime;
    public List<Kcube> kcubes = new ArrayList<>();
    public Map<Date, List<Penalty>> penalties = new HashMap<>();
    public Map<Date, List<KLog>> logs = new HashMap<>();
    public Map<Date, List<Reservation>> reservationList = new HashMap<>();

    private static SharedData instance;
    private SharedData() {}
    public static SharedData getInstance() {
        if (instance == null) {
            instance = new SharedData();
        }
        return instance;
    }
}
