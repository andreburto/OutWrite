package OutWrite;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.text.*;

// This class is designed to run as a thread.
// It runs an external process, accepts output from it, puts that
// output into a file, and allows for the monitoring and control
// of that external process.
public class OutLog extends Thread {

	// The command to be run as an external process.
	public String cmd;
	// The counter to be monitored by the parent thread.
	public int counter;
	// The external process.
	public Process p;
	
	// Builds a log file name based on the time/date.
	// The date format should be: yyyy_MM_dd for daily log files.
	// The date format should be: yyyy_MM_dd_hh_mm for testing.
	public String getFileName() {
		Date nd = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_hh_mm");
		return "log_" + df.format(nd) + ".log";
	}
	
	// This process contails the logic for passing process output to a file.
	// The command is accessed from with the argument, accepted by the constructor.
	public void run() {
		try {
			String line;
			String filename = "";
			OutputStream out = null;
			boolean isOpen = false;
			
			// You have to have a command to do anything.
			if (cmd.length() == 0) { throw new Exception("No arguments passed."); }
			
			// Create a process from the command line arguments 
			p = Runtime.getRuntime().exec(cmd);
			
			// Route the output into bri and err so the lines can be saved to a file.
			BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			// Loop as long as the program is running.
			while ((line = bri.readLine()) != null) {
				if (filename != getFileName()) {
					// This conditional is used to see if the time is the same
					// as it was on the last loop. If not the file is closed
					// and immediately a new file with the new time is opened.
					if (isOpen == true)  {
						out.close();
					} else {
						isOpen = true;
					}
					
					// Get the file name based on the time. 
					filename = getFileName();
					
					// Write the file out to text.
					out = Files.newOutputStream(Paths.get(filename),
							                    StandardOpenOption.CREATE,
							                    StandardOpenOption.APPEND);
				}
				
				// Add a line break
				line += "\n";
				
				// Write to the file
				out.write(line.getBytes(Charset.forName("UTF-8")));
				out.flush();
				
				// Upping the count so the main thread will know the output is still
				// running.
				counter += 1;
			}
			// Close the input stream.
			bri.close();
			
			// Dump the error message
			while ((line = err.readLine()) != null) {
				System.out.println(line);
			}
			// Close the error stream.
			err.close();
			p.waitFor();
		}
		catch (Exception err) {
			// Display the exception.
			System.out.println(err.getMessage());
			
		}
	}
	
	// Constructor for the object.
	public OutLog(String command) {
		cmd = command;
	}
	
	// Use this to kill the external process
	public void killProcess() {
		p.destroy();
	}
	
	// Return the output counter for the main thread to monitor.
	public int getCounter() {
		return counter;
	}
	
	// Allow external operators to reset the counter.
	public void resetCounter() {
		counter = 0;
	}
}
