package com.example.fileio;

import com.example.SharedData;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FileManager {
    public LoadManager loadManager = LoadManager.getInstance();
    public SharedData sharedData = SharedData.getInstance();

    public FileManager() throws IOException {
        load();
    }

    /**
     * 프로그램 시작 시 호출되며, 파일의 데이터들을 프로그램 내에서 쓰일 객체로 변환
     */
    private void load() throws IOException {
        log.info("load 진입");
        loadManager.loadCurrentTime();
        loadManager.loadReservation();
        loadManager.loadLog();
        loadManager.loadKcube();
        loadManager.loadPenalty();
    }

    /**
     * 프로그램 종료 시 호출되며, 런타임 동안 변화한 데이터를 파일에 업데이트하는 역할
     */
    public void save() throws IOException {
        log.info("save 진입");
        SaveManager saveManager = new SaveManager(sharedData.currentTime, dateGenerator(sharedData.currentTime.date));
        saveManager.saveCurrentTime();
        saveManager.savePenalty();
        saveManager.saveLog();
        saveManager.saveReservation();
        log.info("save 성공");
    }

    /**
     * 시작날짜로부터 7일 후까지의 날짜를 생성. (윤년 고려)
     * @param startDate : 시작날짜
     * @return dates : 생성된 8개의 날짜를 리스트로 반환
     */
    public static List<String> dateGenerator(String startDate) {
        // 날짜 형식을 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 입력된 문자열을 LocalDate 객체로 파싱
        LocalDate date = LocalDate.parse(startDate, formatter);

        List<String> dates = new ArrayList<>();
        // 입력된 날짜를 포함하여 8개의 날짜(7일 후까지)를 리스트에 추가
        for (int i = 0; i < 8; i++) {
            dates.add(date.plusDays(i).format(formatter));
        }

        return dates;
    }
}
