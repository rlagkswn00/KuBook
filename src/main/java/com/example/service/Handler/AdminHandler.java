package com.example.service.Handler;

import com.example.SharedData;
import com.example.fileio.FileManager;
import com.example.model.Date;
import com.example.model.DisableKcube;
import com.example.model.Kcube;
import com.example.model.Reservation;
import com.example.utils.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class AdminHandler {
    SharedData sharedData = SharedData.getInstance();

    Kcube kcube;

    public AdminHandler() {
    }

    public void addBuilding() {
        Scanner sc = new Scanner(System.in);
        System.out.print("추가할 건물의 이름을 입력해주세요 (ex. 공학관) :");
        String buildingName;
        while (true) {
            buildingName = sc.nextLine();

            if(Validation.validateBuildingName(buildingName)){
                break;
            }

        }

        String roomNum;
        while (true) {
            System.out.print(buildingName + "에 추가할 호실의 개수를 입력해주세요 (ex. 3) : ");
            roomNum = sc.nextLine();
            if (Validation.validateAddBuildingRoomNum(roomNum, 9)) {
                break;
            }
        }
        List<Kcube> kcubesToAdd = new ArrayList<>();
        for (int i = 1; i < Integer.parseInt(roomNum) + 1; i++) {
            String max;
            while (true) {
                System.out.print(i + "호실 최대 인원수를 입력해주세요 (2~20) :");
                max = sc.nextLine();
                if (Validation.validateMax(max)) {
                    break;
                }
            }
            kcubesToAdd.add(Kcube.from(buildingName, roomNum, max));
        }
        sharedData.kcubes.addAll(kcubesToAdd);
        System.out.println("케이큐브에 " + buildingName + "이 정상적으로 추가되었습니다. 5초 후 관리자 모드메뉴로 돌아갑니다.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void addRoom() {
        Scanner sc = new Scanner(System.in);
        List<Kcube> kcubes = sharedData.getKcubes();
        List<String> nameList = kcubes.stream()
                .map(Kcube::getName)
                .distinct()
                .toList();

        nameList.forEach(name -> System.out.print((nameList.indexOf(name) + 1) + ". " + name + " "));
        System.out.println();
        // 건물 번호 선택
        String buildingNum;
        while (true) {
            System.out.print("호실 추가할 건물을 선택해주세요 (ex. 1) : ");
            buildingNum = sc.nextLine();
            if (Validation.validateBuildingNum(buildingNum, nameList.size())) {
                break;
            }
        }
        String finalBuildingNum = buildingNum;
        String buildingName = nameList.get(Integer.parseInt(finalBuildingNum) - 1);
        long currentRoomCount = kcubes.stream()
                .filter(sharedDateKcube -> sharedDateKcube.getName().equals(buildingName))
                .count();
        System.out.println(buildingName + " 호실 목록입니다.");
        for (int i = 1; i <= currentRoomCount; i++) {
            System.out.print(i + ". " + i + "호실 ");
        }
        System.out.println();

        System.out.println("추가할 호실 번호를 입력하세요 (ex. 1) : ");
        String roomToAdd;
        while (true) {
            roomToAdd = sc.nextLine();
            if (Validation.validateAddRoom(buildingName, roomToAdd)) {
                break;
            }
        }

        String max;
        while (true) {
            System.out.print(roomToAdd + "호실 최대 인원수를 입력해주세요 (2~20) : ");
            max = sc.nextLine();
            if (Validation.validateMax(max)) {
                break;
            }
        }
        sharedData.kcubes
                .add(Kcube.from(buildingName, roomToAdd, max));
    }

    public void deleteBuilding() {
        Scanner sc = new Scanner(System.in);
        List<Kcube> kcubes = sharedData.getKcubes();
        List<String> nameList = kcubes.stream()
                .map(Kcube::getName)
                .distinct()
                .toList();

        nameList.forEach(name -> System.out.print((nameList.indexOf(name) + 1) + ". " + name + " "));
        System.out.println();

        Map<Date, List<Reservation>> reservationList = sharedData.reservationList;
        Set<Date> dates = reservationList.keySet();
        List<Reservation> allReservations = new ArrayList<>();
        for (Date date : dates) {
            List<Reservation> reservations = reservationList.get(date);
            allReservations.addAll(reservations);
        }
        List<String> reservationNameList = allReservations.stream()
                .map(Reservation::getName)
                .distinct()
                .toList();
        if (reservationNameList.size() == nameList.size()) {
            System.out.println("모든 건물에 예약 목록이 있으므로 삭제 가능한 건물이 없습니다.5초 후 관리자 모드 메뉴로 돌아갑니다.");
            try {
                Thread.sleep(5000);
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // 건물 번호 선택
        String buildingNum;
        while (true) {
            System.out.print("삭제할 건물을 선택하세요 (ex. 1) : ");
            buildingNum = sc.nextLine();
            String buildingName = nameList.get(Integer.parseInt(buildingNum) - 1);
            boolean isReserved = allReservations.stream()
                    .map(Reservation::getName)
                    .distinct()
                    .toList()
                    .contains(buildingName);
            if (!isReserved) {
                sharedData.kcubes
                        .removeIf(sharedDateKcube -> sharedDateKcube.getName().equals(buildingName));
                System.out.println("케이큐브에 " + buildingName + "이 정상적으로 삭제되었습니다. 5초 후 관리자 모드 메뉴로 돌아갑니다.");
                try {
                    Thread.sleep(5000);
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("오류! 해당 건물은 예약 목록이 존재합니다. 다시 선택해주세요.");
        }
    }

    public void deleteRoom() {
        Scanner sc = new Scanner(System.in);
        List<Kcube> kcubes = sharedData.getKcubes();
        List<String> nameList = kcubes.stream()
                .map(Kcube::getName)
                .distinct()
                .toList();

        nameList.forEach(name -> System.out.print((nameList.indexOf(name) + 1) + ". " + name + " "));
        System.out.println();
        // 건물 번호 선택
        String buildingNum;
        while (true) {
            System.out.print("호실 삭제할 건물을 선택하세요 (ex. 1) : ");
            buildingNum = sc.nextLine();
            if (Validation.validateBuildingNum(buildingNum, nameList.size())) {
                break;
            }
        }
        String buildingName = nameList.get(Integer.parseInt(buildingNum) - 1);
        long currentRoomCount = kcubes.stream()
                .filter(sharedDateKcube -> sharedDateKcube.getName().equals(buildingName))
                .count();
        System.out.println(buildingName + " 호실 목록입니다.");
        for (int i = 1; i <= currentRoomCount; i++) {
            System.out.print(i + ". " + i + "호실 ");
        }
        System.out.println();

        System.out.print("삭제할 호실을 선택하세요 (ex. 1) : ");
        String roomToDelete;
        while (true) {
            roomToDelete = sc.nextLine();
            if (Validation.validateDeleteRoom(buildingName, roomToDelete)) {
                break;
            }
        }
        String finalRoomToDelete = roomToDelete;
        sharedData.kcubes
                .removeIf(sharedDataKcube -> sharedDataKcube.getName().equals(buildingName)
                        && sharedDataKcube.getRoom().equals(finalRoomToDelete));
        System.out.print(buildingName + " " + roomToDelete + "호실이 정상적으로 삭제되었습니다.");

        boolean isEmptyBuilding = sharedData.kcubes.stream()
                .map(Kcube::getName)
                .distinct()
                .noneMatch(s -> s.equals(buildingName));
        if (isEmptyBuilding) {
            System.out.println();
            System.out.println(buildingName + "에 호실이 존재하지 않습니다. 케이큐브 건물 목록에서 "+ buildingName +"을 삭제합니다. 5초 후 관리자 모드 메뉴로 돌아갑니다.");
        }else{
            System.out.println("5초 후 관리자 모드 메뉴로 돌아갑니다.");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void disableRoom() {
        String selectedKCubeName = readName();

        // Step 2: Date Selection
        String selectedDate = readDate();

        // Step 3: Display current room availability
        System.out.println(selectedKCubeName + " " + selectedDate + " 케이큐브 현황입니다.");
        System.out.println("(■ : 사용 불가 설정 불가능 , □ : 사용 불가 설정 가능)");
        System.out.println("        09  10  11  12  13  14  15  16  17  18  19  20  21");

        List<Kcube> kcubes = sharedData.kcubes;
        kcubes.stream()
                .filter(kcube -> kcube.getName().equals(selectedKCubeName))
                .forEach(kcube -> {
                    System.out.print(kcube.getRoom() + "호실 : ");
                    for (int hour = 9; hour <= 21; hour++) {
                        if (Validation.isRoomAvailable(kcube, selectedDate, String.valueOf(hour))) {
                            System.out.print("  □ ");
                        } else {
                            System.out.print("  ■ ");
                        }
                    }
                    System.out.println();
                });

        // Step 4: Room Selection
        String room = readRoom(selectedKCubeName);

        // Step 5: Time Selection
        String startTime;
        while (true) {
            startTime = readStartTime(selectedKCubeName, room);
            if (Validation.validateDisableStartTime(kcube, selectedDate, startTime)) {
                break;
            }
        }
        String endTime;
        while (true) {
            endTime = readUseTime(startTime);
            if (Validation.validateDisableEndTime(kcube, selectedDate, startTime, endTime)) {
                break;
            }
        }
        // Step 6: Update sharedData
        DisableKcube disable = new DisableKcube(kcube.getName(), room, selectedDate, startTime, endTime);
        Date date = new Date(selectedDate);
        sharedData.getDisableKcubes()
                .computeIfAbsent(date, k -> new ArrayList<>())
                .add(disable);

        System.out.println("정상적으로 사용 불가 처리되었습니다. 5초 후 메뉴로 돌아갑니다.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void setMaxCapacity() {
        Scanner sc = new Scanner(System.in);
        List<Kcube> kcubes = sharedData.getKcubes();
        List<String> nameList = kcubes.stream()
                .map(Kcube::getName)
                .distinct()
                .toList();
        System.out.println();
        // 건물 번호 선택
        String buildingNum;
        while (true) {
            nameList.forEach(name -> System.out.print((nameList.indexOf(name) + 1) + ". " + name + " "));
            System.out.println();
            System.out.print("최대 인원수를 설정할 건물을 선택하세요 (ex. 1) : ");
            buildingNum = sc.nextLine();
            if (Validation.validateBuildingNum(buildingNum, nameList.size())) {
                break;
            }
        }
        String buildingName = nameList.get(Integer.parseInt(buildingNum) - 1);
        long currentRoomCount = kcubes.stream()
                .filter(sharedDateKcube -> sharedDateKcube.getName().equals(buildingName))
                .count();
        System.out.println(buildingName + " 호실 목록입니다.");
        for (int i = 1; i <= currentRoomCount; i++) {
            System.out.print(i + ". " + i + "호실 ");
        }
        System.out.println();
        System.out.print("최대 인원수를 설정할 호실을 선택해주세요 (ex. 1) : ");
        String roomToFix;
        while (true) {
            roomToFix = sc.nextLine();
            if (Validation.validateRoomToFix(buildingName, roomToFix)) {
                break;
            }
        }
        String maxToFix = "";
        for (Kcube k : kcubes) {
            if (k.getRoom().equals(roomToFix) && k.getName().equals(buildingName)) {
                maxToFix = k.getMax();
            }
        }
        System.out.print("현재 " + buildingName + " " + roomToFix + "호실의 최대 인원수는 " + maxToFix + "명입니다. 새로 설정할 최대 인원수를 입력해주세요 (ex. 6) : ");

        String max;
        while (true) {
            max = sc.nextLine();
            if (Validation.validateFixMax(buildingName,roomToFix,maxToFix,max)) {
                break;
            }
        }
        for (Kcube k : kcubes) {
            if (k.getName().equals(buildingName) && k.getRoom().equals(roomToFix)) {
                k.max = max;
                break;
            }
        }
        System.out.println(buildingName + " " + roomToFix + "호실이 최대 인원수가 " + max + "명으로 설정되었습니다. 5초 후 메뉴로 돌아갑니다.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    public String readName() {
        Scanner sc = new Scanner(System.in);
        // Step 1: Building Selection
        List<Kcube> kcubes = sharedData.getKcubes();
        List<String> nameList = kcubes.stream()
                .map(Kcube::getName)
                .distinct()
                .toList();

        String buildingNum;
        while (true) {
            nameList.forEach(name -> System.out.print((nameList.indexOf(name) + 1) + ". " + name + " "));
            System.out.println();
            System.out.print("사용불가 설정할 건물을 선택하세요 (ex. 1) : ");
            buildingNum = sc.nextLine();
            if (Validation.validateBuildingNum(buildingNum, nameList.size())) {
                break;
            }
        }
        return nameList.get(Integer.parseInt(buildingNum) - 1);
    }

    public String readRoom(String name) {
        Scanner sc = new Scanner(System.in);
        List<Kcube> kcubes = sharedData.kcubes;
        List<Kcube> sameKCubes = kcubes.stream()
                .filter(kcube -> kcube.getName().equals(name))
                .toList();

        String roomNum;
        while (true) {
            System.out.print("사용 불가 설정할 호실을 선택해주세요 (ex. 3): ");
            roomNum = sc.nextLine();
            if (Validation.validateDisableRoomNum(roomNum, sameKCubes.size())) { // Assuming max 3 rooms
                break;
            }
        }
        for (Kcube k : kcubes) {
            if (k.getRoom().equals(roomNum) && k.getName().equals(name)) {
                kcube = k;
                break;
            }
        }
        return roomNum;
    }

    public String readMax(String name, String room) {
        return "Max정보";
    }

    public String readStartTime(String name, String room) {
        Scanner sc = new Scanner(System.in);
        String startTime;
        System.out.print("사용불가 시작 시간을 입력하세요 (ex. 12) : ");
        startTime = sc.nextLine();
        return startTime;
    }

    public String readUseTime(String startTime) {
        Scanner sc = new Scanner(System.in);
        String useTime;
        System.out.print("사용불가 끝 시간을 입력하세요 (ex. 15) : ");
        useTime = sc.nextLine();
        return useTime;
    }

    public String readDate() {
        Scanner sc = new Scanner(System.in);
        List<String> dates = FileManager.dateGenerator(sharedData.getCurrentTime().getDate());
        dates.forEach(date -> System.out.print("(" + dates.indexOf(date) + ") " + date + "["+ Date.getDayOfWeek(date) + "] "));
        System.out.println();

        String dateNum;
        while (true) {
            System.out.print("\n사용불가 설정할 날짜를 선택하세요 (ex. 1) : ");
            dateNum = sc.nextLine();
            if (Validation.validateReservationDate(dateNum, dates.size())) {
                break;
            }
        }
        String selectedDate = dates.get(Integer.parseInt(dateNum));
        return selectedDate;
    }
}
