package phohawkenics.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Debugger{
	private static boolean mIsEnabled = false;
	private static boolean mForMessageCountIsEnabled = false;
	private static PrintWriter mLogsOut;
	private static PrintWriter mMessageCountOut;
	
	public static void enable() {
		mIsEnabled = true;
		try {
			mLogsOut = new PrintWriter(new BufferedWriter(new FileWriter("Logs.txt", true)));
			mMessageCountOut = new PrintWriter(new BufferedWriter(new FileWriter("MessageCount.txt", true)));
		} catch (IOException e) {
			System.out.println("Unable to open log files");
		}
	}
	
	public static void enableMessageCount() {
		mForMessageCountIsEnabled = true;
	}
	
    public static void log(Object o){
    	if (mIsEnabled) {
    		System.out.println(getCurrentTime() + " " + o.toString());
    		saveToFile(getCurrentTime() + " " + o.toString());
    	}
    }
    public static void logForMessageCount(Object o){
    	if (mForMessageCountIsEnabled) {
    		System.out.println(getCurrentTime() + " " + o.toString());
    		saveToFileMessageCount(getCurrentTime() + " " + o.toString());
    	}
    }
    
    public synchronized static void saveToFile(Object o) {
		mLogsOut.println(o.toString());
    }
    
    public static void saveToFileMessageCount(Object o) {
		mMessageCountOut.println(o.toString());
    }
    
    public static String getCurrentTime() {
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd:HHmm:ss.SSS").format(Calendar.getInstance().getTime());
		return timeStamp;
	}
}