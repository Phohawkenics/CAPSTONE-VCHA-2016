package phohawkenics.threads.listener;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;

import phohawkenics.models.PhotonModel;
import phohawkenics.threads.manager.AppClientManager;
import phohawkenics.util.Debugger;

public class AppListenerThread extends BaseListenerThread {
	private AppClientManager mAppClientManager;
	private PhotonModel photonModel;
	private Timer mTimer;
	
	public AppListenerThread(AppClientManager _appClientManager, Socket _socket) {
		super(_socket);
		mAppClientManager = _appClientManager;
		Debugger.log("Creating AppListenerThread");
	}
	
	/**
	 * Waits for messages from the client.
	 */
	public void run() {
		Debugger.log("Server Thread " + mId + " running.");
		while (!isInterrupted()) {
			try {
				setEmergencyTimeout();
				String msg = mStreamIn.readLine();
				cancelEmergencyTimeout();
				if (msg != null) {
					mAppClientManager.handleClientInput(mId, msg);
				} else {
					// Graceful close
					mAppClientManager.removeAppClient(mId);
					close();
					interrupt();
				}
			} catch (IOException ioe) {
				Debugger.log(mId + " ERROR reading: " + ioe.getMessage());
				mAppClientManager.removeAppClient(mId);
				close();
				interrupt();
			}
		}
	}
	
	public void setEmergencyTimeout() {
		mTimer = new java.util.Timer();
		mTimer.schedule( 
			        new java.util.TimerTask() {
			            @Override
			            public void run() {
			            	if (photonModel != null) {
			            		photonModel.setStatus("false");
			                	send(photonModel.serialize());
			            	} else {
			            		send("fail");
			            		mAppClientManager.removeAppClient(mId);
								close();
								interrupt();
			            	}
			            }
			        }, 
			        8000 
			);
	}
	
	public void cancelEmergencyTimeout() {
		mTimer.cancel();
		mTimer = null;
	}
}