package com.example;

import com.example.model.Date;
import com.example.model.KLog;
import com.example.model.Kcube;
import com.example.model.Penalty;
import com.example.model.Reservation;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileManager {
    public static final String ETC = "data/etc";
    public static final String KCUBE = "data/etc/kcube.txt";
    public static final String LOG = "data/log";
    public static final String RESERVATION = "data/reservation";
    public static final String CURRENT_TIME = "data/currentTime.txt";
    public SharedData sharedData = SharedData.getInstance();
    public Date curTime;
    public BufferedReader br;
    public BufferedWriter bw;

    FileManager() throws IOException {
        load();
    }

    private void load() throws IOException {
        loadReservation();
        loadLog();
        loadKcube();
        loadPenalty();
        loadCurrentTime();
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
    private void loadCurrentTime() throws IOException {
        File file = new File(CURRENT_TIME);

        if(!file.exists()) {
            return;
        }
        br = new BufferedReader(new FileReader(file));

        String line = "";

        while ((line = br.readLine()) != null) {
            sharedData.currentTime = new Date(line.substring(0,8),line.substring(8));
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

    public void save() throws IOException {
        curTime = sharedData.currentTime;
        savePenalty();
        saveCurrentTime();
        saveLog();
        saveReservation();
    }

    private void saveReservation() throws IOException {
        File logDir = new File(RESERVATION);
        File[] files = logDir.listFiles();

        for(File file : files) {
            file.delete();
        }

        List<String> dates = dateGenerator();
        for(String date: dates){
            List<Reservation> reservations = sharedData.reservationList.get(new Date(date));
            File file = new File(RESERVATION + "/" + date + ".txt");
            bw = new BufferedWriter(new FileWriter(file));

            if(reservations == null) {
                bw.close();
                continue;
            }
            for (Reservation reservation : reservations) {
                bw.write(reservation.toString());
                bw.newLine();
                bw.flush();
            }
        }

        bw.close();
    }

    private void saveLog() throws IOException {
        File logDir = new File(LOG);
        File[] files = logDir.listFiles();

        for(File file : files) {
            file.delete();
        }
        List<String> dates = dateGenerator();
        for(String date: dates){
            List<KLog> kLogs = sharedData.logs.get(new Date(date));

            File file = new File(LOG + "/" + date + ".txt");
            bw = new BufferedWriter(new FileWriter(file));

            if(kLogs == null){
                bw.close();
                continue;
            }
            for (KLog kLog : kLogs) {
                bw.write(kLog.toString());
                bw.newLine();
                bw.flush();
            }
        }

        bw.close();
    }

    private List<String> dateGenerator() {
        curTime = sharedData.currentTime;

        // 날짜 형식을 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 입력된 문자열을 LocalDate 객체로 파싱
        LocalDate date = LocalDate.parse(curTime.date, formatter);

        List<String> dates = new ArrayList<>();
        // 입력된 날짜를 포함하여 8개의 날짜(7일 후까지)를 리스트에 추가
        for (int i = 0; i < 8; i++) {
            dates.add(date.plusDays(i).format(formatter));
        }

        return dates;
    }

    private void saveCurrentTime() throws IOException {
        File file = new File(CURRENT_TIME);
        bw = new BufferedWriter(new FileWriter(file, false));
        bw.write(curTime.date + curTime.time);
        bw.flush();
        bw.close();
    }

    private void savePenalty() throws IOException {
        File etcDir = new File(ETC);
        File[] files = etcDir.listFiles();

        for(File file : files) {
            if(file.getName().startsWith("p")) {
                file.delete();
                break;
            }
        }

        List<Penalty> penalties = sharedData.penalties.get(curTime);
        if(penalties == null) {
            return;
        }
        File file = new File(ETC + "/p" + curTime.date + ".txt");
        bw = new BufferedWriter(new FileWriter(file));
        for (Penalty penalty : penalties) {
            bw.write(penalty.userId);
            bw.newLine();
            bw.flush();
        }
        bw.close();
    }

}
