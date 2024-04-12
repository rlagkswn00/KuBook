package com.example;

import com.example.model.Date;
import com.example.model.Penalty;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class KuBookLauncher {
    public static void main(String[] args) throws IOException {

        FileManager fileManager = new FileManager();

        changeTest();

        // TODO : 프로그램 끝날 때 fileManager.save 호출하세요.
        fileManager.save();

    }

    private static void changeTest() {
        SharedData sharedData = SharedData.getInstance();
        sharedData.currentTime = new Date("20240412", "1530");
        sharedData.penalties.put(
                sharedData.currentTime,
                Arrays.asList(new Penalty("202011247"), new Penalty("202011245"))
        );
    }
}