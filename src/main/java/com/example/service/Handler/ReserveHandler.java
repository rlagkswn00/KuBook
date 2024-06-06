package com.example.service.Handler;

import com.example.SharedData;
import com.example.model.Date;
import com.example.model.*;
import com.example.utils.Validation;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.utils.Validation.printErrorMessage;

public class ReserveHandler {
    private String ID; // 예약자
    private List<String> dates;
    private SharedData sharedData = SharedData.getInstance();
    private Scanner sc = new Scanner(System.in);
    private List<String> kcubeList = new ArrayList<>(); //케이큐브 건물 목록
    private LinkedHashMap<Integer, Integer> selectKcubeRoomList = new LinkedHashMap<>(); // 호실 목록 (roomNum, max)
    private Reservation reservation; // 예약 내역
    private String[][] checkarr = null; // 예약 가능 여부 저장 배열
    private Date reserveDate; // 예약 날짜 객체
    private List<Map<Date,Reservation>> cancelableList = new ArrayList<>(); // 예약 취소 가능 목록
//    Date canceldate = null; //예약 취소 날짜 key
    private List<Reservation> finalCancleList = new ArrayList<>(); //취소 날짜의 예약 목록

    private static final String UNAVAILABLE_TIME_STR = "   ■";
    private static final String AVAILABLE_TIME_STR = "   □";


    public ReserveHandler(List<String> dates, String id) {
        this.dates = dates;
        this.ID = id;
        reservation = new Reservation();
    }

    /** @return 예약하려는 날짜 번호에 해당하는 (String) 날짜 */
    private String getDateByIndex(String idx){
        return dates.get(Integer.parseInt(idx));
    }
    private boolean isToday(Date targetDate){
        return Validation.isSameDate(targetDate, dates.get(0));
    }

    private void loadKcubeList(){
        for(Kcube kcube : sharedData.getKcubes()){
            kcubeList.add(kcube.name);
        }
        kcubeList = kcubeList.stream().distinct().collect(Collectors.toList());
    }

    private void updateSelectKcubeRoom(){
        // 호실 정보 저장
        for(Kcube kcube : sharedData.getKcubes()){
            if(kcube.name.equals(reservation.name)){
                selectKcubeRoomList.put(toInt(kcube.room), toInt(kcube.max));
            }
        }
    }

    private void selectKCube(){
        //건물 선택
        while(true) {
            loadKcubeList();
            int totalBuildingNum = kcubeList.size(); // 건물 총 개수 저장하기

            for(int i = 0; i < totalBuildingNum; i++) {
                System.out.print(i+1+". "+ kcubeList.get(i));
            }
            System.out.print("예약하실 건물을 선택하세요 (ex. 1) : ");
            String kcubeNum = sc.nextLine();
            if(Validation.validateBuildingNum(kcubeNum, totalBuildingNum)){
                reservation.setName(kcubeList.get(toInt(kcubeNum)-1)); // 건물 이름 저장
                // todo kcuberoomList에 해당 건물 호실 정보 저장하기
                updateSelectKcubeRoom();
                break;
            }
        }
    }
    private void selectDate(){
        //날짜 선택 selectedReservationDateNum
        String chosenDateIndex;
        String chosenDateStr;
        Date chosenDate;

        while(true) {
            for (int i = 0; i < 8; i++) {
                String curDate = dates.get(i);
                System.out.print("(" + i + ") " + curDate +"["+ Date.getDayOfWeek(curDate) + "] ");
            }
            System.out.print("\n예약하실 날짜를 선택하세요 (ex. 1) : ");
            chosenDateIndex = sc.nextLine();

            if(!Validation.validateChosenDateIndex(chosenDateIndex))
                continue;

            chosenDateStr = getDateByIndex(chosenDateIndex);
            if(Validation.isWeekend(chosenDateStr)){
                printErrorMessage("주말은 예약 불가합니다.");
                continue;
            }

            chosenDate = new Date(chosenDateStr);
            // 패널티 대상자 처리
            // 예약하려는 날짜가 당일 && 패널티 받은 예약자
            if(isToday(chosenDate) && Validation.isPenaltyUser(chosenDate, ID)){
                printErrorMessage("해당 날짜는 페널티에 의해 예약하실 수 없습니다.");
                continue;
            }

            if(Validation.checkNHoursUsage(chosenDate, ID, 3)){
                printErrorMessage("해당 날짜의 누적 이용시간은 3시간이므로 예약하실 수 없습니다.");
                continue;
            }
            break;

        }

        reserveDate = chosenDate;
    }
    public void display(){
        // 예약 가능한 목록 출력
        System.out.println("\n"+ reservation.name +" "+ reserveDate.date +" 예약 가능 목록입니다.\n(■ : 선택 불가, □ : 선택 가능)");
        System.out.println("        09  10  11  12  13  14  15  16  17  18  19  20  21");

        //예약 가능 여부 체크
        checkarr = new String[selectKcubeRoomList.size()][13];
        for (int room = 0; room < selectKcubeRoomList.size(); room++) {
            for (int time = 0; time < 13; time++) {
                checkarr[room][time] = AVAILABLE_TIME_STR;  // 모두 선택 '가능'으로 초기화
            }
        }

        List<Reservation> reservations = sharedData.reservationList.get(reserveDate);
        for (Reservation res : reservations) {
            if (!res.name.equals(reservation.name))
                continue;
            int resRoom = toInt(res.room);
            int resStart = toInt(res.startTime);
            int resEnd = resStart + toInt(res.useTime);
            // checkarr 에 예약목록 적용
            for (int time = resStart - 9; time < resEnd - 9; time++) {
                checkarr[resRoom - 1][time] = UNAVAILABLE_TIME_STR;
            }
        }

        // 관리자 설정 파일 적용
        List<DisableKcube> disableKcubes = sharedData.disableKcubes.get(reserveDate);
        for (DisableKcube kcube : disableKcubes) {
            if(!kcube.name.equals(reservation.name))
                continue;
            int kcubeRoom = toInt(kcube.room);
            int kcubeStartTime = toInt(kcube.startTime);
            int kcubeEndTime = toInt(kcube.endTime);
            // checkarr 에 예약불가목록 적용
            for (int time = kcubeStartTime - 9; time < kcubeEndTime - 9; time++) {
                checkarr[kcubeRoom - 1][time] = UNAVAILABLE_TIME_STR;
            }
        }
        //예약 가능 테이블 출력
        for(int room = 0; room < selectKcubeRoomList.size(); room++) {
            System.out.print((room+1)+"호실 ");
            for(int time = 0; time < 13; time++){
                System.out.print(checkarr[room][time]);
            }
            System.out.println();
        }
    }
    private void selectRoom(){
        //호실 선택
        String nroom;
        while(true) {
            System.out.print("예약하실 호실을 선택하세요 (ex. 3) : ");
            nroom = sc.nextLine();
            if(Validation.validateReservationRoomNum(nroom, selectKcubeRoomList.size())){
                break;
            }
        }
        reservation.setRoom(nroom);
    }

    private Integer inputNPeople(){
        /* nmates = 본인을 미포함한 동반이용자 인원수 */
        String nMates;

        //인원수 선택
        while(true) {
            int maxPeople = getMaxPeople(reservation.name, reservation.room);
            int minPeople = getMinPeople(maxPeople);
            System.out.print("본인을 제외한 전체 예약 인원수를 입력하세요 (ex. "+(minPeople-1)+" ~ "+(maxPeople-1)+") : ");
            nMates = sc.nextLine();
            if(Validation.validateReservationSize(nMates, maxPeople, minPeople)) {
                break;
            }
        }
        return toInt(nMates);
    }

    /** 동반 예약자 입력 함수
     * @param nmates 동반예약자수 (본인 미포함) */
    private void inputMatesIDs(Integer nmates){
        List<String> reserveIDs = new ArrayList<>();
        reserveIDs.add(ID); // ID = 예약자
        int i = 0;
        // 동반 예약자 입력
        while(i != nmates){
            System.out.print((i + 1) + "번째 동반 예약자의 학번을 입력하세요 (ex. 202011111) : ");
            String nthID = sc.nextLine();
            if(!Validation.validateUserId(nthID)) continue;
            /* 동반이용자 - 이미 등록한 예약자 확인 */
            if (reserveIDs.contains(nthID)) {
                printErrorMessage("이미 등록한 예약자의 학번입니다.");
                continue;
            }
            /* 동반이용자 - 패널티 확인 */
            if (isToday(reserveDate) && Validation.isPenaltyUser(reserveDate, nthID)){
                printErrorMessage("패널티가 있는 예약자의 학번입니다.");
                continue;
            }
            /* 동반이용자 - 누적시간 확인 */
            if(Validation.checkNHoursUsage(reserveDate, nthID, 3)){
                printErrorMessage("동반 예약자의 누적 이용시간은 3시간이므로 예약하실 수 없습니다.");
                continue;
            }
            reserveIDs.add(nthID);
            i++;
        }
        reservation.setNumOfPeople(Integer.toString(nmates + 1));
        reservation.setUserIds(reserveIDs);
    }

    private void inputNStart(){
        String nstart;
        //시작 시간 입력
        while(true) {
            System.out.print("예약 시작 시간을 입력하세요 (ex. 12) :  ");
            nstart = sc.nextLine();
            // todo 사용불가 -> 예약 불가능 예외처리
            if(Validation.validateReservationStartTime(reserveDate.date, nstart)){
                if(checkarr[toInt(reservation.room) - 1][toInt(nstart)-9].equals("   ■")){
                    System.out.println("오류! 예약이 불가한 시간입니다.");
                }else{
                    break;
                }
            }
        }
        reservation.setStartTime(nstart);
    }

    private void inputNUse(){
        //이용 시간 입력
        String nuse;
        label:while(true) {
            System.out.print("이용할 시간을 입력하세요 (1~3시간만 가능) :  ");
            nuse = sc.nextLine();
            if(Validation.validateReservationUseTime(nuse)){
                if((toInt(reservation.startTime)+toInt(nuse))>=23){
                    System.out.println("K CUBE 마감시간을 넘어갑니다");
                }else{
                    boolean useflag = true;
                    for(int i=1; i<toInt(nuse); i++){
                        if(checkarr[toInt(reservation.room)-1][toInt(reservation.startTime)-9+i].equals("   ■")){
                            System.out.println("오류! 예약이 불가한 시간이 포함되어있습니다. 다시 선택해주세요.");
                            useflag = false;
                            break;
                        }
                    }

                    if(useflag){
                        if(!sharedData.logs.get(reserveDate).isEmpty()) {
                            // refactor
                            for(KLog log : sharedData.logs.get(reserveDate)){
                                for(String id : reservation.userIds) {
                                    if(log.userId.equals(id) &&
                                        ((toInt(log.useTime) + toInt(nuse)) > 3)){
                                        System.out.println("예약하려는 날짜의 누적 이용시간은 최대 3시간까지 가능합니다. 다시 선택해주세요.");
                                        continue label;
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        reservation.setUseTime(nuse);
    }

    public void makeReservation(){
        // 예약
        selectKCube();
        selectDate();
        display();
        selectRoom();
//        inputNPeople();
        inputMatesIDs(inputNPeople());
        inputNStart();
        inputNUse();

        //sharedData에 예약 목록과 로그 업데이트
//        reservation = Reservation.from(reserveKcube, nroom, nstart, nuse, npeople, IDs);
        sharedData.reservationList.get(reserveDate).add(reservation);
        boolean logflag = true;
        for(String userId : reservation.getUserIds()){
            logflag = true;
            for(KLog log : sharedData.logs.get(reserveDate)){
                if(log.userId.equals(userId)) {
                    int sum = toInt(log.useTime) + toInt(reservation.useTime);
                    log.setUseTime(Integer.toString(sum));
                    logflag = false;
                    break;
                }
            }
            if(logflag){
                KLog klog = KLog.from(userId, reservation.useTime);
                sharedData.logs.get(reserveDate).add(klog);
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
            for(String date : dates){
                List<Reservation> reslist = sharedData.reservationList.get(new Date(date));
                if(!reslist.isEmpty()){
                    for(Reservation res : reslist){
                        if(res.userIds.contains(ID)){
                            System.out.print(res.name+", "+res.room+"호실, "
                                    + date + ", "+res.startTime+"시, "+res.useTime+"h, ");
                            List<String> others = res.userIds;
                            printIDs(others, ID);
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
    public void personalCancel(String cancelNum, Date cancelDate, List<String> pIDs){
        //todo 개인 예약 취소
    }

    public void allCancel(String cancelNum, Date cancelDate, List<String> pIDs){
        //전체 예약 취소
        int reserveNum = 1; //예약 목록 번호

        if(cancelDate != null && cancelDate.date.equals(sharedData.currentTime.date)){
            String cancelStatus;
            do {
                System.out.print("당일 취소하실 경우 패널티가 부과됩니다. 패널티 부과 시 당일 케이큐브 이용이 불가합니다.\n그래도 취소하시겠습니까? (ex. Y, N ) : ");
                cancelStatus = sc.nextLine();
                if(cancelStatus.equals("Y")){
                    //sharedData에 예약자, 동반 예약자 당일 예약 취소 처리
                    for(String pID : pIDs){
                        //log, 예약목록 삭제
                        sharedData.logs.get(cancelDate).removeIf(n->n.userId.contains(pID));
                        sharedData.reservationList.get(cancelDate).removeIf(n->n.userIds.contains(pID));

                        //패널티 학번들 추가
                        PenaltyUser pu = (new PenaltyUser(pID));
                        if(sharedData.penalizedUsers.getOrDefault(cancelDate, null) == null){
                            sharedData.penalizedUsers.put(cancelDate, new ArrayList<>());
                        }
                        sharedData.penalizedUsers.get(cancelDate).add(pu);
                    }

                    //취소된 후 예약목록
                    System.out.println();
                    cancelableList.removeIf(n->n.containsKey(new Date(sharedData.currentTime.date)));
                    if(!cancelableList.isEmpty()){
                        reserveNum=1;
                        Date prdate = null;
                        for(int i = 0; i< cancelableList.size(); i++){
                            for(Date prkey: cancelableList.get(i).keySet()) {
                                prdate = prkey;
                            }
                            if (prdate != null) {
                                System.out.print(+reserveNum+". "+ cancelableList.get(i).get(prdate).name+", "+ cancelableList.get(i).get(prdate).room+"호실, "
                                        + prdate.date+", "+ cancelableList.get(i).get(prdate).startTime+"시, "+ cancelableList.get(i).get(prdate).useTime+"h, ");
                            }
                            List<String> others = cancelableList.get(i).get(prdate).userIds;
                            printIDs(others, ID);
                            reserveNum++;
                        }
                    }
                    else{
                        System.out.println("예약목록이 없습니다.");
                    }
                    System.out.println("예약이 취소되었습니다. 당일 이용이 불가합니다.");
                    System.out.println("5초 후 메뉴로 돌아갑니다.\n");
                } else if (cancelStatus.equals("N")) {
                    System.out.println("5초 후 메뉴로 돌아갑니다.\n");
                }
            } while(!Validation.validateSameDayCanceling(cancelStatus));
        }
        else {
            //log 업데이트
            int cancelUseTime = toInt(cancelableList.get(toInt(cancelNum)-1).get(cancelDate).useTime); //취소되는 시간
            for(int i=0; i<pIDs.size(); i++){
                for(int j=0; j<sharedData.logs.get(cancelDate).size(); j++){
                    if(sharedData.logs.get(cancelDate).get(j).userId.equals(pIDs.get(i))) {
                        int cancelTime = toInt(sharedData.logs.get(cancelDate).get(j).useTime)-cancelUseTime;
                        sharedData.logs.get(cancelDate).get(j).setUseTime(Integer.toString(cancelTime));
                        break;
                    }
                }
            }
            sharedData.logs.get(cancelDate).removeIf(n->n.useTime.equals("0"));


            //sharedData 에 취소 처리
            for (int i = 0; i < finalCancleList.size(); i++) {
                if (finalCancleList.get(i).equals(cancelableList.get(toInt(cancelNum) - 1).get(cancelDate))) {
                    sharedData.reservationList.get(cancelDate).remove(i);
                    break;
                }
            }

            //취소된 후 예약목록
            cancelableList.remove(toInt(cancelNum)-1);
            if(!cancelableList.isEmpty()){
                System.out.println();
                reserveNum = 1;
                Date rdate = null;
                for(int i = 0; i< cancelableList.size(); i++){
                    for(Date rkey: cancelableList.get(i).keySet()) {
                        rdate = rkey;
                    }
                    System.out.print(+reserveNum+". "+ cancelableList.get(i).get(rdate).name+", "+ cancelableList.get(i).get(rdate).room+"호실, "
                            + rdate.date+", "+ cancelableList.get(i).get(rdate).startTime+"시, "+ cancelableList.get(i).get(rdate).useTime+"h, ");
                    List<String> others = cancelableList.get(i).get(rdate).userIds;
                    printIDs(others, ID);
                    reserveNum++;
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
        int reserveListNum = 1; // 예약 목록 번호
        for(String date : dates){
            List<Reservation> reserveList = sharedData.reservationList.get(new Date(date));
            if(!reserveList.isEmpty()){
                for(Reservation res : reserveList){
                    if(res.userIds.contains(ID)){
                        System.out.print(reserveListNum+". "+res.name+", "+res.room+"호실, "
                                + date+", "+res.startTime+"시, "+res.useTime+"h, ");
                        List<String> others = res.userIds;
                        printIDs(others, ID);
                        cancelableList.add(new HashMap<>(){{put(new Date(date), res);}});
                        reserveListNum++;
                    }
                }
            }
        }
        if(cancelableList.isEmpty()){
            System.out.println("예약 목록이 없습니다. 5초 후 메뉴로 돌아갑니다.");
        }else {
            String cancelNum;
            Date cancelDate;
            while(true) {
                System.out.print("취소할 목록을 선택하세요 (ex. 1) : ");
                cancelNum = sc.nextLine();
                if(Validation.validateCancelNum(cancelNum, cancelableList.size())){
                    break;
                }
            }

            int index = toInt(cancelNum) - 1;  // 인덱스
            Map<Date, Reservation> selectedMap = cancelableList.get(index);
            cancelDate = selectedMap.keySet().iterator().next();
            Reservation selectedReservation = selectedMap.get(cancelDate);


            finalCancleList = sharedData.reservationList.get(cancelDate); // 취소 날짜의 예약 목록
            List<String> pIDs = selectedReservation.userIds; // 선택한 날짜의 예약자 학번들
            int maxNum = getMaxPeople(selectedReservation.name, selectedReservation.room);

            if(toInt(selectedReservation.numOfPeople) > getMinPeople(maxNum)){
                personalCancel(cancelNum, cancelDate, pIDs); //개인 예약 취소
            }
            else{
                allCancel(cancelNum, cancelDate, pIDs); //전체 예약 취소
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void printIDs(List<String> s, String ID){
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

    public int toInt(String str){
        return Integer.parseInt(str);
    }

    public int getMaxPeople(String name, String room){
        for(Kcube kcube : sharedData.getKcubes()){
            if(kcube.name.equals(name) && kcube.room.equals(room)){
                return toInt(kcube.max);
            }
        }
        return Integer.MAX_VALUE;
    }

    public int getMinPeople(int maxNum){
        return Math.round((float) maxNum /2);
    }
}
