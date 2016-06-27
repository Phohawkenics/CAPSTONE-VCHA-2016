package phohawkenics.util.timers;

import java.util.Timer;

import phohawkenics.util.Debugger;

public class BaseTimerCustom {
	protected Timer mTimer = null;
	
	/**
	 * Resets timer if it exist
	 */
	public void cancelTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
		} else {
			Debugger.log("Failure to cancel: Timer is null");
		}
	}
}
