package phohawkenics.threads.listener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import phohawkenics.models.MessageModel;
import phohawkenics.models.PhotonConstants;
import phohawkenics.threads.manager.BaseServerManager;
import phohawkenics.util.Debugger;

public class ClientListenerThread extends BaseListenerThread {
	public final int CONNECT_TIMEOUT = 3000;
	public final int READ_TIMEOUT = 4000;
	public final int READ_CONTROLLER_TIMEOUT = 100;
	
	private BaseServerManager mServer;
	private InetSocketAddress mSocketAddress;
	private MessageModel mMessageModel;

	public ClientListenerThread(BaseServerManager _server,
									InetSocketAddress _socketAddress,
									MessageModel _messageModel) {
		super();
		mServer = _server;
		mSocketAddress = _socketAddress;
		mMessageModel = _messageModel;
		mSocket = new Socket();
	}
	
	public void connect() {
		try {
			int timeout = 0;
			if (mMessageModel.getPhotonModel().getTypeId() != PhotonConstants.TYPE_TV)
				timeout = READ_TIMEOUT;
			else 
				timeout = READ_CONTROLLER_TIMEOUT;
			mSocket.setSoTimeout(timeout);
			mSocket.connect(mSocketAddress, CONNECT_TIMEOUT);
			
		} catch (IOException e) {
			mServer.notificationAppClientConnectFail(mMessageModel);
			Debugger.log("Failure to connect to: " + mSocketAddress);
			Debugger.log("Failure to send: " + mMessageModel.toString());
			interrupt();
		}
	}
	
	/**
	 * Waits for messages from the client.
	 */
	public void run() {
		Debugger.log("Server Thread " + mId + " running.");
		connect();
		open();
		send(mMessageModel.serialize());
			
		while (!isInterrupted()) {
			if (mMessageModel.getPhotonModel().getTypeId() != PhotonConstants.TYPE_TV) {
				try {
					// Wait for messages from the module client
					String msg = mStreamIn.readLine();
					if (msg != null) {
						mServer.handleClientInput(this.mId, msg);
					} else {
						// msg is null when the socket was gracefully closed
						mServer.removeClient(mId);
						close();
						interrupt();
					}
				}
				catch (IOException ioe) {
					// Abrupt socket disconnection handling
					Debugger.log(mId + " ERROR reading: " + ioe.getMessage());
					mServer.removeClient(mId);
					close();
					interrupt();
				}
			} else {
				close();
				mServer.removeClient(mId);
				mServer.notificationAppClientActionSuccess(mMessageModel);
				interrupt();
			}
		}
	}
}