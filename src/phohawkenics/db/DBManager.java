package phohawkenics.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phohawkenics.models.PhotonModel;
import phohawkenics.util.Debugger;

public class DBManager {
	//private final String url = "https://Test.leo.com";
	// llanuzo --> MD5
	//private final String hash_key = "14c4b06b824ec593239362517f538b29";
	private DBConstants c = new DBConstants();
	private Connection conn = null;
	private Statement stmt = null;
    
	public DBManager () {
	}
	
	public void connect() {
	    	try {
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection("jdbc:sqlite:VCHomeAutomation.db");
				Debugger.log("Opened database successfully");
			} catch (ClassNotFoundException e) {
				Debugger.log( e.getClass().getName() + ": " + e.getMessage() );
			} catch (SQLException e) {
				Debugger.log( e.getClass().getName() + ": " + e.getMessage() );
			}
	 
	}
	
	public void createTables() {
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(c.CREATE_PHOTON_TABLE);
			stmt.close();
			Debugger.log("Table created successfully");
		} catch (SQLException e) {
			Debugger.log( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	public void insertToPhotonTable(String name, int type_id, String ip , int port) {
		try {
			stmt = conn.createStatement();
			String sql =
					c.Q_INSERT_INTO + c.CH_SPACE + c.N_PHOTON_TABLE + c.C_ALL_PHOTON
					+ c.Q_VALUES + c.CH_PSTART
					+ c.CH_QUOTE + name + c.CH_QUOTE + c.CH_COMMA + c.CH_SPACE
					+ type_id + c.CH_COMMA + c.CH_SPACE
					+ c.CH_QUOTE + ip + c.CH_QUOTE + c.CH_COMMA + c.CH_SPACE
					+ port
					+ c.CH_PEND;
			stmt.executeUpdate(sql);
			stmt.close();
			Debugger.log("Inserted successfully");
		} catch (SQLException e) {
			Debugger.log( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	
	public void updatePhotonTable(PhotonModel photonModel) {
		try {
			stmt = conn.createStatement();
			String sql =
					c.Q_UPDATE + c.CH_SPACE + c.N_PHOTON_TABLE + c.CH_SPACE
					+ c.Q_SET + c.CH_SPACE
					+ c.CN_PHOTON_STATUS + c.CH_EQUAL + c.CH_QUOTE + photonModel.getStatus() + c.CH_QUOTE + c.CH_COMMA + c.CH_SPACE
					+ c.CN_PHOTON_ID + c.CH_EQUAL + photonModel.getID() + c.CH_COMMA +  c.CH_SPACE
					+ c.CN_PHOTON_NAME + c.CH_EQUAL + c.CH_QUOTE + photonModel.getName() + c.CH_QUOTE + c.CH_COMMA + c.CH_SPACE
					+ c.CN_PHOTON_TYPEID + c.CH_EQUAL + photonModel.getTypeId() + c.CH_COMMA + c.CH_SPACE
					+ c.CN_PHOTON_IP + c.CH_EQUAL + c.CH_QUOTE + photonModel.getIp() + c.CH_QUOTE + c.CH_COMMA + c.CH_SPACE
					+ c.CN_PHOTON_PORT + c.CH_EQUAL + photonModel.getPort() + c.CH_COMMA + c.CH_SPACE
					+ c.CN_PHOTON_VALUE + c.CH_EQUAL + photonModel.getValue() + c.CH_COMMA + c.CH_SPACE
					+ c.CN_PHOTON_FREQ + c.CH_EQUAL + photonModel.getFrequency() + c.CH_COMMA + c.CH_SPACE
					+ c.CN_PHOTON_POWER + c.CH_EQUAL + photonModel.getPowerConsumption() + c.CH_SPACE
					+ c.Q_WHERE + c.CH_SPACE
					+ c.CN_PHOTON_ID + c.CH_EQUAL + photonModel.getID();
			//Debugger.log(sql);
			stmt.executeUpdate(sql);
			stmt.close();
			//Debugger.log("Inserted successfully");
		} catch (SQLException e) {
			Debugger.log( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
	
	public ResultSet getFromPhotonTable(int id) {
		try {
			stmt = conn.createStatement();
			String sql = 
					c.Q_SELECT + c.CH_SPACE
					+ c.CH_STAR + c.CH_SPACE
					+ c.Q_FROM + c.CH_SPACE + c.N_PHOTON_TABLE + c.CH_SPACE
					+ c.Q_WHERE + c.CH_SPACE
					+ c.CN_PHOTON_ID + c.CH_EQUAL + id;
			ResultSet rs = stmt.executeQuery(sql);
			//stmt.close();
			return rs;
		} catch (SQLException e) {
			Debugger.log( e.getClass().getName() + ": " + e.getMessage() );
		}
		return null;
	}
	
	public ResultSet getEntirePhotonTable() {
		try {
			stmt = conn.createStatement();
			String sql = 
					c.Q_SELECT + c.CH_SPACE
					+ c.CH_STAR + c.CH_SPACE
					+ c.Q_FROM + c.CH_SPACE + c.N_PHOTON_TABLE;
			ResultSet rs = stmt.executeQuery(sql);
			//stmt.close();
			return rs;
		} catch (SQLException e) {
			Debugger.log( e.getClass().getName() + ": " + e.getMessage() );
		}
		return null;
	}
	
	public boolean isConnected() {
		if (conn != null) return true;
		else return false;
	}
} 
