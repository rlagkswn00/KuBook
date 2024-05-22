package com.example.model;

import com.example.service.AdminService;
import com.example.service.Service;
import com.example.service.UserService;

public class ServiceFactory {


    public Service login(DataManager dataManager){
        Service service;
        if(dataManager.getId().equals("admin")){
            service = new AdminService(dataManager.getDates(),dataManager.getId());
        }else {
            service = new UserService(dataManager.getDates(),dataManager.getId());
        }
        return service;
    }
}
