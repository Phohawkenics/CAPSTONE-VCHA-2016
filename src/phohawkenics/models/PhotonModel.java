package phohawkenics.models;


public class PhotonModel{
	public final static String SEPARATOR = ";";
	private String mStatus;
	private int mId;
	private String mName;
	private int mTypeId;
	private String mIp;
	private int mPort;
	private int mValue;
	private int mFrequency;
	private float mPowerConsumption;
	
	//default constructor
	public PhotonModel(){
		// Do nothing
	}
	
	public PhotonModel (String photonStatus, int photonId, String photonName, int photonTypeId,
							String ip, int port, int value, int frequency, float powerConsumption) {
		mStatus = photonStatus;
		mId = photonId;
		mName = photonName;
		mTypeId = photonTypeId;
		mIp = ip;
		mPort = port;
		mValue = value;
		mFrequency = frequency;
		mPowerConsumption = powerConsumption;
	}
	
	public String getStatus() {	return mStatus;}
	public void setStatus(String status) { mStatus = status;}
	public int getID() { return mId;}
	public void setID(int id) {	mId = id;}
	public String getName() { return mName; }
	public void setName(String name) { mName = name; }
	public int getTypeId() { return mTypeId; }
	public void setTypeId(int typeId) {	mTypeId = typeId;}
	public String getIp() { return mIp; }
	public void setIp(String ip) { mIp = ip;}
	public int getPort() { return mPort; }
	public void setPort(int port) { mPort = port;}
	public int getValue() {return mValue;}
	public void setValue(int value) { mValue = value; }
	public int getFrequency() {return mFrequency;}
	public void setFrequency(int frequency) { mFrequency = frequency; }
	public float getPowerConsumption() { return mPowerConsumption;}
	public void setPowerConsumption(float powerConsumption) { mPowerConsumption = powerConsumption;}
	
	public boolean equalsV2(PhotonModel photonModel) {
		if (photonModel.getTypeId() != mTypeId
				|| !photonModel.getIp().equals(mIp)
				|| photonModel.getPort() != mPort) {
			return false;
		}
		return true;
	}
	
	public String serialize(){
		String homeModule = 
			mStatus + SEPARATOR
			+ mId + SEPARATOR
			+ mName + SEPARATOR
			+ mTypeId + SEPARATOR
			+ mIp + SEPARATOR
			+ mPort + SEPARATOR
			+ mValue + SEPARATOR
			+ mFrequency + SEPARATOR
			+ mPowerConsumption;
		return homeModule;
	}

	@Override
	public String toString() {
		return "PhotonModel [mStatus=" + mStatus + ", mId=" + mId + ", mName="
				+ mName + ", mTypeId=" + mTypeId + ", mIp=" + mIp + ", mPort="
				+ mPort + ", mValue=" + mValue + ", mFrequency=" + mFrequency
				+ ", mPowerConsumption=" + mPowerConsumption + "]";
	}
}