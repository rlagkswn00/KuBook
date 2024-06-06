package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation implements Model{
    public String name;
    public String room;
    public String startTime;
    public String useTime;
    public String numOfPeople;
    public List<String> userIds;


    public void decreaseNumOfPeople() {
        if(numOfPeople!=null){
            numOfPeople = Integer.toString((Integer.parseInt(numOfPeople)-1));
        }
    }

    public static Reservation from(String name, String room, String startTime, String useTime, String numOfPeople, List<String> userIds){
        return Reservation.builder()
                .name(name)
                .room(room)
                .startTime(startTime)
                .useTime(useTime)
                .numOfPeople(numOfPeople)
                .userIds(userIds)
                .build();
    }

    public static Reservation fromFile(String[] strings) {
        List<String> userIds = new ArrayList<>();
        int numOfPeople = Integer.parseInt(strings[4]);
        for (int i = 0; i < numOfPeople; i++) {
            userIds.add(strings[i + 5]); // 5번째 부터 학번 인덱스 시작
        }

        return from(strings[0], strings[1], strings[2], strings[3], strings[4], userIds);
    }

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
