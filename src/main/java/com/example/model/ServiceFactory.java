package com.example.model;

import com.example.service.AdminService;
import com.example.service.Service;
import com.example.service.UserService;

public class ServiceFactory {


    public Service login(DataManager dataManager){
        if(dataManager.isAdmin()){
            return new AdminService(dataManager.getDates(), dataManager.getId());
        }else {
            return new UserService(dataManager.getDates(), dataManager.getId());
        }
    }
}
