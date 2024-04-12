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
            int i = Integer.parseInt(max);
            int j = Integer.parseInt(room);
            if (i > 20 || i < 1 || j > 3 || j < 1) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println("파일형식에 문제가 있습니다 ! (기타파일)");
            System.exit(0);
        }
        return Kcube.builder()
                .name(name)
                .room(room)
                .max(max)
                .build();
    }
}
