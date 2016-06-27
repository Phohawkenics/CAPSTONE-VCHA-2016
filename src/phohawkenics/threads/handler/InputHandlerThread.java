package phohawkenics.threads.handler;

import java.util.LinkedList;
import java.util.Queue;

import phohawkenics.models.MessageModel;
import phohawkenics.models.PhotonModel;
import phohawkenics.threads.SemaphoreCustom;
import phohawkenics.threads.manager.BaseServerManager;
import phohawkenics.util.Debugger;

public class InputHandlerThread extends Thread {
	private final int mPRIORITY_BUFFER_CAPACITY = 100;
	private final int mBUFFER_CAPACITY = 400;
	
	private BaseServerManager mServer;
	private Queue<MessageModel> mPriorityBufferCommand; 
	private Queue<MessageModel> mBufferCommand; 
	private MessageModel mCurrentAction;
	private ConfirmationHandlerThread mConfirmationListThread;
	private SemaphoreCustom mSemaphore;
	
	public InputHandlerThread (BaseServerManager _server, SemaphoreCustom _inputSem, SemaphoreCustom _confirmSem) {
		mServer = _server;
		mPriorityBufferCommand = new LinkedList<MessageModel>();
		mBufferCommand = new LinkedList<MessageModel>();
		mSemaphore = _inputSem;
		if (_confirmSem != null) {
			mConfirmationListThread = new ConfirmationHandlerThread(this, _confirmSem);
			mConfirmationListThread.start();
		}
	}
	
	public void run() {
		while (!isInterrupted()) {
			try {
				mSemaphore.release();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			while (bufferIsNotEmpty()) {
				mCurrentAction = getNextAction();
				mServer.handleOutput(mCurrentAction);
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public MessageModel getNextAction() {
		MessageModel nextAction = null;
		if (mPriorityBufferCommand.isEmpty()) nextAction = mBufferCommand.poll();
		else nextAction = mPriorityBufferCommand.poll();
		Debugger.log("InputListener: Starting Command: " + nextAction);
		return nextAction;
	}
	
	public synchronized boolean bufferIsNotEmpty() {
		if (mBufferCommand.peek() != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized void addToBuffer(int appClientId, PhotonModel photonModel) {
			if (mBufferCommand.size() < mBUFFER_CAPACITY) {
				MessageModel msg = new MessageModel();
				msg.setAppClientId(appClientId);
				msg.setPhotonModel(photonModel);
				mConfirmationListThread.addToNonConfirmBuffer(msg);
				mBufferCommand.add(msg);
			}
			else
				Debugger.log("MAX BUFFER CAPACITY");
	}
	
	public synchronized void addToBuffer(MessageModel msg) {
		if (mBufferCommand.size() < mBUFFER_CAPACITY) {
			mBufferCommand.add(msg);
		}
	}
	
	public synchronized void addToPriorityBuffer(MessageModel msg) {
		if (mPriorityBufferCommand.size() < mPRIORITY_BUFFER_CAPACITY) {
			mPriorityBufferCommand.add(msg);
		}
	}
	
	public void addConfirmMessage (MessageModel msg) {
		mConfirmationListThread.addToConfirmBuffer(msg);
	}
}
