package com.example.fileio;

import com.example.SharedData;
import com.example.model.*;
import com.example.model.Date;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static com.example.fileio.FilePath.*;

@Slf4j
public class LoadManager {

    private static LoadManager instance;
    private BufferedReader br;
    private SharedData sharedData = SharedData.getInstance();

    private LoadManager() {
    }

    public static LoadManager getInstance() {
        if (instance == null) {
            instance = new LoadManager();
        }
        return instance;
    }

    public void loadCurrentTime() throws IOException {
        File file = new File(CURRENT_TIME_TXT);

        if (!file.exists()) {
            return;
        }
        br = new BufferedReader(new FileReader(file));

        String line;

        while ((line = br.readLine()) != null) {
            sharedData.currentTime = new Date(line.substring(0, 8), line.substring(8));
        }
    }

    public void loadReservation() throws IOException {
        File dir = new File(RESERVATION_DIR);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) return;
        if (!validate8Days(files)) {
            System.err.println("파일명 혹은 파일형식에 문제가 있습니다 ! (예약정보)");
            System.exit(1);
        }
        for (File file : files) {
            br = new BufferedReader(new FileReader(file));
            List<Reservation> reservations = new ArrayList<>();

            String line = "";
            while ((line = br.readLine()) != null) {
                String[] splitedLine = line.split(",");
                if (splitedLine.length < 6) {
                    System.err.println("파일명 혹은 파일형식에 문제가 있습니다 ! (예약정보)");
                    System.exit(1);
                }
                Reservation reservation = Reservation.fromFile(splitedLine);
                reservations.add(reservation);
            }
            String dateStr = parseFileName(file, 0);
            Date date = new Date(dateStr);

            SharedData.getInstance().
                    reservationList.put(date, reservations);
        }
    }

    public void loadLog() throws IOException {
        File dir = new File(LOG_DIR);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) return;

        if (!validate8Days(files)) {
            System.err.println("파일명 혹은 파일형식에 문제가 있습니다 ! (예약로그)");
            System.exit(1);
        }
        for (File file : files) {
            br = new BufferedReader(new FileReader(file));
            List<KLog> kLogs = new ArrayList<>();
            String line = "";

            while ((line = br.readLine()) != null) {
                String[] splitedLine = line.split(",");
                KLog kLog = KLog.fromFile(splitedLine);
                kLogs.add(kLog);
            }

            String dateStr = parseFileName(file, 0);
            Date date = new Date(dateStr);

            SharedData.getInstance().logs.put(date, kLogs);
        }
    }

    public void loadKcube() throws IOException {
        File file = new File(KCUBE_TXT);
        if (!file.exists()) {
            throw new RuntimeException("파일형식에 문제가 있습니다 ! (Kcube)");
        }
        br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] splitedLine = line.split(",");
            Kcube kcube = Kcube.fromFile(splitedLine);
            SharedData.getInstance()
                    .kcubes.add(kcube);
        }
    }

    public void loadPenalty() throws IOException {
        File dir = new File(ETC_DIR);

        File[] files = dir.listFiles();
        /**
         * ETC폴더가 없으면 문제 생기기에 예외처리 필요
         */
        if (files == null) {
            return;
        }
        /**
         * ETC폴더가 있는데 파일이 없으면 문제 생기기에 예외처리 필요
         */
        if (files.length == 0) {
            return;
        }
        //패널티 파일 찾기
        File penalizedUsersFile = Arrays.stream(files)
                .filter(file -> file.getName().startsWith("p"))
                .findFirst().orElse(null);

        if (penalizedUsersFile == null) return;

        br = new BufferedReader(new FileReader(penalizedUsersFile));
        List<PenaltyUser> penalizedUsers = new ArrayList<>();
        String line;

        while ((line = br.readLine()) != null) {
            penalizedUsers.add(new PenaltyUser(line.trim()));
        }

        String dateStr = parseFileName(penalizedUsersFile, 1);

        SharedData.getInstance().penalizedUsers.put(new Date(dateStr), penalizedUsers);
    }

    private String parseFileName(File file, int beginIndex) {
        int dotIndex = file.getName().lastIndexOf(".");
        String dateStr = file.getName().substring(beginIndex, dotIndex);
        return dateStr;
    }

    private boolean validate8Days(File[] files) {
        if (files.length != 8) {
            return false;
        }
        Arrays.sort(files, Comparator.comparing(String::valueOf));
        File file = files[0];
        String fileName = parseFileName(file, 0);
        List<String> dates = FileManager.dateGenerator(fileName);
        for (int i = 0; i < 8; i++) {
            if (!parseFileName(files[i], 0).equals(dates.get(i))) {
                return false;
            }
        }
        return true;
    }

}
