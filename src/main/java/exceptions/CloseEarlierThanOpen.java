package main.java.exceptions;
import java.util.Date;

public class CloseEarlierThanOpen extends RuntimeException {
    public CloseEarlierThanOpen(Date open, Date close) {
    }
}

