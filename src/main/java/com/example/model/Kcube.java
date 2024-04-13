package com.example.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Kcube {
    public String name;
    public String room;
    public String max;

    public static Kcube from(String name, String room, String max) {
        try {
            int maxNum = Integer.parseInt(max);
            int roomNum = Integer.parseInt(room);
            if (maxNum > 20 || maxNum < 1
                    || roomNum > 3 || roomNum < 1) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new RuntimeException("파일형식에 문제가 있습니다 ! (Kcube)");
        }
        return Kcube.builder()
                .name(name)
                .room(room)
                .max(max)
                .build();
    }

    public static Kcube fromFile(String[] strings){
        if(strings.length != 3){
            System.err.println("파일명 혹은 파일형식에 문제가 있습니다 ! (케이큐브)");
            System.exit(1);
        }
        return from(strings[0], strings[1], strings[2]);
    }

}
