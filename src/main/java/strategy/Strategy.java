package main.java.strategy;

import java.util.*;

import main.java.data.MarketData;
import main.java.model.*;
import main.java.order.Order;
import main.java.subscriber.Subscriber;

public abstract class Strategy implements Subscriber {
	protected Order order;
	protected ArrayList<MarketData> askTs;
	protected ArrayList<MarketData> bidTs;

    public boolean isLastBar(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        int hour = cal.get(Calendar.HOUR);
        int minute = cal.get(Calendar.MINUTE);
        if(day == 6 && hour == 4 && minute >=55) {
            return true;
        }

        return false;
    }
}
