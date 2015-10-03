package main.java.report;
import java.util.Date;
import java.text.*;
import main.java.model.TransactionHistory;
import main.java.model.Account;
import main.java.exceptions.*;

public class OpenTransaction extends TransactionHistory {
    private int openAmount;

    public OpenTransaction() {
        super();
    }

    public OpenTransaction(Account account, Date time, String product, double price, int amount) {
        super(account, time, product, price, amount, 0, 0);
        this.openAmount = amount;
    }

    public OpenTransaction(TransactionHistory th) {
        this(th.getAccount(), th.getTime(), th.getProduct(), th.getPrice(), th.getAmount());
    }

    public int getOpenAmount() {
        return this.openAmount;
    }

    public void setOpenAmount(int amount) {
        this.openAmount = amount;
    }

    /**
     * Close transaction - update amount that are still open
     *
     * @return {@link ClosedTransaction} for the closed transaction
     */
    public ClosedTransaction close(Date closeTime, double closePrice, int closeAmount) {
        if(Math.abs(this.openAmount) < Math.abs(closeAmount)) {
            throw new NotEnoughAmountToClose(closeAmount);
        }

        Date openTime = this.getTime();
        if(openTime.compareTo(closeTime) > 0) {
            throw new CloseEarlierThanOpen(openTime, closeTime);
        }

        this.openAmount -= closeAmount;

        String type = this.getAmount() > 0 ? "buy" : "sell";
        return new ClosedTransaction(openTime, type, closeAmount, this.getProduct(), this.getPrice(), closeTime, closePrice);
    }

    public String toString() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strTime = ft.format(this.getTime());
        return String.format("<OpenTransaction ID=[%d] AccountID=[%d] Product=[%s]  Type=[%s] Price=[%f] Amount=[%d] Time=[%s], openAmount=[%d]>",
        this.getId(), this.getAccount().getId(), this.getProduct(), this.getType(),
        this.getPrice(), this.getAmount(), strTime, this.openAmount);
    }
}
