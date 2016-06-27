package phohawkenics.serial;

import phohawkenics.models.PhotonModel;
import phohawkenics.models.MessageModel;
import phohawkenics.util.Debugger;

public class DeserializationUtil {
	public static PhotonModel deserializeAppMessageModel (String input) {
		String[] info = input.split (PhotonModel.SEPARATOR);
		
		if (info.length != 9) {
			Debugger.log("The string cannot be interpreted to AppMessageModel");
			return null;
		} else {
			PhotonModel deserialized = new PhotonModel(
						info[0],
						Integer.parseInt(info[1]),
						info[2],
						Integer.parseInt(info[3]),
						info[4],
						Integer.parseInt(info[5]),
						Integer.parseInt(info[6]),
						Integer.parseInt(info[7]),
						Float.parseFloat(info[8])
					);
			return deserialized;
		}
	}
	
	public static MessageModel deserializeMessage(String messageModel) {
		String info[] = messageModel.split(MessageModel.mDATA_SEPERATOR);
		if (info.length != 3) {
			Debugger.log("The string cannot be interpreted to MessageModel");
			return null;
		} else {
			return new MessageModel(
						Integer.valueOf(info[0]),
						Integer.valueOf(info[1]),
						DeserializationUtil.deserializeAppMessageModel(info[2])
					);
		}
	}
}
