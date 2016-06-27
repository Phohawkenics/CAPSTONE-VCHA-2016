package phohawkenics.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import phohawkenics.models.PhotonModel;

public class DatabaseService {
	private static DBManager dbManager = new DBManager();
	private static DBConstants dbC = new DBConstants();
	
	public synchronized static List<PhotonModel> getAllPhotonInfo() {
		if (!dbManager.isConnected()) {
			dbManager.connect();
			dbManager.createTables();
		}
		
		ResultSet photonModelRS = dbManager.getEntirePhotonTable();
		List<PhotonModel> photonModels = new ArrayList<PhotonModel>();
		try {
			while (photonModelRS.next()) {
				PhotonModel photonModel = new PhotonModel();
				photonModel.setStatus(photonModelRS.getString(dbC.C_PHOTON_STATUS));
				photonModel.setID(photonModelRS.getInt(dbC.C_PHOTON_ID));
				photonModel.setName(photonModelRS.getString(dbC.C_PHOTON_NAME));
				photonModel.setTypeId(photonModelRS.getInt(dbC.C_PHOTON_TYPEID));
				photonModel.setIp(photonModelRS.getString(dbC.C_PHOTON_IP));
				photonModel.setPort(photonModelRS.getInt(dbC.C_PHOTON_PORT));
				photonModel.setValue(photonModelRS.getInt(dbC.C_PHOTON_VALUE));
				photonModel.setFrequency(photonModelRS.getInt(dbC.C_PHOTON_FREQ));
				photonModel.setPowerConsumption(photonModelRS.getFloat(dbC.C_PHOTON_POWER));
				//Debugger.log("Retrieved from DB: " + photonModel.toString());
				photonModels.add(photonModel);
			}
			return photonModels;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public synchronized static PhotonModel updatePhotonStatus(PhotonModel photonModel) {
		if (!dbManager.isConnected()) {
			dbManager.connect();
			dbManager.createTables();
		}
		
		dbManager.updatePhotonTable(photonModel);
		return photonModel;
	}
	
	public synchronized static PhotonModel createNewPhotonInDB(PhotonModel photonModel) {
		if (!dbManager.isConnected()) {
			dbManager.connect();
			dbManager.createTables();
		}
		dbManager.insertToPhotonTable(
				photonModel.getName(), photonModel.getTypeId(),
				photonModel.getIp(), photonModel.getPort());
		return photonModel;
	}
}
