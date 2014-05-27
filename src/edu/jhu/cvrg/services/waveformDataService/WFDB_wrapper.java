package edu.jhu.cvrg.services.waveformDataService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class WFDB_wrapper {
	
	
	private String filePath;
	private String[] signalName;
	private int[][] data;
	private String sep = File.separator;
	
	private static Logger log = Logger.getLogger(WFDB_wrapper.class);
	
	public WFDB_wrapper(String filePath) {
		if(!filePath.endsWith(sep)){
			filePath = filePath + sep; // Because this class was written with the assumption that the path ends with "/".
		}
		this.filePath = filePath;
	}
	
	/** Reads the specified WFDB record into the data array
	 * 
	 * @param recordNm - Name of the record to read.
	 * @param signalsRequested - Number of signals to read, starting with 1st signal.
	 * @return samplesPerSignal
	 */
	public int WFDBtoArray(String recordNm, int signalsRequested, int samplesPerSignal, int signalCount) {
		
	    try{ // read data into the local array, count samplesPerSignal
	    	
	    	log.info("samplesPerSignal: " + samplesPerSignal + "  signalCount: " + signalCount);
	    	data = new int[signalCount][samplesPerSignal];
	    	
			String command = "rdsamp -r " + filePath + recordNm + " -c -p -v -H";
			
			Process process = Runtime.getRuntime().exec(command, new String[0], new File("/"));
		    InputStream is = process.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader stdInputBuffer = new BufferedReader(isr);
		    
		    InputStream errs = process.getErrorStream();
		    InputStreamReader esr = new InputStreamReader(errs);
		    BufferedReader stdError = new BufferedReader(esr);

		    
		    String line, error;
	
		    String[] aSigNames, aSample;
		    int lineNum = 0;
		    while ((line = stdInputBuffer.readLine()) != null) {
		    	if(lineNum==0){
		    		aSigNames = line.split(",");
		    		signalName = new String[signalCount];
		    		for(int sig=1;sig<=signalCount;sig++){ // zeroth column is time, not a signal
		    			signalName[sig-1] = aSigNames[sig];// column names to be used later to verify the order.
		    		}			    	  
		    	}else{
		    		if (lineNum > 1){
		    			aSample = line.split(",");
		    			for(int sig=1;sig<=signalCount;sig++){ // zeroth column is time, not a signal
		    				data[sig-1][lineNum-2] = (int)(Float.parseFloat(aSample[sig])*1000);// convert float millivolts to integer microvolts.
		    			}			    	  
		    		}		    	  
		    	}
		    	lineNum++;
		    }		    
		    
		    if(log.isInfoEnabled()){
		    	log.info("First 10 rows of data read:");
			    for (int row = 0; row < 10; row++) {  // try reading the first 10 rows. 
			        for (int sig = 0; sig < signalCount; sig++) {
			        	log.info(data[sig][row] + " ");
			        }
				}
		    }
		    // read any errors from the attempted command
		    log.info("Here is the standard error of the command (if any):\n");
	        while ((error = stdError.readLine()) != null) {
	        	log.error(error);
	        }

		
		} catch (IOException ioe) {
			log.error("IOException Message: rdsamp " + ioe.getMessage());
			ioe.printStackTrace();
		} catch (Exception e) {
			log.error("Exception Message: rdsamp " + e.getMessage());
			e.printStackTrace();
		}
		
	    return samplesPerSignal;
	}
	
	public int[][] getData() {
		return data;
	}
	
	public String getFilePath() {
		return filePath;
	}

}
