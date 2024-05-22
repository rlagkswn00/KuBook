package com.example.utils;

import com.example.SharedData;
import com.example.model.Date;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Validation {
    public static SharedData sharedData = SharedData.getInstance();

    public static int selectedRoomNum;
    public static int selectedCancelNum;
    public static int selectedReservationDate;

    public static boolean isTheSameDay = false;

    public static boolean validateUserId(String userId) {
        if(userId.length() != 9) {
            printErrorMessage("학번은 9자리여야 합니다.");
            return false;
        }

        try {
            Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            printErrorMessage("학번은 숫자로 이루어져야 합니다.");
            return false;
        }

        int year = Integer.parseInt(userId.substring(0,4));
        int month = sharedData.currentTime.getMonth();
        int diff = sharedData.currentTime.getYear() - year;

        Date currentTime = sharedData.getCurrentTime();

        String date = currentTime.date;
        int dateYear = Integer.parseInt(date.substring(0, 4));
        int idYear = Integer.parseInt(userId.substring(0, 4));
        if (dateYear < idYear) {
            printErrorMessage("아직 입학하지 않은 학번은 입력할 수 없습니다.");
            return false;
        }
        if(diff > 15) {
            printErrorMessage("15년 이내 입학 학생들만 이용 가능합니다.");
            return false;
        }
        if(diff == 0 && month < 3) {
            printErrorMessage("당해년도 입학생은 3월 이후 사용 가능합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateUseTime(String useTime) {
        if (!useTime.matches("\\d+")) {
            printErrorMessage("이용시간은 숫자로만 이루어져야 합니다.");
            return false;
        }
        
        try {
            int time = Integer.parseInt(useTime);
            if(time < 1) {
                printErrorMessage("이용시간은 1시간 이상이여야 합니다.");
                return false;
            }
        } catch (NumberFormatException e) {
            printErrorMessage("이용 시간은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static void printErrorMessage(String line) {
        System.out.println(line);
        System.out.println();
    }
    public static boolean validateDate(String date) {
        if(date == null){
            printErrorMessage("date is null");
            return false;
        }

        if(date.length() != 8) {
            printErrorMessage("날짜는 8자리여야 합니다.");
            return false;
        }

        try {
            int day=Integer.parseInt(date.substring(6,8));
            // 날짜 형식을 지정
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            // 입력된 문자열을 LocalDate 객체로 파싱
            LocalDate localDate = LocalDate.parse(date, formatter);

            boolean isTheSameYear=false;

            // 1900 이상, 2999 이하
            int year = localDate.getYear();
            if(year < 1900 || year > 2999) {
                printErrorMessage("날짜는 1900년 이상 2999년 이하여야 합니다.");
                return false;
            }
            //윤일인데, 윤일이 없는 해인지 체크
            String monthStr = date.substring(4,6);
            String dayStr = date.substring(6);
            if (monthStr.equals("02") && dayStr.equals("29")) {
                if (!isLeapYear(year)) {
                    printErrorMessage("윤일이 없는 해입니다.");
                    return false;
                }
            }

            if(year < sharedData.currentTime.getYear()){
                printErrorMessage("현재 날짜보다 예약할 날짜가 이후여야 합니다");
                return false;
            }else if(year==sharedData.currentTime.getYear()) isTheSameYear=true;


            int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

            if (localDate.isLeapYear()) {
                daysInMonth[1] = 29;
            }

            int month=Integer.parseInt(localDate.toString().substring(5,7));

            if (day < 1 || day > daysInMonth[month - 1]) {
                printErrorMessage("없는 날짜를 입력하셨습니다");
                return false;
            }
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("MMdd");
            String monthDayString = localDate.format(formatter1);

            isTheSameDay=false;

            if(isTheSameYear==true && Integer.parseInt(monthDayString) < sharedData.currentTime.getMontToDay()) {
                printErrorMessage("현재 날짜보다 예약할 날짜가 이후여야 합니다");
                return false;
            }else if(Integer.parseInt(monthDayString)==sharedData.currentTime.getMontToDay()){
                isTheSameDay=true;
            }
        } catch (DateTimeException e) {
            printErrorMessage("날짜형식은 yyyyMMdd여야 합니다.");
            return false;
        }
        return true;
    }
    private static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    public static boolean validateTime(String time) {
        if(time == null){
            printErrorMessage("time is null");
            return false;
        }

        if(time.length()!=4){
            printErrorMessage("시간은 4자리로 적어주세요");
            return false;
        }

        try {
            int hour = Integer.parseInt(time.substring(0, 2));
            int minute= Integer.parseInt(time.substring(2, 4));
            if(hour < 0 || hour>23 || minute < 0 || minute > 59) {
                printErrorMessage("00시 ~ 23시, 00분 ~ 59분 만 입력 가능합니다.");
                return false;
            }

            if(isTheSameDay && (Integer.parseInt(sharedData.currentTime.time) > Integer.parseInt(time))){
                printErrorMessage("현재 시간보다 예약할 시간이 이후여야 합니다");
                return false;
            }

        } catch (NumberFormatException e) {
            printErrorMessage("날짜는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateBuildingNum(String buildingNum, int totBuildingNum){
        if(buildingNum==null){
            printErrorMessage("buildingName is null");
            return false;
        }

        if (!buildingNum.matches("\\d+")) {
            printErrorMessage("건물 번호는 숫자로만 이루어져야 합니다.");
            return false;
        }

        try {
            int intBuildingNum=Integer.parseInt(buildingNum);
            if(intBuildingNum >totBuildingNum || intBuildingNum <= 0){
                printErrorMessage("건물 번호가 올바르지 않습니다.");
                return false;
            }
        } catch(NumberFormatException e){
            printErrorMessage("건물 번호는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateReservationDate(String reservationDate,int max){
        if(reservationDate==null){
            printErrorMessage("reservationDate is null");
            return false;
        }

        if (!reservationDate.matches("\\d+")) {
            printErrorMessage("예약하실 날짜 번호는 숫자로만 이루어져야 합니다.");
            return false;
        }

        try{
            int intReservationDate= Integer.parseInt(reservationDate);
            if(intReservationDate > max || intReservationDate < 0){
                printErrorMessage("예약하실 날짜가 올바르지 않습니다.");
                return false;
            }
            selectedReservationDate=intReservationDate;
        }catch(NumberFormatException e){
            printErrorMessage("예약하실 날짜는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateReservationRoomNum(String reservationRoomNum,int maxRoomNumInBuilding){
        if(reservationRoomNum==null){
            printErrorMessage("reservationRoomNum is null");
            return false;
        }

        if (!reservationRoomNum.matches("\\d+")) {
            printErrorMessage("예약하실 호실은 숫자로만 이루어져야 합니다.");
            return false;
        }

        try{
            int intReservationRoomNum= Integer.parseInt(reservationRoomNum);
            if(intReservationRoomNum > maxRoomNumInBuilding || intReservationRoomNum<=0){
                /* 호실 존재 여부, 호실 개수 범위 예외처리 */
                printErrorMessage("예약하실 호실이 올바르지 않습니다.");
                return false;
            }
            selectedRoomNum=intReservationRoomNum;
        }catch(NumberFormatException e) {
            printErrorMessage("예약하실 호실은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateSelfExcludedTotMemberNumber(String selfExcludedTotMemberNumber,int maxRoomCapacity){
        if(selfExcludedTotMemberNumber==null){
            printErrorMessage("selfExcludedTotMemberNumber is null");
            return false;
        }

        if (!selfExcludedTotMemberNumber.matches("\\d+")) {
            printErrorMessage("본인을 제외한 전체 예약 인원 수는 숫자로만 이루어져야 합니다.");
            return false;
        }
        /*최소 인원수 만족 예외처리*/
        try{
            int intSelfExcludedTotMemberNumber = Integer.parseInt(selfExcludedTotMemberNumber);
            if(intSelfExcludedTotMemberNumber > maxRoomCapacity || intSelfExcludedTotMemberNumber <=0){
                printErrorMessage("입력하신 인원 수가 올바르지 않습니다.");
                return false;
            }
        }catch(NumberFormatException e){
            printErrorMessage("입력하신 인원 수는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateCancelNum(String cancelNum,int maxCancelNum){
        if(cancelNum==null){
            printErrorMessage("cancelNum is null");
            return false;
        }

        if (!cancelNum.matches("\\d+")) {
            printErrorMessage("취소 번호는 숫자로만 이루어져야 합니다.");
            return false;
        }

        try{
            int intCancelNum = Integer.parseInt(cancelNum);
            if(intCancelNum > maxCancelNum || intCancelNum <=0){
                printErrorMessage("입력하신 취소 번호가 올바르지 않습니다.");
                return false;
            }
            selectedCancelNum=intCancelNum;
        }catch(NumberFormatException e){
            printErrorMessage("입력하신 취소 번호는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateSameDayCanceling(String input){
        if(input.equals("Y")||input.equals("N")){
            return true;
        }
        printErrorMessage("Y 또는 N을 입력해주세요");
        return false;
    }

    public static boolean validateReservationStartTime(String reservedate, String reservationStartTime){
        if(reservationStartTime == null || reservedate == null){
            printErrorMessage("prarameter is null");
            return false;
        }
        
        if (!reservationStartTime.matches("\\d+")) {
            printErrorMessage("예약시작 시간은 숫자로만 이루어져야 합니다.");
            return false;
        }
        
        try{
            int intReservationStartTime = Integer.parseInt(reservationStartTime);
            if(intReservationStartTime < 9 || intReservationStartTime > 21){
                printErrorMessage("입력하신 예약시작 시간이 올바르지 않습니다.");
                return false;
            }
            boolean isCurDate = Integer.parseInt(reservedate) == 0;
            if(isCurDate){
                if(sharedData.currentTime.getHour() > intReservationStartTime) {
                    printErrorMessage("현재시간 이전의 시간은 예약이 불가합니다.");
                    return false;
                }else if(sharedData.currentTime.getHour() == intReservationStartTime
                && sharedData.currentTime.getMinute() !=0){
                    printErrorMessage("현재시간 이전의 시간은 예약이 불가합니다.");
                    return false;
                }
            }
        }catch(NumberFormatException e){
            printErrorMessage("예약시작 시간은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateReservationUseTime(String reservationUseTime){
        if(reservationUseTime==null){
            printErrorMessage("reservationUseTime is null");
            return false;
        }

        if (!reservationUseTime.matches("\\d+")) {
            printErrorMessage("이용 시간은 숫자로만 이루어져야 합니다.");
            return false;
        }
        /*사용불가 예외처리*/
        try{
            int intReservationUseTime = Integer.parseInt(reservationUseTime);
            if(intReservationUseTime <= 0 || intReservationUseTime >= 4){
                printErrorMessage("입력하신 이용 시간이 올바르지 않습니다.");
                return false;
            }
        }catch(NumberFormatException e){
            printErrorMessage("이용시간은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean existFile(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
            return false;
        }
        return true;
    }

    public static boolean existDir(File file) {
        if (!file.exists()) {
            file.mkdir();
            return false;
        }
        return true;
    }
}
