package iu.edu.indycar.streamer.experiments;

import iu.edu.indycar.streamer.TimeUtils;

import java.util.UUID;

public class AnomalyLabel implements Comparable<AnomalyLabel> {

    private String uuid = UUID.randomUUID().toString();
    private String carNumber;
    private String fromStr;
    private long from;
    private long to;
    private String toStr;
    private String label;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFromStr() {
        return fromStr;
    }

    public void setFromStr(String fromStr) {
        this.from = TimeUtils.convertTimestampToLong(fromStr);
        this.fromStr = fromStr;
    }

    public String getToStr() {
        return toStr;
    }

    public void setToStr(String toStr) {
        this.to = TimeUtils.convertTimestampToLong(toStr);
        this.toStr = toStr;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int compareTo(AnomalyLabel o) {
        return Long.compare(this.from, o.from);
    }

    @Override
    public String toString() {
        return "AnomalyLabel{" +
                "carNumber='" + carNumber + '\'' +
                ", fromStr='" + fromStr + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", toStr='" + toStr + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
