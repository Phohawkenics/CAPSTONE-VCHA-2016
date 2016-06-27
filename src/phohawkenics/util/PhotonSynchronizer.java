package phohawkenics.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import phohawkenics.db.DatabaseService;
import phohawkenics.db.PseudoDBManager;
import phohawkenics.models.PhotonConstants;
import phohawkenics.models.PhotonModel;
import phohawkenics.threads.listener.BaseListenerThread;


public class PhotonSynchronizer {
	private static int IP_RANGE_END = 255;
	private static int IP_RANGE_START = 0;
	private static int PHOTON_PORT = 9999;
	private static int CONNECT_TIMEOUT = 1000;
	private static int READ_TIMEOUT = 3000;
	private static List<PhotonModel> ips = new ArrayList<PhotonModel>();
	
	public PhotonSynchronizer () {
	}
	
	public List<PhotonModel> getUniqueAvailableIPs(String subnet) {
		Debugger.log("START");
		final ExecutorService es = Executors.newFixedThreadPool(255);
		Debugger.log("Searching for available IPs");
		for (int i = IP_RANGE_START; i < IP_RANGE_END;i++){
			String host= subnet + "." + i;
			portIsOpen(es, host);
		}
		es.shutdown();
		Debugger.log("DONE");
		return ips;
	}
	
	public Future<Boolean> portIsOpen(final ExecutorService es, final String host) {
		  return es.submit(new Callable<Boolean>() {
		      @Override public Boolean call() {
		    	  try {
		    		  Socket socket = new Socket();
					  // STEP 1: Attempt to connect to ip/port
					  socket.connect(new InetSocketAddress(host, PHOTON_PORT), CONNECT_TIMEOUT);
					  Debugger.log(host + ":" + PHOTON_PORT + " is connected.");
					  BaseListenerThread client = new BaseListenerThread(socket);
					  client.setSoTimeout(READ_TIMEOUT);
					  client.open();
					  // STEP 2: Prompt for its type
					  client.send(PhotonConstants.MSG_TYPE);
					  int typeId = Integer.valueOf(client.receive());
					  
					  client.close();
					  
					  if (PseudoDBManager.isValidTypeId(typeId)) {
						   
						  PhotonModel photonModel = new PhotonModel();
						  photonModel.setName(PseudoDBManager.getName(typeId));
						  photonModel.setTypeId(typeId);
						  photonModel.setIp(host);
						  photonModel.setPort(PHOTON_PORT);
						   
						  // STEP 3: Add photon object to the list
						  if (PseudoDBManager.isUniquePhoton(photonModel)) {
							   ips.add(photonModel);
							   DatabaseService.createNewPhotonInDB(photonModel);
							   PseudoDBManager.dBDataPull();
						  }
					  }
					  return true;
		    	  } catch (IOException e) {
						//Debugger.log("Failed connection for " + host + ":" + PHOTON_PORT);
						return false;
				  } catch(NumberFormatException nfe) {  
					    Debugger.log("Error: Typeid receive is not a number");  
					    return false;
				  }  
		      }
		   });
		}
}
