package com.example.service;

import com.example.SharedData;
import com.example.fileio.FileManager;
import com.example.service.Handler.ReserveHandler;

import java.io.IOException;
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
    public void menu() throws IOException {
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
                    userMenu1_bookReservation();
                    break;
                case "2":
                    userMenu2_showReservationList();
                    break;
                case "3":
                    userMenu3_cancelReservation();
                    break;
                case "4":
                    System.out.print("시스템을 종료합니다.");
                    sc.close();
                    FileManager.getInstance().save();
                    System.exit(0);
                    break;
                default:
                    System.out.println("오류! 메뉴에 없는 입력입니다. 다시 입력해주세요.\n");
                    break;
            }
        }
    }

    public void userMenu1_bookReservation() {
        reserveHandler.makeReservation();
    }


    public void userMenu2_showReservationList() {
        reserveHandler.getReservations();
    }


    public void userMenu3_cancelReservation() {
        reserveHandler.cancelReservation();
    }




}
