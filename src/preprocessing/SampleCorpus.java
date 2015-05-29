package preprocessing;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import configure.ConfigConstants;

/**
 * Performs document instances sampling
 * generating training and test files
 * with specific balance input by user.
 *   
 * @author Hayda Almeida
 * @since 2015
 *
 */
public class SampleCorpus {

	public static void main(String[] args) throws Exception {	

		ConfigConstants pathVars = new ConfigConstants();
		SampleCorpus sampling = new SampleCorpus();
		
		String pathToLiteratureFolder = pathVars.HOME_DIR + pathVars.CORPUS_DIR;

		String positiveDir = pathToLiteratureFolder + pathVars.POS_DIR;
		List positives = new LinkedList();

		String negativeDir = pathToLiteratureFolder + pathVars.NEG_DIR;
		List negatives = new LinkedList();

		//train or test sampling
		Boolean training = Boolean.valueOf(pathVars.SAMPLE_TRAIN);
		Boolean testing = Boolean.valueOf(pathVars.SAMPLE_TEST);

		//% of test corpus WRT the collection, % positive on test set, % positive on training set 
		int percTs = Integer.parseInt(pathVars.PERCT_TEST); 
		int percPosTr = Integer.parseInt(pathVars.PERCT_POS_TRAIN);
		int percPosTs = Integer.parseInt(pathVars.PERCT_POS_TEST);

		if(!(training || testing)){
			System.out.println("Training or Test sampling: not set up.\n"
					+ "Please define sampling params in file: \n"
					+ "@ config.cfg.");
			System.exit(0);			
		}		

		positives = sampling.loadFiles(positiveDir);
		negatives = sampling.loadFiles(negativeDir);
		
		if(testing) sampling.sampleTest(pathToLiteratureFolder + pathVars.TEST_DIR, positives, negatives, percTs, percPosTs);
		
		if(training) sampling.sampleTrain(pathToLiteratureFolder + pathVars.TRAIN_DIR, positives, negatives, percPosTr);		


	}	
	
	/**
	 * Lists XML files within a folder 
	 * @param dirSrc folder path
	 * @return returns list of file IDs
	 */
	public List loadFiles(String dirSrc){						

		List fileIDs = new LinkedList();
		
		File sourceDir = new File(dirSrc);
		File[] srcXMLs = sourceDir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
				return name.endsWith(".xml");
			}
		});	

		fileIDs = new LinkedList(Arrays.asList(srcXMLs));
		
		return fileIDs;
	}
	
	/**
	 * Moves a specific number of files 
	 * in a list from origin folder to a test folder
	 * @param pathVars 
	 * @param files List of file IDs
	 * @param numFiles number of files to be moved
	 */
	public void moveFile(String path, List files, int numFiles){
		
		Iterator<File> filesList = files.iterator();
		File testDir = new File(path);
		
		if(!testDir.exists()){
			try{
				testDir.mkdir();
			}catch(Exception e){
				System.out.println("Error creating Test folder.");
				System.exit(0);
			}
		}
		
		while(filesList.hasNext() && numFiles > 0){		
			try{
				File file = (File) filesList.next();
				File newFile = new File(testDir + "/" + file.getName());
				
				Files.move(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				
				filesList.remove();				
				numFiles--;
			}
			catch(Exception e){
				System.out.println("Error moving files.");
				System.exit(0);
			}
		}	
		
	}
	
	/**
	 * Copies a specific number of files 
	 * in a list from origin folder to a train folder
	 * @param pathVars
	 * @param files  List of file IDs
	 * @param numFiles number of files to be moved
	 */
	public void copyFile(String path, List files, int numFiles, int percPos){
		
		Iterator<File> filesList = files.iterator();
		String trainPath = path.substring(0, path.length()-1) + "_" + percPos + "/";
		File trainDir = new File(trainPath);
		
		if(!trainDir.exists())
			try{
				trainDir.mkdir();
			}catch(Exception e){
				System.out.println("Error creating Training folder.");
				System.exit(0);
			}
		
		while(filesList.hasNext() && numFiles > 0){				
			try{				
				File file = (File) filesList.next();
				File newFile = new File(trainDir + "/"+ file.getName());
				
				Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			catch(Exception e){
				System.out.println("Error copying files.");
				System.exit(0);
			}
		}
		
	}
	
	/**
	 * Samples document instances from the collection
	 * to generate a test set.
	 * 
	 * @param pathVars
	 * @param positives list of positive documents IDs
	 * @param negatives list of negative documents IDs
	 * @param total  percentage of the document collection for test
	 * @param pos  percentage of positive documents in the test set
	 */
	public void sampleTest(String path, List positives, List negatives, int total, int pos){
		
		int instances = positives.size() + negatives.size();		
		int testSize = (instances * total) / 100; 		
		int posSize = (testSize * pos) / 100;		
		int negSize = testSize - posSize;		
		
		Collections.shuffle(negatives);	
		System.out.println("===== Test > Negative instances shuffled for test set.");
		moveFile(path, negatives, negSize);
		System.out.println("===== Test > Negative instances moved to test folder. \n");
		
		Collections.shuffle(positives);	
		System.out.println("===== Test > Positive instances shuffled for test set.");
		moveFile(path, positives, posSize);	
		System.out.println("===== Test > Positive instances moved to test folder. \n");
		
	}
	
	/**
	 * Samples document instances from the collection
	 * to generate a training set.
	 * 
	 * @param pathVars
	 * @param positives list of positive documents IDs
	 * @param negatives list of negative documents IDs
	 * @param pos percentage of positive documents in the training set
	 */	
    public void sampleTrain(String path, List positives, List negatives, int percPos){
		
    	int posSize = positives.size();
    	int trainSize = (100 * posSize) / percPos;
    	  	
    	int negSize = trainSize - posSize;
    	
    	if(positives.size() < posSize){
    		System.out.println("Not enough positive instances for training set.");
    		System.exit(0);
    	}
    	else if(negatives.size() < negSize){
    		System.out.println("Not enough negative instances for training set.");
    		System.exit(0);    	
    	}
    	else{    		
    		Collections.shuffle(negatives);
    		System.out.println("===== Training > Negative instances shuffled for training set.");
    		copyFile(path, negatives, negSize, percPos);
    		System.out.println("===== Training > Negative instances copied to training folder. \n");
    		
    		Collections.shuffle(positives);
    		System.out.println("===== Training > Positive instances shuffled for training set.");
    		copyFile(path, positives, posSize, percPos);
    		System.out.println("===== Training > Positive instances copied to training folder. \n");
    	}			
		
	}
	

	

}
