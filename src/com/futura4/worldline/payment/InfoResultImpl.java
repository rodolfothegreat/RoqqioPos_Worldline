package com.futura4.worldline.payment;

import com.futura4.paymentservice.api.PaymentState;
import com.futura4.paymentservice.api.info.InfoResult;
import com.futura4.paymentservice.api.info.InfoState;
import com.futura4.paymentservice.impl.CheckResultImpl;

public class InfoResultImpl extends CheckResultImpl implements InfoResult {
	   private InfoState state = InfoState.UNKNOWN;
	      
	      public InfoState getState()
	      {
	   /* 14 */     return this.state;
	      }
	      
	      public void setInfoState(InfoState state)
	      {
	   /* 19 */     switch (state) {
	        case INITIAL: 
	        case ACTIVATED: 
	        case REDEEMED_NEEDS_RELOAD: 
	        case EXPIRED: 
	        case REDEEMED: 
	   /* 25 */       setPaymentState(PaymentState.SUCCESS);
	   /* 26 */       break;
	        default: 
	   /* 28 */       setPaymentState(PaymentState.FAILURE);
	        }
	        
	   /* 31 */     this.state = state;
	      }
	      
	   /* 34 */   private String otherState = null;
	      
	      public String getOtherState()
	      {
	   /* 38 */     return this.otherState;
	      }

}