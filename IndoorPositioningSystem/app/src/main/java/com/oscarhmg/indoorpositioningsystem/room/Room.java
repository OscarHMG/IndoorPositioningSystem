package com.oscarhmg.indoorpositioningsystem.room;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by user on 19/12/2016.
 */
public class Room {
    private ArrayList<String> nameUsers;
    private String nameRoom;
    private LatLng coordinates;
    private String nickName;


    public ArrayList<String> getNameUsers() {
        return nameUsers;
    }

    public void setNameUsers(ArrayList<String> nameUsers) {
        this.nameUsers = nameUsers;
    }

    public String getNameRoom() {
        return nameRoom;
    }

    public void setNameRoom(String nameRoom) {
        this.nameRoom = nameRoom;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public Room(String nameRoom, LatLng coordinates,String nickName){
        nameUsers = new ArrayList<>();
        this.coordinates = coordinates;
        this.nameRoom = nameRoom;
        this.nickName = nickName;
    }


}
