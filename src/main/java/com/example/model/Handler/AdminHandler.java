package com.example.model.Handler;

import com.example.SharedData;
import com.example.model.Kcube;

public class AdminHandler {
    SharedData sharedData = SharedData.getInstance();
    public AdminHandler() {
        //
    }
    public void addBuilding(){
        // 빌딩 추가
            // 건물 이름 입력

            // 호실 개수 지정

            // 호실별 최대 인원수 지정

        // sharedData 에 반영
    }
    public void addRoom(){

        // 방 추가
            // 건물 번호 입력

            // 추가 호실 지정

            // 호실별 최대 인원수 지정

        //sharedData 에 반영

    }
    public void deleteBuilding(){
        // 빌딩 삭제
            // 건물 번호 입력

        //sharedData 에 반영

    }
    public void deleteRoom(){
        // 방 삭제
            // 건물 번호 선택

            // 방 번호 선택
        //sharedData 에 반영

    }
    public void setMaxCapacity(){
        // 최대 인원 지정
            // 건물 선택

            // 호실 선택

            // 최대 인원 선택

        //sharedData 에 반영

    }
    public void disableRoom(){
        // 사용불가 지정
            // building 선택

            // 날짜 선택

            // 호실 선택

            // 시작 시간

            // 끝시간

        //shardData 에 반영

    }
    public String readName(){
        return
    }

    public String readRoom(String Name){
        return
    }
    public String readMax(String Name, String Room){
        return
    }
    public String readStartTime(String Name,String Room){
        return
    }
    public String readUseTime(String StartTime){
        return
    }

}
