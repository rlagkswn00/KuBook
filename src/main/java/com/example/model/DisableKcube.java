package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class DisableKcube implements Model {
    public String name;
    public String room;
    public String date;
    public String startTime;
    public String endTime;


    public static DisableKcube from(String name, String room, String date, String startTime, String endTime){
        return DisableKcube.builder()
                .name(name)
                .room(room)
                .startTime(startTime)
                .date(date)
                .endTime(endTime)
                .build();
    }

    public static DisableKcube fromFile(String[] strings) {
        return from(strings[0], strings[1], strings[2], strings[3], strings[4]);
    }


    @Override
    public String toString() {
        return name + "," + room + "," + date + "," + startTime + "," + endTime;
    }

}
