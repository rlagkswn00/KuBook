package com.example.service.Handler;

import com.example.SharedData;
import com.example.model.Date;
import com.example.model.PenaltyUser;
import com.example.model.KLog;
import com.example.model.Reservation;
import com.example.utils.Validation;

import java.util.*;

import static com.example.utils.Validation.printErrorMessage;

public class ReserveHandler {
    String ID;
    List<String> dates;
    SharedData sharedData = SharedData.getInstance();
    Scanner sc = new Scanner(System.in);

    /* 건물 추가 및 삭제에 따른 건물 목록 구현 */
    List<String> kcubelist = new ArrayList<>(); //케이큐브 건물 목록
    LinkedHashMap<String, String> kcuberoomlist = new LinkedHashMap<>(); //호실 목록
    private Reservation reservation; //예약 내역
    private String reservekcube; //예약 건물 이름
    private String reservedate; //예약 날짜
    private String nroom; //예약 호실
    private String nmates; //동반 예약 인원수
    private String npeople; //예약 인원수
    private String nstart; //에약 시작 시간
    private String nuse; //예약 이용 시간
    String [][] checkarr = null; //예약 가능 여부 저장 배열
    Date checkreserve = new Date(dates.get(Integer.parseInt(reservedate)),null); //예약 날짜 객체
    List<String> IDs = new ArrayList<>(); //학번 목록 저장
    List<Map<Date,Reservation>> cancellist = new ArrayList<>(); //예약 취소 가능 목록
    Date canceldate = null; //예약 취소 날짜 key
    List<Reservation> pfinalcanlist = new ArrayList<>();; //취소 날짜의 예약 목록
    List<String> pIDs = new ArrayList<>();; //학번들

    public ReserveHandler(List<String> dates, String id) {
        this.dates = dates;
        this.ID = id;
    }
    public String selectKCube(){
        //건물 선택

        while(true) {
            int totBuildingNum = kcubelist.size(); //건물 총 개수 처장하기
            for(int i=0; i<kcubelist.size(); i++) {
                System.out.print(i+1+". "+kcubelist.get(i));
            }
            System.out.print("예약하실 건물을 선택하세요 (ex. 1) : ");
            String kcubenum = sc.nextLine();
            if(Validation.validateBuildingNum(kcubenum,totBuildingNum)){
                reservekcube = kcubelist.get(Integer.parseInt(kcubenum)-1); //건물 이름 저장
                //todo kcuberoomlist에 해당 건물 호실 정보 저장하기
                break;
            }
        }
        return reservekcube;
    }
    public String selectDate(){
        //날짜 선택

        /* todo 주말 예약 불가 구현 */
        label: while(true) {
            for (int i = 0; i < 8; i++) {
                System.out.print("(" + i + ") " + dates.get(i) + " ");
            }
            System.out.print("\n예약하실 날짜를 선택하세요 (ex. 1) : ");
            reservedate = sc.nextLine();
            //출력할 날짜의 개수
            int totPrintedDate=7;

            if(Validation.validateReservationDate(reservedate, totPrintedDate)){
                //패널티 대상자 처리
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
        return reservedate;
    }
    public void display(){
        // 예약 가능한 목록 출력
        System.out.println("\n"+reservekcube+" "+dates.get(Integer.parseInt(reservedate)) +" 예약 가능 목록입니다.\n(■ : 선택 불가, □ : 선택 가능)");
        System.out.println("        09  10  11  12  13  14  15  16  17  18  19  20  21");

        //예약 가능 여부 체크
        checkarr = new String[kcuberoomlist.size()][13];
        for(int i=0; i<kcuberoomlist.size(); i++){
            for(int j=0; j<13; j++){
                checkarr[i][j] = "   □";  // 모두 선택 '가능'으로 초기화
            }
        }

        //todo 호실 문법규칙 수정에 따른 변수, 예외처리 수정
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
        for(int i=0; i<kcuberoomlist.size(); i++) {
            System.out.print(i+1+"호실 ");
            for(int j=0; j<13; j++){
                System.out.print(checkarr[i][j]);
            }
            System.out.println();
        }
    }
    public String selectRoom(){
        //호실 선택
        while(true) {
            System.out.print("예약하실 호실을 선택하세요 (ex. 3) : ");
            nroom = sc.nextLine();
            if(Validation.validateReservationRoomNum(nroom, sharedData.kcubes.size())){
                break;
            }
        }
        return nroom;
    }
    public String inputNPeople(){
        //인원수 선택
        while(true) {
            System.out.print("본인을 제외한 전체 예약 인원수를 입력하세요 (ex. 3) : "); //todo 최소~최대 인원수 출력

            nmates = sc.nextLine();
            if(Validation.validateSelfExcludedTotMemberNumber(nmates, Integer.parseInt(sharedData.kcubes.get(Validation.selectedRoomNum-1).getMax())-1)) {
                break;
            }
        }
        return "인원수 정보";
    }
    public List<String> inputMatesIDs(){
        //동반 예약자 입력
        IDs.add(ID);
        for(int i=0; i<Integer.parseInt(nmates);) {
            label:
            while (true) {
                System.out.print(i + 1 + "번째 동반 예약자의 학번을 입력하세요 (ex. 202011111) : ");
                String numID = sc.nextLine();

                if (Validation.validateUserId(numID)) {
                    if (IDs.contains(numID)) {
                        System.out.println("이미 등록한 예약자의 학번입니다.");
                    } else {
                        if (checkreserve.date.equals(sharedData.currentTime.date) && sharedData.penalizedUsers.get(checkreserve) != null) {
                            for (int j = 0; j < sharedData.penalizedUsers.get(checkreserve).size(); j++) {
                                if (sharedData.penalizedUsers.get(checkreserve).get(j).userId.equals(numID)) {
                                    System.out.println("패널티가 있는 예약자의 학번입니다.");
                                    continue label;
                                }
                            }
                            IDs.add(numID);
                            i++;
                        } else {
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
                if (i == Integer.parseInt(nmates)) break;
            }
        }
        npeople = Integer.toString(Integer.parseInt(nmates)+1);
        return IDs; // 확인
    }
    public String inputNStart(){
        //시작 시간 입력
        while(true) {
            System.out.print("예약 시작 시간을 입력하세요 (ex. 12) :  ");
            nstart = sc.nextLine();
            //todo 사용불가 -> 예약 불가능 예외처리
            if(Validation.validateReservationStartTime(reservedate, nstart)){
                if(checkarr[Integer.parseInt(nroom)-1][Integer.parseInt(nstart)-9].equals("   ■")){
                    System.out.println("오류! 예약이 불가한 시간입니다.");
                }else{
                    break;
                }
            }
        }
        return nstart;
    }
    public String inputNUse(){
        //이용 시간 입력
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
        return nuse;
    }
    public void makeReservation(){
        // 예약
        selectKCube();
        selectDate();
        display();
        selectRoom();
        inputNPeople();
        inputMatesIDs();
        inputNStart();
        inputNUse();

        //sharedData에 예약 목록과 로그 업데이트
        reservation = Reservation.from(reservekcube, nroom, nstart, nuse, npeople, IDs);
        sharedData.reservationList.get(checkreserve).add(reservation);
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
    public void getReservations(){
        // 예약 목록 확인
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
                            ListToString(others, ID);
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
    public void personalcancel(String cancel){
        //todo 개인 예약 취소
    }
    public void allcancel(String cancel){
        //전체 예약 취소
        int reservenum = 1; //예약 목록 번호
        if(canceldate != null && canceldate.date.equals(sharedData.currentTime.date)){
            String currentcancel;
            do {
                System.out.print("당일 취소하실 경우 패널티가 부과됩니다. 패널티 부과 시 당일 케이큐브 이용이 불가합니다.\n그래도 취소하시겠습니까? (ex. Y, N ) : ");
                currentcancel = sc.nextLine();
                if(currentcancel.equals("Y")){
                    //sharedData에 예약자, 동반 예약자 당일 예약 취소 처리
                    for(String pID:pIDs){
                        //log, 예약목록 삭제
                        sharedData.logs.get(canceldate).removeIf(n->n.userId.contains(pID));
                        sharedData.reservationList.get(canceldate).removeIf(n->n.userIds.contains(pID));

                        //패널티 학번들 추가
                        PenaltyUser pu = (new PenaltyUser(pID));
                        if(sharedData.penalizedUsers.getOrDefault(canceldate, null) == null){
                            sharedData.penalizedUsers.put(canceldate, new ArrayList<>());
                        }
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
                            if (prdate != null) {
                                System.out.print(+reservenum+". "+cancellist.get(i).get(prdate).name+", "+cancellist.get(i).get(prdate).room+"호실, "
                                        + prdate.date+", "+cancellist.get(i).get(prdate).startTime+"시, "+cancellist.get(i).get(prdate).useTime+"h, ");
                            }
                            List<String> others = cancellist.get(i).get(prdate).userIds;
                            ListToString(others, ID);
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
            //log 업데이트
            int cancelusetime = Integer.parseInt(cancellist.get(Integer.parseInt(cancel)-1).get(canceldate).useTime); //취소되는 시간
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


            //sharedData 에 취소 처리
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
                    ListToString(others, ID);
                    reservenum++;
                }
            }
            else{
                System.out.println("\n예약목록이 없습니다.");
            }
            System.out.println("취소되었습니다. 5초 후 메뉴로 돌아갑니다.\n");
        }
    }
    public void cancelReservation(){
        // 예약 취소
        System.out.println("\n[ 건물, 호실, 사용할 날짜, 예약 시작 시간, 이용시간, (학번들) ]");
        int reservenum = 1; //예약 목록 번호
        for(int i=0; i<8; i++){
            List<Reservation> reslist = sharedData.reservationList.get(new Date(dates.get(i)));
            if(!reslist.isEmpty()){
                for(int j=0; j<reslist.size(); j++){
                    if(reslist.get(j).userIds.contains(ID)){
                        System.out.print(reservenum+". "+reslist.get(j).name+", "+reslist.get(j).room+"호실, "
                                + dates.get(i)+", "+reslist.get(j).startTime+"시, "+reslist.get(j).useTime+"h, ");
                        List<String> others = reslist.get(j).userIds;
                        ListToString(others, ID);
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
            String cancel;
            while(true) {
                System.out.print("취소할 목록을 선택하세요 (ex. 1) : ");
                cancel = sc.nextLine();
                if(Validation.validateCancelNum(cancel, cancellist.size())){
                    break;
                }
            }
            for(Date key:cancellist.get(Validation.selectedCancelNum-1).keySet()){
                canceldate = key;
            }
            pfinalcanlist = sharedData.reservationList.get(canceldate); //취소 날짜의 예약 목록
            pIDs = cancellist.get(Integer.parseInt(cancel)-1).get(canceldate).userIds; //학번들
            if(){ // todo 예약 취소 조건 설정
                personalcancel(cancel); //개인 예약 취소
            }
            else{
                allcancel(cancel); //전체 예약 취소
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void ListToString(List<String> s, String ID){
        //학번 출력 방식
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

    public int minNum(int maxnum){
        //todo 최소 인원수 계산 구현
        int mimnum = 0; //최소 인원수
        return mimnum;
    }
}
