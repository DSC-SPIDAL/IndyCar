package iu.edu.indycar.streamer.exceptions;

public class NotParsableException extends Exception {

    public boolean log = true;

    public NotParsableException() {

    }

    public NotParsableException(String message) {
        super(message);
    }

    public NotParsableException(Exception ex) {
        super(ex);
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }
}
