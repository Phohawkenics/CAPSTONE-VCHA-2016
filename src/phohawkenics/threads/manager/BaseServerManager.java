package phohawkenics.threads.manager;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import phohawkenics.db.PseudoDBManager;
import phohawkenics.models.PhotonConstants;
import phohawkenics.models.PhotonModel;
import phohawkenics.models.MessageModel;
import phohawkenics.serial.DeserializationUtil;
import phohawkenics.threads.SemaphoreCustom;
import phohawkenics.threads.handler.InputHandlerThread;
import phohawkenics.threads.listener.ClientListenerThread;
import phohawkenics.util.Debugger;

public class BaseServerManager implements Runnable {
	private ClientListenerThread mClients[] = new ClientListenerThread[20];
	private AppClientManager mAppClientManager = null;
	private int mAppClientManagerPort = 9999;
	private InputHandlerThread mInputListener;
	private Thread mThread = null;
	private int mClientCount = 0;
	private SemaphoreCustom mInputListenerSem;
	private SemaphoreCustom mConfirmListenerSem;
	private int messagesReceived = 0;
	private int messagesConfirmed = 0;
	
	public BaseServerManager(int appClientPort) {
		mAppClientManagerPort = appClientPort;
		start();
	}
	
	/**
	 *  Unblocks the other threads
	 */
	public void run() {
		while (mThread != null) {
			// Only lets the confirmationListenerRun if there are clients
			if (mClientCount > 0) mConfirmListenerSem.take();
			if (mAppClientManager.getAppClientCount() > 0) mInputListenerSem.take();
			
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void start() {
		if (mThread == null) {
			mThread = new Thread(this);
			
			PseudoDBManager.init("");
			
			mInputListenerSem = new SemaphoreCustom();
			mConfirmListenerSem = new SemaphoreCustom();
			
			mInputListener =  new InputHandlerThread(this, mInputListenerSem, mConfirmListenerSem);
			
			mAppClientManager = new AppClientManager(this, mAppClientManagerPort);
			mAppClientManager.start();

			mInputListener.start();
			mThread.start();
		}
	}

	public void stop() {
		if (mThread != null) {
			mAppClientManager.interrupt();
			mThread.interrupt();
			mThread = null;
		}
	}
	
	public InputHandlerThread getInputListener() {
		return mInputListener;
	}
	
	public void notificationAppClientConnectFail(MessageModel msgModel) {
		PhotonModel photonModel = msgModel.getPhotonModel();
		photonModel.setStatus(PhotonConstants.STATUS_FALSE);
		mAppClientManager.handleClientOutput(msgModel.getAppClientId(), photonModel);
	}
	
	public void notificationAppClientActionSuccess(MessageModel msgModel) {
		PhotonModel photonModel = msgModel.getPhotonModel();
		photonModel.setStatus(PhotonConstants.STATUS_TRUE);
		mAppClientManager.handleClientOutput(msgModel.getAppClientId(), photonModel);
	}
	
	/**
	 * Handles what to do with the input messages.
	 * @param input
	 */
	public synchronized void handleClientInput(int ID, String input) {
		Debugger.log("RECEIVED FROM PHOTON: " + input);
		MessageModel messageModel = DeserializationUtil.deserializeMessage(input);
		if (messageModel != null) {
			PhotonModel photonModel = PseudoDBManager.updatePhoton(messageModel.getPhotonModel());
			if (photonModel != null) {
				mInputListener.addConfirmMessage(messageModel);
				messagesConfirmed++;
				mAppClientManager.handleClientOutput(messageModel.getAppClientId(), photonModel);
				PseudoDBManager.dBDataPush();
			} else {
				Debugger.log("Photon Module not Found");
			}
		} else {
			Debugger.log("Client input cannot be interpreted");
		}
	}
	
	/**
	 * Handles what to do with the input messages.
	 * @param input
	 */
	public synchronized void handleOutput(MessageModel output) {
		messagesReceived++;
		Debugger.logForMessageCount("Messages Received: " + messagesReceived);
		Debugger.logForMessageCount("Messages Confirmed: " + messagesConfirmed);
		PhotonModel photonModel = output.getPhotonModel();
		
		if (photonModel.getTypeId() == PhotonConstants.TYPE_DOOR
						&& photonModel.getValue() == 0) {
			// Trying to unlock a door
			// Can't unlock door. Thus, tell the app that it failed
			photonModel.setStatus(PhotonConstants.STATUS_FALSE);
			mAppClientManager.handleClientOutput(output.getAppClientId(), photonModel);
		} else {
			InetSocketAddress socketAddress = 
					new InetSocketAddress(photonModel.getIp(), photonModel.getPort());
			int iD = findClientBySocketAddress(socketAddress);
			
			if (iD != -1) {
				// If client thread already exist, send
				String msg = output.serialize();
				mClients[findClient(iD)].send(msg);
			} else {
				// Add a thread for the client
				addClientThread(socketAddress, output);
			}
		}
	}
	
	/**
	 * Removes a thread and closes its corresponding sockets by their ID.
	 * @param ID
	 */
	public void removeClient(int ID) {
		int pos = findClient(ID);
		if (pos >= 0) {
			ClientListenerThread toTerminate = mClients[pos];
			Debugger.log("Removing client thread " + ID + " at " + pos);
			if (pos < mClientCount - 1)
				for (int i = pos + 1; i < mClientCount; i++)
					mClients[i - 1] = mClients[i];
			mClientCount--;
			toTerminate.close();
			toTerminate.interrupt();
		}
	}
	
	/**
	 * Adds a listener thread for a client socket.
	 * @param socket
	 */
	private void addClientThread(InetSocketAddress socketAddress, MessageModel msgModel) {
		if (mClientCount < mClients.length) {
			Debugger.log("Client Connection Established: " + socketAddress);
			mClients[mClientCount] = new ClientListenerThread(this, socketAddress, msgModel);
			mClients[mClientCount].start();
			mClientCount++;
		} else
			Debugger.log("Client refused: maximum " + mClients.length
					+ " reached.");
	}
	
	/**
	 * Finds a client by their ID.
	 * @param ID
	 * @return
	 */
	private int findClient(int ID) {
		for (int i = 0; i < mClientCount; i++)
			if (mClients[i].getID() == ID)
				return i;
		return -1;
	}
	
	private int findClientBySocketAddress(SocketAddress socketAddress) {
		int iD = -1;
		for (int i = 0; i < mClientCount; i++) {
			if (socketAddress.equals(mClients[i].getSocketAddress())) {
				iD = mClients[i].getID();
				return iD;
			}
		}
		return iD;
	}
}