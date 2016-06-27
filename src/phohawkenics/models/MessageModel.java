package phohawkenics.models;

public class MessageModel {
	public static String mDATA_SEPERATOR = "-";
	public static String mIP_SEPERATOR = ":";
	private static int mLastUsedId = -1;
	private int mMessageId;
	private int mAppClientThreadId;
	private PhotonModel mPhotonModel;
	
	public MessageModel (int messageId, int appClientThreadId, PhotonModel photonModel) {
		mMessageId = messageId;
		mAppClientThreadId = appClientThreadId;
		mPhotonModel = photonModel; 
	}
	
	public MessageModel () {
		mMessageId = getNextMessageId();
		mPhotonModel = null;
	}
	
	public static int getNextMessageId() {
		int nextId = ++mLastUsedId;
		if (nextId < 256) { mLastUsedId = nextId; return nextId; }
		else { mLastUsedId = 0; return 0; }
	}
	public int getMessageId() { return mMessageId; }
	public int getAppClientId() { return mAppClientThreadId; }
	public void setAppClientId(int mAppClientId) { this.mAppClientThreadId = mAppClientId; }
	public PhotonModel getPhotonModel() { return mPhotonModel; }
	public void setPhotonModel(PhotonModel photonModel) { mPhotonModel = photonModel;}

	@Override
	public String toString() {
		return "MessageModel [mMessageId=" + mMessageId
				+ ", mAppClientThreadId=" + mAppClientThreadId
				+ ", mPhotonModel=" + mPhotonModel.serialize() + "]";
	}

	public String serialize() {
		String serialized =
				mMessageId + mDATA_SEPERATOR
				+ mAppClientThreadId + mDATA_SEPERATOR
				+ mPhotonModel.serialize();
		return serialized;
	}
}
