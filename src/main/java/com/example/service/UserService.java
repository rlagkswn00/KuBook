package com.example.service;

import com.example.fileio.FileManager;
import com.example.model.Date;
import com.example.model.Handler.ReserveHandler;
import com.example.model.KLog;
import com.example.model.Reservation;
import com.example.utils.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.example.utils.Validation.printErrorMessage;

public class UserService extends Service {
    private final Scanner sc = new Scanner(System.in);

    public UserService(List<String> dates, String id){
        super(dates,id);
    }
    ReserveHandler reserveHandler = new ReserveHandler(dates,id);
    @java.lang.Override
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

    public void menu1() {
        reserveHandler.makeReservation();
        Scanner sc = new Scanner(System.in);

        String reservedate;

        /*건물, 날짜 예외처리*/
        while(true) {
            /*건물 총 개수*/
            int totBuildingNum=1;
            System.out.println("1. 공학관");  //일단 건물 1개로 설정
            System.out.print("예약하실 건물을 선택하세요 (ex. 1) : ");
            String reservekcube = sc.nextLine();

            if(Validation.validateBuildingNum(reservekcube,totBuildingNum)){
                break;
            }
        }

        label: while(true) {
            for (int i = 0; i < 8; i++) {
                System.out.print("(" + i + ") " + dates.get(i) + " ");
            }
            System.out.print("\n예약하실 날짜를 선택하세요 (ex. 1) : ");
            reservedate = sc.nextLine();
            /*출력할 날짜의 개수*/
            int totPrintedDate=7;

            if(Validation.validateReservationDate(reservedate, totPrintedDate)){
                /*패널티 대상자 예외처리 필요*/
                if(sharedData.penalizedUsers.get(Date.fromWithNoValidation(dates.get(0),null))!=null) {
                    for (int i = 0; i < sharedData.penalizedUsers.get(Date.fromWithNoValidation(dates.get(0), null)).size(); i++) {
                        if (sharedData.penalizedUsers.get(Date.fromWithNoValidation(dates.get(0), null)).get(i).getUserId().equals(ID) && Validation.selectedReservationDate == 0) {
                            printErrorMessage("해당 날짜는 페널티에 의해 예약하실 수 없습니다.");
                            continue label;
                        }
                    }
                }
                if(sharedData.logs.get(new Date(dates.get(Integer.parseInt(reservedate)),null))!=null) {
                    for (int i = 0; i < sharedData.logs.get(new Date(dates.get(Integer.parseInt(reservedate)),null)).size(); i++) {
                        if (sharedData.logs.get(new Date(dates.get(Integer.parseInt(reservedate)),null)).get(i).userId.equals(ID)) {
                            if (Integer.parseInt(sharedData.logs.get(new Date(dates.get(Integer.parseInt(reservedate)),null)).get(i).useTime) >= 3) {
                                System.out.println("해당 날짜의 누적 이용시간은 3시간이므로 예약하실 수 없습니다.");
                                continue label;
                            }
                        }
                    }
                }
                break;
            }
        }


        System.out.println("\n공학관 "+dates.get(Integer.parseInt(reservedate)) +" 예약 가능 목록입니다.\n(■ : 선택 불가, □ : 선택 가능)");
        System.out.println("        09  10  11  12  13  14  15  16  17  18  19  20  21");

        //예약 가능 여부 체크
        String [][] checkarr = new String[3][13]; //예약 가능 여부 저장 배열
        for(int i=0; i<3; i++){
            for(int j=0; j<13; j++){
                checkarr[i][j] = "   □";  // 모두 선택 '가능'으로 초기화
            }
        }
        Date checkreserve = new Date(dates.get(Integer.parseInt(reservedate)));
        List<Reservation> reslist = sharedData.reservationList.get(checkreserve);
        if (reslist != null) { // 해당 날짜에 예약 목록이 존재하는 경우
            for (int i = 0; i < reslist.size(); i++) {
                int resroom = Integer.parseInt(reslist.get(i).room);
                int resstart = Integer.parseInt(reslist.get(i).startTime);
                int resuse = Integer.parseInt(reslist.get(i).useTime);
                for(int j=resstart-9; j<resstart-9+resuse; j++){
                    checkarr[resroom-1][j] = "   ■";
                }
            }
        }

        //예약 가능 테이블 출력
        for(int i=0; i<3; i++) {
            System.out.print(i+1+"호실 ");
            for(int j=0; j<13; j++){
                System.out.print(checkarr[i][j]);
            }
            System.out.println();
        }

        String nroom;
        int intRoom;
        String npeople;

        /*각 문법 규칙에 알맞는 예외 처리 필요*/
        while(true) {
            System.out.print("예약하실 호실을 선택하세요 (ex. 3) : ");

            nroom = sc.nextLine();
            if(Validation.validateReservationRoomNum(nroom, sharedData.kcubes.size())){
                break;
            }
        }

        while(true) {
            System.out.print("본인을 제외한 전체 예약 인원수를 입력하세요 (ex. 3): ");

            npeople = sc.nextLine();
            if(Validation.validateSelfExcludedTotMemberNumber(npeople, Integer.parseInt(sharedData.kcubes.get(Validation.selectedRoomNum-1).getMax())-1)) {
                break;
            }
        }

        List<String> IDs = new ArrayList<>(); //학번 목록 저장
        IDs.add(ID);
        for(int i=0; i<Integer.parseInt(npeople);){
            label:while (true) {
                System.out.print(i + 1 + "번째 동반 예약자의 학번을 입력하세요 (ex. 202011111) : ");
                String numID = sc.nextLine();

                if(Validation.validateUserId(numID)) {
                    if(IDs.contains(numID)){
                        System.out.println("이미 등록한 예약자의 학번입니다.");
                    }
                    else {
                        if(checkreserve.date.equals(sharedData.currentTime.date) && sharedData.penalizedUsers.get(checkreserve) != null){
                            for(int j=0; j<sharedData.penalizedUsers.get(checkreserve).size(); j++) {
                                if(sharedData.penalizedUsers.get(checkreserve).get(j).userId.equals(numID)){
                                    System.out.println("패널티가 있는 예약자의 학번입니다.");
                                    continue label;
                                }
                            }
                            IDs.add(numID);
                            i++;
                        }
                        else {
                            if (sharedData.logs.get(checkreserve) != null) {
                                for (int j = 0; j < sharedData.logs.get(checkreserve).size(); j++) {
                                    if (sharedData.logs.get(checkreserve).get(j).userId.equals(numID)) {
                                        if (Integer.parseInt(sharedData.logs.get(checkreserve).get(j).useTime) >= 3) {
                                            System.out.println("동반 예약자의 누적 이용시간은 3시간이므로 예약하실 수 없습니다.");
                                            continue label;
                                        }
                                    }
                                }
                            }
                            IDs.add(numID);
                            i++;
                        }
                    }
                }
                if(i == Integer.parseInt(npeople)) break;
            }
        }

        String nstart;
        while(true) {
            System.out.print("예약 시작 시간을 입력하세요 (ex. 12) :  ");
            nstart = sc.nextLine();
            /* 예약 불가능 예외처리 */
            if(Validation.validateReservationStartTime(reservedate, nstart)){
                if(checkarr[Integer.parseInt(nroom)-1][Integer.parseInt(nstart)-9].equals("   ■")){
                    System.out.println("오류! 예약이 불가한 시간입니다.");
                }else{
                    break;
                }
            }
        }

        String nuse;
        label:while(true) {
            System.out.print("이용할 시간을 입력하세요 (1~3시간만 가능) :  ");
            nuse = sc.nextLine();
            if(Validation.validateReservationUseTime(nuse)){
                if((Integer.parseInt(nstart)+Integer.parseInt(nuse))>=23){
                    System.out.println("K CUBE 마감시간을 넘어갑니다");
                }else{
                    boolean useflag = true;
                    for(int i=1; i<Integer.parseInt(nuse); i++){
                        if(checkarr[Integer.parseInt(nroom)-1][Integer.parseInt(nstart)-9+i].equals("   ■")){
                            System.out.println("오류! 예약이 불가한 시간이 포함되어있습니다. 다시 선택해주세요.");
                            useflag = false;
                            break;
                        }
                    }

                    if(useflag){
                        if(!sharedData.logs.get(checkreserve).isEmpty()) {
                            for (int i = 0; i < sharedData.logs.get(checkreserve).size(); i++) {
                                for(int j=0; j<IDs.size(); j++) {
                                    if (sharedData.logs.get(checkreserve).get(i).userId.equals(IDs.get(j))) {
                                        if (Integer.parseInt(sharedData.logs.get(checkreserve).get(i).useTime) + Integer.parseInt(nuse) > 3) {
                                            System.out.println("예약하려는 날짜의 누적 이용시간은 최대 3시간까지 가능합니다. 다시 선택해주세요.");
                                            continue label;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        //sharedData에 예약 목록과 로그 추가
        Reservation nreserve = Reservation.from("공학관", nroom, nstart, nuse, Integer.toString(Integer.parseInt(npeople)+1), IDs);
        sharedData.reservationList.get(checkreserve).add(nreserve);
        boolean logflag = true;
        for(int i=0; i<IDs.size(); i++){
            logflag = true;
            for(int j=0; j<sharedData.logs.get(checkreserve).size(); j++){
                if(sharedData.logs.get(checkreserve).get(j).userId.equals(IDs.get(i))) {
                    int sum = Integer.parseInt(sharedData.logs.get(checkreserve).get(j).useTime)+Integer.parseInt(nuse);
                    sharedData.logs.get(checkreserve).get(j).setUseTime(Integer.toString(sum));
                    logflag = false;
                    break;
                }
            }
            if(logflag){
                KLog klog = KLog.from(IDs.get(i), nuse);
                sharedData.logs.get(checkreserve).add(klog);
            }
        }

        System.out.println("예약이 완료되었습니다. 5초 후 메뉴로 돌아갑니다.\n");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public void menu2() {
        reserveHandler.getReservations(dates,ID);
    }


    public void menu3() {
        reserveHandler.cancelReservation(dates,ID);
    }




}
