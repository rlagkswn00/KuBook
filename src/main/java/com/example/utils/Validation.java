package com.example.utils;

import com.example.SharedData;
import com.example.model.Date;
import com.example.model.KLog;
import com.example.model.PenaltyUser;
import com.example.model.DisableKcube;
import com.example.model.Kcube;
import com.example.model.Reservation;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class Validation {
    public static SharedData sharedData = SharedData.getInstance();
    public static boolean isTheSameDay = false;
    public static final Integer MAX_RESERVABLE_DATES = 8;

    public static boolean validateUserId(String userId) {
        if (userId.length() != 9) {
            printErrorMessage("학번은 9자리여야 합니다.");
            return false;
        }

        try {
            Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            printErrorMessage("학번은 숫자로 이루어져야 합니다.");
            return false;
        }

        int year = Integer.parseInt(userId.substring(0, 4));
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
        if (diff > 15) {
            printErrorMessage("15년 이내 입학 학생들만 이용 가능합니다.");
            return false;
        }
        if (diff == 0 && month < 3) {
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
            if (time < 1) {
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
        if (date == null) {
            printErrorMessage("date is null");
            return false;
        }

        if (date.length() != 8) {
            printErrorMessage("날짜는 8자리여야 합니다.");
            return false;
        }

        try {
            int day = Integer.parseInt(date.substring(6, 8));
            // 날짜 형식을 지정
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            // 입력된 문자열을 LocalDate 객체로 파싱
            LocalDate localDate = LocalDate.parse(date, formatter);

            boolean isTheSameYear = false;

            // 1900 이상, 2999 이하
            int year = localDate.getYear();
            if (year < 1900 || year > 2999) {
                printErrorMessage("날짜는 1900년 이상 2999년 이하여야 합니다.");
                return false;
            }
            //윤일인데, 윤일이 없는 해인지 체크
            String monthStr = date.substring(4, 6);
            String dayStr = date.substring(6);
            if (monthStr.equals("02") && dayStr.equals("29")) {
                if (!isLeapYear(year)) {
                    printErrorMessage("윤일이 없는 해입니다.");
                    return false;
                }
            }

            if (year < sharedData.currentTime.getYear()) {
                printErrorMessage("현재 날짜보다 예약할 날짜가 이후여야 합니다");
                return false;
            } else if (year == sharedData.currentTime.getYear()) isTheSameYear = true;


            int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

            if (localDate.isLeapYear()) {
                daysInMonth[1] = 29;
            }

            int month = Integer.parseInt(localDate.toString().substring(5, 7));

            if (day < 1 || day > daysInMonth[month - 1]) {
                printErrorMessage("없는 날짜를 입력하셨습니다");
                return false;
            }
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("MMdd");
            String monthDayString = localDate.format(formatter1);

            isTheSameDay = false;

            if (isTheSameYear == true && Integer.parseInt(monthDayString) < sharedData.currentTime.getMontToDay()) {
                printErrorMessage("현재 날짜보다 예약할 날짜가 이후여야 합니다");
                return false;
            } else if (Integer.parseInt(monthDayString) == sharedData.currentTime.getMontToDay()) {
                isTheSameDay = true;
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
        if (time == null) {
            printErrorMessage("time is null");
            return false;
        }

        if (time.length() != 4) {
            printErrorMessage("시간은 4자리로 적어주세요");
            return false;
        }

        try {
            int hour = Integer.parseInt(time.substring(0, 2));
            int minute = Integer.parseInt(time.substring(2, 4));
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                printErrorMessage("00시 ~ 23시, 00분 ~ 59분 만 입력 가능합니다.");
                return false;
            }

            if (isTheSameDay && (Integer.parseInt(sharedData.currentTime.time) > Integer.parseInt(time))) {
                printErrorMessage("현재 시간보다 예약할 시간이 이후여야 합니다");
                return false;
            }

        } catch (NumberFormatException e) {
            printErrorMessage("날짜는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateBuildingNum(String buildingNum, int totBuildingNum) {
        if (buildingNum == null) {
            printErrorMessage("buildingName is null");
            return false;
        }

        if (!buildingNum.matches("\\d+")) {
            printErrorMessage("건물 번호는 숫자로만 이루어져야 합니다.");
            return false;
        }

        try {
            int intBuildingNum = Integer.parseInt(buildingNum);
            if (intBuildingNum > totBuildingNum || intBuildingNum <= 0) {
                printErrorMessage("건물 번호가 올바르지 않습니다.");
                return false;
            }
        } catch (NumberFormatException e) {
            printErrorMessage("건물 번호는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    // userId의 date 패널티 여부 반환
    public static boolean isPenaltyUser(Date date, String userId) {
        List<PenaltyUser> penaltyUsers = sharedData.penalizedUsers.get(date);
        for (PenaltyUser penaltyUser : penaltyUsers) {
            if (penaltyUser.userId.equals(userId))
                return true;
        }
        return false;
    }

    public static boolean isSameDate(Date targetDate, String today) {
        return targetDate.date.equals(today);
    }

    public static boolean checkNHoursUsage(Date targetDate, String userId, int N) {
        List<KLog> kLogs = sharedData.logs.get(targetDate);
        for (KLog kLog : kLogs) {
            if (kLog.userId.equals(userId) && Integer.parseInt(kLog.useTime) >= N) {
                return true;
            }
        }
        return false;
    }

    public static boolean validateChosenDateIndex(String chosenDateIndex) {
        if (chosenDateIndex == null) {
            printErrorMessage("선택한 예약 날짜가 null 입니다.");
            return false;
        }

        if (!chosenDateIndex.matches("\\d+")) {
            printErrorMessage("예약하실 날짜 번호는 숫자로만 이루어져야 합니다.");
            return false;
        }

        try {
            int intChosenDateIndex = Integer.parseInt(chosenDateIndex);
            if (intChosenDateIndex < 0 || intChosenDateIndex > MAX_RESERVABLE_DATES) {
                printErrorMessage("예약하실 날짜가 올바르지 않습니다.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            printErrorMessage("예약하실 날짜 번호는 숫자로만 이루어져야 합니다.");
            return false;
        }
    }

    public static boolean validateReservationDate(String reservationDate, int max) {
        if (reservationDate == null) {
            printErrorMessage("reservationDate is null");
        }
        if (!reservationDate.matches("\\d+")) {
            printErrorMessage("예약하실 날짜 번호는 숫자로만 이루어져야 합니다.");
            return false;
        }

        try {
            int intReservationDate = Integer.parseInt(reservationDate);
            if (intReservationDate > max || intReservationDate < 0) {
                printErrorMessage("예약하실 날짜가 올바르지 않습니다.");
                return false;
            }
        } catch (NumberFormatException e) {
            printErrorMessage("예약하실 날짜는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateReservationRoomNum(String reservationRoomNum, int maxRoomNumInBuilding) {
        if (reservationRoomNum == null) {
            printErrorMessage("reservationRoomNum is null");
            return false;
        }

        if (!reservationRoomNum.matches("\\d+")) {
            printErrorMessage("예약하실 호실은 숫자로만 이루어져야 합니다.");
            return false;
        }

        try {
            int intReservationRoomNum = Integer.parseInt(reservationRoomNum);
            if (intReservationRoomNum > maxRoomNumInBuilding || intReservationRoomNum <= 0) {
                /* 호실 존재 여부, 호실 개수 범위 예외처리 */
                printErrorMessage("예약하실 호실이 올바르지 않습니다.");
                return false;
            }
        } catch (NumberFormatException e) {
            printErrorMessage("예약하실 호실은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }


    public static boolean validateDisableRoomNum(String disableRoomNum, int maxRoomNumInBuilding) {
        if (disableRoomNum == null) {
            printErrorMessage("disableRoom is null");
            return false;
        }

        if (!disableRoomNum.matches("\\d+")) {
            printErrorMessage("사용불가할 호실은 숫자로만 이루어져야 합니다.");
            return false;
        }

        try {
            int intDisableRoomNum = Integer.parseInt(disableRoomNum);
            if (intDisableRoomNum > maxRoomNumInBuilding || intDisableRoomNum <= 0) {
                /* 호실 존재 여부, 호실 개수 범위 예외처리 */
                printErrorMessage("사용불가할 호실이 올바르지 않습니다.");
                return false;
            }
        } catch (NumberFormatException e) {
            printErrorMessage("사용불가할 호실은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateAddBuildingRoomNum(String addBuildingRoomNum, int maxRoomNumInBuilding) {
        if (addBuildingRoomNum == null) {
            printErrorMessage("addBuildingRoomNum is null");
            return false;
        }

        if (!addBuildingRoomNum.matches("\\d+")) {
            printErrorMessage("추가할 호실 수는 숫자로만 이루어져야 합니다.");
            return false;
        }

        try {
            int intAddBuildingRoomNum = Integer.parseInt(addBuildingRoomNum);
            if (intAddBuildingRoomNum > maxRoomNumInBuilding || intAddBuildingRoomNum <= 0) {
                /* 호실 존재 여부, 호실 개수 범위 예외처리 */
                printErrorMessage("추가할 호실 숫자가 올바르지 않습니다.");
                return false;
            }
        } catch (NumberFormatException e) {
            printErrorMessage("추가할 호실 수는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateMax(String max) {
        if (max == null) {
            printErrorMessage("max is null");
            return false;
        }

        if (!max.matches("\\d+")) {
            printErrorMessage("최대 인원수는 숫자로만 이루어져야 합니다.");
            return false;
        }

        try {
            int intAddBuildingMax = Integer.parseInt(max);
            if (intAddBuildingMax > 20 || intAddBuildingMax < 2) {
                /* 호실 존재 여부, 호실 개수 범위 예외처리 */
                printErrorMessage("최대 인원수가 올바르지 않습니다.");
                return false;
            }
        } catch (NumberFormatException e) {
            printErrorMessage("최대 인원수는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateFixMax(String buildingName, String roomToFix, String maxToFix, String max) {
        if (!max.matches("\\d+")) {
            System.out.print("최대 인원수는 숫자로만 이루어져야 합니다. 다시 입력해주세요 (2~20) :  ");
            return false;
        }

        try {
            int intMaxToFix = Integer.parseInt(maxToFix);
            int intMax = Integer.parseInt(max);
            if (intMax > 20 || intMax < 2) {
                /* 호실 존재 여부, 호실 개수 범위 예외처리 */
                System.out.print("최대 인원수가 올바르지 않습니다. 다시 입력해주세요 (2~20) :  ");
                return false;
            }
            if (intMax == intMaxToFix) {
                System.out.print("오류! 현재 설정되어 있는 인원수입니다. 다시 입력해주세요 (2~20) :");
                return false;
            }
            Map<Date, List<Reservation>> reservationList = sharedData.reservationList;
            Set<Date> dates = reservationList.keySet();
            List<Reservation> allReservations = new ArrayList<>();
            for (Date date : dates) {
                List<Reservation> reservations = reservationList.get(date);
                allReservations.addAll(reservations);
            }
            boolean isOverFlowPeople = allReservations.stream()
                    .anyMatch(reservation -> reservation.getRoom().equals(roomToFix)
                            && reservation.getName().equals(buildingName)
                            && (Integer.parseInt(reservation.getNumOfPeople()) > intMax));

            if (isOverFlowPeople) {
                System.out.print("오류! 입력한 최대 인원수를 넘어가는 예약 목록이 존재합니다. 다시 입력해주세요. (2~20)  :");
                return false;
            }
        } catch (NumberFormatException e) {
            printErrorMessage("최대 인원수는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateAddRoom(String buildingName, String roomToAdd) {
        try {
            int intRoomToAdd = Integer.parseInt(roomToAdd);
            if (!roomToAdd.matches("\\d+")) {
                throw new NumberFormatException();
            }
            if (intRoomToAdd > 9 || intRoomToAdd < 2) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.print("오류! 1~9사이만 입력가능합니다. 다시 입력해주세요. (ex. 1) : ");
            return false;
        }
        boolean isDuplicateRoom = sharedData.kcubes.stream()
                .filter(kcube -> kcube.getName().equals(buildingName))
                .map(Kcube::getRoom)
                .anyMatch(room -> room.equals(roomToAdd));

        if (isDuplicateRoom) {
            System.out.println("오류! 이미 존재하는 호실입니다. 다시 입력해주세요 (ex. 1) :");
            return false;
        }
        return true;
    }

    public static boolean validateDeleteRoom(String buildingName, String roomToDelete) {
        try {
            int intRoomToDelete = Integer.parseInt(roomToDelete);
            if (!roomToDelete.matches("\\d+")) {
                throw new NumberFormatException();
            }
            if (intRoomToDelete > 9 || intRoomToDelete < 1) {
                throw new NumberFormatException();
            }
            boolean isContainRoomNum = sharedData.kcubes.stream()
                    .filter(kcube -> kcube.getName().equals(buildingName))
                    .map(Kcube::getRoom)
                    .collect(Collectors.toList())
                    .contains(roomToDelete);
            if (!isContainRoomNum) {
                System.out.print("오류! 존재하지 않는 호실입니다. 다시 선택해주세요 (ex. 1) : ");
                return false;
            }
            Map<Date, List<Reservation>> reservationList = sharedData.reservationList;
            Set<Date> dates = reservationList.keySet();
            List<Reservation> allReservations = new ArrayList<>();
            for (Date date : dates) {
                List<Reservation> reservations = reservationList.get(date);
                allReservations.addAll(reservations);
            }
            boolean isReserved = allReservations.stream()
                    .anyMatch(reservation -> reservation.getRoom().equals(roomToDelete) && reservation.getName().equals(buildingName));
            if (isReserved) {
                System.out.print("오류! 해당 호실은 예약 목록이 존재합니다. 다시 선택해주세요 (ex. 1) : ");
                return false;
            }

        } catch (NumberFormatException e) {
            System.out.print("오류! 1~9사이만 입력가능합니다. 다시 입력해주세요. (ex. 1) : ");
            return false;
        }
        return true;
    }


    public static boolean validateRoomToFix(String buildingName, String roomToFix) {
        try {
            int intRoomToFix = Integer.parseInt(roomToFix);
            if (!roomToFix.matches("\\d+")) {
                throw new NumberFormatException();
            }
            if (intRoomToFix > 9 || intRoomToFix < 1) {
                throw new NumberFormatException();
            }
            boolean isContainRoomNum = sharedData.kcubes.stream()
                    .filter(kcube -> kcube.getName().equals(buildingName))
                    .map(Kcube::getRoom)
                    .collect(Collectors.toList())
                    .contains(roomToFix);
            if (!isContainRoomNum) {
                System.out.print("오류! 존재하지 않는 호실입니다. 다시 선택해주세요 (ex. 1) : ");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.print("오류! 1~9사이만 입력가능합니다. 다시 입력해주세요. (ex. 1) : ");
            return false;
        }
        return true;
    }

    public static boolean validateSelfExcludedTotMemberNumber(String selfExcludedTotMemberNumber, int maxRoomCapacity) {
        if (selfExcludedTotMemberNumber == null) {
            printErrorMessage("selfExcludedTotMemberNumber is null");
            return false;
        }

        if (!selfExcludedTotMemberNumber.matches("\\d+")) {
            printErrorMessage("본인을 제외한 전체 예약 인원 수는 숫자로만 이루어져야 합니다.");
            return false;
        }
        /*최소 인원수 만족 예외처리*/
        try {
            int intSelfExcludedTotMemberNumber = Integer.parseInt(selfExcludedTotMemberNumber);
            if (intSelfExcludedTotMemberNumber > maxRoomCapacity || intSelfExcludedTotMemberNumber <= 0) {
                printErrorMessage("입력하신 인원 수가 올바르지 않습니다.");
                return false;
            }
        } catch (NumberFormatException e) {
            printErrorMessage("입력하신 인원 수는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateReservationSize(String nmates, int maxPeople, int minPeople) {
        if (nmates == null) {
            printErrorMessage("nmates is null");
            return false;
        }

        if (!nmates.matches("\\d+")) {
            printErrorMessage("입력하신 인원수는 숫자로 이루어져야 합니다.");
            return false;
        }
        /*최소 인원수 만족 예외처리*/
        try {
            int intNmates = Integer.parseInt(nmates);
            if (intNmates >= maxPeople || intNmates < minPeople - 1) {
                printErrorMessage("입력하신 인원수가 올바르지 않습니다.");
                return false;
            }
        } catch (NumberFormatException e) {
            printErrorMessage("입력하신 인원수는 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;

    }

    public static boolean validateCancelNum(String cancelNum, int maxCancelNum) {
        if (cancelNum == null) {
            printErrorMessage("cancelNum is null");
            return false;
        }

        if (!cancelNum.matches("\\d+")) {
            printErrorMessage("취소 번호는 숫자로만 이루어져야 합니다.");
            return false;
        }

        try {
            int intCancelNum = Integer.parseInt(cancelNum);
            if (intCancelNum > maxCancelNum || intCancelNum <= 0) {
                printErrorMessage("입력하신 취소 번호가 올바르지 않습니다.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            printErrorMessage("입력하신 취소 번호는 숫자로 이루어져야 합니다.");
            return false;
        }
    }

    public static boolean validateSameDayCanceling(String input) {
        if (input.equals("Y") || input.equals("N")) {
            return true;
        }
        printErrorMessage("Y 또는 N을 입력해주세요");
        return false;
    }

    public static boolean validateReservationStartTime(String reserveDate, String reservationStartTime) {
        if (reservationStartTime == null || reserveDate == null) {
            printErrorMessage("pararameter is null");
            return false;
        }

        if (!reservationStartTime.matches("\\d+")) {
            printErrorMessage("예약시작 시간은 숫자로만 이루어져야 합니다.");
            return false;
        }

        try {
            int intReservationStartTime = Integer.parseInt(reservationStartTime);
            if (intReservationStartTime < 9 || intReservationStartTime > 21) {
                printErrorMessage("입력하신 예약시작 시간이 올바르지 않습니다.");
                return false;
            }
            boolean isCurDate = Integer.parseInt(reserveDate) == 0;
            if (isCurDate) {
                if (sharedData.currentTime.getHour() > intReservationStartTime) {

                    printErrorMessage("현재시간 이전의 시간은 예약이 불가합니다.");
                    return false;
                } else if (sharedData.currentTime.getHour() == intReservationStartTime
                        && sharedData.currentTime.getMinute() != 0) {
                    printErrorMessage("현재시간 이전의 시간은 예약이 불가합니다.");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            printErrorMessage("예약시작 시간은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }


    public static boolean validateDisableStartTime(Kcube kcube, String disableDate, String disableStartTime) {
        if (disableStartTime == null || disableDate == null) {
            printErrorMessage("prarameter is null");
            return false;
        }

        if (!disableStartTime.matches("\\d+")) {
            printErrorMessage("사용불가할 시간은 숫자로만 이루어져야 합니다.");
            return false;
        }

        try {
            int intDisableStartTime = Integer.parseInt(disableStartTime);
            if (intDisableStartTime < 9 || intDisableStartTime > 21) {
                printErrorMessage("입력하신 사용불가 시작 시간이 올바르지 않습니다.");
                return false;
            }
            boolean isCurDate = Integer.parseInt(disableDate) == 0;
            if (isCurDate) {
                if (sharedData.currentTime.getHour() > intDisableStartTime) {
                    printErrorMessage("현재시간 이전의 시간은 사용불가가 불가합니다.");
                    return false;
                } else if (sharedData.currentTime.getHour() == intDisableStartTime
                        && sharedData.currentTime.getMinute() != 0) {
                    printErrorMessage("현재시간 이전의 시간은 사용불가가 불가합니다.");
                    return false;
                }
            }
            boolean isAvailable = isRoomAvailable(kcube, disableDate, disableStartTime);
            if (!isAvailable) {
                printErrorMessage("사용불가 설정이 불가한 시간입니다.");
                return false;
            }
        } catch (NumberFormatException e) {
            printErrorMessage("사용불가할 시간은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean validateDisableEndTime(Kcube kcube, String disableDate, String
            disableStartTime, String disableEndTime) {
        if (disableEndTime == null) {
            printErrorMessage("disableUseTime is null");
            return false;
        }

        if (!disableEndTime.matches("\\d+")) {
            printErrorMessage("사용불가 시간은 숫자로만 이루어져야 합니다.");
            return false;
        }
        /*사용불가 예외처리*/
        try {
            int intDisableStartTime = Integer.parseInt(disableStartTime);
            int intDisableEndTime = Integer.parseInt(disableEndTime);
            int intDisableDate = Integer.parseInt(disableDate);
            if (intDisableEndTime < 9 || intDisableEndTime > 21) {
                printErrorMessage("입력하신 사용불가 끝 시간이 올바르지 않습니다.");
                return false;
            }
            boolean isCurDate = intDisableDate == 0;
            if (isCurDate) {
                if (sharedData.currentTime.getHour() > intDisableEndTime) {
                    printErrorMessage("현재시간 이전의 시간은 사용불가가 불가합니다.");
                    return false;
                } else if (sharedData.currentTime.getHour() == intDisableEndTime
                        && sharedData.currentTime.getMinute() != 0) {
                    printErrorMessage("현재시간 이전의 시간은 사용불가가 불가합니다.");
                    return false;
                }
            }
            for (int hour = intDisableStartTime; hour <= intDisableEndTime; hour++) {
                boolean isAvailable = isRoomAvailable(kcube, disableDate, String.valueOf(hour));
                if (!isAvailable) {
                    printErrorMessage("사용불가 설정이 불가한 시간입니다.");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            printErrorMessage("사용불가할 시간은 숫자로 이루어져야 합니다.");
            return false;
        }
        return true;
    }

    public static boolean isRoomAvailable(Kcube kcube, String date, String hour) {
        List<DisableKcube> disableKcubeList = sharedData.disableKcubes.getOrDefault(new Date(date), null);
        List<Reservation> reservations = sharedData.reservationList.getOrDefault(new Date(date), null);
        boolean isDisable = false;
        if (disableKcubeList != null) {
            isDisable = disableKcubeList.stream()
                    .filter(disableKcube -> {
                        String disableKcubeDate = disableKcube.getDate();
                        String name = disableKcube.getName();
                        String room = disableKcube.getRoom();
                        return name.equals(kcube.name) && room.equals(kcube.room) && disableKcubeDate.equals(date);
                    })
                    .anyMatch(disableKcube -> (Integer.parseInt(disableKcube.getStartTime()) <= Integer.parseInt(hour) && Integer.parseInt(disableKcube.getEndTime()) >= Integer.parseInt(hour)));
        }
        boolean isReserved = false;
        if (reservations != null) {
            isReserved = reservations.stream()
                    .anyMatch(reservation -> (Integer.parseInt(reservation.getStartTime()) <= Integer.parseInt(hour)
                            && Integer.parseInt(hour) <= Integer.parseInt(reservation.getStartTime()) + Integer.parseInt(reservation.getUseTime()) - 1));
        }
//        System.out.println(isDisable + " " + isReserved);
        return !isDisable && !isReserved;
    }

    public static boolean validateReservationUseTime(String reservationUseTime) {
        if (reservationUseTime == null) {
            printErrorMessage("reservationUseTime is null");
            return false;
        }

        if (!reservationUseTime.matches("\\d+")) {
            printErrorMessage("이용 시간은 숫자로만 이루어져야 합니다.");
            return false;
        }
        /*사용불가 예외처리*/
        try {
            int intReservationUseTime = Integer.parseInt(reservationUseTime);
            if (intReservationUseTime <= 0 || intReservationUseTime >= 4) {
                printErrorMessage("입력하신 이용 시간이 올바르지 않습니다.");
                return false;
            }
        } catch (NumberFormatException e) {
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

    public static boolean isWeekend(String dateStr) {
        if (dateStr == null) {
            printErrorMessage("dateStr is null");
        }
        String dayOfWeek = Date.getDayOfWeek(dateStr);
        // 주말인지 확인
        return dayOfWeek.equals("토") || dayOfWeek.equals("일");
    }
}
