package phohawkenics.db;

public class DBConstants {
	public final String CH_SPACE = " ";
	public final String CH_PSTART = "(";
	public final String CH_PEND = ")";
	public final String CH_COMMA = ",";
	public final String CH_QUOTE = "'";
	public final String CH_EQUAL = "=";
	public final String CH_STAR = "*";
	
	public final String Q_CREATE_TABLE = "CREATE TABLE";
	public final String Q_INSERT_INTO = "INSERT INTO";
	public final String Q_VALUES = "VALUES";
	public final String Q_UPDATE = "UPDATE";
	public final String Q_SET = "SET";
	public final String Q_WHERE = "WHERE";
	public final String Q_SELECT = "SELECT";
	public final String Q_FROM = "FROM";
	public final String Q_IF_NOT_EXISTS = "IF NOT EXISTS";
	
	public final String N_PHOTON_TABLE = "PHOTON";
	public final String CN_PHOTON_STATUS = "status";
	public final String CN_PHOTON_ID = "id";
	public final String CN_PHOTON_NAME = "name";
	public final String CN_PHOTON_TYPEID = "type_id";
	public final String CN_PHOTON_IP = "ip";
	public final String CN_PHOTON_PORT = "port";
	public final String CN_PHOTON_VALUE = "value";
	public final String CN_PHOTON_FREQ = "frequency";
	public final String CN_PHOTON_POWER = "power_consumption";
	
	public final String C_ALL_PHOTON = "(name, type_id, ip, port)";
	
	public final int C_PHOTON_STATUS = 1;
	public final int C_PHOTON_ID = 2;
	public final int C_PHOTON_NAME = 3;
	public final int C_PHOTON_TYPEID = 4;
	public final int C_PHOTON_IP = 5;
	public final int C_PHOTON_PORT = 6;
	public final int C_PHOTON_VALUE = 7;
	public final int C_PHOTON_FREQ = 8;
	public final int C_PHOTON_POWER = 9;
	
	public final String CREATE_PHOTON_TABLE =
			Q_CREATE_TABLE + CH_SPACE + Q_IF_NOT_EXISTS + CH_SPACE + N_PHOTON_TABLE
			+ CH_PSTART
			+ "status CHAR(10) DEFAULT 'true'" + CH_COMMA + CH_SPACE
			+ "id INTEGER PRIMARY KEY AUTOINCREMENT" + CH_COMMA + CH_SPACE
			+ "name CHAR(50)" + CH_COMMA + CH_SPACE
			+ "type_id INTEGER NOT NULL" + CH_COMMA + CH_SPACE
            + "ip CHAR(20) NOT NULL" + CH_COMMA + CH_SPACE
            + "port INTEGER NOT NULL" + CH_COMMA + CH_SPACE
            + "value INTEGER DEFAULT 0" + CH_COMMA + CH_SPACE
            + "frequency INTEGER DEFAULT 60" + CH_COMMA + CH_SPACE
            + "power_consumption FLOAT DEFAULT 0"
            + CH_PEND;
	
}