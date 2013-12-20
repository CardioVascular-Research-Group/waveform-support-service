package edu.jhu.cvrg.services.waveformDataService;

import edu.jhu.cvrg.services.waveformDataService.serviceDescriptionData.AlgorithmServiceData;


public class AlgorithmDetailLookup {
	public AlgorithmServiceData[] serviceList;
	public boolean verbose = true;
	
	public void loadDetails(){
		debugPrintln("AlgorithmDetailLookup.loadDetails() started. Version 2.");
		

		AlgorithmDetailLookup_rdsamp rdsamp 		=  new AlgorithmDetailLookup_rdsamp();
		
		rdsamp.verbose = verbose;
	
//		manually alphabetized for now, it will automated later
		serviceList = new AlgorithmServiceData[1];
 
		serviceList[0] = rdsamp.getDetails_rdsamp();

		debugPrintln("AlgorithmDetailLookup.loadDetails() finished.");
	}
	
	private void debugPrintln(String text){
		if(verbose)	System.out.println("# AlgorithmDetailLookup # " + text);
	}	
}
