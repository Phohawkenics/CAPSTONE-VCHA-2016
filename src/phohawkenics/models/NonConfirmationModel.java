package phohawkenics.models;

import phohawkenics.util.timers.NonConfirmedTimerCustom;

public class NonConfirmationModel {
	private NonConfirmedTimerCustom mTimer;
	private MessageModel mMessage;
	private int mAttempts = 0;

	public NonConfirmedTimerCustom getTimer() {
		return mTimer;
	}

	public void setTimer(NonConfirmedTimerCustom mTimer, int milli) {
		this.mTimer = mTimer;
		this.mTimer.setTimerMilli(milli);
	}

	public MessageModel getMessage() {
		return mMessage;
	}

	public void setMessage(MessageModel mMessage) {
		this.mMessage = mMessage;
	}

	public int getmAttempts() {
		return mAttempts;
	}
	
	public void incrementAttempt() {
		++mAttempts;
	}

	@Override
	public String toString() {
		return "NonConfirmationModel [" + "mMessage="
				+ mMessage.toString() + ", mAttempts=" + mAttempts + "]";
	}
}
