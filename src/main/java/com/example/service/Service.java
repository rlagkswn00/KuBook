package com.example.service;

import com.example.SharedData;


import java.util.List;

public abstract class Service {
    
    protected List<String> dates;
    protected String id;


    public Service(List<String> dates, String id){
        this.dates = dates;
        this.id = id;
    }
    public void display(){ // 날짜, building
        // 예약하려고 할때
    }
    public abstract void menu();
    public abstract void menu1(List<String> dates, String ID);
    public abstract void menu2(List<String> dates, String ID);
    public abstract void menu3(List<String> dates, String ID);


}
