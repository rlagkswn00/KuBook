package com.example.service;

import com.example.SharedData;
import com.example.fileio.FileManager;
import com.example.service.Handler.AdminHandler;
import com.example.utils.Validation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class AdminService extends Service {
    public AdminHandler adminHandler = new AdminHandler();
    private final Scanner sc = new Scanner(System.in);
    SharedData sharedData = SharedData.getInstance();

    public AdminService(List<String> dates, String id) {
        super(dates, id);
    }

    @Override
    public void menu() throws IOException {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("1. 사용불가 설정 2. 건물/호실 추가 및 삭제 3. 최대인원수 설정 4. 종료하기");
            System.out.print("메뉴를 선택하세요 (ex. 1) : ");
            String menu = sc.nextLine();
            switch (menu) {
                case "1":
                    adminMenu1_setUnavailable();
                    break;
                case "2":
                    adminMenu2_updateBuildingAndRooms();
                    break;
                case "3":
                    adminMenu3_setMaxPeople();
                    break;
                case "4":
                    System.out.print("시스템을 종료합니다.");
                    sc.close();
                    FileManager.getInstance().save();
//                    fileManager.save();
                    System.exit(0);
                    break;
                default:
                    System.out.println("오류! 메뉴에 없는 입력입니다. 다시 입력해주세요.\n메뉴를 선택하세요 (ex. 1) : ");
                    break;
            }
        }
    }

    public void adminMenu1_setUnavailable() {
        // 사용 불가 설정
        System.out.println("\n...선택한 메뉴 창으로 이동...\n");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        adminHandler.disableRoom();
    }

    public void adminMenu2_updateBuildingAndRooms() {
        System.out.println("1. 건물 추가 2. 건물 삭제 3. 호실 추가 4. 호실 삭제");
        System.out.print("메뉴를 선택하세요 (ex. 1) : ");
        String[] menus = {"1","2","3","4"};
        while (true) {
            String menu = sc.nextLine();
            boolean isValidInput = Arrays.asList(menus).contains(menu);
            if (!isValidInput) {
                System.out.print("오류! 메뉴에 없는 입력입니다. 다시 입력해주세요.\n메뉴를 선택하세요 (ex. 1) : ");
                continue;
            }
            System.out.println("\n...선택한 메뉴 창으로 이동...\n");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (menu.equals("1")) {
                adminHandler.addBuilding();
                break;
            }
            if (menu.equals("2")) {
                adminHandler.deleteBuilding();
                break;
            }
            if (menu.equals("3")) {
                adminHandler.addRoom();
                break;
            }
            if (menu.equals("4")) {
                adminHandler.deleteRoom();
                break;
            }
        }
    }

    public void adminMenu3_setMaxPeople() {
        // 최대 인원수 설정
        adminHandler.setMaxCapacity();
    }
}
