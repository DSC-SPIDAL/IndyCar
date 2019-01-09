package iu.edu.indycar.streamer.records;

import java.util.Objects;

public class EntryRecord implements IndycarRecord {

    private String carNumber;
    private String uid;
    private String driverName;
    private String license;
    private String team;
    private String engine;
    private String hometown;

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    @Override
    public String getGroupTag() {
        return "ENTRY";
    }

    @Override
    public long getTimeField() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryRecord that = (EntryRecord) o;
        return carNumber.equals(that.carNumber) &&
                uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(carNumber, uid);
    }
}
