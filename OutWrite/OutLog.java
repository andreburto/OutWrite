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
	// Hold the old count
	public int old_count;
	// The external process.
	public Process p;
	// Need to write from multiple methods
	protected OutputStream out = null;
	
	// Builds a log file name based on the time/date.
	// The date format should be: yyyy_MM_dd for daily log files.
	// The date format should be: yyyy_MM_dd_hh_mm for testing.
	public String getFileName() {
		Date nd = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd");
		return "log_" + df.format(nd) + ".log";
	}
	
	// This process contains the logic for passing process output to a file.
	// The command is accessed from with the argument, accepted by the constructor.
	public void run() {
		try {
			String line;
			String filename = "";
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
				writeOut(line);
				
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
	
	protected void writeOut(String msg) {
		try {
			out.write(msg.getBytes(Charset.forName("UTF-8")));
			out.flush();
		}
		catch(Exception err) {
			System.out.println(err.getMessage());
		}
	}
	
	// Constructor for the object.
	public OutLog(String command) {
		cmd = command;
	}
	
	// Use this to kill the external process
	public void killProcess() {
		try {
			if (p != null) {
				writeOut("Restarting. Last line count: "+old_count+"\n");
				p.destroy();
			}
		}
		catch(NullPointerException npe) {
			System.out.println(npe.getMessage());
		}
	}
	
	// Return the output counter for the main thread to monitor.
	public int getCounter() {
		return counter;
	}
	
	// Allow external operators to reset the counter.
	public void resetCounter() {
		// Save the count
		old_count = counter;
		// Log the count
		writeOut("Line count: "+counter+"\n");
		// Now we reset the count
		counter = 0;
	}
}
