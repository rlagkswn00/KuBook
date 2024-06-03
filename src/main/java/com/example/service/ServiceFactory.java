package com.example.service;

import com.example.model.DataManager;

public class ServiceFactory {


    public Service login(DataManager dataManager){
        if(dataManager.isAdmin()){
            return new AdminService(dataManager.getDates(), dataManager.getId());
        }else {
            return new UserService(dataManager.getDates(), dataManager.getId());
        }
    }
}
