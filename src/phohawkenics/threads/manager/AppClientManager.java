package phohawkenics.threads.manager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import phohawkenics.db.PseudoDBManager;
import phohawkenics.models.PhotonModel;
import phohawkenics.models.MessageModel;
import phohawkenics.serial.DeserializationUtil;
import phohawkenics.threads.handler.InputHandlerThread;
import phohawkenics.threads.listener.AppListenerThread;
import phohawkenics.util.Debugger;

public class AppClientManager extends Thread{
	private BaseServerManager mServer;
	private int mPort; 
	private ServerSocket mAppClientManager;
	private InputHandlerThread mInputListener;
	private AppListenerThread mAppClients[] = new AppListenerThread[5];
	private int mAppClientCount;
	private Thread mThread = null;
	private Socket mSocket = null;
	
	public AppClientManager (BaseServerManager _server, int _port) {
		try {
		mServer = _server;
		mPort = _port;
		mAppClientManager = new ServerSocket(mPort);
		mInputListener = mServer.getInputListener();
		} catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void run() {
		while (!isInterrupted()) {
			try {
				if (mAppClientCount != 0) {
					Debugger.log("Active connections");
					for(int i = 0; i < mAppClientCount; i++)
						Debugger.log(String.valueOf(mAppClients[i].getID()));
				} else {
					Debugger.log("Waiting for an app client ...");
				}
				mSocket = mAppClientManager.accept();
				mThread = new Thread(new Runnable() {
				     public void run() {
				         addAppClientThread(mSocket);
				     }
				});  
				mThread.start();
			} catch (IOException e) {
				Debugger.log("App connection error");
				Debugger.log("IOException: " + e.getMessage());
			}
		}
	}

	/**
	 * Handles what to do with the input messages.
	 * @param input
	 */
	public synchronized void handleClientInput(int ID, String sInput) {
		Debugger.log("RECEIVED FROM APP: " + sInput);
		AppListenerThread appClient = getAppClientListener(ID);
		if (sInput.equals("SERVER")){
			appClient.send("SERVER");
		} else if (sInput.equals("EXTIP")){
			try {
				URL whatismyip = new URL("http://checkip.amazonaws.com");
				BufferedReader in = new BufferedReader(new InputStreamReader(
				                whatismyip.openStream()));

				String ip = in.readLine(); //you get the IP as a String
				appClient.send(ip);
			} catch (IOException e) {
				Debugger.log("Probably no internet connection. Can't connect to http://checkip.amazonaws.com");
				appClient.send("000.0.0.0");
			}
		} else {
			PhotonModel input = DeserializationUtil.deserializeAppMessageModel(sInput);
			
			if (input.getID() == -2) {
				appClient.send(PseudoDBManager.getSerializedAllPhotonInfo());
				PseudoDBManager.renewAvailability();
			} else if (input.getPowerConsumption() == -1) {
				PhotonModel srcPhotonModel = PseudoDBManager.getPhotonByName(input.getName());
				mInputListener.addToBuffer(ID,
						new PhotonModel(
								srcPhotonModel.getStatus(),
								srcPhotonModel.getID(),
								input.getName(),
								input.getTypeId(),
								srcPhotonModel.getIp(),
								srcPhotonModel.getPort(),
								input.getValue(),
								srcPhotonModel.getFrequency(),
								srcPhotonModel.getPowerConsumption()
								));
			} else {
				mInputListener.addToBuffer(ID, input);
			}
		}
	}
	
	/**
	 * Handles what to do with the input messages.
	 * @param output
	 */
	public synchronized void handleClientOutput(int ID, PhotonModel output) {
		AppListenerThread appClient = getAppClientListener(ID);
		if (appClient != null) {
			appClient.send(output.serialize());
		} else {
			Debugger.log("Unable to find appClient");
		}
	}
	
	/**
	 * Removes a thread and closes its corresponding sockets by their ID.
	 * @param ID
	 */
	public boolean removeAppClient(int ID) {
		int pos = findAppClient(ID);
		if (pos >= 0) {
			AppListenerThread toTerminate = mAppClients[pos];
			Debugger.log("Removing app client thread " + ID + " at " + pos);
			if (pos < mAppClientCount - 1)
				for (int i = pos + 1; i < mAppClientCount; i++)
					mAppClients[i - 1] = mAppClients[i];
			mAppClientCount--;
			toTerminate.close();
			toTerminate.interrupt();
			
			return true;
		} else {
			return false;
		}
	}

	public int getAppClientCount() {
		return mAppClientCount;
	}
	
	public void addActionToInputBuffer (MessageModel msg) {
		mServer.getInputListener().addToBuffer(msg);
	}
	
	public AppListenerThread getAppClientListener(int ID) {
		int position = findAppClient(ID);
		if (position != -1)
			return mAppClients[findAppClient(ID)];
		else
			return null;
	}
	
	/**
	 * Finds a client by their ID.
	 * @param ID
	 * @return
	 */
	private int findAppClient(int ID) {
		for (int i = 0; i < mAppClientCount; i++)
			if (mAppClients[i].getID() == ID)
				return i;
		return -1;
	}
	
	/**
	 * Adds a listener thread for a client socket.
	 * @param socket
	 */
	private void addAppClientThread(Socket socket) {
		if (mAppClientCount < mAppClients.length) {
			Debugger.log("App Client Connection Established: " + socket);
			mAppClients[mAppClientCount] = new AppListenerThread(this, socket); // @TO DO
			mAppClients[mAppClientCount].open();
			mAppClients[mAppClientCount].start();
			mAppClientCount++;
		} else {
			Debugger.log("Client refused: maximum " + mAppClients.length
					+ " reached.");
			Debugger.log("Removing all clients");
			for (int i = 0; i < mAppClients.length; i++) {
				removeAppClient(mAppClients[i].getID());
			}
		}
	}
}
