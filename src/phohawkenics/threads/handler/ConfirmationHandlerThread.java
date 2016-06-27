package phohawkenics.threads.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import phohawkenics.models.MessageModel;
import phohawkenics.models.NonConfirmationModel;
import phohawkenics.threads.SemaphoreCustom;
import phohawkenics.util.Debugger;
import phohawkenics.util.timers.NonConfirmedTimerCustom;

public class ConfirmationHandlerThread extends Thread {
	private final int mMAX_NON_CONFIRM_BUFFER = 400;
	private final int mMAX_CONFIRM_BUFFER = 400;
	private final int mMAX_ATTEMPTS = 3;
	private final int mTIMEOUT_RESEND = 0;
	
	private InputHandlerThread mInputListenerThread;
	private SemaphoreCustom mSemaphore;
	private List<NonConfirmationModel> mNonConfirmBuffer;
	private List<MessageModel> mConfirmBuffer;
	
	public ConfirmationHandlerThread(InputHandlerThread _iListenerThread, SemaphoreCustom _semaphore) {
		mInputListenerThread = _iListenerThread;
		mSemaphore = _semaphore;
		mNonConfirmBuffer = Collections.synchronizedList(
				new ArrayList<NonConfirmationModel>(mMAX_NON_CONFIRM_BUFFER));
		mConfirmBuffer = Collections.synchronizedList(
				new ArrayList<MessageModel>(mMAX_CONFIRM_BUFFER));
	}
	
	/**
	 * Cross references the non confirmed buffer with the confirmed buffer
	 *  to verify which msgs have been confirmed
	 */
	public void run() {
		int nonConfirmPosition = -1;
		while (!isInterrupted()) {
			while(!mConfirmBuffer.isEmpty()) {
				for(int i = 0; i < mConfirmBuffer.size(); i++) {
					try {
						mSemaphore.release();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					nonConfirmPosition = searchByMsgId(mConfirmBuffer.get(i).getMessageId());
					Debugger.log("CONFIRMED: " + mConfirmBuffer.get(i).toString());
					confirm(nonConfirmPosition, i);
				}
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Adds a non-confirmed msg to the buffer
	 */
	public void addToNonConfirmBuffer(MessageModel msg) {
		if (mNonConfirmBuffer.size() < mMAX_NON_CONFIRM_BUFFER) {
			NonConfirmationModel model = new NonConfirmationModel();
			model.setMessage(msg);
			model.setTimer(new NonConfirmedTimerCustom(this, msg.getMessageId()), mTIMEOUT_RESEND);
			mNonConfirmBuffer.add(model);
		} else {
			Debugger.log("MAX NON CONFIRM BUFFER SIZE REACHED");
		}
	}
	
	/**
	 * Adds a confirmed msg to the buffer
	 */
	public void addToConfirmBuffer(MessageModel msg) {
		if (mConfirmBuffer.size() < mMAX_CONFIRM_BUFFER) {
			mConfirmBuffer.add(msg);
		} else {
			Debugger.log("MAX CONFIRM BUFFER SIZE REACHED");
		}
	}
	
	/**
	 * Confirms that an action has been completed and removes it from the queue
	 */
	public void confirm(int nonConfirm, int confirm) {
		if (nonConfirm != -1) {
			NonConfirmationModel obj = mNonConfirmBuffer.get(nonConfirm);
			obj.getTimer().cancelTimer();
			mNonConfirmBuffer.remove(nonConfirm);
			mConfirmBuffer.remove(confirm);
			Debugger.log("Confirmed: " + obj.getMessage().toString());
		} else {
			mConfirmBuffer.remove(confirm);
		}
	}
	
	/**
	 * Find the position of the NonConfirmationModel in the buffer
	 * @param msgId
	 * @return
	 */
	public int searchByMsgId(int msgId) {
		for(int i = 0; i < mNonConfirmBuffer.size(); i++) {
			if (mNonConfirmBuffer.get(i).getMessage().getMessageId() == msgId) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Resends an action if the number of attempts has not been exceeded
	 */
	public void resend(int msgId) {
		if (msgId != -1) {
			NonConfirmationModel msg = mNonConfirmBuffer.get(msgId);
			int numberOfAttempts = msg.getmAttempts();
			if (numberOfAttempts < mMAX_ATTEMPTS) {
				mNonConfirmBuffer.remove(msgId);
				msg.getTimer().setTimerMilli(mTIMEOUT_RESEND * numberOfAttempts);
				mNonConfirmBuffer.add(msg);
				msg.incrementAttempt();
				Debugger.log(msg.toString());
				mInputListenerThread.addToPriorityBuffer(msg.getMessage());
			} else {
				Debugger.log("ERASING MSG: " + msg.getMessage().toString());
				mNonConfirmBuffer.remove(msgId);
			}
		}
	}
}
