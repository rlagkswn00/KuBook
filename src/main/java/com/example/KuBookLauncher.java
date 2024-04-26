package com.example;

import com.example.fileio.FileManager;
import com.example.model.Date;
import com.example.model.KLog;
import com.example.model.PenaltyUser;
import com.example.model.Reservation;
import com.example.utils.Validation;
import com.sun.source.tree.WhileLoopTree;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.*;

@Slf4j
public class KuBookLauncher {
    public static SharedData sharedData = SharedData.getInstance();
    public static void main(String[] args) throws IOException {
        FileManager fileManager = new FileManager();
//        log.info("load 성공\n"+SharedData.getInstance().toString());
        //changeTest();

        Scanner sc = new Scanner(System.in);

        /*날짜, 시간, 학번 예외처리*/
        String date="";
        String time="";
        String ID;
        List<String> dates;

        while (true) {
            System.out.print("현재 날짜를 입력해주세요 (ex. 20240101) : ");
            date=sc.nextLine();
            if (Validation.validateDate(date)) {
                dates = FileManager.dateGenerator(date);
                break;
            }
        }

        while(true){
            System.out.print("현재 시간을 입력해주세요 (ex. 1230) : ");

            time = sc.nextLine();
            if(Validation.validateTime(time)){
                 /*현재 시간 예외처리에 따른 sharedData penalty, log, reservation update 처리*/
                if(Integer.parseInt(sharedData.currentTime.date)<Integer.parseInt(date)){  //currentTime 이후 날짜인 경우
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
        while(true){
            System.out.print("학번을 입력해주세요 (ex. 202012345) : ");

            ID = sc.nextLine();
            if(Validation.validateUserId(ID)){
                break;
            }
        }

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
                    menu1(dates, ID);
                    break;
                case "2":
                    menu2(dates, ID);
                    break;
                case "3":
                    menu3(dates, ID);
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
        // TODO : 프로그램 끝날 때 꼭! fileManager.save 호출하기.

    }

    public static void menu1(List<String> dates, String ID){
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
            int totPrintedDate=8;

            if(Validation.validateReservationDate(reservedate, totPrintedDate)){
                /*패널티 대상자 예외처리 필요*/
                if(sharedData.penalizedUsers.get(Date.fromWithNoValidation(dates.get(0),null))!=null) {
                    for (int i = 0; i < sharedData.penalizedUsers.get(Date.fromWithNoValidation(dates.get(0), null)).size(); i++) {
                        if (sharedData.penalizedUsers.get(Date.fromWithNoValidation(dates.get(0), null)).get(i).getUserId().equals(ID) && Validation.selectedReservationDate == 0) {
                            System.out.println("해당 날짜는 페널티에 의해 예약하실 수 없습니다.");
                            continue label;
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
        int intNRoom;
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
            while (true) {
                System.out.print(i + 1 + "번째 동반 예약자의 학번을 입력하세요 (ex. 202011111) : ");
                String numID = sc.nextLine();

                if(Validation.validateUserId(numID)) {
                    if(!IDs.contains(numID)) {
                        IDs.add(numID);
                        i++;
                    }else{
                        System.out.println("이미 등록한 예약자의 학번입니다.");

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
            if(Validation.validateReservationStartTime(nstart)){
                if(checkarr[Integer.parseInt(nroom)-1][Integer.parseInt(nstart)-9].equals("   ■")){
                    System.out.print("오류! 예약이 불가한 시간입니다.");
                    System.out.print(" 다시 입력해주세요. (ex. 12) : ");
                }else{
                    break;
                }
            }
        }

        String nuse;
        while(true) {
            System.out.print("이용할 시간을 입력하세요 (1~3시간만 가능) :  ");

            nuse = sc.nextLine();
            if(Validation.validateReservationUseTime(nuse)){
                if((Integer.parseInt(nstart)+Integer.parseInt(nuse))>=23){
                    System.out.println("K CUBE 마감시간을 넘어갑니다");
                }else{
                    break;
                }
            }
        }
        //sharedData에 예약 목록과 로그 추가
        Reservation nreserve = Reservation.from("공학관", nroom, nstart, nuse, npeople, IDs);
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

    public static void menu2(List<String> dates, String ID){
        System.out.println("\n[ 건물, 호실, 사용할 날짜, 예약 시작 시간, 이용시간, (학번들) ]");
        while(true){
            boolean reserveflag = true;
            for(int i=0; i<8; i++){
                List<Reservation> reslist = sharedData.reservationList.get(new Date(dates.get(i)));
                if(!reslist.isEmpty()){
                    for(int j=0; j<reslist.size(); j++){
                        if(reslist.get(j).userIds.contains(ID)){
                            System.out.print(reslist.get(j).name+", "+reslist.get(j).room+"호실, "
                                    + dates.get(i)+", "+reslist.get(j).startTime+"시, "+reslist.get(j).useTime+"h, ");
                            List<String> others = reslist.get(j).userIds;
                            ListtoString(others, ID);
                        }
                    }
                    reserveflag = false;
                }
            }
            if(reserveflag){
                System.out.println("예약목록이 없습니다.");

            }
            Scanner sc = new Scanner(System.in);
            System.out.print("처음으로 돌아가려면 B를 입력하세요. (ex. B) : ");
            String Back = sc.nextLine();
            if(Back.equals("B")) {
                System.out.println("\n처음으로 돌아갑니다.\n");
                break;
            }
                System.out.println("오류! 잘못된 입력입니다. B만 입력가능합니다.");


        }
    }

    public static void menu3(List<String> dates, String ID){
        System.out.println("\n[ 건물, 호실, 사용할 날짜, 예약 시작 시간, 이용시간, (학번들) ]");
        int reservenum = 1; //예약 목록 번호
        List<Map<Date,Reservation>> cancellist = new ArrayList<>(); //예약 취소 가능 목록
        for(int i=0; i<8; i++){
            List<Reservation> reslist = sharedData.reservationList.get(new Date(dates.get(i)));
            if(!reslist.isEmpty()){
                for(int j=0; j<reslist.size(); j++){
                    if(reslist.get(j).userIds.contains(ID)){
                        System.out.print(reservenum+". "+reslist.get(j).name+", "+reslist.get(j).room+"호실, "
                                + dates.get(i)+", "+reslist.get(j).startTime+"시, "+reslist.get(j).useTime+"h, ");
                        List<String> others = reslist.get(j).userIds;
                        ListtoString(others, ID);
                        Map<Date, Reservation> map = new HashMap<>();
                        map.put(new Date(dates.get(i)), reslist.get(j));
                        cancellist.add(map);
                        reservenum++;
                    }
                }
            }
        }
        if(cancellist.isEmpty()){System.out.println("예약 목록이 없습니다. 5초 후 메뉴로 돌아갑니다.");}
        else {
            Scanner sc = new Scanner(System.in);
            /*취소 번호 예외처리*/
            String cancel;
            while(true) {
                System.out.print("취소할 목록을 선택하세요 (ex. 1) : ");

                cancel = sc.nextLine();
                if(Validation.validateCancelNum(cancel, cancellist.size())){
                    break;
                }
            }
            Date canceldate = null;
            for(Date key:cancellist.get(Validation.selectedCancelNum-1).keySet()){
                canceldate = key;
            }
            List<Reservation> pfinalcanlist = sharedData.reservationList.get(canceldate); //취소 날짜의 예약 목록
            List<String> pIDs = cancellist.get(Integer.parseInt(cancel)-1).get(canceldate).userIds; //학번들
            if(canceldate.date.equals(sharedData.currentTime.date)){
                String currentcancel;
                do {
                    System.out.print("당일 취소하실 경우 패널티가 부과됩니다. 패널티 부과 시 당일 케이큐브 이용이 불가합니다.\n그래도 취소하시겠습니까? (ex. Y, N ) : ");
                    /*문자 예외처리*/
                    currentcancel = sc.nextLine();
                    if(currentcancel.equals("Y")){
                        //sharedData에 예약자, 동반 예약자 당일 예약 취소 처리
                        for(String pID:pIDs){
                            /*log 현재 파일에 저장된 데이터로는 오류 발생...일단 주석 처리
                            sharedData.penalizedUsers.get(canceldate).removeIf(n->n.userId.contains(pID));
                            */
                            sharedData.reservationList.get(canceldate).removeIf(n->n.userIds.contains(pID));
                            //패널티 학번들 추가
                            PenaltyUser pu = (new PenaltyUser(pID));
                            sharedData.penalizedUsers.get(canceldate).add(pu);
                        }

                        //취소된 후 예약목록
                        System.out.println();
                        cancellist.removeIf(n->n.containsKey(new Date(sharedData.currentTime.date)));
                        if(!cancellist.isEmpty()){
                            reservenum=1;
                            Date prdate = null;
                            for(int i=0; i<cancellist.size(); i++){
                                for(Date prkey:cancellist.get(i).keySet()) {
                                    prdate = prkey;
                                }
                                System.out.print(+reservenum+". "+cancellist.get(i).get(prdate).name+", "+cancellist.get(i).get(prdate).room+"호실, "
                                        + prdate.date+", "+cancellist.get(i).get(prdate).startTime+"시, "+cancellist.get(i).get(prdate).useTime+"h, ");
                                List<String> others = cancellist.get(i).get(prdate).userIds;
                                ListtoString(others, ID);
                                reservenum++;
                            }
                        }
                        else{
                            System.out.println("예약목록이 없습니다.");
                        }
                        System.out.println("예약이 취소되었습니다. 당일 이용이 불가합니다.");
                        System.out.println("5초 후 메뉴로 돌아갑니다.\n");
                    } else if (currentcancel.equals("N")) {
                        System.out.println("5초 후 메뉴로 돌아갑니다.\n");
                    }
                } while(!Validation.validateSameDayCanceling(currentcancel));
            }
            else {
                //log 현재 파일에 저장된 데이터로는 오류 발생...일단 주석 처리
                /*int cancelusetime = Integer.parseInt(cancellist.get(Integer.parseInt(cancel)-1).get(canceldate).useTime); //취소되는 시간
                for(int i=0; i<pIDs.size(); i++){
                    for(int j=0; j<sharedData.logs.get(canceldate).size(); j++){
                        if(sharedData.logs.get(canceldate).get(j).userId.equals(pIDs.get(i))) {
                            int canceltime = Integer.parseInt(sharedData.logs.get(canceldate).get(j).useTime)-cancelusetime;
                            sharedData.logs.get(canceldate).get(j).setUseTime(Integer.toString(canceltime));
                            break;
                        }
                    }
                }
                sharedData.logs.get(canceldate).removeIf(n->n.useTime.equals("0"));
                */

                //sharedData에 취소 처리
                for (int i = 0; i < pfinalcanlist.size(); i++) {
                    if (pfinalcanlist.get(i).equals(cancellist.get(Integer.parseInt(cancel) - 1).get(canceldate))) {
                        sharedData.reservationList.get(canceldate).remove(i);
                        break;
                    }
                }

                //취소된 후 예약목록
                cancellist.remove(Integer.parseInt(cancel)-1);
                if(!cancellist.isEmpty()){
                    System.out.println();
                    reservenum=1;
                    Date rdate = null;
                    for(int i=0; i<cancellist.size(); i++){
                        for(Date rkey:cancellist.get(i).keySet()) {
                            rdate = rkey;
                        }
                        System.out.print(+reservenum+". "+cancellist.get(i).get(rdate).name+", "+cancellist.get(i).get(rdate).room+"호실, "
                                + rdate.date+", "+cancellist.get(i).get(rdate).startTime+"시, "+cancellist.get(i).get(rdate).useTime+"h, ");
                        List<String> others = cancellist.get(i).get(rdate).userIds;
                        ListtoString(others, ID);
                        reservenum++;
                    }
                }
                else{
                    System.out.println("\n예약목록이 없습니다.");
                }
                System.out.println("취소되었습니다. 5초 후 메뉴로 돌아갑니다.\n");
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //학번 출력 방식
    public static void ListtoString(List<String> s, String ID){
        System.out.print("(");
        boolean flag = true;
        for(int i=0; i<s.size(); i++){
            if(s.get(i).equals(ID));
            else{
                if(flag) {System.out.print(s.get(i)); flag = false;}
                else System.out.print(", "+s.get(i));
            }
        }
        System.out.print(")\n");
    }


    private static void changeTest() {
        SharedData sharedData = SharedData.getInstance();
        sharedData.currentTime = new Date("20240428", "1530");
        sharedData.penalizedUsers.put(
                sharedData.currentTime,
                Arrays.asList(new PenaltyUser("202011247"), new PenaltyUser("202011245"))
        );
    }
}