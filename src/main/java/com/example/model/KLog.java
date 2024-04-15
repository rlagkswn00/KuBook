package com.example.model;

import com.example.utils.Validation;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KLog implements Model{
    public String userId;
    public String useTime;

    public static KLog from(String userId, String useTime) {
        Validation.validateUserId(userId);
        Validation.validateUseTime(useTime);
        return KLog.builder()
                .userId(userId)
                .useTime(useTime).build();
    }

    public static KLog fromFile(String[] strings){
        if(strings.length != 2){
            System.err.println("파일명 혹은 파일형식에 문제가 있습니다 ! (로그정보)");
            System.exit(1);
        }
        return from(strings[0], strings[1]);
    }

    @Override
    public String toString() {
        return userId+","+ useTime;
    }

}
