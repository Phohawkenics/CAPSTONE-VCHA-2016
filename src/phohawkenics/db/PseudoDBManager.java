package phohawkenics.db;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import phohawkenics.models.PhotonConstants;
import phohawkenics.models.PhotonModel;
import phohawkenics.threads.listener.BaseListenerThread;
import phohawkenics.util.Debugger;
//import phohawkenics.util.Debugger;
import phohawkenics.util.PhotonSynchronizer;

public class PseudoDBManager{
	public final static int READ_TIMEOUT = 700;
	public final static int CONNECT_TIMEOUT = 700;
	public final static String DATA_SEPERATOR = "-";
	
	private static String SUBNET = "192.168.0";
	private static List<PhotonModel> mPhotonModels;
	
	/**
	 * Init
	 * @param subnet
	 */
	public static void init(String subnet) {
		if (subnet.isEmpty()){}
		else SUBNET = subnet;
		mPhotonModels =  new ArrayList<PhotonModel>();
		// Pull data from db
		dBDataPull();
		PhotonSynchronizer synch  = new PhotonSynchronizer();
		synch.getUniqueAvailableIPs(SUBNET);
	}
	
	public synchronized static String getSerializedAllPhotonInfo() {
		String serialized = "";
		for (int i = 0; i < mPhotonModels.size(); i ++) {
			serialized += mPhotonModels.get(i).serialize();
			if (i != mPhotonModels.size() - 1) {
				serialized += DATA_SEPERATOR;
			}
		}
		return serialized;
	}
	
	public static PhotonModel updatePhoton(PhotonModel photonModel) {
		
		int position = findPhotonByName(photonModel.getName());
		
		if (position == -1)
			position = findPhotonById(photonModel.getID());
		
		if (position != -1) {
			PhotonModel temp = mPhotonModels.get(position);
			temp.setStatus("true");
			temp.setName(photonModel.getName());
			temp.setValue(photonModel.getValue());
			temp.setFrequency(photonModel.getFrequency());
			temp.setPowerConsumption(photonModel.getPowerConsumption());
			mPhotonModels.set(position, temp);
			return mPhotonModels.get(position);
		}
		return null;
	}
	
	public static PhotonModel getPhotonByName(String name) {
		int position = findPhotonByName(name);
		if (position != -1) {
			return mPhotonModels.get(position);
		}
		return null;
	}
	
	public static void dBDataPull() {
		Debugger.log("Pulling Data");
		mPhotonModels = DatabaseService.getAllPhotonInfo();
	}
	
	public static void dBDataPush() {
		Debugger.log("Pushing Data");
		for (PhotonModel photonModel: mPhotonModels) {
			DatabaseService.updatePhotonStatus(photonModel);
		}
	}
	
	/**
	 * Verifies whether or not a photon already exist in the db
	 * Conditions: Photons are NOT unique only if their type, ip and port are the same
	 * @param photonModel
	 * @return
	 */
	
	public static boolean isUniquePhoton(PhotonModel photonModel) {
		for (PhotonModel cPhotonModel: mPhotonModels) {
			if (photonModel.equalsV2(cPhotonModel)) {
				cPhotonModel.setStatus("true");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 	Generates the name for new photons
	 * @param typeid
	 * @return
	 */
	
	public static String getName(int typeid) {
		List<String> names = new ArrayList<String>();
		for (PhotonModel cPhotonModel: mPhotonModels) {
			if (cPhotonModel.getTypeId() == typeid && cPhotonModel.getName().length() == 3) {
				names.add(cPhotonModel.getName());
			}
		}
		String prefix = "";
		if (typeid == PhotonConstants.TYPE_LIGHT) {
			prefix = PhotonConstants.NAME_PRE_LIGHT;
		} else if (typeid == PhotonConstants.TYPE_HEATER) {
			prefix = PhotonConstants.NAME_PRE_HEATER;
		} else if (typeid == PhotonConstants.TYPE_DOOR) {
			prefix = PhotonConstants.NAME_PRE_DOOR;
		} else if (typeid == PhotonConstants.TYPE_TV) {
			prefix = PhotonConstants.NAME_PRE_TV;
		}
		
		List<Integer> numbers = new ArrayList<Integer>();
		for (String name: names) {
			String info[] = name.split(PhotonConstants.C_SPACE);
			if (info[0].equals(prefix)) 
				numbers.add(Integer.valueOf(info[1]));
		}
		int i = 1;
		for (int number: numbers) { 
			if (i != number)break;
			else i++;  
		}
		String name = prefix + PhotonConstants.C_SPACE + i;
		
		return name;
	}
	
	/**
	 * 	1 Light - 2 Heater - 3 Door Lock - 4 TV
	 * @param typeid
	 * @return
	 */
	public static boolean isValidTypeId(int typeid) {
		if ( typeid != PhotonConstants.TYPE_LIGHT
				&& typeid != PhotonConstants.TYPE_HEATER
				&& typeid != PhotonConstants.TYPE_DOOR
				&& typeid != PhotonConstants.TYPE_TV) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Attempts to connect with photons and updates their availability
	 */
	public synchronized static void renewAvailability() {
		Debugger.log("RenewAvailability");
		final ExecutorService es = Executors.newFixedThreadPool(4);
		for (PhotonModel photonModel: mPhotonModels) {
			isAvailable(es, photonModel);
		}
		es.shutdown();
	}
	
	public static Future<Boolean> isAvailable(final ExecutorService es, final PhotonModel photonModel) {
		return es.submit(new Callable<Boolean>() {
	    @SuppressWarnings("resource")
		@Override public Boolean call() {
	    	
	        	Socket socket = new Socket();
	        	try {
	   			   socket.connect(new InetSocketAddress(photonModel.getIp(), photonModel.getPort()), CONNECT_TIMEOUT);
	   			   photonModel.setStatus(PhotonConstants.STATUS_TRUE);
	   			   BaseListenerThread client = new BaseListenerThread(socket);
	   			   client.setSoTimeout(READ_TIMEOUT);
	   			   client.open();
	   			   
	   			   // STEP 2: Verify that it is indeed a photon by requesting its typeId
	   			   client.send(PhotonConstants.MSG_AVAIL);
	   			   String info = client.receive();
	   			   Debugger.log(photonModel.getIp() + ": " + info);
	   			   client.close();
	   			   if (info != null) {
		   			   String infoArray[] = info.split(";");
		   			   String type = infoArray[0];
		   			   if (!type.equals(String.valueOf(photonModel.getTypeId()))) {
		   				   photonModel.setStatus(PhotonConstants.STATUS_FALSE);
		   			   } 
		   			   String doorStatus = "";
		   			   if (infoArray.length == 2 && photonModel.getTypeId() == 3) {
		   				   // If its a door
		   				   doorStatus = infoArray[1];
		   				   photonModel.setValue(Integer.parseInt(doorStatus));
		   			   }
		   			   return true;
	   			   }
	   			   return false;
	   			} catch (IOException e) {
	   				//Photon Isn't connected
	   				photonModel.setStatus(PhotonConstants.STATUS_FALSE);
	   			    return false;
	   			}
	    	}
		});
	}
	
	private static int findPhotonByName(String name) {
		for (int i = 0; i < mPhotonModels.size(); i++) {
			if (name.equalsIgnoreCase(mPhotonModels.get(i).getName())) {
				return i;
			}
		}
		return -1;
	}
	
	private static int findPhotonById(int id) {
		for (int i = 0; i < mPhotonModels.size(); i++) {
			if (id == mPhotonModels.get(i).getID()) {
				return i;
			}
		}
		return -1;
	}
}
