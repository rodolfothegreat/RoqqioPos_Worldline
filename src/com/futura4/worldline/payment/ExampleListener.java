package com.futura4.worldline.payment;

import com.six.timapi.ActivateResponse;
import com.six.timapi.DeactivateResponse;
import com.six.timapi.DefaultTerminalListener;
import com.six.timapi.ErrorMessages;
import com.six.timapi.PrintData;
import com.six.timapi.Receipt;
import com.six.timapi.SystemInformationResponse;
import com.six.timapi.Terminal;
import com.six.timapi.TerminalSettings;
import com.six.timapi.TimEvent;
import com.six.timapi.TimException;
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

import java.util.List;

import javax.swing.SwingUtilities;

import com.futura4.paymentservice.api.info.InfoResult;


public class ExampleListener extends DefaultTerminalListener  {
	
	private Terminal aterminal;
	private RequestType requestInProgress = null;

	public boolean  theTranCompleted = false;
	public boolean transSuccessful = false;
	public String transMessage = "";
	public String cardType = "";
	
	WL_Print wl_Print;
	
	public void setRequestInProgress(RequestType type) {
		requestInProgress = type;
	}
	
	public RequestType getRequestInProgress()
	{
		return requestInProgress;
	}
	
	public ExampleListener(Terminal _terminal, WL_Print _wl_Print)
	{
		aterminal = _terminal;
		wl_Print = _wl_Print;
	}
	
	@Override
	public void transactionCompleted(final TimEvent event, final TransactionResponse data) {
		// Always super-call transactionCompleted(). This ensures requestCompleted() and
		// printReceipts() are properly called. You can do you own processing before or
		// after the super-call depending on your needs.
		super.transactionCompleted(event, data);

		// Events usually originate from a different thread than the main thread. Always
		// use SwingUtilities.invokeLater to be safe
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WorldLinePaymentService.log.info("Transaction completed");
				setRequestInProgress(null);
				theTranCompleted = true;	
				if(event.getException() == null) {
					// If event contains a null exception the transaction completed successfully.
					// Use data.getTransactionType() to see what kind of transaction finished if you
					// do not track this information yourself already. getTransactionType() is
					// present for your convenience.
					transSuccessful = true;
					if(data != null){
						switch (data.getTransactionType()) {
						case PURCHASE:
						case CREDIT:
							// Stores transaction reference number to be used by an upcoming reversal
							// and re-enabled the ECR.
							if(data.getTransactionInformation().getSixTrxRefNum() != null){
								WorldLinePaymentService.log.info(data.getTransactionInformation().getSixTrxRefNum());
								WorldLinePaymentService.log.info("data.getTransactionInformation() " +  data.getTransactionInformation().toString() );
								WorldLinePaymentService.log.info("data" + data.toString());
								//WorldLinePaymentService.log.info(data.getAdditionalInfo().toString() );
								cardType = data.getCardData().getBrandName();
								WorldLinePaymentService.log.info("Card Type: " + cardType);
							}
							break;


						default:
							break;
						}
					}

				} else {
					// If event contains an exception the transaction failed. Show an error message.
					// The exception contains the error code and additional information if present.
					// The error message is provided in the exception.
					WorldLinePaymentService.log.error("Transaaction failed - " +
							ErrorMessages.getErrorMessage(event.getException().getResultCode(), aterminal));
				}
			}
		});
	}
	
	@Override
	public void systemInformationCompleted(TimEvent arg0,
			SystemInformationResponse arg1) {
		// Super calling systemInformationCompleted is not required. DefaultTerminalListener
		// has no implementation for this event.

		// Events usually originate from a different thread than the main thread. Always
		// use SwingUtilities.invokeLater to be safe
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WorldLinePaymentService.log.info("System information completed");
				setRequestInProgress(null);
			}
		});
	}

	
	@Override
	public void terminalStatusChanged(final Terminal terminal) {
		// Super calling terminalStatusChanged is not required. DefaultTerminalListener
		// has no implementation for this event.

		// Events usually originate from a different thread than the main thread. Always
		// use SwingUtilities.invokeLater to be safe
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WorldLinePaymentService.log.info("Terminal status changed");
				List<String> aconntentsList = terminal.getTerminalStatus().getDisplayContent();
				for(int i = 0; i < aconntentsList.size(); i++ )
				{
					WorldLinePaymentService.log.info(aconntentsList.get(i) );
				}
			}
		});
	}

	
	@Override
	public void requestAliasCompleted(TimEvent event, final String data) {

		// Events usually originate from a different thread than the main thread. Always
		// use SwingUtilities.invokeLater to be safe
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WorldLinePaymentService.log.info("RequestAlias completed");
				WorldLinePaymentService.log.info(data);
				setRequestInProgress(null);
			}
		});
	}
	
	@Override
	public void connectCompleted(TimEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Clear the request only if the user explicitly called
				// {@link Terminal#connectAsync()}.
				if (requestInProgress == RequestType.CONNECT) {
					WorldLinePaymentService.log.info("Connect completed");
					setRequestInProgress(null);
				}
			}
		});
	}

	@Override
	public void disconnected(Terminal terminal, TimException exception) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WorldLinePaymentService.log.info("Disconnected");
				setRequestInProgress(null);
			}
		});
	}
	
	@Override
	public void loginCompleted(TimEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Clear the request only if the user explicitly called
				// {@link Terminal#loginAsync()}.
				if (requestInProgress == RequestType.LOGIN) {
					WorldLinePaymentService.log.info("Login completed");
					setRequestInProgress(null);
				}
			}
		});
	}
	
	@Override
	public void logoutCompleted(TimEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Clear the request only if the user explicitly called
				// {@link Terminal#loginAsync()}.
				if (requestInProgress == RequestType.LOGOUT) {
					WorldLinePaymentService.log.info("Logout completed");
					setRequestInProgress(null);
				}
			}
		});
	}
	
	@Override
	public void activateCompleted(TimEvent event, final ActivateResponse data) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Clear the request only if the user explicitly called
				// {@link Terminal#activateAsync()}.
				if (requestInProgress == RequestType.ACTIVATE) {
					WorldLinePaymentService.log.info("Activate completed");
					WorldLinePaymentService.log.info(data);
					setRequestInProgress(null);
				}
			}
		});
	}

	@Override
	public void deactivateCompleted(TimEvent event, final DeactivateResponse data) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Clear the request only if the user explicitly called
				// {@link Terminal#deactivateAsync()}.
				if (requestInProgress == RequestType.DEACTIVATE) {
					WorldLinePaymentService.log.info("Deactivate completed");
					WorldLinePaymentService.log.info(data);
					setRequestInProgress(null);
				}
			}
		});
	}

	@Override
	public void commitCompleted(TimEvent event, PrintData data) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WorldLinePaymentService.log.info("Commit completed");
				setRequestInProgress(null);
			}
		});
	}
	

	@Override
	public void printReceipts(final Terminal terminal, final PrintData printData) {
		// Events usually originate from a different thread than the main thread. Always
		// use SwingUtilities.invokeLater to be safe
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Add here your implementation. All receipts come by here no matter
				// if print mode is set to receipt or fields. If fields are used
				// DefaultTerminalListener calls {@link DefaultTerminalListener#processPrintReceipts}
				// to create the final receipts using {@link Terminal#getReceiptFormatter}

				// As an example the receipts are logged and shown in a dialog
				for(Receipt receipt: printData.getReceipts()) {
					
					wl_Print.printAReceipt(receipt);
					
					WorldLinePaymentService.log.info(receipt.getRecipient().toString() + ":\n" + receipt.getValue());

				}
			}
		});
	}
	
	
	
	
	
}
