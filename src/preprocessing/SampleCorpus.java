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

import configure.PathConstants;

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

		PathConstants pathVars = new PathConstants();
		SampleCorpus sampling = new SampleCorpus();

		String positiveDir = pathVars.HOME_DIR + pathVars.CORPUS_DIR + pathVars.POS_DIR;
		List positives = new LinkedList();

		String negativeDir = pathVars.HOME_DIR + pathVars.CORPUS_DIR + pathVars.NEG_DIR;
		List negatives = new LinkedList();

		//train or test sampling
		Boolean tr = true, ts = true;
		//% of test corpus WRT the collection, % positive on test set, % positive on training set 
		int percTs = 20, posTr = 50, posTs = 10;

		for(int i = 0; i < args.length; i++){
			try{				
				if(args[i].matches("-tr")){ 
					tr = true;
					posTr = Integer.parseInt(args[i+1]);					
				}				 
				if(args[i].matches("-ts")){
					ts = true;
					percTs = Integer.parseInt(args[i+1]);
					posTs = Integer.parseInt(args[i+2]);
				}				
			}
			catch(Exception e){
				System.out.println(" Use: \n "
						+ "-tr -> (% of positives) to sample trainig set \n"
						+ "-ts -> (% of collection) (% of positives) to sample test set");
				System.exit(0);
			};
		}
		
		positives = sampling.loadFiles(positiveDir);
		negatives = sampling.loadFiles(negativeDir);
		
		if(tr) sampling.sampleTest(pathVars, positives, negatives, percTs, posTs);
		
		if(ts) sampling.sampleTrain(pathVars, positives, negatives, posTr);		

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
	public void moveFile(PathConstants pathVars, List files, int numFiles){
		
		Iterator<File> filesList = files.iterator();
		File testDir = new File(pathVars.HOME_DIR + pathVars.CORPUS_DIR + pathVars.TEST_DIR);
		
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
	public void copyFile(PathConstants pathVars, List files, int numFiles){
		
		Iterator<File> filesList = files.iterator();
		File trainDir = new File(pathVars.HOME_DIR + pathVars.CORPUS_DIR + pathVars.TRAIN_DIR);
		
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
	public void sampleTest(PathConstants pathVars, List positives, List negatives, int total, int pos){
		
		int instances = positives.size() + negatives.size();		
		int testSize = (instances * total) / 100; 		
		int posSize = (testSize * pos) / 100;		
		int negSize = testSize - posSize;		
		
		Collections.shuffle(negatives);	
		System.out.println("===== Test > Negative instances shuffled for test set.");
		moveFile(pathVars, negatives, negSize);
		System.out.println("===== Test > Negative instances moved to test folder. \n");
		
		Collections.shuffle(positives);	
		System.out.println("===== Test > Positive instances shuffled for test set.");
		moveFile(pathVars, positives, posSize);	
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
    public void sampleTrain(PathConstants pathVars, List positives, List negatives, int pos){
		
    	int trainSize = positives.size() + negatives.size();  	
    	int posSize = (trainSize * pos) / 100;
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
    		copyFile(pathVars, negatives, negSize);
    		System.out.println("===== Training > Negative instances copied to training folder. \n");
    		
    		Collections.shuffle(positives);
    		System.out.println("===== Training > Positive instances shuffled for training set.");
    		copyFile(pathVars, positives, posSize);
    		System.out.println("===== Training > Positive instances copied to training folder. \n");
    	}			
		
	}
	

	

}
