package phohawkenics.threads.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import phohawkenics.util.Debugger;

public class BaseListenerThread extends Thread{
	protected Socket mSocket;
	protected int mId = -1;
	protected BufferedReader mStreamIn;
	protected PrintWriter mStreamOut;
	
	public BaseListenerThread() {
		super();
	}
	
	public BaseListenerThread(Socket _socket) {
		super();
		mSocket = _socket;
		mId = mSocket.getPort();
	}
	
	public void send(String output) {
		Debugger.log("SENDING: " + output);
		mStreamOut.println(output);
		mStreamOut.flush();
	}
	
	public void setSoTimeout(int millisec) {
		try {
			mSocket.setSoTimeout(millisec);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public String receive() {
		try {
			String input = mStreamIn.readLine();
			if (input != null) {
				Debugger.log("Received: " + input);
				return input;
			}
			else
				return "DEFAULT";
		} catch (IOException e) {
			Debugger.log("Failure to receive");
			close();
		}
		return null;
	}
	
	public SocketAddress getSocketAddress() {
		return mSocket.getRemoteSocketAddress();
	}
	
	public int getID() {
		return mId;
	}
	
	public void open() {
		try {
		mStreamIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
		mStreamOut = new PrintWriter(mSocket.getOutputStream());
		} catch (IOException e) {
			Debugger.log("Error Opening Listener: " + e.getMessage());
		}
	}
	public void close() {
		try {
			if (mSocket != null)
			mSocket.close();
			if (mStreamIn != null)
				mStreamIn.close();
			if (mStreamOut != null)
				mStreamOut.close();
		} catch (IOException e) {
			Debugger.log("Error Closing Listener: " + e.getMessage());
		}
	}
}
