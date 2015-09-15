package main.java.report;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import main.java.exceptions.*;
import main.java.model.TransactionHistory;

/**
 * Double linked list to store open transactions, sorted by created date.
 *
 * @author Zhongqiang Shen
 */
public class OpenTransactionList {
    private int openAmount;
    private LinkedList<OpenTransaction> openList;

    public OpenTransactionList() {
        this.openAmount = 0;
        this.openList = new LinkedList<OpenTransaction>();
    }

    public int getOpenAmount() {
        return this.openAmount;
    }

    public LinkedList getOpenList() {
        return this.openList;
    }

    /**
     * Add open transactions, append to last
     */
    public void open(TransactionHistory th) {
        if(this.openAmount > 0 && th.getAmount() < 0) {
            throw new InvalidOpenAction();
        }

        if(this.openAmount < 0 && th.getAmount() > 0) {
            throw new InvalidOpenAction();
        }
        this.openAmount = this.openAmount + th.getAmount();
        this.openList.addLast(new OpenTransaction(th));
    }

    /**
     * Close open transactions, starting from the first element
     *
     * @return ArrayList<{@link CloseTransaction}>
     */
    public ArrayList<ClosedTransaction> close(TransactionHistory th) {
        if(this.openAmount == 0) {
            throw new NothingToClose();
        }

        if(this.openAmount > 0) {
            return this.closeLong(th);
        }
        else {
            return this.closeShort(th);
        }
    }

    public ArrayList<ClosedTransaction> closeLong(TransactionHistory th) {
        int closeAmount = th.getAmount() * -1;
        if(this.openAmount <=0 || closeAmount <=0) {
            throw new InvalidCloseAction();
        }

        double closePrice = th.getPrice();
        Date closeTime = th.getTime();
        if(closeAmount > this.openAmount) {
            throw new NotEnoughAmountToClose(closeAmount);
        }
        this.openAmount -= closeAmount;

        ArrayList<ClosedTransaction> closed = new ArrayList<ClosedTransaction>();
        int remainToClose = closeAmount;
        while(remainToClose > 0) {
            OpenTransaction node = this.openList.getFirst();
            int nodeAmount = node.getOpenAmount();
            if(remainToClose >= nodeAmount) {
                ClosedTransaction ct = node.close(closeTime, closePrice, nodeAmount);
                closed.add(ct);
                this.openList.removeFirst();
                remainToClose -= nodeAmount;
            }
            else {
                ClosedTransaction ct = node.close(closeTime, closePrice, remainToClose);
                closed.add(ct);
                node.setOpenAmount(nodeAmount - remainToClose);
                remainToClose = 0;
            }
        }
        return closed;
    }

    public ArrayList<ClosedTransaction> closeShort(TransactionHistory th) {
        int closeAmount = th.getAmount() * -1;
        if(this.openAmount >=0 || closeAmount >=0) {
            throw new InvalidCloseAction();
        }
        double closePrice = th.getPrice();
        Date closeTime = th.getTime();
        if(closeAmount < this.openAmount) {
            throw new NotEnoughAmountToClose(closeAmount);
        }
        this.openAmount -= closeAmount;

        ArrayList<ClosedTransaction> closed = new ArrayList<ClosedTransaction>();
        int remainToClose = closeAmount;
        while(remainToClose < 0) {
            OpenTransaction node = this.openList.getFirst();
            int nodeAmount = node.getOpenAmount();
            if(remainToClose <= nodeAmount) {
                ClosedTransaction ct = node.close(closeTime, closePrice, nodeAmount);
                closed.add(ct);
                this.openList.removeFirst();
                remainToClose -= nodeAmount;
            }
            else {
                ClosedTransaction ct = node.close(closeTime, closePrice, remainToClose);
                closed.add(ct);
                node.setOpenAmount(nodeAmount - remainToClose);
                remainToClose = 0;
            }
        }
        return closed;
    }
}
