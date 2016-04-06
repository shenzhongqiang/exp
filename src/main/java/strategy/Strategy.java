package main.java.strategy;

import java.util.*;
import org.joda.time.*;
import main.java.data.MarketData;
import main.java.model.*;
import main.java.order.Order;
import main.java.subscriber.Subscriber;

public abstract class Strategy implements Subscriber {
	protected Order order;
	protected ArrayList<MarketData> askTs;
	protected ArrayList<MarketData> bidTs;

    public boolean isLastBar(Date time) {
        DateTime dt = new DateTime(time);
        int day = dt.getDayOfWeek();
        int hour = dt.getHourOfDay();
        int minute = dt.getMinuteOfHour();
        if(day == 6 && hour == 4 && minute >=55) {
            return true;
        }

        return false;
    }
}
