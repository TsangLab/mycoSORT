package filter;

import weka.core.Attribute;
import weka.core.Instances;

/**
 * This class implements informed feature selection
 * methods, to be used as filters after vector 
 * generation and pre-model building 
 * 
 * @author Hayda Almeida
 * @since 2015
 *
 */
public class InformedFilter {
	
	private boolean verbose = true;
	
	/**
	 * Calculates oddsRatio of each feature 
	 * in a given set of Instances
	 *  
	 * @param data set of instances, read from ARFF file
	 * @return oddsRatio for each attribute in the matrix
	 */
	public double[] oddsRatio(Instances data, int threshold){

		double[] oddsRatio = new double[data.numAttributes()];
		

		for(int i = 0; i < data.numAttributes()-1; i++ ){

			double OR = 0;

			Attribute current = data.attribute(i);
			double pos_docs = 0, //number of documents in class C 
					pos_oc = 0,  //number of times term t occured in class C
					pos_term_docs = 0, //number of docs in class C that have term
					pos_not_docs = 0,  //number of docs in class C that do not have term
					neg_term_docs = 0,   //number of docs not in class C with term
					neg_not_docs = 0,  //number of docs not in class C nor with term
					neg_docs = 0; //number of documents not in class C

			for(int j = 0; j < data.size(); j++){

				double current_value = data.instance(j).value(current);
				double current_class = data.instance(j).classValue();

				//class is positive  
				if(current_class < 1){
					pos_docs = pos_docs + 1;

					//the feature occurred in the document
					if(current_value > 0){
						pos_oc = pos_oc + current_value;
						pos_term_docs = pos_term_docs +1;
					}
					//the feature did not occur in positive docs
					else pos_not_docs = pos_not_docs + 1;
				}
				//class is negative
				else{
					neg_docs = neg_docs+1;

					//the feature occurred in the document
					if(current_value > 0){
						neg_term_docs = neg_term_docs +1;
					}
					//the feature did not occur in negative docs
					else neg_not_docs = neg_not_docs + 1;
				}

			}

			OR = ( ( (pos_term_docs / pos_docs) / (pos_not_docs/ pos_docs) ) / 
					( (neg_term_docs / neg_docs) / (neg_not_docs / neg_docs) ) ); 
			
		//	OR = (pos_term_docs / pos_not_docs) / (neg_term_docs / neg_not_docs);
			
			
			//99% confidence: 2.575
			//95% confidence: 1.96
			double confidenceLow =  Math.exp(Math.log(OR) - (1.96 * Math.sqrt((1/pos_term_docs) + (1/pos_not_docs) + (1/neg_term_docs) + (1/neg_not_docs))));
			double confidenceHigh = Math.exp(Math.log(OR) + (1.96 * Math.sqrt((1/pos_term_docs) + (1/pos_not_docs) + (1/neg_term_docs) + (1/neg_not_docs))));
						
			//checking if OR value is within the confidence interval
			//and if it satisfies the threshold
			if( ((OR <= confidenceHigh) && (OR >= confidenceLow) 
					&& !(OR == threshold))
					//checking if the confidence interval holds the null hypothesis (i.e., spans 1.0)
					&& !(confidenceLow <=1 && confidenceHigh >=1))
				oddsRatio[i] = OR;
			else
				oddsRatio[i] = 0;
			
			if(verbose){
			System.out.println("Attribute: "+ data.attribute(i).toString() +"\t\t OddsRatio: " + oddsRatio[i] + 
					"\tConfidenceLow: " + confidenceLow + "\tConfidenceHigh: "+ confidenceHigh);
			}
		}
		
		return oddsRatio;		
	}
	
	/**
	 * Calculates the inverse document frequency
	 * for each attribute in the dataset. 
	 * 
	 * @param data instances
	 * @param threshold 
	 * @return list of idfs for each attribute
	 */
	public double[] idf(Instances data, int threshold){
		
		double[] idf = new double[data.numAttributes()];		
		
		for(int i = 0; i < data.numAttributes()-1; i++ ){

			double idf_at = 0;
			double idf_at2 = 0;

			Attribute current = data.attribute(i);
			double pos_docs = 0, //number of documents in class C				
					pos_term_docs = 0, //number of docs in class C that have term
					neg_term_docs = 0,   //number of docs not in class C with term					
					neg_docs = 0; //number of documents not in class C

			for(int j = 0; j < data.size(); j++){

				double current_value = data.instance(j).value(current);
				double current_class = data.instance(j).classValue();

				//class is positive  
				if(current_class < 1){					
					pos_docs = pos_docs + 1;

					//the feature occurred in the document
					if(current_value > 0){						
						pos_term_docs = pos_term_docs +1;	
					}						
				}
				else{
					//class is negative 
					neg_docs = neg_docs+1;
					
					//the feature occurred in the document
					if(current_value > 0){						
						neg_term_docs = neg_term_docs +1;
					}					
				}
			}			
						
//			double idf_pos = Math.log((pos_docs)/(pos_term_docs));
//			double idf_neg = Math.log((neg_docs)/(neg_term_docs));

			//check if the idf in the "positive" collection
			//is greater than the idf in the "negative" collection
//			if (idf_pos > idf_neg) 
//				idf_at = idf_pos;
//				
//			else idf_at = 0;			

			idf_at = Math.log((pos_docs + neg_docs)/(pos_term_docs + neg_term_docs));
			
			if(idf_at <= threshold)
				idf[i] = 0;				
			else
				idf[i] = idf_at;
		}
		
		if(verbose){
			for(int i = 0; i < idf.length; i++){
				if(idf[i]>0)
				   System.out.println("Attribute: "+ data.attribute(i).toString()+ "\t\t\t IDF: " + idf[i]);				
			}
		}
		
		return idf;			
	}	
	

}
