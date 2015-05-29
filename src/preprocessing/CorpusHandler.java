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


package preprocessing;

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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import configure.ConfigConstants;

/**
 * Generates a corpus from raw XML doc instances, 
 * so that features can be extracted from it
 *   
 * @author Hayda Almeida
 * @since 2014
 *
 */
public class CorpusHandler{
	
	private String tag1;
	private String tag2;
	private String tag3;
	private String tag4;
	private String id;
	private String corpusTag;
	private String corpusTagC;


	public CorpusHandler(){	

		this.setId("PMID");				
		this.setTag1("(?s)<.*?xml.*?>");
		this.setTag2("(?s)<.*?!DOCTYPE.*?>");
		this.setTag3("(?s)<.*?corpus.*?>");
		this.seTag4("(?s)<.*?/corpus.*?>");
		this.setCorpusTag("<corpus>");
		this.setCorpusTag("</corpus>");		
	}
	
	

	public static void main(String[] args) throws Exception {	
		
		ConfigConstants pathVars = new ConfigConstants();
		
		String xmlDir = "";
		if(Integer.parseInt(pathVars.EXP_TYPE)== 1)
			xmlDir = pathVars.TEST_DIR.substring(0, pathVars.TEST_DIR.length()-1);
		else xmlDir = pathVars.TRAIN_DIR.substring(0, pathVars.TRAIN_DIR.length()-1) + "_" + pathVars.PERCT_POS_TRAIN;
		
		String sourceDir = "", duplicatesDir = "";

		Boolean dc = false, df = false, cl = false, cc = false;

		String param = "";

		try{
			param = args[0];

			if(param.length() > 1){
				if(param.contains("dc"))
					dc = true;
				if(param.contains("df"))
					df = true;
				if(param.contains("cl"))
					cl = true;
				if(param.contains("cc"))
					cc = true;
			}
		}
		catch(Exception e){
			System.out.println("Use: \n"				
					+ "-dc 	-> check duplicates in corpus vs. folder; \n "
					+ "-df  -> check duplicates in two folders; \n"
					+ "-cl  -> clean a source folder; \n"
					+ "-cc  -> concatenate files in a folder ");
			System.exit(0);
		};		
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_hh:mm").format(new Date());
		String trainCorpusPath = pathVars.HOME_DIR + pathVars.CORPUS_DIR + pathVars.TRAINING_FILE;

		sourceDir = pathVars.HOME_DIR + pathVars.CORPUS_DIR + xmlDir;
		duplicatesDir = pathVars.HOME_DIR + pathVars.CORPUS_DIR + pathVars.DUP_DIR;
				
		String concatCorpus = pathVars.HOME_DIR + pathVars.CORPUS_DIR +"triagecorpus_"+ xmlDir +"_"+timeStamp+".xml";
		String tagCorpus = concatCorpus;
		
		CorpusHandler concat = new CorpusHandler();		
		
		//================= Checking for duplicates =====================//
		//
		//Check for duplicates between training file and a specific folder
		if(dc) concat.checkDupCorpus(trainCorpusPath, sourceDir);
		//
		//----------------------------------------------------
		//
		//Check for duplicates between two folders (duplicates found being sinalized in duplicatesDir)
		if(df) concat.checkDupFolder(sourceDir, duplicatesDir);
		//
		//==================== Creating corpus ==========================//		
		//
		//Removing XML tags from files 
		if(cl){
			concat.cleanXML(sourceDir, xmlDir);
			if(duplicatesDir.length()>1 && (dc || df))
				concat.cleanXML(duplicatesDir, xmlDir);
		}
		//
		//------------------------------------
		//
		//Concatenating files from folders and outputting a corpus file
		//Inserting <corpus> tag in file 
		if(cc){
			concat.concatenateXML(sourceDir, "", concatCorpus, xmlDir);
			concat.tagCorpus(tagCorpus, xmlDir);
		  }
		//
		//===============================================================//
	}	
	
	/**
	 * Returns the ID of a XML jsoup document
	 * @param doc  a XML doc parsed by jsoup 
	 * @return ID string
	 * @throws IOException
	 */
	public String returnID(Document doc) throws IOException{
		
		String id = "";
		
		Elements paper = doc.body().getElementsByTag("pubmedarticleset");						
								
		//fetching the paper ID - 
		//for all items in a paper, retrieve only PMIDs 
		for(Element e : paper.select(getId())){
			//only consider the ID if the parent is medline citation
			if(e.parentNode().nodeName().contains("medline")){						
				id = e.text();
			}
		}
		return id;
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
			
			System.out.println("Source directory: "+ dirSrc + " \n");
			System.out.println("Duplicates directory: " + dirDup + " \n");
			
			//Loading files in the source folder
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
						
						String id  = "";
						//Loading file
						File input = new File(xml.getPath());
						//Jsoup parse
						Document doc = Jsoup.parse(input, "UTF-8");
												
						//fetching the document ID
						id = returnID(doc);

						if(!id.isEmpty()){
							sourceIDs.add(id);
							ids++;
						}

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

			//Loading files in the duplicated folder
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
						String id  = "";
												//Loading file
						File input = new File(xml.getPath());
						//Jsoup parse
						Document doc = Jsoup.parse(input, "UTF-8");
												
						//fetching the document ID
						id = returnID(doc);

						if(!id.isEmpty()){
							dupIDs.add(id);
							String dupFileID = id;
							ids++;
							
							for(int j = 0; j < sourceIDs.size(); j++){
								if(sourceIDs.get(j).equalsIgnoreCase(dupFileID)){
									
									//add ID to duplicated list
									duplicated.add(dupFileID);
									
									//rename the original file									
									Path from = xml.toPath(); //convert from File to Path
									Path to = Paths.get(xml.toPath()+".duplicated"); //convert from String to Path
						    	    Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
								}
							}							
						}

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
			System.out.println("Corpus directory: "+ corpus + " \n");
			System.out.println("Duplicates directory: " + dir + " \n");
			
			File input = new File(corpus);
			//Jsoup parse
			Document doc = Jsoup.parse(input, "UTF-8");
			Elements corp = doc.body().getElementsByTag("pubmedarticleset");

			String id  = "";		

			for(Element paper : corp){
				Document thisDoc = Jsoup.parseBodyFragment(paper.toString());
				
				//fetching the document ID
				id = returnID(thisDoc);

				if(!id.isEmpty()){
					trainingIDs.add(id);
					ids++;
				}	
			}		
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
					String id  = "";
					//Loading file
					File input = new File(xml.getPath());
					//Jsoup parse
					Document doc = Jsoup.parse(input, "UTF-8");

					//fetching the document ID
					id = returnID(doc);

					if(!id.isEmpty()){						

						newFiles.add(id);
						String newFileID = id;
						ids++;


						for(int j = 0; j < trainingIDs.size(); j++){
							if(trainingIDs.get(j).equalsIgnoreCase(newFileID)){

								//add ID to duplicated list
								duplicated.add(newFileID);

								//moving the original file									
								Path from = xml.toPath(); //convert from File to Path
								Path to = Paths.get(xml.toPath()+".duplicated"); //convert from String to Path
								Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
							}
						}
					}
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
	
	public void cleanXML(String dir, String xmlDir){	

		//listing files on corpus dir
		File sourceDir = new File(dir);
		
		File[] newXMLs = sourceDir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
				return name.endsWith(".xml");
			}
		});		

		System.out.println("... Files list loaded: "+ dir);					

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
						content = content.replaceFirst(getTag1(), "");
						content = content.replaceFirst(getTag2(), "");
						//cleaning previous corpus tags
						content = content.replaceFirst(getTag3(), "");
						content = content.replaceFirst(getTag4(), "");
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

		System.out.println("... Files cleaned and saved for " + xmlDir + ".");
		System.out.println("Ready for concatenation.");
		System.out.println("\n========================\n");
		
	}
	


	/**
	 * Concatenates all XMLs in one folder or between two folders.
	 * @param sourceDir main directory with XML files.
	 * @param duplicDir second directory with duplicated XML files 
	 * @param concatFile path name to saved concatenated corpus
	 */
	
	public void concatenateXML(String sourceDir, String duplicDir, String concatFile, String xmlDir){		

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
		
		System.out.println("... Files list loaded: "+ xmlDir + ".");			

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

		System.out.println("... File concatenated and saved for "+ xmlDir+ ".");
		System.out.println("Ready for corpus tagging.");
		System.out.println("\n========================\n");
	}
	
	/**
	 * Inserts corpus tag on XML file
	 * 
	 * @param pathToCorpus path to 
	 * 		  concatenated corpus 
	 */
	
	public void tagCorpus(String pathToCorpus, String xmlDir){
		
		//tagging as corpus		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(pathToCorpus));
						
			String line = null;
			String edit = null;
			List<String> allLines = new ArrayList<String>();
			
			//adds tag at beggining of corpus
			allLines.add(getCorpusTag());
			
			while((line = reader.readLine()) != null){	
				 
				allLines.add(line);					
			}
			//adds tag at the end of corpus
			allLines.add(getCorpusTagC());			
			
			System.out.println("... Corpus loaded and tagged.");
			//re-writting the file
			PrintWriter writer = new PrintWriter(pathToCorpus);
			
			for (String l : allLines){
				writer.println(l);			
			}
			reader.close();
			writer.close();
			
			System.out.println("... File saved as tagged " + xmlDir +  " corpus.");
			System.out.println("... DONE!");		
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private String getCorpusTagC() {		
		return corpusTagC;
	}

	private String getCorpusTag() {
		// TODO Auto-generated method stub
		return corpusTag;
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
	
	private String getTag4() {
		// TODO Auto-generated method stub
		return tag4;
	}

	private String getTag3() {
		// TODO Auto-generated method stub
		return tag3;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}	
	
	private void setCorpusTag(String string) {
		this.corpusTag = string;
		
	}
	
	private void setCorpusTagC(String string) {
		this.corpusTagC = string;
		
	}

	private void seTag4(String string) {
		this.tag4 = string;
		
	}

	private void setTag3(String string) {
		this.tag3 = string;
		
	}
		
}


