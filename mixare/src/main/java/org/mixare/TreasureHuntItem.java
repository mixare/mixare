package org.mixare;

public class TreasureHuntItem {
    private String firebaseKey; // Firebase Realtime Database 에 등록된 Key 값
    private String itemName; // 아이템 이름
    private String itemPhotoUrl; // 아이템 사진 URL
    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    private boolean itemSelectionFlag; // 아이템 선택 여부
    private long time; // 작성한 시간

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemPhotoUrl() {
        return itemPhotoUrl;
    }

    public void setItemPhotoUrl(String itemPhotoUrl) {
        this.itemPhotoUrl = itemPhotoUrl;
    }

    public boolean isItemSelectionFlag() {
        return itemSelectionFlag;
    }

    public void setItemSelectionFlag(boolean itemSelectionFlag) {
        this.itemSelectionFlag = itemSelectionFlag;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
