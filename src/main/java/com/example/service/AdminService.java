package com.example.service;

import com.example.SharedData;
import com.example.fileio.FileManager;
import com.example.model.Handler.AdminHandler;

import java.util.List;
import java.util.Scanner;

public class AdminService extends Service {
    SharedData sharedData = SharedData.getInstance();
    public AdminHandler adminHandler = new AdminHandler();

    public AdminService(List<String> dates, String id){
        super(dates,id);
    }

    @java.lang.Override
    public void menu() {
        Scanner sc = new Scanner(System.in);
        while(true) {
            // todo 메뉴 목록 출력 코드 작성
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
                    fileManager.save();
                    System.exit(0);
                    break;
                default:
                    System.out.println("오류! 메뉴에 없는 입력입니다. 다시 입력해주세요.\n");
                    break;
            }
        }
    }

    public void menu1() {
        // 사용 불가 설정
        adminHandler.disableRoom();
    }

    public void menu2() {
        //todo 건물/ 호실 추가 및 삭제 선택 메뉴 작성

        // 대응하는 일들
        if(){
            adminHandler.addBuilding();
        }
        if(){
            adminHandler.addRoom();
        }
        if(){
            adminHandler.deleteBuilding();
        }
        if(){
            adminHandler.deleteRoom();
        }


    }
    public void menu3() {
        // 최대 인원수 설정
        adminHandler.setMaxCapacity();
    }
}
