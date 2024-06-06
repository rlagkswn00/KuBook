package com.example.model;

import com.example.utils.Validation;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static com.example.utils.Validation.printErrorMessage;

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

    public int getYear() {
        return Integer.parseInt(date.substring(0, 4));
    }

    public int getMonth() {
        return Integer.parseInt(date.substring(4, 6));
    }

    public int getDay() {
        return Integer.parseInt(date.substring(6, 8));
    }

    public int getMontToDay() {
        return Integer.parseInt(date.substring(4, 8));
    }

    public int getHour() {
        return Integer.parseInt(time.substring(0, 2));
    }

    public int getMinute() {
        return Integer.parseInt(time.substring(2, 4));
    }

    public static Date from(String date, String time) {
        Validation.validateDate(date);
        Validation.validateTime(time);
        return new Date(date, time);
    }

    public static Date fromWithNoValidation(String date, String time) {
        return new Date(date, time);
    }

    //인자값보다 this 시간이 이후이면 true, 아니면 false
    public boolean isAfterFrom(Date date) {
        int thisDate = Integer.parseInt(this.date);
        int thisTime = 0;
        if (this.time != null) {
            thisTime = Integer.parseInt(this.time);
        }

        int argDate = Integer.parseInt(date.getDate());
        int argTime = 0;
        if (this.time != null) {
            argTime = Integer.parseInt(date.getTime());
        }
        if (thisDate > argDate) {
            return true;
        }
        if (thisTime != 0 && argTime != 0) {
            if (thisDate == argDate) {
                if (thisTime >= argTime) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getDayOfWeek(String dateStr){
        DayOfWeek dayOfWeek = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(dateStr, formatter);
            dayOfWeek = date.getDayOfWeek();
        } catch (DateTimeParseException e) {
            printErrorMessage("Invalid date format: " + dateStr);
            return null;
        }

        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}
