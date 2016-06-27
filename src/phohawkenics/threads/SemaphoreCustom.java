package phohawkenics.threads;

public class SemaphoreCustom {
	private boolean mSignal = false;

	public synchronized void take() {
		this.mSignal = true;
		this.notify();
	}

	public synchronized void release() throws InterruptedException {
		while (!this.mSignal) {
			wait();
		}
		this.mSignal = false;
	}
}