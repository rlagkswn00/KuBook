package com.example.model;

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
}
