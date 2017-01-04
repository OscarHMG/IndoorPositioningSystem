package com.oscarhmg.indoorpositioningsystem;

/**
 * Created by user on 14/12/2016.
 */
public class Beacon {
    private final String deviceAddress;
    private int rssi;


    public class URLStatus{
        private String urlValue;
        private String urlNotSet;
        private String urlNotInvariant;
        private String txtPower;


        public String getErrors(){
            StringBuilder sb = new StringBuilder();
            if(txtPower!= null){
                sb.append(txtPower).append("\n");
            }
            if(urlNotSet != null){
                sb.append(urlNotSet).append("\n");
            }
            if(urlNotInvariant != null){
                sb.append(urlNotInvariant).append("\n");
            }
            return sb.toString().trim(); //String without spaces
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (urlValue != null) {
                sb.append(urlValue).append("\n");
            }
            return sb.append(getErrors()).toString().trim();
        }
    }

    class FrameStatus{
        private String nullServiceData;
        private String invalidFrameType;

        public String getErrors() {
            StringBuilder sb = new StringBuilder();
            if (nullServiceData != null) {
                sb.append(nullServiceData).append("\n");
            }
            if (invalidFrameType != null) {
                sb.append(invalidFrameType).append("\n");
            }
            return sb.toString().trim();
        }

        @Override
        public String toString() {
            return getErrors();
        }
    }
    //Beacon Class
    private boolean hasUrlFrame;
    private URLStatus urlStatus = new URLStatus();
    private FrameStatus frameStatus = new FrameStatus();
    public Beacon(String deviceAddress, int rssi) {
        this.deviceAddress = deviceAddress;
        this.rssi = rssi;
    }

    boolean contains(String s){
        return (s == null )
                || (s.isEmpty())
                || deviceAddress.replace(":","").toLowerCase().contains(s.toLowerCase())
                || (urlStatus.urlValue.toLowerCase().contains(s.toLowerCase()));
    }

    public int getRssi() {
        return rssi;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public boolean isHasUrlFrame() {
        return hasUrlFrame;
    }

    public URLStatus getUrlStatus() {
        return urlStatus;
    }

    public FrameStatus getFrameStatus() {
        return frameStatus;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
