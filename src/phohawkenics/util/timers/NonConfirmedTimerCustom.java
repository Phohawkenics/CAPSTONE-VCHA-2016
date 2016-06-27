package phohawkenics.util.timers;

import java.util.Timer;
import java.util.TimerTask;

import phohawkenics.threads.handler.ConfirmationHandlerThread;
import phohawkenics.util.Debugger;

public class NonConfirmedTimerCustom extends BaseTimerCustom{
	private ConfirmationHandlerThread mThread;
	private int mMsgId = 0;
	private int mWaitingPeriod = 0;
	
	public NonConfirmedTimerCustom(ConfirmationHandlerThread _thread, int msgId) {
		super();
		mThread = _thread;
		mMsgId = msgId;
	}
	
	/**
	 * Starts a new timer
	 * @param milliSec
	 */
	
	public void setTimerMilli(int milliSec) {
		mWaitingPeriod = milliSec;
		mTimer = new Timer();
		if (mWaitingPeriod != 0) {
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
						Debugger.log("Timeout: No confirmation for " + mWaitingPeriod + "ms");
						mThread.resend(mThread.searchByMsgId(mMsgId));
				}
			}, mWaitingPeriod);
		}
	}
}
