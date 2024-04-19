package com.example.utils;

import com.example.SharedData;
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

    public static boolean validateUserId(String userId) {
        if(userId.length() != 9) {
            log.error("학번은 9자리여야 합니다.");
            return false;
        }

        try {
            Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            log.error("학번은 숫자로 이루어져야 합니다.");
            return false;
        }

        int year = Integer.parseInt(userId.substring(0,4));
        int month = sharedData.currentTime.getMonth();
        int diff = sharedData.currentTime.getYear() - year;

        if(diff > 15) {
            log.error("15년 이내 입학 학생들만 이용 가능합니다.");
            return false;
        }
        if(diff == 0 && month < 3) {
            log.error("당해년도 입학생은 3월 이후 사용 가능합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateUseTime(String useTime) {
        try {
            int time = Integer.parseInt(useTime);
            if(time < 1 || time > 3) {
                log.error("이용시간은 1시간 이상 3시간 이하여야 합니다.");
                return false;
            }
        } catch (NumberFormatException e) {
            log.error("이용 시간은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateDate(String date) {
        if(date == null){
            log.error("date is null");
            return false;
        }

        if(date.length() != 8) {
            log.error("날짜는 8자리여야 합니다.");
            return false;
        }

        try {
            // 날짜 형식을 지정
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            // 입력된 문자열을 LocalDate 객체로 파싱
            LocalDate localDate = LocalDate.parse(date, formatter);

            // 1900 이상, 2999 이하
            int year = localDate.getYear();
            if(year < 1900 || year > 2999) {
                log.error("날짜는 1900년 이상 2999년 이하여야 합니다.");
                return false;
            }

        } catch (DateTimeException e) {
            log.error("날짜형식은 yyyyMMdd여야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateTime(String time) {
        if(time == null){
            log.error("time is null");
            return false;
        }

        try {
            int hour = Integer.parseInt(time.substring(0, 2));
            int minute= Integer.parseInt(time.substring(2, 4));
            if(hour < 0 || hour>23 || minute < 0 || minute > 59) {
                log.error("00시 ~ 23시, 00분 ~ 59분 만 입력 가능합니다.");
                return false;
            }

        } catch (NumberFormatException e) {
            log.error("날짜는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateBuildingNum(String buildingNum, int totBuildingNum){
        if(buildingNum==null){
            log.error("buildingName is null");
            return false;
        }

        try {
            int intBuildingNum=Integer.parseInt(buildingNum);
            if(intBuildingNum >totBuildingNum || intBuildingNum <= 0){
                log.error("건물 번호가 올바르지 않습니다.");
                return false;
            }
        } catch(NumberFormatException e){
            log.error("건물 번호는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateReservationDate(String reservationDate,int max){
        if(reservationDate==null){
            log.error("reservationDate is null");
            return false;
        }
        
        try{
            int intReservationDate= Integer.parseInt(reservationDate);
            if(intReservationDate > max || intReservationDate < 0){
                log.error("예약하실 날짜가 올바르지 않습니다.");
                return false;
            }
            selectedReservationDate=intReservationDate;
        }catch(NumberFormatException e){
            log.error("예약하실 날짜는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateReservationRoomNum(String reservationRoomNum,int maxRoomNumInBuilding){
        if(reservationRoomNum==null){
            log.error("reservationRoomNum is null");
            return false;
        }

        try{
            int intReservationRoomNum= Integer.parseInt(reservationRoomNum);
            if(intReservationRoomNum > maxRoomNumInBuilding || intReservationRoomNum<=0){
                log.error("예약하실 호실이 올바르지 않습니다.");
                return false;
            }
            selectedRoomNum=intReservationRoomNum;
        }catch(NumberFormatException e) {
            log.error("예약하실 호실은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateSelfExcludedTotMemberNumber(String selfExcludedTotMemberNumber,int maxRoomCapacity){
        if(selfExcludedTotMemberNumber==null){
            log.error("selfExcludedTotMemberNumber is null");
            return false;
        }

        try{
            int intSelfExcludedTotMemberNumber = Integer.parseInt(selfExcludedTotMemberNumber);
            if(intSelfExcludedTotMemberNumber > maxRoomCapacity || intSelfExcludedTotMemberNumber <=0){
                log.error("입력하신 인원 수가 올바르지 않습니다.");
                return false;
            }
        }catch(NumberFormatException e){
            log.error("입력하신 인원 수는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateCancelNum(String cancelNum,int maxCancelNum){
        if(cancelNum==null){
            log.error("cancelNum is null");
            return false;
        }

        try{
            int intCancelNum = Integer.parseInt(cancelNum);
            if(intCancelNum > maxCancelNum || intCancelNum <=0){
                log.error("입력하신 취소 번호가 올바르지 않습니다.");
                return false;
            }
            selectedCancelNum=intCancelNum;
        }catch(NumberFormatException e){
            log.error("입력하신 취소 번호는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateSameDayCanceling(String input){
        if(input.equals("Y")||input.equals("N")){
            return true;
        }
        log.error("Y 또는 N을 입력해주세요");
        return false;
    }

    public static boolean validateReservationStartTime(String reservationStartTime){
        if(reservationStartTime==null){
            log.error("reservationStartTime is null");
            return false;
        }

        try{
            int intReservationStartTime = Integer.parseInt(reservationStartTime);
            if(intReservationStartTime < 9 || intReservationStartTime > 21){
                log.error("입력하신 예약시작 시간이 올바르지 않습니다.");
                return false;
            }
        }catch(NumberFormatException e){
            log.error("예약시작 시간은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateReservationUseTime(String reservationUseTime){
        if(reservationUseTime==null){
            log.error("reservationUseTime is null");
            return false;
        }

        try{
            int intReservationUseTime = Integer.parseInt(reservationUseTime);
            if(intReservationUseTime < 0 || intReservationUseTime > 4){
                log.error("입력하신 이용 시간이 올바르지 않습니다.");
                return false;
            }
        }catch(NumberFormatException e){
            log.error("이용시간은 숫자로 이루어져야 합니다.");
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
