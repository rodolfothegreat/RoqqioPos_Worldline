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
import com.futura4.paymentservice.api.interaction.CommunicationHandler;
import com.futura4.paymentservice.api.interaction.Interactive;
import com.futura4.paymentservice.impl.CheckResultImpl;
import com.futura4.paymentservice.impl.LoginResultImpl;
import com.futura4.paymentservice.impl.payment.PaymentResultImpl;
import com.futura4.paymentservice.impl.configuration.ConfigurationImpl;


import org.apache.log4j.Logger;

public class WorldLinePaymentService implements CardService, Refundable, Interactive, Configurable
{

	private static final String PS_IP_ADDRESS = "paymentService.worldline.ipAddress";
	private static final String PS_PORT_NUMBER = "paymentService.worldline.portNo";

	
	private LoginResult loginResult = null;

	private Configuration config = null;
	
	private CommunicationHandler communicationHandler = null;

	
	public static final Logger log = Logger
			.getLogger(WorldLinePaymentService.class);

	private static final String serviceID = "EFTPOS_WORLDLINE";
	
	@Override
	public LogoffResult logoff() {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public PaymentResult startPayment(PaymentData paymentData) {
    	log.info("Payment data: " + paymentData.toString());
        PaymentResult result = new PaymentResultImpl();
        result.setPaymentState(PaymentState.SUCCESS);

        return result;
	}

	@Override
	public boolean isLoggedIn() {
        return loginResult != null && loginResult.getLoginState() == LoginState.LOGGED_IN;
	}

	@Override
	public LoginResult login(String posID, String cashierID) {
		log.info("Start Login posid: " + posID + " cashierId: " + cashierID);
		this.loginResult = new LoginResultImpl(LoginState.LOGGED_IN, posID, cashierID);
			return loginResult;
			
	}

	
	@Override
	public InfoResult getInfo(PaymentData arg0) {
		log.info("getInfo - " + arg0.toString());
		return null;
	}

	@Override
	public String getServiceID() {
		return serviceID;
	}


	@Override
	public Configuration getConfiguration() {
        if (config == null) {
            config = new ConfigurationImpl();
            
            config.setValue(PS_IP_ADDRESS, "");
            config.setValue(PS_PORT_NUMBER , "");
        }
        
        
        return config;
	}

	@Override
	public void setCommunicationHandler(CommunicationHandler arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CheckResult checkRefund(PaymentData arg0, String arg1) {
    	log.info("checkRefund starting - " + arg0.toString());
        CheckResult cr = new CheckResultImpl();
        cr.setPaymentState(PaymentState.SUCCESS);
        return cr;
	}

	@Override
	public PaymentResult startRefund(PaymentData arg0, String arg1) {
    	log.info("Payment data: " + arg0.toString() + " - " + arg1);
        PaymentResult result = new PaymentResultImpl();
        result.setPaymentState(PaymentState.SUCCESS);

        return result;
	}

}
