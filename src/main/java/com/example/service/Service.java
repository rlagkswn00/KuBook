package com.example.service;

import com.example.SharedData;


import java.io.IOException;
import java.util.List;

public abstract class Service {
    
    protected List<String> dates;
    protected String id;


    public Service(List<String> dates, String id){
        this.dates = dates;
        this.id = id;
    }

    public abstract void menu() throws IOException;
    public abstract void menu1();
    public abstract void menu2();
    public abstract void menu3();


}
