package com.example.model;

import com.example.SharedData;
import com.example.fileio.FileManager;
import com.example.utils.Validation;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Getter
public class DataManager {
    private static final SharedData sharedData = SharedData.getInstance();
    private String time;
    private String date;
    private String id;
    private List<String> dates;
    private final Scanner sc = new Scanner(System.in);
    public DataManager(){
        this.date = readDate();
        this.time = readTime();
        this.id = readId();
    }

    public String readDate(){
        while (true) {
            System.out.print("현재 날짜를 입력해주세요 (ex. 20240101) : ");
            date=sc.nextLine();
            if (sharedData.currentTime == null || Validation.validateDate(date)) {
                dates = FileManager.dateGenerator(date);
                if(sharedData.reservationList.isEmpty()){
                    for(String d : dates){
                        sharedData.reservationList.put(new Date(d), new ArrayList<>());
                    }
                }
                if(sharedData.logs.isEmpty()){
                    for(String d : dates){
                        sharedData.logs.put(new Date(d), new ArrayList<>());
                    }
                }
                if(sharedData.penalizedUsers.isEmpty()){
                    sharedData.penalizedUsers.put(new Date(date),new ArrayList<>());
                }
                break;
            }
        }
        return date;
    }
    public String readTime(){
        while(true){
            System.out.print("현재 시간을 입력해주세요 (ex. 1230) : ");

            this.time = sc.nextLine();
            if(Validation.validateTime(time)){
                /*현재 시간 예외처리에 따른 sharedData penalty, log, reservation update 처리*/
                if(sharedData.currentTime == null || Integer.parseInt(sharedData.currentTime.date) < Integer.parseInt(date)){  //currentTime 이후 날짜인 경우
                    List<PenaltyUser> penaltyUsers = new ArrayList<>();
                    sharedData.penalizedUsers.clear();
                    sharedData.penalizedUsers.put(new Date(date), penaltyUsers); //당일 패널티 목록 새로 생성
                    int tempdate = Integer.parseInt(date);
                    sharedData.logs.keySet().removeIf(r-> (Integer.parseInt(r.date) < tempdate));
                    sharedData.reservationList.keySet().removeIf(r -> (Integer.parseInt(r.date) < tempdate));
                    for(int i=0; i<8; i++){
                        if(!sharedData.logs.containsKey(new Date(dates.get(i)))) {//해당 날짜가 없는 경우
                            List<KLog> kLogs = new ArrayList<>();
                            sharedData.logs.put(new Date(dates.get(i)), kLogs);
                            List<Reservation> reservations = new ArrayList<>();
                            sharedData.reservationList.put(new Date(dates.get(i)), reservations);
                        }
                    }
                }
                sharedData.currentTime = new Date(date, time);
                break;
            }
        }
        return time;
    }
    public String readId(){
        while(true){
            System.out.print("학번을 입력해주세요 (ex. 202012345) : ");
            this.id = sc.nextLine();
            if(id.equals("admin")){
                while (true) {
                    System.out.print("비밀번호를 입력해주세요.: ");
                    String password = sc.nextLine();
                    if (password.equals("qwer1234")) {
                        //정상 로그인됨
                        return id;
                    }
                    System.out.println("오류! 유효하지 않은 비밀번호입니다");
                }
            }
            if(Validation.validateUserId(id)){
                break;
            }
        }
        return id;
    }

    public boolean isAdmin() {
        if("admin".equals(id)) return true;
        return false;
    }
}
