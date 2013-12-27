package edu.jhu.cvrg.services.waveformDataService;

import java.io.File;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import edu.jhu.cvrg.services.waveformDataService.serviceDescriptionData.AlgorithmServiceData;
import edu.jhu.cvrg.waveform.service.ServiceUtils;

/** A collection of methods for building a generic Web Service to wrap around an arbitrary analysis algorithm..
 * 
 * @author Michael Shipway - 3/29/2012
 *
 */
public class WFDBdata {
	
	private static Logger log = Logger.getLogger(WFDBdata.class);
	
	/** For testing of service.
	 * @return version and usage text.
	 * @throws Exception
	 */
	public String getVersion() throws Exception{
		
		return "Version: 0.1.0 (03/26/2013)";
	}

	/** Looks up the details of all the algorithms provided by this web service.
	 *  see the reference at: http://xstream.codehaus.org/index.html
	 * @param param0 - contains the input parameters coded as XML.
	 * 			Contains one parameter, Verbose which if true causes debugging text to be generated
	 * @return - Result files names  coded as XML.
	 * @throws Exception 
	 */
	public org.apache.axiom.om.OMElement getAlgorithmDetails(org.apache.axiom.om.OMElement param0) throws Exception {
		String errorMessage = "";
		System.out.println("Physionet.getAlgorithmDetails faked version() started.");
		
		String xml="";
		XStream xstream = new XStream();
		xstream.alias("AlgorithmServiceData", AlgorithmServiceData.class);

		try {
			DataServiceUtils util = new DataServiceUtils();
			// parse the input parameter's OMElement XML into a Map.
			Map<String, Object> paramMap = util.buildParamMap(param0);
			// Assign specific input parameters to local variables.
			debugPrintln("paramMap.get(\"verbose\") " + (String) paramMap.get("verbose"));
			Boolean bVerbose    = Boolean.parseBoolean((String) paramMap.get("verbose"));
			
			System.out.println("++ verbose set to :" + bVerbose);
			//************* Calls the wrapper of the analysis algorithm. *********************
			try {
				//************* Calls the wrapper of the analysis algorithm. *********************
				AlgorithmDetailLookup details = new AlgorithmDetailLookup();
				details.verbose = bVerbose;
				details.loadDetails();
				AlgorithmServiceData[] asdDetail = details.serviceList;
				System.out.println("++ asdDetail[] populated. Length: " + asdDetail.length);
				//************* Return value is an array of result files.    *********************
				
				if (asdDetail.length != 0){
					xml = xstream.toXML(asdDetail); 
					System.out.println("++ xml.length:" + xml.length());
				}else{
					String error = "No analysis found " + errorMessage;
					xml = xstream.toXML(error); 
				}
			} catch (Exception e) {
				e.printStackTrace();
				errorMessage = "getAlgorithmDetails2 failed.";
			}
			
			debugPrintln("++ xml.length:" + xml.length());
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = "getAlgorithmDetails2 failed.";
		}
		
		OMElement payload = AXIOMUtil.stringToOM(xml);
		return  payload;
	}
	
	/** FTPs the WFDB files to the execute directory, extracts the segment requested, cleans up the temp files, then returns the data segment.
	 * Assumes that the data fetching will return fast enough to avoid the connection timeouts.
	 * 
	 * @param param0 - contains the input parameters coded as XML.
	 * @return - Result files names coded as XML.
	 * @throws Exception 
	 */
	public org.apache.axiom.om.OMElement fetchWFDBdataSegmentType2(org.apache.axiom.om.OMElement param0) throws Exception {
		
		debugPrintln("fetchWFDBdataSegmentType2() started.");
		String[] asWorkingFileHandles = null;
		
		DataServiceUtils util = new DataServiceUtils();
		Map<String, OMElement> paramsMap = util.parseInputParametersType2(param0);

		//************* Calls the wrapper of the analysis algorithm. *********************
		if(!util.bTestPattern){
			asWorkingFileHandles = copyDataFilesToAnalysis(paramsMap, util);
		}
		
		WFDBExecute execute = new WFDBExecute(util);
		OMElement omeWFDBdataReturn = execute.collectWFDBdataSegment(asWorkingFileHandles); 

		return omeWFDBdataReturn;	
	}
	
	/** Copies the (data)files from the specified repository(via ftp) to the analysis server's local filesystem.
	 * @param param0 
	 *  
	 * @param param0 - the data transfer parameters contained in the OMElement node: fileCount, fileNameList, relativePath, ftpHost, ftpUser, ftpPassword, and verbose.
	 * @return - The list of transfered files using the local path relative to the localTranferRoot (ftp root).
	 * @throws Exception
	 */
	private String[] copyDataFilesToAnalysis(Map<String, OMElement> params, DataServiceUtils util) throws Exception {
		debugPrintln("Running copyDataFilesToAnalysis()");
		
		String[] tmpFileNames = null;
		
		if(util.getInputFileNames() != null){
			
			tmpFileNames = new String[util.getInputFileNames().length];
			
			String inputPath = ServiceUtils.SERVER_TEMP_VISUALIZE_FOLDER + File.separator + util.userId;
			for (int i = 0; i < util.getInputFileNames().length; i++) {
				String fileName = util.getInputFileNames()[i];
			
				tmpFileNames[i] = inputPath+File.separator+fileName;
				
				ServiceUtils.createTempLocalFile(params, fileName, inputPath, fileName);
			}
		}
		
		return tmpFileNames;
	}

	private void debugPrintln(String text){
		log.debug("+ waveform-support-Service + " + text);
	}
	
}
