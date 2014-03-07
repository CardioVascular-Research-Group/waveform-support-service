package edu.jhu.cvrg.services.waveformDataService;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.log4j.Logger;

import edu.jhu.cvrg.waveform.service.ServiceUtils;

public class DataServiceUtils {

	String errorMessage="";
	/** uri parameter for OMNamespace.createOMNamespace() - the namespace URI; must not be null, <BR>e.g. http://www.cvrgrid.org/physionetAnalysisService/ **/
	private String sOMNameSpaceURI = "http://www.cvrgrid.org/physionetAnalysisService/";  
	
	/** prefix parameter for OMNamespace.createOMNamespace() - the prefix<BR>e.g. physionetAnalysisService **/
	private String sOMNameSpacePrefix =  "physionetAnalysisService";  
	private String[] inputFileNames = null;
	private String fileName="";
	private long fileSize=0;
	private int offsetMilliSeconds=0, durationMilliSeconds=0, graphWidthPixels=0, signalCount= 0, samplesPerSignal = 0;
	private double sampleFrequency = 0.0;
	private boolean skipSamples = false;
	
	public Map<String, Object> mapCommandParam = null;
	public String sJobID="";
	public String tempFile ="";
	public Long userId;
	public String[] saLeadCSV = null; // array of comma separated ECG values, one string per lead.
	public VisualizationData visData=null;
	public boolean bTestPattern = false;
	
	
	private static Logger log = Logger.getLogger(DataServiceUtils.class);
	
	public Map<String, OMElement> parseInputParametersType2(OMElement param0){
		// parse the input parameter's OMElement XML into a Map.
	    Map<String, OMElement> mapWServiceParam = null;
	    
		try {
			mapWServiceParam = ServiceUtils.extractParams(param0);
			// Assign specific input parameters to local variables.
			
			int iFileCount      = Integer.parseInt( (String) mapWServiceParam.get("fileCount").getText() ); 
			int iParameterCount = Integer.parseInt( (String) mapWServiceParam.get("parameterCount").getText()); 
			/********************************************/
			bTestPattern		= Boolean.parseBoolean((String) mapWServiceParam.get("testPattern").getText());
			if(mapWServiceParam.get("fileSize") != null && bTestPattern){
				fileSize			= Long.parseLong((String) mapWServiceParam.get("fileSize").getText());
			}
			
			offsetMilliSeconds	= Integer.parseInt((String) mapWServiceParam.get("offsetMilliSeconds").getText());
			durationMilliSeconds= Integer.parseInt((String) mapWServiceParam.get("durationMilliSeconds").getText());
			graphWidthPixels	= Integer.parseInt((String) mapWServiceParam.get("graphWidthPixels").getText());
			userId				= Long.valueOf((String) mapWServiceParam.get("userId").getText());
			
			sampleFrequency		= Double.valueOf((String) mapWServiceParam.get("sampleFrequency").getText());
			signalCount			= Integer.valueOf((String) mapWServiceParam.get("signalCount").getText());
			samplesPerSignal	= Integer.valueOf((String) mapWServiceParam.get("samplesPerSignal").getText());
			
			if(mapWServiceParam.get("noSkip")!=null){
				skipSamples	= Boolean.valueOf((String) mapWServiceParam.get("noSkip").getText());
			}
			
			debugPrintln("Extracting fileNameList, should be " + iFileCount + " files ...;");

			if(!bTestPattern & (iFileCount>0)){
				OMElement filehandlelist = (OMElement) mapWServiceParam.get("fileNameList");
				debugPrintln("Building Input Filename array...;");
				inputFileNames = buildParamArray(filehandlelist);
				debugPrintln("Finished Extracting fileNameList, founnd " + inputFileNames.length + " files ...;");
				
			}
			if(iParameterCount>0){
				debugPrintln("Extracting parameterlist, should be " + iParameterCount + " parameters ...;");
				OMElement parameterlist = (OMElement) mapWServiceParam.get("parameterlist");
				debugPrintln("Building Command Parameter map...;");
				mapCommandParam = buildParamMap(parameterlist);
			}else{
				debugPrintln("There are no parameters, so Command Parameter map was not built.");
			}
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = "parseInputParametersType2 failed.";
		}
		
		return mapWServiceParam;
	}
	


	/** Parses a service's incoming XML and builds a string array of all the parameters for easy access.
	 * @param param0 - OMElement representing XML with the incoming parameters.
	 */
	private String[] buildParamArray(OMElement param0){
		debugPrintln("buildParamArray()");

		ArrayList<String> paramList = new ArrayList<String>();

		try {
			@SuppressWarnings("unchecked")
			Iterator<OMElement> iterator = param0.getChildren();
			
			while(iterator.hasNext()) {
				OMElement param = iterator.next();
				paramList.add(param.getText());

				debugPrintln(" -- paramList.add(v): " + param.getText());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		String[] ret = new String[paramList.size()];
		ret = paramList.toArray(ret);
		
		return ret;
	}
	
	/** Parses a service's incoming XML and builds a Map of all the parameters for easy access.
	 * @param param0 - OMElement representing XML with the incoming parameters.
	 */
	public Map<String, Object> buildParamMap(OMElement param0){
		debugPrintln("buildParamMap()");
	
		String key="";
		Object oValue = null;
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		try {
			@SuppressWarnings("unchecked")
			Iterator<OMElement> iterator = param0.getChildren();
			
			while(iterator.hasNext()) {
				OMElement param = iterator.next();
				key = param.getLocalName();
				oValue = param.getText();
				if(oValue.toString().length()>0){
					debugPrintln(" - Key/Value: " + key + " / '" + oValue + "'");
					paramMap.put(key,oValue);
				}else{
					Iterator<OMElement> iterTester = param.getChildren();
					if(iterTester.hasNext()){
						OMElement omValue = (OMElement)param;
						paramMap.put(key,param);
					}else{
						debugPrintln(" - Key/Blank: " + key + " / '" + oValue + "'");
						paramMap.put(key,"");	
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = "buildParamMap() failed.";
			return null;
		}
		
		debugPrintln("buildParamMap() found " + paramMap.size() + " parameters.");
		return paramMap;
	}
	

	public OMElement buildOmeReturnType2(String sJobID, String[] asOutputFileNameList, String sReturnOMEName, 
			String localAnalysisOutputRoot, String localOutputRoot){
		OMElement omeReturn = null;
		try{
			OMFactory omFactory = OMAbstractFactory.getOMFactory(); 	 
			OMNamespace omNs = omFactory.createOMNamespace(sOMNameSpaceURI, sOMNameSpacePrefix); 	 

			omeReturn = omFactory.createOMElement(sReturnOMEName, omNs); 
	
			// Convert an array of absolute paths into relative paths for return to caller (CVRGrid Toolkit)
			asOutputFileNameList = trimRootFromPaths(asOutputFileNameList, localOutputRoot);
			if (errorMessage.length() == 0){
				addOMEChild("filecount", 
						new Long(asOutputFileNameList.length).toString(), 
						omeReturn,omFactory,omNs);
				omeReturn.addChild( makeOutputOMElement(asOutputFileNameList, "filenamelist", "filename", omFactory, omNs) );
				addOMEChild("jobID", 
						sJobID, 
						omeReturn,omFactory,omNs);
			}else{
				addOMEChild("error",
						"If analysis failed, put your message here: \"" + errorMessage + "\"", 
						omeReturn,omFactory,omNs);
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = "genericWrapperType2 failed.";
		}
		
		return omeReturn;
	}


	/** Converts the array of output (relative) filenames to a single OMElement whose sub-elements are the filenames.
	 * 
	 * @param asFileNames - array of (relative) file path/name strings.
	 * @return - a single OMElement containing the path/names.
	 */
	private OMElement makeOutputOMElement(String[] asFileNames, String sParentOMEName, String sChildOMEName, OMFactory omFactory, OMNamespace omNs){
		debugPrintln("makeOutputOMElement()" + asFileNames.length + " file names");
		OMElement omeArray = null;
		if (asFileNames != null) {
			try {
				omeArray = omFactory.createOMElement(sParentOMEName, omNs); 
				
				for(int i=0; i<asFileNames.length;i++){
					addOMEChild(sChildOMEName,
							asFileNames[i], 
							omeArray,omFactory,omNs);					
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return omeArray;
	}
	

	/** Wrapper around the 3 common functions for adding a child element to a parent OMElement.
	 * 
	 * @param name - name/key of the child element
	 * @param value - value of the new element
	 * @param parent - OMElement to add the child to.
	 * @param factory - OMFactory
	 * @param dsNs - OMNamespace
	 */
	public void addOMEChild(String name, String value, OMElement parent, OMFactory factory, OMNamespace dsNs){
		OMElement child = factory.createOMElement(name, dsNs);
		child.addChild(factory.createOMText(value));
		parent.addChild(child);
	}


	/** Moves the files listed in the array from the source root directory to the destination root directory.
	 * 
	 * @param asFileNames - array of full file path/name strings.
	 * @param sSourceRoot - root directory to move the files from.
	 * @param sDestinationRoot - root directory to move the files to.
	 * @return - array of the new full file path/name strings.
	 */
	public String[] moveFiles(String[] asFileNames, String sSourceRoot, String sDestinationRoot){
		debugPrintln("moveFiles() from: '" + sSourceRoot + "' to: '" + sDestinationRoot + "'");
		if (asFileNames != null) {
			int iMovedCount=0;
			String sDestination = "", sOrigin;
			try {
				if(sSourceRoot.compareTo(sDestinationRoot) == 0){ // nop if source and destination are identical.
					debugPrintln(" - Source and Destination are identical, no moving needed.");
				}else{
					for(int i=0;i<asFileNames.length;i++){
						sDestination  =  sDestinationRoot + asFileNames[i];
						
						try {
							String destinationDir = extractPath(sDestination); 
							debugPrintln("Creating local directory: " + destinationDir);
							File f = new File(destinationDir); // create destination directory 	        
							if(f.mkdir()){
								debugPrintln("Creating directory succeeded: " + destinationDir );
							}else{
								debugPrintln("Creating directory failed: " + destinationDir);
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}


						sOrigin = sSourceRoot + "/" + asFileNames[i];
						debugPrintln(" - rename: '" + sOrigin + "' to: '" + sDestination + "'");
						File fSource = new File(sOrigin);
						debugPrintln(" - fSource.getPath(): '" + fSource.getPath());
						boolean bSuccess = fSource.renameTo(new File(sDestination));
						debugPrintln(" -- success: '" + bSuccess + "'");
						if(bSuccess) iMovedCount++;
						asFileNames[i] = sDestination;
					}
					if(iMovedCount != asFileNames.length){
						errorMessage += "moveFiles() failed. " + iMovedCount + " of " + asFileNames.length + " moved successfully.";
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				errorMessage += "moveFiles() failed.";
				return null;
			}
	    }
		return asFileNames;		
	}


	/** Trims the root path from each file path/name in the array.
	 * Used to convert an array of absolute paths into relative paths, 
	 *  e.g. for building the return value for the service.
	 * 
	 * @param asFileNames
	 * @param localOutputRoot
	 * @return
	 */
	private String[] trimRootFromPaths(String[] asFileNames, String localOutputRoot){	
		debugPrintln("trimRootFromPaths() , trimming off: '" + localOutputRoot + "'");
		if (asFileNames != null) {
			try {
				for(int i=0;i<asFileNames.length;i++){
					debugPrintln("Trimmed: '" + asFileNames[i] + "' " );
					asFileNames[i] = asFileNames[i].replace(localOutputRoot, "");
					debugPrintln("---- to: '" + asFileNames[i] + "'");
				}
			} catch (Exception e) {  
				e.printStackTrace();
				errorMessage = "trimRootFromPaths() failed.";
				debugPrintln(errorMessage);
				return null;
			}
		}
		return asFileNames;
	}
	
	/** Find the first filename in the array with the "hea" extension.
	 * 
	 * @param asInputFileNames - array of filenames to search
	 * @return - full path/name.ext as found in the array.
	 */
	public String findHeaderPathName(String[] asInputFileNames){
		debugPrintln("findHeaderPathName()");
		String sHeaderPathName="";
		sHeaderPathName = findPathNameExt(asInputFileNames, "hea");
		return sHeaderPathName;
	}

	/** Find the first filename in the array with the specified extension.
	 * 
	 * @param asInputFileNames - array of filenames to search
	 * @param sExtension - extension to look for, without the dot(".") e.g. "hea".
	 * @return - full path/name.ext as found in the array.
	 */
	public String findPathNameExt(String[] asInputFileNames, String sExtension){
		debugPrintln("findHeaderPathName()");
		String sHeaderPathName="", sTemp="";
		int iIndexPeriod=0;
		
		for(int i=0;i<asInputFileNames.length;i++){
			sTemp = asInputFileNames[i];
			debugPrintln("- asInputFileNames[" + i + "]: " + asInputFileNames[i]);
			iIndexPeriod = sTemp.lastIndexOf(".");
			
			if( sTemp.substring(iIndexPeriod+1).equalsIgnoreCase(sExtension) ){
				sHeaderPathName = sTemp;
			}
		}
		debugPrintln("- ssHeaderPathName: " + sHeaderPathName);
		return sHeaderPathName;
	}

	
	public String extractPath(String sHeaderPathName){
		debugPrintln("extractPath() from: '" + sHeaderPathName + "'");

		String sFilePath="";
		int iIndexLastSlash = sHeaderPathName.lastIndexOf("/");
		
		sFilePath = sHeaderPathName.substring(0,iIndexLastSlash+1);
		
		return sFilePath;
	}
	
	public String extractName(String sFilePathName){
		debugPrintln("extractName() from: '" + sFilePathName + "'");

		String sFileName="";
		int iIndexLastSlash = sFilePathName.lastIndexOf("/");
		
		sFileName = sFilePathName.substring(iIndexLastSlash+1);

		return sFileName;
	}
	
	
	/** Creates a unique string based on the current timestamp, plus a pseudorandom number between zero and 1000
	 * 	In the form YYYYyMMmDDdHH_MM_SSxRRRR
	 * @return - a mostly unique string
	 */
	public String generateTimeStamp() {		
			Calendar now = Calendar.getInstance();
			int year = now.get(Calendar.YEAR);
			int month = now.get(Calendar.MONTH) + 1;
			int day = now.get(Calendar.DATE);
			int hour = now.get(Calendar.HOUR_OF_DAY); // 24 hour format
			int minute = now.get(Calendar.MINUTE);
			int second = now.get(Calendar.SECOND);
			
			String date = new Integer(year).toString() + "y";
			
			if(month<10)date = date + "0"; // zero padding single digit months to aid sorting.
			date = date + new Integer(month).toString() + "m"; 

			if(day<10)date = date + "0"; // zero padding to aid sorting.
			date = date + new Integer(day).toString() + "d";

			if(hour<10)date = date + "0"; // zero padding to aid sorting.
			date = date + new Integer(hour).toString() + "_"; 

			if(minute<10)date = date + "0"; // zero padding to aid sorting.
			date = date + new Integer(minute).toString() + "_"; 

			if(second<10)date = date + "0"; // zero padding to aid sorting.
			date = date + new Integer(second).toString() + "x";
			
			// add the random number just to be sure we avoid name collisions
			Random rand = new Random();
			date = date + rand.nextInt(1000);
			return date;
		}
	
	public void debugPrintln(String text){
		log.debug("++ DataServiceUtils + " + text);
	}



	public int getOffsetMilliSeconds() {
		return offsetMilliSeconds;
	}



	public int getDurationMilliSeconds() {
		return durationMilliSeconds;
	}



	public int getGraphWidthPixels() {
		return graphWidthPixels;
	}



	public int getSignalCount() {
		return signalCount;
	}



	public int getSamplesPerSignal() {
		return samplesPerSignal;
	}



	public double getSampleFrequency() {
		return sampleFrequency;
	}



	public String[] getInputFileNames() {
		return inputFileNames;
	}



	public String getFileName() {
		return fileName;
	}



	public long getFileSize() {
		return fileSize;
	}



	public boolean isSkipSamples() {
		return skipSamples;
	}



	public void setSkipSamples(boolean noSkip) {
		this.skipSamples = noSkip;
	}

}
