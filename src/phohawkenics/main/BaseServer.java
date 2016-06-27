package phohawkenics.main;
import phohawkenics.threads.manager.BaseServerManager;
import phohawkenics.util.Debugger;

public class BaseServer {
	/**
	 * Instantiates the program
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String args[]) throws InterruptedException {
		Debugger.enable();
		Debugger.enableMessageCount();
		
		@SuppressWarnings("unused")
		BaseServerManager k = null;
		if (args.length != 2) {
			Debugger.log("Using default port 8888");
			k = new BaseServerManager(8888);
		} else
			k = new BaseServerManager(Integer.parseInt(args[0]));
	}
}
