package com.example.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Reservation {
    public String name;
    public String room;
    public String startTime;
    public String useTime;
    public String numOfPeople;
    public List<String> userIds;

    @Override
    public String toString() {
        String str = "";
        for(String userId : userIds) {
            str += "," + userId ;
        }
        return name + "," + room + "," + startTime + "," + useTime + "," + numOfPeople
                + str;

    }
}
