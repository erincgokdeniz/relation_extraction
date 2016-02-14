package pubmedresearch;

import java.util.List;
import pubmedresearch.bo.PubMedObject;

public class PVTResearch {
	
	public static void main(String[] args) {

		/** Corpus Options :
		 	"whitetext training" 	  
			"whitetext testing" 	
			"pvt abstract" 		
			"pvt fulltext" 		
			"pvt annotated" 
			"corpus test"		 
		 */
		String option = "whitetext testing";
		// apply abbreviation is used only for whitetext corpus, use "true" only for whitetext
		boolean applyAbbreviationExpansion = true;
		PubMedProcessor processor = new PubMedProcessor(option, applyAbbreviationExpansion);
		List<PubMedObject> listOfPubMedObjects =  processor.getPubMedList(option);
		
		PubMedEvaluator pubMedEvaluator = new PubMedEvaluator(option);
		pubMedEvaluator.evaluate(listOfPubMedObjects);
		
	}

}
