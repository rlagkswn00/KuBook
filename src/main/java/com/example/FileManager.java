package com.example;

import com.example.model.Date;
import com.example.model.KLog;
import com.example.model.Kcube;
import com.example.model.Penalty;
import com.example.model.Reservation;
import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManager {
    public static final String ETC = "data/etc";
    public static final String KCUBE = "data/etc/kcube.txt";
    public static final String LOG = "data/log";
    public static final String RESERVATION = "data/reservation";
    public static final String CURRENT_TIME = "currentTime.txt";
    public BufferedReader br;

    FileManager() throws IOException {
        load();
    }

    private void load() throws IOException {
        loadReservation();
        loadLog();
        loadKcube();
        loadPenalty();
    }

    private void loadPenalty() throws IOException {
        File dir = new File(ETC);
        File[] files = dir.listFiles();
        if (files.length == 1) {
            //패널티 파일이 존재하지 않음. kcube.txt만 존재
            return;
        }
        File penaltyFile = null;
        //패널티 파일 찾기
        for (File file : files) {
            if (file.getName().startsWith("p")) {
                penaltyFile = file;
                break;
            }
        }

        br = new BufferedReader(new FileReader(penaltyFile));
        List<Penalty> penalties = new ArrayList<>();
        String line = "";

        while ((line = br.readLine()) != null) {
            penalties.add(new Penalty(line.trim()));
        }

        int dotIndex = penaltyFile.getName().lastIndexOf(".");
        String dateStr = penaltyFile.getName().substring(1, dotIndex);
        Date date = new Date(dateStr);

        SharedData.getInstance().penalties.put(date,penalties);
    }

    private void loadKcube() throws IOException {
        br = new BufferedReader(new FileReader(KCUBE));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(",");
            Kcube kcube = Kcube.builder()
                    .name(splitedLine[0])
                    .room(splitedLine[1])
                    .max(splitedLine[2]).build();
            SharedData.getInstance()
                    .kcubes.add(kcube);
        }
    }

    private void loadLog() throws IOException {
        File dir = new File(LOG);
        File[] files = dir.listFiles();

        for (File file : files) {
            br = new BufferedReader(new FileReader(file));
            List<KLog> kLogs = new ArrayList<>();
            String line = "";

            while ((line = br.readLine()) != null) {
                String[] splitedLine = line.split(",");
                KLog kLog = KLog.builder()
                        .userId(splitedLine[0])
                        .time(splitedLine[1])
                        .build();
                kLogs.add(kLog);
            }

            int dotIndex = file.getName().lastIndexOf("."); // 파일 명에서 날짜 파싱
            String dateStr = file.getName().substring(0, dotIndex);
            Date date = new Date(dateStr);

            SharedData.getInstance().logs.put(date, kLogs);
        }
    }

    private void loadReservation() throws IOException {
        File dir = new File(RESERVATION);
        File[] files = dir.listFiles();
        for (File file : files) {
            br = new BufferedReader(new FileReader(file));
            List<Reservation> reservations = new ArrayList<>();

            String line = "";
            while ((line = br.readLine()) != null) {
                List<String> userIds = new ArrayList<>();
                String[] splitedLine = line.split(",");
                int numOfPeople = Integer.parseInt(splitedLine[4]);
                for (int i = 0; i < numOfPeople; i++) {
                    userIds.add(splitedLine[i + 5]); // 5번째 부터 학번 인덱스 시작
                }
                Reservation reservation = Reservation.builder()
                        .name(splitedLine[0])
                        .room(splitedLine[1])
                        .startTime(splitedLine[2])
                        .useTime(splitedLine[3])
                        .numOfPeople(splitedLine[4])
                        .userIds(userIds)
                        .build();
                reservations.add(reservation);
            }
            int dotIndex = file.getName().lastIndexOf("."); // 파일 명에서 날짜 파싱
            String dateStr = file.getName().substring(0, dotIndex);
            Date date = new Date(dateStr);

            SharedData.getInstance().
                    reservationList.put(date, reservations);
        }
    }
}
