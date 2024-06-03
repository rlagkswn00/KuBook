package com.example;

import com.example.fileio.FileManager;
import com.example.service.Service;
import com.example.model.*;
import com.example.service.ServiceFactory;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
public class KuBookApplication {
    public static SharedData sharedData = SharedData.getInstance();

    public static void main(String[] args) throws IOException {
        ServiceFactory serviceFactory = new ServiceFactory();
        FileManager.getInstance().load();
        DataManager datamanager = new DataManager();
        Service service = serviceFactory.login(datamanager);
        service.menu();
    }
}