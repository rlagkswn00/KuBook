package com.example.service;

import com.example.SharedData;
import com.example.fileio.FileManager;
import com.example.service.Handler.ReserveHandler;

import java.util.List;
import java.util.Scanner;

public class UserService extends Service {
    public ReserveHandler reserveHandler = new ReserveHandler(dates,id);
    private final Scanner sc = new Scanner(System.in);
    SharedData sharedData = SharedData.getInstance();

    public UserService(List<String> dates, String id){
        super(dates,id);
    }

    @Override
    public void menu() {
        while(true) {
            System.out.println("1. 예약하기(예약가능목록) 2. 예약목록(본인) 3. 예약취소 4. 종료하기");
            System.out.print("메뉴를 선택하세요 (ex. 1) : ");
            String menu = sc.nextLine();
            switch (menu) {
                case "1":
                    System.out.println("\n...선택한 메뉴 창으로 이동...\n");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    menu1();
                    break;
                case "2":
                    menu2();
                    break;
                case "3":
                    menu3();
                    break;
                case "4":
                    System.out.print("시스템을 종료합니다.");
                    sc.close();
                    FileManager.save();
                    System.exit(0);
                    break;
                default:
                    System.out.println("오류! 메뉴에 없는 입력입니다. 다시 입력해주세요.\n");
                    break;
            }
        }
    }
    @Override
    public void menu1() {
        reserveHandler.makeReservation();
    }

    @Override
    public void menu2() {
        reserveHandler.getReservations();
    }

    @Override
    public void menu3() {

        reserveHandler.cancelReservation();
    }




}
