package com.example.service.Handler;

import com.example.SharedData;
import com.example.model.Kcube;


public class AdminHandler {
    SharedData sharedData = SharedData.getInstance();

    Kcube kcube;
    public AdminHandler() {}

    public void addBuilding(){
        String building ="";



        kcube.setName(building);
    }
    public void addRoom(){
        String room = "";
        // 방 추가
            // 건물 번호 입력

            // 추가 호실 지정

            // 호실별 최대 인원수 지정

        //sharedData 에 반영
        kcube.setRoom(room);
    }
    public void deleteBuilding(){
        String building;
        // 빌딩 삭제
            // 건물 번호 입력

        //sharedData 에 반영
        kcube.setName("");
    }
    public void deleteRoom(){
        String room;
        // 방 삭제
            // 건물 번호 선택

            // 방 번호 선택
        //sharedData 에 반영

    }
    public void setMaxCapacity(){
        String max = "";
        // 최대 인원 지정
            // 건물 선택

            // 호실 선택

            // 최대 인원 선택

        //sharedData 에 반영
        kcube.setMax(max);
    }
    public void disableRoom(){
        // 사용불가 지정
            // building 선택

            // 날짜 선택

            // 호실 선택

            // 시작 시간

            // 끝시간

        //shardData 에 반영
        kcube.setMax("");
    }
    public String readName(){
        // todo 구현
        return "name";
    }
    public String readRoom(String Name){
        // todo 구현
        return "room정보";
    }
    public String readMax(String Name, String Room){
        // todo 구현
        return "Max정보";
    }
    public String readStartTime(String Name,String Room){
        // todo 구현
        return "StartTime 정보";
    }
    public String readUseTime(String StartTime){
        // todo 구현
        return "사용시간 정보";
    }
    public String readDate(){
        // todo 구현
        return "사용시간 정보";
    }
}
