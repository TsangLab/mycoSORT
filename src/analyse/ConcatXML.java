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


package analyse;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import configure.PathConstants;

/**
 * Generates a corpus from raw XML doc instances, 
 * so that features can be extracted from it
 *   
 * @author halmeida
 *
 */
public class ConcatXML extends Extractor{
	
	private String tag1;
	private String tag2;
	private String tag3;


	public ConcatXML(){		
		this.id = "<PubmedArticleSet><PubmedArticle><MedlineCitation";
		this.endId = "</PMID>";	
		this.openFile = "<corpus>";
		this.endFile = "</corpus>";
		this.openJournal = "<Title>";
		this.tag1 = "<?xml version=\"1.0\"?>";
		this.tag2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
		this.tag3 = "<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2013//EN\" \"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_130101.dtd\">";
	}
	
	public static void main(String[] args) throws IOException {	
		
		PathConstants pathVars = new PathConstants();									
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_hh:mm").format(new Date());
		
		String trainCorpusPath = pathVars.HOME_DIR + pathVars.CORPUS_DIR + pathVars.TRAIN_DIR +pathVars.TRAINING_FILE;
		String xmlDir = "train";
		String sourceDir = pathVars.HOME_DIR + pathVars.CORPUS_DIR + "all_nbs/"+ xmlDir;
		String duplicatesDir = pathVars.HOME_DIR + pathVars.CORPUS_DIR + "/src"+ "/annotated_GH27-36_2013_12_31";
				
		String concatCorpus = pathVars.HOME_DIR + pathVars.CORPUS_DIR +"triagecorpus_"+ xmlDir +"_"+timeStamp+".xml";
		String tagCorpus = concatCorpus;
		
		ConcatXML concat = new ConcatXML();		
		
		//================= Checking for duplicates =====================//
		//concat.checkDupCorpus(trainCorpusPath, sourceDir);
		//concat.checkDupFolder(sourceDir, duplicatesDir);
		
		
		//================== Creating corpus ==========================//		
		concat.cleanXML(sourceDir);
		//concat.cleanXML(duplicatesDir);
		concat.concatenateXML(sourceDir, "", concatCorpus);
		concat.tagCorpus(tagCorpus);	
	}	
	
	/**
	 * Reads the file IDs in a folder and 
	 * checks a second folder for duplicates. 
	 *  
	 * @param dirSrc source folder
	 * @param dirDup folder to check for duplicates
	 */
	
	public void checkDupFolder(String dirSrc, String dirDup){
		ArrayList<String> sourceIDs = new ArrayList<String>();
		ArrayList<String> duplicated = new ArrayList<String>();
		ArrayList<String> dupIDs = new ArrayList<String>();
		int ids = 0;

		if(dirSrc.contentEquals(dirDup)){		
			System.out.println("Source and duplicates directories are the same.\n\n========================\n");			
		}		
		else {		

			File sourceDir = new File(dirSrc);
			File[] srcXMLs = sourceDir.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name){
					return name.endsWith(".xml");
				}
			});		

			try{
				//for each file on the source dir 
				for (File xml : srcXMLs){				

					try{
						BufferedReader reader = new BufferedReader(new FileReader(xml.getPath()));

						String line = null;

						String id  = null;

						while((line = reader.readLine()) != null){

							line = line.replaceAll("\t","");
							line = line.replace("\"", "");	

							//get the IDs of the new files
							if (line.contains(getid())){

								line = line.substring(line.indexOf("><PMID"), line.indexOf(getendId()));
								line = line.replaceAll("><PMID Version=1>", "");

								id = line.replace(getendId(), "");

								sourceIDs.add(id);							

								line = reader.readLine();
								line = line.replaceAll("\t","");
							}

							if(line.contains(getOpenJournal())){
								ids++;					
							}

							line = line.replaceAll("\t","");
							line = line.replace("\"", "");
						}

						reader.close();

					}catch (FileNotFoundException e) {
						e.printStackTrace();
					}

				}				

			}catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch(Exception e){
				throw new RuntimeException(e);
			}

			System.out.println(ids + " source file IDs encountered.");
			ids = 0;

			File dupDir = new File(dirDup);

			File[] dupXMLs = dupDir.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name){
					return name.endsWith(".xml");
				}
			});		

			try{
				//for each file on the possibly duplicated dir 
				for (File xml : dupXMLs){				

					try{
						BufferedReader reader = new BufferedReader(new FileReader(xml.getPath()));

						String line = null;

						String id  = null;

						while((line = reader.readLine()) != null){

							line = line.replaceAll("\t","");
							line = line.replace("\"", "");	

							//get the IDs of the new files
							if (line.contains(getid())){

								line = line.substring(line.indexOf("><PMID"), line.indexOf(getendId()));
								line = line.replaceAll("><PMID Version=1>", "");

								id = line.replace(getendId(), "");

								dupIDs.add(id);
								String dupFileID = id;

								for(int j = 0; j < sourceIDs.size(); j++){
									if(sourceIDs.get(j).equalsIgnoreCase(dupFileID)){
										//moving the original file									
										Path from = xml.toPath(); //convert from File to Path
										Path to = Paths.get(xml.toPath()+".duplicated"); //convert from String to Path
										Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
									}
								}


								line = reader.readLine();
								line = line.replaceAll("\t","");
							}

							if(line.contains(getOpenJournal())){
								ids++;					
							}

							line = line.replaceAll("\t","");
							line = line.replace("\"", "");
						}

						reader.close();

					}catch (FileNotFoundException e) {
						e.printStackTrace();
					}

				}				

			}catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch(Exception e){
				throw new RuntimeException(e);
			}

			//count number of existing papers on possibly duplicated folder
			//just to make sure we are gathering all IDs
			System.out.println(ids + " new file IDs encountered.");
			ids = 0;

			//for each possible duplicated ID, 
			//check if it exists on source folder ID list
			//if yes, list the duplicated ones
			for(int i = 0; i < dupIDs.size(); i++){
				for(int j = 0; j < sourceIDs.size(); j++){
					if(sourceIDs.get(j).equalsIgnoreCase(dupIDs.get(i))){
						duplicated.add(dupIDs.get(i));
					}
				}
			}

			//sorting the list of duplicated IDs
			Collections.sort(duplicated, new Comparator<String>(){
				@Override
				public int compare(String one, String two){
					return one.compareTo(two);
				}
			});	

			System.out.println("\nReaded source files: " + sourceIDs.size());				
			System.out.println("Readed new files: " + dupIDs.size());	

			System.out.println("\nDuplicated files renamed: " + duplicated.size()+"\n");

			System.out.println("\nDuplicated files IDs: ");
			for(int i = 0; i < duplicated.size(); i++){
				System.out.println(duplicated.get(i));
			}

			System.out.println("\n========================\n");
		}


	}
	
	/**
	 * Reads the corpus and checks the papers IDs
	 * to identify duplicates in case new papers 
	 * are being concatenated to corpus.
	 * 
	 * @param corpus path to current corpora to check
	 * @param dir path to folder with new files to be concatenated
	 */
	
	public void checkDupCorpus(String corpus, String dir){
		ArrayList<String> trainingIDs = new ArrayList<String>();
		ArrayList<String> duplicated = new ArrayList<String>();
		ArrayList<String> newFiles = new ArrayList<String>();
		
		int ids = 0;
		
		try 
		{			
			BufferedReader reader = new BufferedReader(new FileReader(corpus));
		
			String line = null;
			String id = null;
			
			
			while((line = reader.readLine()) != null){
				
				line = line.replaceAll("\t","");
				line = line.replace("\"", "");				

				//on the previous training corpus
				//find exact paper ID and store it
				if (line.contains(getid())){					
					
					line = line.substring(line.indexOf("><PMID"), line.indexOf(getendId()));
					line = line.replaceAll("><PMID Version=1>", "");
										
					id = line.replace(getendId(), "");
					
					//insert paper ID to existing training file list
					trainingIDs.add(id);									
					
					line = reader.readLine();
					line = line.replaceAll("\t","");					
				}
				
				//count number of existing papers on the training file
				//just to make sure we are gathering all IDs
				if(line.contains(getOpenJournal())){
					ids++;					
				}
				
				line = line.replaceAll("\t","");
				line = line.replace("\"", "");
			}
			
			reader.close();
		
		}catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		System.out.println(ids + " training file IDs encountered.");
		ids = 0;
		
		File corpusDir = new File(dir);
		File[] newXMLs = corpusDir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
				return name.endsWith(".xml");
			}
		});		
		
		try{
			//for each file on the corpus dir 
			for (File xml : newXMLs){				

				try{
					BufferedReader reader = new BufferedReader(new FileReader(xml.getPath()));

					String line = null;

					String id  = null;

					while((line = reader.readLine()) != null){

						line = line.replaceAll("\t","");
						line = line.replace("\"", "");	

						//get the IDs of the new files
						if (line.contains(getid())){

							line = line.substring(line.indexOf("><PMID"), line.indexOf(getendId()));
							line = line.replaceAll("><PMID Version=1>", "");

							id = line.replace(getendId(), "");

							newFiles.add(id);
							String newFileID = id;
							
							for(int j = 0; j < trainingIDs.size(); j++){
								if(trainingIDs.get(j).equalsIgnoreCase(newFileID)){
									 //moving the original file									
									Path from = xml.toPath(); //convert from File to Path
									Path to = Paths.get(xml.toPath()+".duplicated"); //convert from String to Path
						    	    Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
								}
							}
							

							line = reader.readLine();
							line = line.replaceAll("\t","");
						}
						
						if(line.contains(getOpenJournal())){
							ids++;					
						}

						line = line.replaceAll("\t","");
						line = line.replace("\"", "");
					}
				
					reader.close();
						
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			}				

		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
		
		//count number of existing papers on the training file
		//just to make sure we are gathering all IDs
		System.out.println(ids + " new file IDs encountered.");
		ids = 0;
		
		//for each new ID, check if it exists on training file ID list
		//if yes, list the duplicated ones
		for(int i = 0; i < newFiles.size(); i++){
			for(int j = 0; j < trainingIDs.size(); j++){
				if(trainingIDs.get(j).equalsIgnoreCase(newFiles.get(i))){
					duplicated.add(newFiles.get(i));
				}
			}
		}
		
		//sorting the list of duplicated IDs
		Collections.sort(duplicated, new Comparator<String>(){
			@Override
			public int compare(String one, String two){
				return one.compareTo(two);
			}
		});	
		
		System.out.println("\nReaded training files: " + trainingIDs.size());				
		System.out.println("Readed new files: " + newFiles.size());	
				
		System.out.println("\nDuplicated files renamed: " + duplicated.size()+"\n");
		
		System.out.println("\nDuplicated files IDs: ");
		for(int i = 0; i < duplicated.size(); i++){
			System.out.println(duplicated.get(i));
		}
		
		System.out.println("\n========================\n");
		
	}
	
	
	/**
	 * Reads and edits a list of XMLs files in a folder
	 * to remove XML and previous corpus tags, 
	 * preparing the files to be concatenated. 
	 *  
	 * @param dir string with folder path
	 */
	
	public void cleanXML(String dir){		

		//listing files on corpus dir
		File sourceDir = new File(dir);
		
		File[] newXMLs = sourceDir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
				return name.endsWith(".xml");
			}
		});		

		System.out.println("... Files list loaded.");				

		try{
			//for each file on the corpus dir 
			for (File xml : newXMLs){				

				try{
					BufferedReader reader = new BufferedReader(new FileReader(xml.getPath()));

					String line = null;
					ArrayList<String> allLines = new ArrayList<String>();
					String content  = null;

					while((line = reader.readLine()) != null){						
						content = line;	

						//cleaning XML markups
						if(content.contains(getTag1())){							
							content = content.replace(getTag1(), "");
							allLines.add(content);							
						}
						if(content.contains(getTag2())){							
							content = content.replace(getTag2(), "");
							allLines.add(content);							
						}
						if(content.contains(getTag3())){							
							content = content.replace(getTag3(), "");
							allLines.add(content);							
						}
						
						//cleaning previous corpus tags
						if(content.contains(getOpenFile())){
							content = content.replace(getOpenFile(), "");
							allLines.add(content);
						}
						if(content.contains(getendFile())){
							content = content.replace(getendFile(), "");
							allLines.add(content);
						}

						allLines.add(content);						
					}					

					PrintWriter writer = new PrintWriter(xml.getPath());

					for (String l : allLines){
						writer.println(l);			
					}					
					reader.close();
					writer.close();				

				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			}				

		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}

		System.out.println("... Files cleaned and saved.");
		System.out.println("Ready for concatenation.");
		System.out.println("\n========================\n");
	}
	
	/**
	 * Concatenates all XMLs in one folder or between two folders.
	 * @param sourceDir main directory with XML files.
	 * @param duplicDir second directory with duplicated XML files 
	 * @param concatFile path name to saved concatenated corpus
	 */
	
	public void concatenateXML(String sourceDir, String duplicDir, String concatFile){		

		final int BUFFER = 1024 << 8;
		byte[] buffer = new byte[BUFFER];

		//listing files on corpus dir
		File srcDir = new File(sourceDir);
		File[] srcXMLs = srcDir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
				return name.endsWith(".xml");
			}
		});
		
		File dupDir = new File(duplicDir);
		File[] dupXMLs = dupDir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {				
				return name.endsWith(".xml");
			}			
		}); 
		
		System.out.println("... Files list loaded.");		

		//defining the output file (concatenated)
		File newCorpus = new File(concatFile);		

		try{	
			OutputStream output = new BufferedOutputStream(new FileOutputStream(newCorpus));
			

			//for each file on the corpus dir 
			for (File xmls : srcXMLs){				
				InputStream input = new FileInputStream(xmls);				
				int count;				
				
				//if the file is not empty/finished
				try{
					while((count = input.read(buffer)) >= 0){										
						
						//write it on the concatenated final file
						output.write(buffer, 0, count);
					}
				}finally{
					input.close();
				}
			}
			
		if(dupXMLs != null){
			for(File xmld : dupXMLs){
				InputStream input = new FileInputStream(xmld);				
				int count;				
				
				//if the file is not empty/finished
				try{
					while((count = input.read(buffer)) >= 0){										
						
						//write it on the concatenated final file
						output.write(buffer, 0, count);
					}
				}finally{
					input.close();
				}
			}
		}
			output.flush();
			output.close();				
			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}

		System.out.println("... File concatenated and saved.");
		System.out.println("Ready for corpus tagging.");
		System.out.println("\n========================\n");
	}
	
	/**
	 * Inserts corpus tag on XML file
	 * 
	 * @param pathToCorpus path to 
	 * 		  concatenated corpus 
	 */
	
	public void tagCorpus(String pathToCorpus){
		
		//tagging as corpus		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(pathToCorpus));
						
			String line = null;
			String edit = null;
			List<String> allLines = new ArrayList<String>();
			
			//adds tag at beggining of corpus
			allLines.add(getOpenFile());
			
			while((line = reader.readLine()) != null){	
				 
				allLines.add(line);					
			}
			//adds tag at the end of corpus
			allLines.add(getendFile());			
			
			System.out.println("... Corpus loaded and tagged.");
			//re-writting the file
			PrintWriter writer = new PrintWriter(pathToCorpus);
			
			for (String l : allLines){
				writer.println(l);			
			}
			reader.close();
			writer.close();
			
			System.out.println("... File saved as tagged corpus.");			
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public String getTag1() {
		return tag1;
	}

	public void setTag1(String tag1) {
		this.tag1 = tag1;
	}

	public String getTag2() {
		return tag2;
	}

	public void setTag2(String tag2) {
		this.tag2 = tag2;
	}
	
	public String getTag3() {
		return tag3;
	}

	public void setTag3(String tag3) {
		this.tag3 = tag3;
	}
	
		
}


