package com.example;

import com.example.fileio.FileManager;
import com.example.model.Date;
import com.example.model.PenaltyUser;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class KuBookLauncher {
    public static void main(String[] args) throws IOException {

        FileManager fileManager = new FileManager();
        log.info("load 성공\n"+SharedData.getInstance().toString());
        changeTest();

        // TODO : 프로그램 끝날 때 꼭! fileManager.save 호출하기.
        fileManager.save();
    }

    private static void changeTest() {
        SharedData sharedData = SharedData.getInstance();
        sharedData.currentTime = new Date("20240428", "1530");
        sharedData.penalizedUsers.put(
                sharedData.currentTime,
                Arrays.asList(new PenaltyUser("202011247"), new PenaltyUser("202011245"))
        );
    }
}