package OutWrite;

public class OutWrite {

	public static OutLog ol;
	
	// Saving a few lines by not having an exception for every Thread.sleep
	// called in the program.
	public static void olSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	// Transform command line arguments into one string.
	public static String getCommand(String[] args) {
		String cmd = "";
		for(int cntr = 0; cntr < args.length; cntr++) {
			if (cmd.length() > 0) { cmd += " "; }
			cmd += args[cntr];
		}
		return cmd;		
	}
	
	// Start of the program.
	public static void main(String[] args) {
		String command = getCommand(args);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				ol.killProcess();
				ol.interrupt();
			}
		}));
		
		// This loop will run forever. Ctrl+C must be invoked to stop
		// OutWrite from running. You will need to check the TaskManager
		// or Process List to stop the external process if you force a stop.
		// addShutdownHook should be implemented at some point, but not now.
		while(true) {
			ol = new OutLog(command);
			
			ol.start();
			
			// Sleep for one second to ive the thread a moment to get
			// started running.
			olSleep(1000);
						
			// Check for output continuously.
			while(ol.getCounter() > 0) {
				ol.resetCounter();
				
				// Wait ten seconds before checking for more output.
				olSleep(10000);
			}
			
			// End the external process, stop the thread, clear the instantiated
			// object OutLog.
			ol.killProcess();
			ol.interrupt();
			ol = null;
		}
	}
}