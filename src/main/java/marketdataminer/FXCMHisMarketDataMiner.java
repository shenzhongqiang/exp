package main.java.marketdataminer;

import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import au.com.bytecode.opencsv.CSVWriter;

import com.fxcm.external.api.transport.FXCMLoginProperties;
import com.fxcm.external.api.transport.GatewayFactory;
import com.fxcm.external.api.transport.IGateway;
import com.fxcm.external.api.transport.listeners.IGenericMessageListener;
import com.fxcm.external.api.transport.listeners.IStatusMessageListener;
import com.fxcm.fix.FXCMTimingIntervalFactory;
import com.fxcm.fix.IFixDefs;
import com.fxcm.fix.NotDefinedException;
import com.fxcm.fix.SubscriptionRequestTypeFactory;
import com.fxcm.fix.UTCDate;
import com.fxcm.fix.UTCTimeOnly;
import com.fxcm.fix.UTCTimestamp;
import com.fxcm.fix.posttrade.CollateralReport;
import com.fxcm.fix.pretrade.MarketDataRequest;
import com.fxcm.fix.pretrade.MarketDataRequestReject;
import com.fxcm.fix.pretrade.MarketDataSnapshot;
import com.fxcm.fix.pretrade.TradingSessionStatus;
import com.fxcm.messaging.ISessionStatus;
import com.fxcm.messaging.ITransportable;

/**
 * Example of how to request and process historical rate data from the Java API
 *
 * @author rkichenama
 */
public class FXCMHisMarketDataMiner implements IGenericMessageListener,
		IStatusMessageListener {
	private static final String server = "http://www.fxcorporate.com/Hosts.jsp";
	private static final String TEST_CURRENCY = "EUR/USD";
	private static final String BASE_DIR = "F:\\Documents and Settings\\Zhongqiang Shen\\My Documents\\project\\exp\\marketdata\\FX_FXCM_Demo_EUR-USD_2013_EST_5";

	private FXCMLoginProperties login;
	private IGateway gateway;
	private String currentRequest;
	private boolean requestComplete;

	private ArrayList<CollateralReport> accounts = new ArrayList<CollateralReport>();
	private HashMap<UTCDate, MarketDataSnapshot> historicalRates = new HashMap<UTCDate, MarketDataSnapshot>();

	TradingSessionStatus tradeSessionStatus;

	private Calendar currentDate;
	private Calendar currentEndDate;
	private Calendar endDate;

	private String terminal;

	private static PrintWriter output = new PrintWriter((OutputStream) System.out, true);

	public PrintWriter getOutput() {
		return output;
	}

	public void setOutput(PrintWriter newOutput) {
		output = newOutput;
	}

	/**
	 * Creates a new JavaFixHistoryMiner with credentials with configuration
	 * file
	 *
	 * @param username
	 * @param password
	 * @param terminal
	 *            - which terminal to login into, dependent on the type of
	 *            account, case sensitive
	 * @param server - url, like 'http://www.fxcorporate.com/Hosts.jsp'
	 * @param file - a local file used to define configuration
	 */
	public FXCMHisMarketDataMiner(String username, String password,
			String terminal, Calendar startDate, Calendar endDate, String file) {
		this.terminal = terminal;
		// if file is not specified
		if (file == null)
			// create a local LoginProperty
			this.login = new FXCMLoginProperties(username, password, terminal, server);
		else
			this.login = new FXCMLoginProperties(username, password, terminal, server, file);

		this.currentDate = (Calendar)startDate.clone();
		this.endDate = (Calendar)endDate.clone();
	}

	/**
	 * Creates a new JavaFixHistoryMiner with credentials and no configuration file
	 *
	 * @param username
	 * @param password
	 * @param terminal - which terminal to login into, dependent on the type of account, case sensitive
	 * @param server - url, like 'http://www.fxcorporate.com/Hosts.jsp'
	 */
	public FXCMHisMarketDataMiner(String username, String password,
			String terminal, Calendar startDate, Calendar endDate) {
		// call the proper constructor
		this(username, password, terminal, startDate, endDate, null);
	}

	/**
	 * Attempt to login with credentials supplied in constructor, assigning self
	 * as listeners
	 */
	public boolean login() {
		return this.login(this, this);
	}

	/**
	 * Attempt to login with credentials supplied in constructor
	 *
	 * @param genericMessageListener - the listener object for trading events
	 * @param statusMessageListener - the listener object for status events
	 *
	 * @return true if login successful, false if not
	 */
	public boolean login(IGenericMessageListener genericMessageListener,
			IStatusMessageListener statusMessageListener) {
		try {
			// if the gateway has not been defined
			if (gateway == null)
				// assign it to a new gateway created by the factory
				gateway = GatewayFactory.createGateway();
			// register the generic message listener with the gateway
			gateway.registerGenericMessageListener(genericMessageListener);
			// register the status message listener with the gateway
			gateway.registerStatusMessageListener(statusMessageListener);
			// if the gateway has not been connected
			if (!gateway.isConnected()) {
				// attempt to login with the local login properties
				gateway.login(this.login);
			} else {
				// attempt to re-login to the api
				gateway.relogin();
			}
			// set the state of the request to be incomplete
			requestComplete = false;
			// request the current trading session status
			currentRequest = gateway.requestTradingSessionStatus();
			// wait until the request is complete
			while (!requestComplete) {
			}
			// return that this process was successful
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// if any error occurred, then return that this process failed
		return false;
	}

	/**
	 * Attempt to logout, assuming that the supplied listeners reference self
	 */
	public void logout() {
		this.logout(this, this);
	}

	/**
	 * Attempt to logout, removing the supplied listeners prior to disconnection
	 *
	 * @param genericMessageListener - the listener object for trading events
	 * @param statusMessageListener - the listener object for status events
	 */
	public void logout(IGenericMessageListener genericMessageListener,
			IStatusMessageListener statusMessageListener) {
		// attempt to logout of the api
		gateway.logout();
		// remove the generic message listener, stop listening to updates
		gateway.removeGenericMessageListener(genericMessageListener);
		// remove the status message listener, stop listening to status changes
		gateway.removeStatusMessageListener(statusMessageListener);
	}

	/**
	 * Request a refresh of the collateral reports under the current login
	 */
	public void retrieveAccounts() {
		// if the gateway is null then attempt to login
		if (gateway == null)
			this.login();
		// set the state of the request to be incomplete
		requestComplete = false;
		// request the refresh of all collateral reports
		currentRequest = gateway.requestAccounts();
		// wait until all the reqports have been processed
		while (!requestComplete) {
		}
	}

	/**
	 * Send a fully formed order to the API and wait for the response.
	 *
	 * @return the market order number of placed trade, NONE if the trade did
	 *         not execute, null on error
	 */
	public String sendRequest(ITransportable request) {
		try {
			// set the completion status of the requst to false
			requestComplete = false;
			// send the request message to the api
			currentRequest = gateway.sendMessage(request);
			// wait until the api answers on this particular request
			// while(!requestComplete) {}
			// if there is a value to return, it will be passed by currentResult
			return currentRequest;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// if an error occured, return no result
		return null;
	}

	/**
	 * Implementing IStatusMessageListener to capture and process messages sent
	 * back from API
	 *
	 * @param status
	 *            - status message received by API
	 */
	@Override
	public void messageArrived(ISessionStatus status) {
		// check to the status code
		if (status.getStatusCode() == ISessionStatus.STATUSCODE_ERROR
				|| status.getStatusCode() == ISessionStatus.STATUSCODE_DISCONNECTING
				|| status.getStatusCode() == ISessionStatus.STATUSCODE_CONNECTING
				|| status.getStatusCode() == ISessionStatus.STATUSCODE_CONNECTED
				|| status.getStatusCode() == ISessionStatus.STATUSCODE_CRITICAL_ERROR
				|| status.getStatusCode() == ISessionStatus.STATUSCODE_EXPIRED
				|| status.getStatusCode() == ISessionStatus.STATUSCODE_LOGGINGIN
				|| status.getStatusCode() == ISessionStatus.STATUSCODE_LOGGEDIN
				|| status.getStatusCode() == ISessionStatus.STATUSCODE_PROCESSING
				|| status.getStatusCode() == ISessionStatus.STATUSCODE_DISCONNECTED) {
			// display status message
			output.println("\t\t" + status.getStatusMessage());
		}
	}

	/**
	 * Implementing IGenericMessageListener to capture and process messages sent
	 * back from API
	 *
	 * @param message
	 *            - message received for processing by API
	 */
	@Override
	public void messageArrived(ITransportable message) {
		// decide which child function to send an cast instance of the message

		try {
			// if it is an instance of CollateralReport, process the collateral
			// report
			if (message instanceof CollateralReport)
				messageArrived((CollateralReport) message);
			// if it is an instance of MarketDataSnapshot, process the
			// historical data
			if (message instanceof MarketDataSnapshot)
				messageArrived((MarketDataSnapshot) message);
			// if it is an instance of MarketDataRequestReject, process the
			// historical data request error
			if (message instanceof MarketDataRequestReject)
				messageArrived((MarketDataRequestReject) message);
			// if the message is an instance of TradingSessionStatus, cast it
			// and send to child function
			else if (message instanceof TradingSessionStatus)
				messageArrived((TradingSessionStatus) message);
		} catch (Exception e) {
			e.printStackTrace(output);
		}
	}

	/**
	 * Separate function to handle collateral report requests
	 *
	 * @param cr - message interpreted as an instance of CollateralReport
	 */
	public void messageArrived(CollateralReport cr) {
		// if this report is the result of a direct request by a waiting process
		if (currentRequest.equals(cr.getRequestID()) && !accounts.contains(cr)) {
			// add the trading account to the account list
			accounts.add(cr);
			// set the state of the request to be completed only if this is the
			// last collateral report requested
			requestComplete = cr.isLastRptRequested();
		}
	}

	/**
	 * Separate function to handle the trading session status updates and pull the trading instruments
	 *
	 * @param tss - the message interpreted as a TradingSessionStatus instance
	 */
	public void messageArrived(TradingSessionStatus tss) {
		// check to see if there is a request from main application for a session update
		if (currentRequest.equals(tss.getRequestID())) {
			this.tradeSessionStatus = tss;
			// set that the request is complete for any waiting thread
			requestComplete = true;
			// attempt to set up the historical market data request
		}
	}

	public void getMarketData() throws Exception {
		while(this.tradeSessionStatus == null){
			Thread.sleep(500);
		}

		while(hasNext()){
			sendNextRequest();
			recordMarketData();
			this.currentDate.add(Calendar.DAY_OF_MONTH, 1);
			historicalRates.clear();
			Thread.sleep(2500);
		}
	}

	/**
	 * Separate function to handle the rejection of a market data historical
	 * snapshot
	 *
	 * @param mdrr - message interpreted as an instance of MarketDataRequestReject
	 */
	public void messageArrived(MarketDataRequestReject mdrr) {
		// display note consisting of the reason the request was rejected
		output.println("Historical data rejected; " + mdrr.getMDReqRejReason() + "," + mdrr.getText());
		// set the state of the request to be complete
		requestComplete = true;
	}

	/**
	 * Separate function to handle the receipt of market data snapshots
	 *
	 * Current dealing rates are retrieved through the same class as historical
	 * requests. The difference is that historical requests are 'answers' to a
	 * specific request.
	 *
	 * @param mds
	 */
	public void messageArrived(MarketDataSnapshot mds) {
		// if the market data snapshot is part of the answer to a specific request
		try {
			if (mds.getRequestID() != null && mds.getRequestID().equals(currentRequest)) {
				// add that snapshot to the historicalRates table
				synchronized (historicalRates) {
					historicalRates.put(mds.getDate(), mds);
				}
				// set the request to be complete only if the continuous flag is at the end
				requestComplete = (mds.getFXCMContinuousFlag() == IFixDefs.FXCMCONTINUOUS_END);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean hasNext(){
		return currentDate.compareTo(endDate) <= 0;
	}

	private void sendNextRequest() throws Exception {
        // get one day data at once
		// create a new market data request
		MarketDataRequest mdr = new MarketDataRequest();
		// set the subscription type to ask for only a snapshot of the history
		mdr.setSubscriptionRequestType(SubscriptionRequestTypeFactory.SNAPSHOT);
		// request the response to be formated FXCM style
		mdr.setResponseFormat(IFixDefs.MSGTYPE_FXCMRESPONSE);
		// set the intervale of the data candles
		mdr.setFXCMTimingInterval(FXCMTimingIntervalFactory.MIN5);
		// set the type set for the data candles
		mdr.setMDEntryTypeSet(MarketDataRequest.MDENTRYTYPESET_ALL);

		int timeframe = 5;
		int total = 1440;
		int loop = (int)Math.ceil(total / (300.0 * timeframe));
		currentEndDate = (Calendar)currentDate.clone();
		currentEndDate.add(Calendar.DAY_OF_MONTH, 1);
		Calendar start = (Calendar)currentDate.clone();
		Calendar end = (Calendar)currentDate.clone();
		int interval = 300 * timeframe;
		for(int i = 1; i <= loop; i++) {
			if(i == loop) {
				int left = total - interval * (i - 1);
				end.add(Calendar.MINUTE, left);
			}
			else {
				end.add(Calendar.MINUTE, interval);
			}

			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ft.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			System.out.println("start:" + ft.format(start.getTime()));
			System.out.println("end:" + ft.format(end.getTime()));
			// configure the start and end dates set the dates and times for the market data request
			mdr.setFXCMStartDate(new UTCDate(start.getTime()));
			mdr.setFXCMStartTime(new UTCTimeOnly(start.getTime()));
			mdr.setFXCMEndDate(new UTCDate(end.getTime()));
			mdr.setFXCMEndTime(new UTCTimeOnly(end.getTime()));
			// set the instrument on which the we want the historical data
			mdr.addRelatedSymbol(tradeSessionStatus.getSecurity(TEST_CURRENCY));

			output.println("request marketdata start date: " + ft.format(currentDate.getTime()));
			output.println("request marketdata end date: " + ft.format(currentEndDate.getTime()));
			// send the request
			sendRequest(mdr);
			start.add(Calendar.MINUTE, interval);
			Thread.sleep(2500);
		}
	}

	private void recordMarketData() throws Exception {
		int wait = 1;
		while(!requestComplete){
			output.println(String.format("wait %s time ...", wait));
			Thread.sleep(500);
			wait++;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-z");
		sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		// set filename
		String filename = String.format("%s\\FX_FXCM_%s_%s_%s.csv",
				BASE_DIR, terminal, TEST_CURRENCY.replaceAll("/", "-"), sdf.format(this.currentDate.getTime()));
		// give the table a header
		output.println("candle History for " + TEST_CURRENCY + ". interval ");
		// get the keys for the historicalRates table into a sorted list
		SortedSet<UTCDate> candle = new TreeSet<UTCDate>(historicalRates.keySet());
		// define a format for the dates
		sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
		// go through the keys of the historicalRates table
		if (candle.size() > 0) {
			output.println(String.format("total record: %d.", candle.size()));
			CSVWriter writer = new CSVWriter(new FileWriter(filename));
			for (int i = 0; i < candle.size(); i++) {
				// create a single instance of the snapshot
				MarketDataSnapshot candleData;
				synchronized (historicalRates) {
					candleData = historicalRates.get(candle.toArray()[i]);
				}
				// convert the key to a Date
				Date startTime = candleData.getOpenTimestamp().toDate();
				Date endTime = candleData.getCloseTimestamp().toDate();

				if (! endTime.after(this.currentEndDate.getTime())) {
					// print out the historicalRate table data
					String[] snapShotArray = new String[] {
							sdf.format(startTime),
							sdf.format(endTime),
							String.valueOf(candleData.getBidOpen()),
							String.valueOf(candleData.getBidClose()),
							String.valueOf(candleData.getBidHigh()),
							String.valueOf(candleData.getBidLow()),
							String.valueOf(candleData.getAskOpen()),
							String.valueOf(candleData.getAskClose()),
							String.valueOf(candleData.getAskHigh()),
							String.valueOf(candleData.getAskLow()),
							"0" };
					writer.writeNext(snapShotArray);
				}
			}
			writer.close();
		}
	}

	public static void main(String[] args) throws Exception {
		Calendar startTime = Calendar.getInstance();
		startTime.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		Calendar endTime = (Calendar)startTime.clone();

		startTime.set(2013, 0, 1, 0, 0, 0);
		endTime.set(2013, 2, 31, 0, 0, 0);

		FXCMHisMarketDataMiner miner = new FXCMHisMarketDataMiner("rkichenama", "1311016", "Demo", startTime, endTime);
		miner.login();
		miner.retrieveAccounts();
		miner.getMarketData();

		miner.logout();
	}
}
