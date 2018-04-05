package com.example.joelwasserman.apsmap1;

/**
 * Created by b00sti on 30.03.2018
 */

public class Beacon {

    private String macAddress;
    private int RSSI;
    private int lat;
    private int lng;
    private int latOnMap;
    private int lngOnMap;

    public Beacon(String macAddress, int RSSI, int lat, int lng, int latOnMap, int lngOnMap) {
        this.macAddress = macAddress;
        this.RSSI = RSSI;
        this.lat = lat;
        this.lng = lng;
        this.latOnMap = latOnMap;
        this.lngOnMap = lngOnMap;
    }

    public int getLatOnMap() {
        return latOnMap;
    }

    public void setLatOnMap(int latOnMap) {
        this.latOnMap = latOnMap;
    }

    public int getLngOnMap() {
        return lngOnMap;
    }

    public void setLngOnMap(int lngOnMap) {
        this.lngOnMap = lngOnMap;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public int getLng() {
        return lng;
    }

    public void setLng(int lng) {
        this.lng = lng;
    }
}
