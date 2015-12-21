import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.text.*;

@SuppressWarnings("unused")
public class OutWrite {

	// Builds a log file name based on the time/date.
	public static String getName() {
		Date nd = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_hh_mm");
		return "log_" + df.format(nd) + ".log";
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
	
	// Main function
	public static void main(String[] args) throws Exception {
		try {
			String cmd = getCommand(args);
			String line;
			String filename = "";
			ByteArrayOutputStream bos = null;
			OutputStream out = null;
			boolean isOpen = false;
			
			if (cmd.length() == 0) { throw new Exception("No arguments passed."); }
			
			// Create a process from the command line arguments 
			Process p = Runtime.getRuntime().exec(cmd);
			
			// Route the output into bri and err so the lines can be saved to a file.
			BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			// Loop as long as the program is running.
			while ((line = bri.readLine()) != null) {
				if (filename != getName()) {
					if (isOpen == true)  {
						out.close();
					} else {
						isOpen = true;
					}
					
					// Get the file name based on the time. 
					filename = getName();
					
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
			}
			bri.close();
			
			// Dump the error message
			while ((line = err.readLine()) != null) {
				System.out.println(line);
			}
			err.close();
			p.waitFor();
		}
		catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}
}