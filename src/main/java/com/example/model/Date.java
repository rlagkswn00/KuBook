package com.example.model;

import com.example.utils.Validation;
import lombok.Data;

@Data
public class Date {
    public String date;
    public String time; // nullable

    public Date(String date) {
        this.date = date;
    }

    public Date(String date, String time) {
        this.date = date;
        this.time = time;
    }

    public int getYear(){
        return Integer.parseInt(date.substring(0,4));
    }
    public int getMonth(){
        return Integer.parseInt(date.substring(4,6));
    }
    public int getDay(){
        return Integer.parseInt(date.substring(6,8));
    }
    public int getMontToDay(){
        return Integer.parseInt(date.substring(4,8));
    }
    public int getHour(){
        return Integer.parseInt(time.substring(0,2));
    }
    public int getMinute(){
        return Integer.parseInt(time.substring(2,4));
    }

    public static Date from(String date, String time) {
        Validation.validateDate(date);
        Validation.validateTime(time);
        return new Date(date, time);
    }

    public static Date fromWithNoValidation (String date, String time){
        return new Date(date,time);
    }
}
