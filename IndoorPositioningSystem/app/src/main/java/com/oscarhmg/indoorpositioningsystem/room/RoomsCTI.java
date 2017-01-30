package com.oscarhmg.indoorpositioningsystem.room;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by OscarHMG on 19/12/2016.
 */
public final class RoomsCTI {

    public static final Room BODEGA = new Room("Warehouse", new LatLng((-2.145885955943222),-79.9486081302166),"bodega");
    public static final Room COMEDOR1 = new Room("Main Cafeteria", new LatLng((-2.145933866806433),-79.94887333363295),"comedor1");
    public static final Room COMEDOR2 =  new Room("Cafeteria", new LatLng((-2.145738537893167),-79.94862053543329),"comedor2");
    public static final Room HALL = new Room("Hall", new LatLng((-2.1458353647503747),-79.94892999529839),"hall");
    public static final Room OFICINA1 = new Room("Office", new LatLng((-2.1459901536927504),-79.94875632226467),"oficina1");
    public static final Room OFICINA2 = new Room("PhD. Dominguez's Office", new LatLng((-2.145987138323904),-79.94871843606234),"oficina2");
    public static final Room OFICINA3 = new Room("Office", new LatLng((-2.145678230504966),-79.94873654097319),"oficina3");
    public static final Room OFICINA4 = new Room("Office", new LatLng((-2.1456805757923254),-79.94876872748137),"oficina4");
    public static final Room LAB1 = new Room("Lab", new LatLng((-2.1459429129133034),-79.94880426675081),"lab1");
    public static final Room LAB_IHM = new Room("Human Computer Interaction Lab", new LatLng((-2.1457268114567625),-79.94881600141525),"labihm");
    public static final Room LAB_PROTO = new Room("Rapid Prototyping Lab", new LatLng((-2.145940902667342),-79.94867417961359),"labproto");
    public static final Room LAB_PROTO2 = new Room("3D Printing Lab", new LatLng((-2.145717430307578),-79.9486855790019),"labproto2");
    public static final Room SALA_ESPERA = new Room("Lounge", new LatLng((-2.145801190566184),-79.94862053543329),"salaespera");
    public static final Room PASILLO_PROTO1 = new Room("Hall", new LatLng((-2.14588796618926),-79.94867786765099),"pasilloproto1");
    public static final Room PASILLO_PROTO2 = new Room("Hall", new LatLng((-2.145764671093984),-79.9486993253231),"pasilloproto2");
    public static final Room PASILLO_LAB_IHM = new Room("Hall", new LatLng((-2.1457609856426205),-79.94877744466066),"pasillolabihm");
    public static final Room PASILLO_LAB20 = new Room("Hall", new LatLng((-2.1459127592235285),-79.948770403862),"pasillolab20");
    public static final Room MAIN_STAIRS = new Room("Main Stairs", new LatLng((-2.145845751021898),-79.94899470359088),"mainstairs");
    public static final Room WBATH1 = new Room("Women's Left Restroom", new LatLng((-2.1459549743890456),-79.94892362505198),"wbath1");
    public static final Room MBATH1 = new Room("Men's Left Restroom", new LatLng((-2.1459757469303864),-79.94896352291107),"mbath1");
    public static final Room WBATH2 = new Room("Women's Right Restroom", new LatLng((-2.1457258063336355),-79.94893670082092),"wbath2");
    public static final Room MBATH2 = new Room("Men's Right Restroom", new LatLng((-2.1457060389120373),-79.9489776045084),"mbath2");
    public static final Room NET1 = new Room("Networking 1", new LatLng((-2.145970721315566),-79.949018843472),"net1");
    public static final Room NET2 = new Room("NetWorking 2", new LatLng((-2.1457177653486075),-79.94902890175581),"net2");
    public static final Room OFFICES1 = new Room("More Offices", new LatLng((-2.1459720614795192),-79.94909897446631),"offices1");
    public static final Room OFFICES2 = new Room("Offices", new LatLng((-2.145713744856087),-79.94910970330238),"offices2");
    public static final Room OFFICE5 = new Room("Office", new LatLng((-2.145982447750113),-79.94917776435614),"office5");
    public static final Room OFFICE6 = new Room("Office", new LatLng((-2.1457217858411277),-79.94918882846832),"office6");
    public static final Room OFFICE7 = new Room("Main Office", new LatLng((-2.1459187899615264),-79.94927231222391),"office7");
    public static final Room RIGHT_STAIRS = new Room("Right Stairs", new LatLng((-2.1457304969082154),-79.94923811405897),"rightstairs");
    public static final Room LEFT_STAIRS = new Room("Left Stairs", new LatLng((-2.1459790973402497),-79.94922336190939),"leftstairs");
    public static final Room MEETING = new Room("Meeting Room", new LatLng((-2.145787453884085),-79.94927935302258),"meeting");
    public static final Room SECRETARY = new Room("Secretary", new LatLng((-2.145850441596096),-79.94926124811172),"secretary");
    public static final Room DOMO = new Room("Domo", new LatLng((-2.145835029709358),-79.9487455934286),"domo");
    public static final ArrayList<Room> rooms = createRooms();

    private static ArrayList<Room> createRooms(){
        ArrayList<Room> tmpRooms = new ArrayList<>();
        tmpRooms.add(BODEGA);
        tmpRooms.add(COMEDOR1);
        tmpRooms.add(COMEDOR2);
        tmpRooms.add(HALL);
        tmpRooms.add(OFICINA1);
        tmpRooms.add(OFICINA2);
        tmpRooms.add(OFICINA3);
        tmpRooms.add(OFICINA4);
        tmpRooms.add(LAB1);
        tmpRooms.add(LAB_IHM);
        tmpRooms.add(LAB_PROTO);
        tmpRooms.add(LAB_PROTO2);
        tmpRooms.add(SALA_ESPERA);
        tmpRooms.add(PASILLO_PROTO1);
        tmpRooms.add(PASILLO_PROTO2);
        tmpRooms.add(PASILLO_LAB_IHM);
        tmpRooms.add(PASILLO_LAB20);
        tmpRooms.add(MAIN_STAIRS);
        tmpRooms.add(WBATH1);
        tmpRooms.add(MBATH1);
        tmpRooms.add(WBATH2);
        tmpRooms.add(MBATH2);
        tmpRooms.add(NET1);
        tmpRooms.add(NET2);
        tmpRooms.add(OFFICES1);
        tmpRooms.add(OFFICES2);
        tmpRooms.add(OFFICE5);
        tmpRooms.add(OFFICE6);
        tmpRooms.add(OFFICE7);
        tmpRooms.add(RIGHT_STAIRS);
        tmpRooms.add(LEFT_STAIRS);
        tmpRooms.add(MEETING);
        tmpRooms.add(SECRETARY);
        tmpRooms.add(DOMO);
        return tmpRooms;
    }





}
