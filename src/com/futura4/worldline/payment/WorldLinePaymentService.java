package com.futura4.worldline.payment;

import com.futura4.paymentservice.api.CardService;
import com.futura4.paymentservice.api.CheckResult;
import com.futura4.paymentservice.api.LoginResult;
import com.futura4.paymentservice.api.LoginState;
import com.futura4.paymentservice.api.LogoffResult;
import com.futura4.paymentservice.api.PaymentData;
import com.futura4.paymentservice.api.PaymentResult;
import com.futura4.paymentservice.api.PaymentState;
import com.futura4.paymentservice.api.Refundable;
import com.futura4.paymentservice.api.configuration.Configurable;
import com.futura4.paymentservice.api.configuration.Configuration;
import com.futura4.paymentservice.api.info.InfoResult;
import com.futura4.paymentservice.api.info.InfoState;
import com.futura4.paymentservice.api.interaction.CommunicationHandler;
import com.futura4.paymentservice.api.interaction.Interactive;
import com.futura4.paymentservice.api.interaction.PaymentActionInfo;
import com.futura4.paymentservice.api.interaction.PaymentActionType;
import com.futura4.paymentservice.api.payment.CardPaymentResult;
import com.futura4.paymentservice.impl.CheckResultImpl;
import com.futura4.paymentservice.impl.LoginResultImpl;
import com.futura4.paymentservice.impl.payment.CardPaymentResultImpl;
import com.futura4.paymentservice.impl.payment.PaymentResultImpl;
import com.six.timapi.PrintData;
import com.six.timapi.Receipt;
import com.six.timapi.Terminal;
import com.six.timapi.TerminalSettings;
import com.six.timapi.TimException;
import com.six.timapi.TransactionData;
import com.six.timapi.TransactionInformation;
import com.six.timapi.TransactionResponse;
import com.six.timapi.TransactionResponse.Action;
import com.six.timapi.constants.ConnectionMode;
import com.six.timapi.constants.Currency;
import com.six.timapi.constants.Recipient;
import com.six.timapi.constants.RequestType;
import com.six.timapi.constants.TransactionType;
import com.six.timapi.protocol.sixml.Amount;
import com.futura4.paymentservice.impl.configuration.ConfigurationImpl;
import com.futura4.paymentservice.api.info.InfoResult;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class WorldLinePaymentService implements CardService, Refundable, Interactive, Configurable, WL_Print
{
	private static final String PS_IP_PORT = "paymentService.worldline.port";
	private static final String PS_IP_ADDRESS = "paymentService.worldline.ipAddress";
	private static final String PS_TERMINAL_ID = "paymentService.worldline.terminalId";
	private static final String PS_LOG_DIRECTORY = "paymentService.worldline.logDirectory";
	private static final String PS_CONNECTION_MODE = "paymentService.worldline.connectionMode";
	private static final String PS_AUTO_COMMIT = "paymentService.worldline.autocommit";
	
	private int port = 0;
	private String ipAddress = "";
	private String terminalId = "";
	private String logDirectory = "";
	private ConnectionMode connectionMode = ConnectionMode.BROADCAST;
	private boolean autoCommit = true; 
	
	Terminal terminal = null;

	ExampleListener exampleListener = null;
	
	private LoginResult loginResult = null;

	private Configuration config = null;
	
	private CommunicationHandler communicationHandler = null;

	
	public static final Logger log = Logger
			.getLogger(WorldLinePaymentService.class);

	private static final String serviceID = "EFTPOS_WORLDLINE";
	
	private void aStartup()
	{
		if(terminal == null)
		{
			populateParams();
			TerminalSettings settings = new TerminalSettings();
			settings.setAutoCommit(autoCommit);
			settings.setTerminalId(terminalId);
			settings.setConnectionMode(connectionMode); 
			
			settings.setConnectionIPString(ipAddress);
			settings.setCommitTimeout(port);
			
			terminal = new Terminal(settings);
			exampleListener = new ExampleListener(terminal, this);
			terminal.addListener(exampleListener);
			try {
				terminal.login();
				terminal.activate();
			} catch (TimException e) {
				// TODO Auto-generated catch block
				log.error("Terminal activate error. - " + e.getErrorMessage());
			}
			log.info("Terminal " + terminal.toString());
		//	TransactionResponse aresponse = null;
		//	TransactionInformation anInf =  aresponse.getTransactionInformation();
		//	anInf.
			
		}
	}
	
	private void populateParams()
	{
		port = getConfigValueInt(PS_IP_PORT);
		ipAddress = getConfigValue(PS_IP_ADDRESS);
		terminalId = getConfigValue(PS_TERMINAL_ID);
		logDirectory = getConfigValue(PS_LOG_DIRECTORY);
		autoCommit = true;
		
		String conMode = getConfigValue(PS_CONNECTION_MODE);
		if(conMode.toUpperCase().contains("FIXIP"))
		{
			connectionMode = ConnectionMode.ON_FIX_IP;
		}
			
		logDirectory = ".";
				
	}
	
	@Override
	public LogoffResult logoff() {
		
		try {
			terminal.dispose();
		
		} catch (Exception ex) {
			// TODO: handle exception
		}
		terminal = null;
	    LogoffResult result = new com.futura4.paymentservice.impl.LogoffResultImpl(LoginState.LOGGED_OFF);
	    loginResult = null;
	    return result;
	}

	@Override
	public LoginResult getLoggedInResult() {
		return loginResult;
	}

	
	@Override
	public CheckResult checkPayment(PaymentData arg0) {
    	log.info("checkPayment starting - " + arg0.toString());
        CheckResult cr = new CheckResultImpl();
        cr.setPaymentState(PaymentState.SUCCESS);
        return cr;
	}

	private PaymentResult execTransactionAsynch(double amount, boolean isReturn)
	{
       // PaymentResult result = new PaymentResultImpl();
        CardPaymentResult result = new CardPaymentResultImpl();
        result.setPaymentState(PaymentState.SUCCESS);
		if(terminal == null)
		{
			result.setPaymentState(PaymentState.FAILURE);
			result.setHostMessage("EFTPOS terminal is not ready");
			return result;
		}
		
		exampleListener.theTranCompleted = false;
		exampleListener.transSuccessful = false;
		exampleListener.transMessage = "";
		exampleListener.cardType = "";
		exampleListener.setRequestInProgress(RequestType.TRANSACTION);
		terminal.setTransactionData(null);
		final TransactionData trxData = new TransactionData();
		trxData.setSaferpayRecurring(false);
		terminal.setTransactionData(trxData);
		
		com.six.timapi.Amount anAmount = new com.six.timapi.Amount(amount, Currency.AUD );
		try {
			if(!isReturn)
			{
				terminal.transactionAsync(TransactionType.PURCHASE, anAmount);
				
			} else 
			{
				terminal.transactionAsync(TransactionType.CREDIT, anAmount);
			}
								
			
		} catch (TimException e) {
			log.error("execTransactionAsynch error - " + e.getErrorMessage());
			result.setPaymentState(PaymentState.FAILURE);
			result.setHostMessage(e.getErrorMessage());
			return result;

		}
		
		int iii = 0;
		while(!exampleListener.theTranCompleted)
		{
			try {
				Thread.sleep(1000);
				iii++;
				if(iii > 180)
					break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		if(!exampleListener.theTranCompleted)
		{
			try {
				terminal.cancel();
			} catch (TimException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			result.setPaymentState(PaymentState.TIMED_OUT);
			return result;		
		}
		
		if(!exampleListener.transSuccessful)
		{
			result.setPaymentState(PaymentState.FAILURE);
			result.setHostMessage(exampleListener.transMessage);
		}
		
		if(exampleListener.cardType.length() > 0)
		{
			setPayResult(result, "cardType", exampleListener.cardType );
			result.setCustomerSelectedPaymentType(exampleListener.cardType);


		}
		
		return result;
	}
	
	private PaymentResult execTransaction(double amount, boolean isReturn)
	{
        PaymentResult result = new PaymentResultImpl();
        result.setPaymentState(PaymentState.SUCCESS);
		if(terminal == null)
		{
			result.setPaymentState(PaymentState.FAILURE);
			result.setHostMessage("EFTPOS terminal is not ready");
			return result;
		}
		
		com.six.timapi.Amount anAmount = new com.six.timapi.Amount(amount, Currency.AUD );
		if(isReturn) {
			anAmount = new com.six.timapi.Amount(-1 * amount, Currency.AUD );
		}
		
		TransactionResponse tranResponse = null;
		
		try {
			if(isReturn)
			{
				tranResponse = terminal.transaction(TransactionType.PURCHASE , anAmount);
			} else {
				tranResponse = terminal.transaction(TransactionType.PURCHASE, anAmount);			
			}
			Action action =  tranResponse.needsAction();
			PrintData printData = tranResponse.getPrintData();
			List<Receipt> lstReceip = printData.getReceipts();
			
			for (Receipt areceipt : lstReceip) 
			{
				String strReceipt = areceipt.getValue();
				Recipient arecipient = areceipt.getRecipient();
				log.info(arecipient.name());
				log.info(strReceipt);
				
				printReceipts(areceipt);
			//	terminal.commit();
			}
			
		} catch (TimException e) {
			result.setPaymentState(PaymentState.FAILURE);
			result.setHostMessage(e.getErrorMessage());
			log.error(e.getErrorMessage());
			return result;
		}
		
		
		
		
		return result;
	}
	
	@Override
	public PaymentResult startPayment(PaymentData paymentData) {
    	log.info("startPayment - Payment data: " + paymentData.toString());
        PaymentResult result = new PaymentResultImpl();
        result.setPaymentState(PaymentState.SUCCESS);
        
		double amount = Math.abs( paymentData.getAmount().getAmount().doubleValue());
   //     return execTransaction(amount, false);
        return execTransactionAsynch(amount, false);
	}
	
	
	
	private void printReceipt(List<String> receiptText, boolean isMerchant )
	{
		if(isMerchant)
		{
			communicationHandler.printMerchantReceipt(new PaymentActionInfo(
					PaymentActionType.PAYMENT_START), receiptText);

		} else 
		{
			communicationHandler.printCustomerReceipt(new PaymentActionInfo(
					PaymentActionType.PAYMENT_START), receiptText);
			
		}
	}
	
	private void printReceipts(Receipt areceipt)
	{
		String strReceipt = areceipt.getValue();
		Recipient arecipient = areceipt.getRecipient();
		
		String[] stringList = strReceipt.replaceAll("\r\n", "\n").split("\n");
		List<String> receiptLst = new ArrayList<String>();
		for(int i = 0; i < stringList.length; i++)
		{
			String aline = stringList[i];
			receiptLst.add(aline);
		}
		
		if(arecipient == Recipient.CARDHOLDER)
		{
			printReceipt(receiptLst, false);
		} else if (arecipient == Recipient.MERCHANT) 
		{
			printReceipt(receiptLst, true);
		} else if(arecipient == Recipient.BOTH)
		{
			printReceipt(receiptLst, false);
			printReceipt(receiptLst, true);
		}
		

	}
	
	

	@Override
	public boolean isLoggedIn() {
        return loginResult != null && loginResult.getLoginState() == LoginState.LOGGED_IN;
	}

	@Override
	public LoginResult login(String posID, String cashierID) {
		log.info("Start Login posid: " + posID + " cashierId: " + cashierID);
		aStartup();
		this.loginResult = new LoginResultImpl(LoginState.LOGGED_IN, posID, cashierID);
		return loginResult;
			
	}

	
	@Override
	public InfoResult getInfo(PaymentData arg0) {
		log.info("getInfo - " + arg0.toString());
		InfoResultImpl infoResult = new InfoResultImpl();
		 if (arg0 == null) {
			 log.warn("[x] paymentData is null");
		       infoResult.setInfoState(InfoState.PAYMENT_DATA_NOT_FOUND);
		    } else {
		      infoResult.setInfoState(InfoState.ACTIVATED);
		     }
		     return infoResult;
	}

	@Override
	public String getServiceID() {
		return serviceID;
	}


    private String getConfigValue(String key) {
        String result = null;
        if (config != null) {
            result = config.getValue(key);
        }
        return result == null ? "" : result;
    }

    private int getConfigValueInt(String key) {
        String result = null;
        if (config != null) {
            result = config.getValue(key);
        }
        return result == null || result.isEmpty() ? 0 : Integer.valueOf(result);
    }
	
	private boolean getConfigValueBool(String key){
		String result = "false";
		if(config != null) {
			result = config.getValue(key);
			if(result == null)
				result = "false";
		}
		return result.equalsIgnoreCase("true");
	}
	
	
	@Override
	public Configuration getConfiguration() {
        if (config == null) {
            config = new ConfigurationImpl();
            config.setValue(PS_IP_ADDRESS, "");
            config.setValue(PS_TERMINAL_ID, "");
            config.setValue(PS_LOG_DIRECTORY , "");

            config.setValue(PS_CONNECTION_MODE, "");
            config.setValue(PS_AUTO_COMMIT , "");
            config.setValue(PS_IP_PORT, "");
        }
        
        
        return config;
	}
	
	private void configWorldLine()
	{
		TerminalSettings settings = new TerminalSettings();
	}

	@Override
	public void setCommunicationHandler(CommunicationHandler arg0) {
		// TODO Auto-generated method stub
		communicationHandler = arg0;
	}

	@Override
	public CheckResult checkRefund(PaymentData arg0, String arg1) {
    	log.info("checkRefund starting - " + arg0.toString());
        CheckResult cr = new CheckResultImpl();
        cr.setPaymentState(PaymentState.SUCCESS);
        return cr;
	}

	@Override
	public PaymentResult startRefund(PaymentData paymentData, String arg1) {
    	log.info("startRefund - Payment data: " + paymentData.toString() + " - " + arg1);
        PaymentResult result = new PaymentResultImpl();
        result.setPaymentState(PaymentState.SUCCESS);
		double amount = Math.abs( paymentData.getAmount().getAmount().doubleValue());
        return execTransactionAsynch(amount, true);
	}

	@Override
	public void printAReceipt(Receipt areceipt) {
		printReceipts(areceipt);
		
	}
	
	private void setPayResult(CardPaymentResult paymentResult, String key, String value)
	  {
		 String newvalue = "";
	    if ((paymentResult != null) && (value != null) && (value.length() > 0)) {
	        newvalue = value.replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\t", " ").trim();	
	    }
	    if (newvalue.length() > 1)
	    {
	        paymentResult.addAdditionalObject(key, newvalue);
	    }
	    
	  }


}
