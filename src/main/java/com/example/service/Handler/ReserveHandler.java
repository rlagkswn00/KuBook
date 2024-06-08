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
    private List<String> dates; // 8일
    private SharedData sharedData = SharedData.getInstance();
    private Scanner sc = new Scanner(System.in);
    private List<String> kcubeList = new ArrayList<>(); //케이큐브 건물 이름 목록
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

    /** @return 예약 불가 시간인가? */
    private boolean isTimeForbidden(int RoomNum, int StartTime){
        return checkarr[RoomNum - 1][StartTime-9].equals(UNAVAILABLE_TIME_STR);
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
                System.out.print((i+1)+". "+ kcubeList.get(i));
            }
            System.out.print("\n예약하실 건물을 선택하세요 (ex. 1) : ");
            String kcubeNum = sc.nextLine();
            if(Validation.validateBuildingNum(kcubeNum, totalBuildingNum)){
                reservation.setName(kcubeList.get(toInt(kcubeNum)-1)); // 건물 이름 set
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
        if(disableKcubes!=null) {
            for (DisableKcube kcube : disableKcubes) {
                if (!kcube.name.equals(reservation.name))
                    continue;
                int kcubeRoom = toInt(kcube.room);
                int kcubeStartTime = toInt(kcube.startTime);
                int kcubeEndTime = toInt(kcube.endTime);
                // checkarr 에 예약불가목록 적용
                for (int time = kcubeStartTime - 9; time < kcubeEndTime - 9; time++) {
                    checkarr[kcubeRoom - 1][time] = UNAVAILABLE_TIME_STR;
                }
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
        String nStart;
        // 시작 시간 입력
        while(true) {
            System.out.print("예약 시작 시간을 입력하세요 (ex. 12) :  ");
            nStart = sc.nextLine();
            if(Validation.validateReservationStartTime(reserveDate.date, nStart)){
                if(isTimeForbidden(toInt(reservation.room), toInt(nStart))){
                    printErrorMessage("오류! 예약이 불가한 시간입니다.");
                }else{
                    break;
                }
            }
        }
        reservation.setStartTime(nStart);
    }

    private void inputNUse(){
        //이용 시간 입력
        String nUse;
        while(true) {
            System.out.print("이용할 시간을 입력하세요 (1~3시간만 가능) :  ");
            nUse = sc.nextLine();
            if(!Validation.validateReservationUseTime(nUse))
                continue;

            if((toInt(reservation.startTime)+toInt(nUse))>=23){
                printErrorMessage("K-CUBE 마감시간을 넘어갑니다");
                continue;
            }

            boolean flag = true;
            for(int i=1; i < toInt(nUse); i++){
                if(isTimeForbidden(toInt(reservation.room), toInt(reservation.startTime) + i)){
                    printErrorMessage("오류! 예약이 불가한 시간이 포함되어있습니다. 다시 선택해주세요.");
                    flag = false;
                    break;
                }
            }
            if(!flag) continue;

            // 로그가 없으면 누적이용시간 확인 필요 x
            if(sharedData.logs.get(reserveDate).isEmpty()) break;

            // 누적 이용시간 확인
            for(String id : reservation.userIds) {
                if(Validation.checkNHoursUsage(reserveDate, id, 4 - toInt(nUse))){
                    printErrorMessage("예약하려는 날짜의 누적 이용시간은 최대 3시간까지 가능합니다. 다시 선택해주세요.");
                    flag = false;
                    break;
                }
            }

            if(!flag) continue;

            break;
        }

        reservation.setUseTime(nUse);
    }

    public void makeReservation(){
        // 예약
        selectKCube();
        selectDate();
        display();
        selectRoom();
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
                            printIDs(res.userIds);
                        }
                    }
                    reserveflag = false;
                }
            }
            if(reserveflag){
                System.out.println("예약목록이 없습니다.");
            }
            Scanner sc = new Scanner(System.in);
            String Back;
            while(true) {
                System.out.print("처음으로 돌아가려면 B를 입력하세요. (ex. B) : ");
                Back = sc.nextLine();
                if (Back.equals("B")) {
                    System.out.println("\n처음으로 돌아갑니다.\n");
                    return;
                }
                System.out.println("오류! 잘못된 입력입니다. B만 입력가능합니다.");
            }
        }

    }

    /** 개인 예약 취소
     * @param cancelNum 취소하고싶은 번호
     * @param pIDs cancelDate 의 예약자 목록
     */
    public void personalCancel(String cancelNum, List<String> pIDs){

        int cancelIdx = toInt(cancelNum) - 1;
        Date cancelDate = getDateByIndexFromCancelableList(cancelIdx);
        String pID = ID;

        Reservation cancelReservation = cancelableList.get(cancelIdx).get(cancelDate);

        /* 1) log 삭제 */
        List<KLog> updatedKLogList = new ArrayList<>();

        // 1. kLog 리스트 update(변경 || 삭제) - 교체방식
        for (KLog kLog : sharedData.logs.get(cancelDate)) {
            if (kLog.userId.equals(pID)) { // pID의 로그
                int changeUseTime = toInt(kLog.useTime) - toInt(cancelReservation.useTime);
                if (changeUseTime > 0)
                    updatedKLogList.add(KLog.from(kLog.userId, Integer.toString(changeUseTime))); // 새로운 로그 추가
            }else{
                updatedKLogList.add(kLog);
            }
        }
        // 2. update한 kLog리스트를 sharedData에 적용 - 교체방식
        sharedData.logs.remove(cancelDate);
        sharedData.logs.put(cancelDate, updatedKLogList);


        /* 2) 예약목록에서 해당 이용자만 삭제 */
        sharedData.reservationList.get(cancelDate).remove(cancelReservation);
        cancelReservation.userIds.removeIf(userId -> userId.equals(ID));
        cancelReservation.decreaseNumOfPeople();
        sharedData.reservationList.get(cancelDate).add(cancelReservation);


        // 예약 취소 성공 후 - cancelableList에서 삭제
        cancelableList.remove(cancelIdx);
        // 취소된 후 사용자의 예약목록 출력
        printCancelList();
        System.out.println("사용자 개인 예약이 취소되었습니다. 5초후 메인 화면으로 돌아갑니다.");
    }

    /** 전체 예약 취소
     * @param cancelNum 취소하고싶은 번호
     * @param pIDs cancelDate 의 예약자 목록
     */
    public void allCancel(String cancelNum, List<String> pIDs){
        int cancelIdx = toInt(cancelNum) - 1;
        Date cancelDate = getDateByIndexFromCancelableList(cancelIdx);
        Reservation cancelReservation = cancelableList.get(cancelIdx).get(cancelDate);

        boolean today = false; // 당일 여부
        if(cancelDate != null && isToday(cancelDate)){
            today = true;
            // 패널티 공지 후 예약 삭제 여부 되묻기
            while(true) {
                System.out.print("당일 취소하실 경우 패널티가 부과됩니다. 패널티 부과 시 당일 케이큐브 이용이 불가합니다.\n그래도 취소하시겠습니까? (ex. Y, N ) : ");
                String cancelStatus = sc.nextLine();
                if (!Validation.validateSameDayCanceling(cancelStatus))
                    continue;
                if (cancelStatus.equals("N")) {
                    System.out.println("5초 후 메인 화면으로 돌아갑니다.\n");
                    return;
                }
                System.out.println();
                break;
            }
        }
        // sharedData에 예약자, 동반 예약자 당일 예약 취소 처리 & 로그 업데이트 & 패널티
        for(String pID : pIDs){
            /* 1) log 삭제 */
            List<KLog> updatedKLogList = new ArrayList<>();

            // 1. kLog 리스트 update(변경 || 삭제) - 교체방식
            for (KLog kLog : sharedData.logs.get(cancelDate)) {
                if (kLog.userId.equals(pID)) { // pID의 로그
                    int changeUseTime = toInt(kLog.useTime) - toInt(cancelReservation.useTime);
                    if (changeUseTime > 0)
                        updatedKLogList.add(KLog.from(kLog.userId, Integer.toString(changeUseTime))); // 새로운 로그 추가
                    else{
                        updatedKLogList.add(kLog);
                    }
                }
            }
            // 2. update한 kLog리스트를 sharedData에 적용 - 교체방식
            sharedData.logs.remove(cancelDate);
            sharedData.logs.put(cancelDate, updatedKLogList);


            /* 2) 예약목록 삭제 */
            sharedData.reservationList.get(cancelDate).remove(cancelReservation);

            /* 3) 당일이면 패널티 부여 */
            if(today){
                // 만약 cancelDate에 해당하는 패널티가 없다면 - 새로 추가
                if(!sharedData.penalizedUsers.containsKey(cancelDate)){
                    sharedData.penalizedUsers.put(cancelDate, new ArrayList<>());
                }
                // cancelDate에 해당하는 패널티유저리스트에 패널티 유저 추가
                sharedData.penalizedUsers.get(cancelDate).add(new PenaltyUser(pID));
            }
        }
        // 예약 취소 성공 후 - cancelableList에서 삭제
        cancelableList.remove(cancelIdx);
        // 취소된 후 사용자의 예약목록 출력
        printCancelList();

        String printMessage = "취소되었습니다.";
        if(today) printMessage += " 당일 이용이 불가합니다.";
        System.out.println(printMessage + " 최소예약인원수 부적합으로 전체 예약이 삭제됩니다. 5초 후 메인 화면으로 돌아갑니다.");
    }

    /** 예약 취소 함수 */
    public void cancelReservation(){
        System.out.println("\n[ 건물, 호실, 사용할 날짜, 예약 시작 시간, 이용시간, (학번들) ]");
        loadAndPrintCancelList();

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

            int cancelIdx = toInt(cancelNum) - 1;  // 인덱스
            cancelDate = getDateByIndexFromCancelableList(cancelIdx);

            // cancelableList로부터 cancelDate에 해당하는 Reservation 추출
            Reservation cancelReservation = cancelableList.get(cancelIdx).get(cancelDate);

            finalCancleList = sharedData.reservationList.get(cancelDate); // 취소할 날짜의 모든 예약 목록 백업

            List<String> pIDs = cancelReservation.userIds; // 취소할 날짜의 예약자 학번들
            int maxNum = getMaxPeople(cancelReservation.name, cancelReservation.room);

//            // 예약자 본인일 경우 - 전체 예약 취소
//            if(ID.equals(pIDs.get(0)))
//                allCancel(cancelNum, pIDs);
//            else { // 동반 예약자일 경우
                // 사용자가 예약에서 빠져도 해당 예약의 인원수 제한조건이 충족될 때 - 개인 예약 취소
                if(toInt(cancelReservation.numOfPeople) > getMinPeople(maxNum)){
                    personalCancel(cancelNum, pIDs);
                }
                else{ // 인원수 제한조건이 충족되지 않는다면 - 전체 예약 취소
                    allCancel(cancelNum, pIDs);
                }
//            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadAndPrintCancelList() {
        cancelableList = new ArrayList<>();
        int reserveListNum = 1; // 예약 목록 번호
        for(String date : dates){
            List<Reservation> reserveList = sharedData.reservationList.get(new Date(date));
            if(!reserveList.isEmpty()){
                for(Reservation res : reserveList){
                    if(res.userIds.contains(ID)){
                        System.out.print(reserveListNum +". "+res.name+", "+res.room+"호실, "
                                + date + ", "+res.startTime+"시, "+res.useTime+"h, ");
                        printIDs(res.userIds);
                        cancelableList.add(new HashMap<>(){{put(new Date(date), res);}});
                        reserveListNum++;
                    }
                }
            }
        }
    }

    // cancelIdx 번째 Map<Date, Reservation>으로부터 키(Date) 추출
    private Date getDateByIndexFromCancelableList(int cancelIdx) {
        Date cancelDate;
        cancelDate = cancelableList.get(cancelIdx).keySet()
                    .stream().findFirst().orElse(null);
        return cancelDate;
    }

    public void printIDs(List<String> pIDs){
        System.out.println(
                pIDs.stream()
                .collect(Collectors.joining(", ", "(", ")")));
    }

    private void printCancelList() {
        if(cancelableList.isEmpty()){
            System.out.println("\n예약목록이 없습니다.");
            return;
        }
        for(int i = 0; i< cancelableList.size(); i++){
            Date printDate = getDateByIndexFromCancelableList(i);
            Reservation printRes = cancelableList.get(i).get(printDate);
            System.out.print((i+1)+". "+ printRes.name+", "+ printRes.room+"호실, "
                    + printDate.date+", "+ printRes.startTime+"시, "+ printRes.useTime+"h, ");
            printIDs(printRes.userIds);
        }
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
