/*
 * The MIT License (MIT)

Copyright (c) 2014 

Hayda Almeida
Marie-Jean Meurs

Concordia University
Tsang Lab


Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package classifier;
import java.util.Random;

import weka.attributeSelection.LatentSemanticAnalysis;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.LMT;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import configure.PathConstants;

/**
 * Trains and tests a classifier, 
 * executes k-fold cross validation on train data 
 * and outputs the classification results.
 * 
 * @author halmeida
 *
 */

public class Trainer {
	
	public static int SEED = 1; //the seed for randomizing the data
	public static int FOLDS = 5; //the # of folds to generate
	double[][] ranking;
	String rank;
	

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		PathConstants pathVars = new PathConstants();
		Trainer evaluator = new Trainer();
			
		
		//Creating classifier
        Classifier cls = (Classifier) new LMT();
        //Classifier cls = (Classifier) new NaiveBayes();
        //Classifier cls = (Classifier) new LibSVM();
						
		//Loading train data
		DataSource sourceTrain = new DataSource(pathVars.HOME_DIR + pathVars.OUTPUT_MODEL + pathVars.TRAIN_DIR + pathVars.ARFF_TRAIN);
		Instances trainData = sourceTrain.getDataSet();
		
		//Flagging the class index on the training data
		trainData.setClassIndex(trainData.numAttributes()-1);		
		System.out.println("Class index set on training data.");
		
		System.out.println("Training data loaded. Number of instances: " + trainData.numInstances() + "\n");	
		
		//Executing k-fold cross validation
		//train.crossFold(trainData, cls);			
		
		//Loading test data
		DataSource sourceTest = new DataSource(pathVars.HOME_DIR + pathVars.OUTPUT_MODEL + pathVars.TEST_DIR + pathVars.ARFF_TEST);
		Instances testData = sourceTest.getDataSet();
		
		//Flagging the class index on the training data
		testData.setClassIndex(trainData.numAttributes()-1);		
		System.out.println("Class index set on testing data.");
		
		System.out.println("Test data loaded. Number of instances: " + testData.numInstances() + "\n");		
			
		//Creating filtered classifiers
		//AttributeSelectedClassifier PCAclassifier = evaluator.setPCAFilter(cls);
		//AttributeSelectedClassifier LSAclassifier = evaluator.setLSAFilter(cls);
		//AttributeSelectedClassifier GRclassifier = evaluator.setGRFilter(cls);
		//AttributeSelectedClassifier Corrclassifier = evaluator.setCorrFilter(cls);
				
		//Training and testing classifier
		evaluator.classify(trainData, testData, cls);	
		
		//Training and testing costSensitive classifier
		//evaluator.classify(trainData, testData, evaluator.classifySensitive(cls));		
		
		//Executing k-fold cross validation on filtered classifiers
		//evaluator.crossFold(trainData, PCAclassifier);
		//evaluator.crossFold(trainData, LSAclassifier);		
		
	}	
	

	/**
	 * Trains and tests a classifier when two separated
	 * datasets are provided.
	 * 
	 * @param train training data to build classifier
	 * @param test  test data to evaluate classifier
	 * @param classif  type of classifier applied
	 * @throws Exception
	 */
	public void classify(Instances train, Instances test, Classifier classif) throws Exception{

		classif.buildClassifier(train);
		Evaluation evaluateClassifier = new Evaluation(train);		
		evaluateClassifier.evaluateModel(classif, test);	
		
		stats(evaluateClassifier, classif);		
	}
	
	/**
	 * Trains and tests a classifier using a 
	 * provided Cost matrix 
	 * 
	 * @param classif type of classifier to be trained
	 * @return CostSensitive classifier with costs and classifier
	 * @throws Exception
	 */
	
	public CostSensitiveClassifier classifySensitive(Classifier classif) throws Exception{
		CostSensitiveClassifier costSensitive = new CostSensitiveClassifier();
		CostMatrix matrix = new CostMatrix(2);
		matrix.setElement(0, 1, 4);
		matrix.setElement(1, 0, 1);
		costSensitive.setClassifier(classif);
		costSensitive.setCostMatrix(matrix);
		
		return costSensitive;
	}
	
	
	/**
	 * Outputs classifier results.
	 * 
	 * @param eval  Evaluation model built by a classifier
	 * @param classif  type of classifier applied
	 * @throws Exception 
	 */

	public void stats(Evaluation eval, Classifier classif) throws Exception{		
		System.out.println("Number of attributes: " + eval.getHeader().numAttributes());
		System.out.println(eval.toSummaryString("\n======== RESULTS ========\n", false));
		System.out.println(eval.toClassDetailsString("\n\n======== Detailed accuracy by class ========\n"));
		System.out.println(eval.toMatrixString("\n\n======== Confusion Matrix ========\n"));		
	}
	
	/**
	 * Executes k-fold cross validation 
	 * on a given dataset
	 * @param data training data provided
	 * @param classif type of classifier usedsearch
	 * @throws Exception
	 */
			
	public void crossFold(Instances data, Classifier classif) throws Exception{

		Random random = new Random(SEED); //creating seed number generator
		Evaluation evaluateClassifier = new Evaluation(data);
		
		System.out.println("Classifier working...\n\n");
		//Classifier should not be trained when cross-validation is executed. 
		//because subsequent calls to buildClassifier method will return the same results always.
		evaluateClassifier.crossValidateModel(classif, data, FOLDS, random);		
						
		stats(evaluateClassifier, classif);		
	}
	
	
	/**
	 * Implements a Filtered GainRatio classifier, 
	 * using the ranker as a search method.
	 * 
	 * @param classif type of classifier to be used
	 * @return  filtered classif with Correlation analysis
	 */
	
	public AttributeSelectedClassifier setGRFilter(Classifier classif){
		AttributeSelectedClassifier fClassif = new AttributeSelectedClassifier();
		
		//Creating evaluator and search method
		GainRatioAttributeEval GR = new GainRatioAttributeEval();
		Ranker rank = new Ranker();
		//return the attributes with evaluation greater than 0
		double threshold = 0.0;
		rank.setThreshold(threshold);
		
		//Setting GainRatio filtered classifier		
		fClassif.setClassifier(classif);
		fClassif.setEvaluator(GR);
		fClassif.setSearch(rank);
		
		return fClassif;
		
	}
	
	/**
	 * Implements a Filtered Correlation classifier, 
	 * using the ranker as a search method.
	 * 
	 * @param classif type of classifier to be used
	 * @return  filtered classif with Correlation analysis
	 */
	
	public AttributeSelectedClassifier setCorrFilter(Classifier classif){
		AttributeSelectedClassifier fClassif = new AttributeSelectedClassifier();
		
		//Creating evaluator and search method
		CorrelationAttributeEval Corr = new CorrelationAttributeEval();
		Ranker rank = new Ranker();
		
		//return the attributes with evaluation greater than 0
		double threshold = 0.03;
		rank.setThreshold(threshold);
		
		//Setting GainRatio filtered classifier		
		fClassif.setClassifier(classif);
		fClassif.setEvaluator(Corr);
		fClassif.setSearch(rank);
		
		return fClassif;
		
	}
	
	/**
	 * Implements a Filtered PCA classifier, 
	 * using the ranker as a search method.
	 * 
	 * @param classif type of classifier to be used
	 * @return  filtered classif with PCA analysis config
	 */
	public AttributeSelectedClassifier setPCAFilter(Classifier classif){
		AttributeSelectedClassifier fClassif = new AttributeSelectedClassifier();
		
		//Creating evaluator and search method
		PrincipalComponents PCA = new PrincipalComponents();
		PCA.setMaximumAttributeNames(-1);
		Ranker rank = new Ranker();
		//return the attributes with evaluation greater than 0
		rank.setThreshold(0);
				
		//Setting the PCA classifier configurations
		fClassif.setClassifier(classif);
		fClassif.setEvaluator(PCA);
		fClassif.setSearch(rank);		
		
		return fClassif;
	}
	
	/**
	 * Implements a Filtered LSA classifier, 
	 * using the ranker as a search method
	 * @param classif
	 * @return
	 */
	
	private AttributeSelectedClassifier setLSAFilter(Classifier classif) {
		AttributeSelectedClassifier fClassif = new AttributeSelectedClassifier();
		
		//Creating evaluator
		LatentSemanticAnalysis LSA = new LatentSemanticAnalysis();
		LSA.setMaximumAttributeNames(-1);
		//value between 0 and 1 includes proportion of total latent variables
		//greater than 1 = exact # of variables to include;
		//less than or equal zero = include all;
		//default = 0.95 (proportional)
		double defaul = 0;
		LSA.setRank(defaul);
		//Creating search method
		Ranker rank = new Ranker();
		rank.setThreshold(0);
				
		//Setting the LSA classifier configurations
		fClassif.setClassifier(classif);		
		fClassif.setEvaluator(LSA);
		fClassif.setSearch(rank);				
		
		return fClassif;
	}	
	
	

}
