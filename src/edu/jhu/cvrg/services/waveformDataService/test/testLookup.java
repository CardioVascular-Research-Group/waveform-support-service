package edu.jhu.cvrg.services.waveformDataService.test;


import edu.jhu.cvrg.services.waveformDataService.AlgorithmDetailLookup;
import edu.jhu.cvrg.services.waveformDataService.serviceDescriptionData.AlgorithmServiceData;


public class testLookup {

	/**
	 * test
	 * @param args
	 */
	public static void main(String[] args) {
		AlgorithmDetailLookup details = new AlgorithmDetailLookup();
		details.loadDetails();
		AlgorithmServiceData[] asdDetail = details.serviceList;
	}

}
