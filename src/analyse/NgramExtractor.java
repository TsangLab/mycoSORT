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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import configure.PathConstants;

/**
 * This class extracts and parses n-grams
 * from doc instances.
 * 
 * @author halmeida
 */

public class NgramExtractor extends Extractor{
		
	public NgramExtractor(){
		this.id = "<PMID Version=1>";
		this.endId = "</PMID>";
		this.endFile = "</PubmedArticleSet>";
		this.openAbst = "<AbstractText>";
		this.closeAbst = "</AbstractText>";
		this.abstractLabel = "<AbstractText ";
		this.classTag = "<TRIAGE>";
		this.openTitle = "<ArticleTitle>";
		this.closeTitle = "</ArticleTitle>";
	}	
	
	static String certainty = "?"; //very relevant, relevant, fairly relevant
	
	
	public static void main(String[] args) {
		
		PathConstants pathVars = new PathConstants();
		
		String AnCorpus = pathVars.HOME_DIR + pathVars.CORPUS_DIR + pathVars.TRAIN_DIR +pathVars.TRAINING_FILE;
		NgramExtractor nextrac = new NgramExtractor();
		//store abstract ngrams and its count
		HashMap<String,Integer> ngram_count = new HashMap<String,Integer>();
		//store abstract ngrams, count and "relevance(TBD)"
		HashMap<Map<String,String>,Integer> ngrams  = new HashMap<Map<String,String>,Integer>();
		//store title ngrams and its count
		HashMap<String,Integer> ngram_title_count = new HashMap<String,Integer>();
		//store title ngrams, count and "relevance(TBD)"
		HashMap<Map<String,String>,Integer> ngram_title = new HashMap<Map<String,String>,Integer>();
		
		nextrac.initialize();		
		
		try 
		{			
			BufferedReader reader = new BufferedReader(new FileReader(AnCorpus));	       

			//---------------------------
			// repeat until all lines 
			// of the file are read
			//---------------------------
			String line = null;
			String features = null;
			String id = null;


			while((line = reader.readLine()) != null){

				line = line.replaceAll("\t","");
				line = line.replace("\"", "");

				//find paper ID and store it
				if (line.contains(nextrac.getid())){
					line = line.replace(nextrac.getid(), "");
					id = line.replace(nextrac.getendId(), "");

					//keep reading the file
					features = reader.readLine();
					features = features.replaceAll("\t","");	       		

					String tit_content = "";

					//continue reading until the end of file
					while(!(features.contentEquals(nextrac.getendFile()))){
						
						String abstrac = "";

						//find relevant doc section - Article title
						if(features.contains(nextrac.getOpenTitle())){

							//cleaning title content
							features = features.replace(nextrac.getOpenTitle(),"");
							features = features.replace(nextrac.getCloseTitle(), "");
							features = nextrac.removeSpecialChar(features);
							tit_content = nextrac.removeTags(features);

							//extract n-grams from section
							ArrayList<String> title_c = nextrac.nGrams(tit_content, pathVars);
							nextrac.addNGram(title_c, ngram_title_count,ngram_title, pathVars);

							features = reader.readLine();
							features = features.replaceAll("\t","");
						}
						

						if(features.contains(nextrac.getAbstractLabel())){
							
							String temp = "";
							String newAbs = nextrac.getopenAbst();
							
							if(features.contains("</Abstract>")){
								temp = temp + nextrac.processAbstract(features);
							}
							else{						
								do{							
									temp = temp + nextrac.processAbstract(features);								
									features = reader.readLine();							
								}while(!(features.contains("</Abstract>")));
							}
								
							newAbs = newAbs + temp;
							features = newAbs + nextrac.getcloseAbst();							
						}

						//find relevant paper section
						if(features.contains(nextrac.getopenAbst())){							
							
							features = features.replace(nextrac.getopenAbst(),"");
							features = features.replace(nextrac.getcloseAbst(), "");
							features = features.replace("-", " ");
							features = nextrac.removeSpecialChar(features);
							
							//handle lines in which abstract text tag
							//is separated from the actual text
							if(features.isEmpty()){
								features = reader.readLine();
								features = features.replaceAll("\t","");
								features = features.replace(nextrac.getopenAbst(),"");
								features = features.replace(nextrac.getcloseAbst(), "");
								features = features.replace("-", " ");
								features = nextrac.removeSpecialChar(features);
							}					
							
							//features = nextrac.removeSpecialChar(features);
							abstrac = nextrac.removeTags(features);
							abstrac = nextrac.removeAbstractTags(abstrac);
							//extract n-grams from section
							ArrayList<String> abstract_c = nextrac.nGrams(abstrac, pathVars);
							nextrac.addNGram(abstract_c, ngram_count, ngrams, pathVars);												

							//keep reading file
							features = reader.readLine();
							features = features.replaceAll("\t","");
							//features = features.replaceAll("\\s+", "");
						}
						
						features = reader.readLine();
						features = features.replaceAll("\t","");
						//features = features.replaceAll("\\s+", "");
					}			
				}
			}			

			reader.close();      				


		}catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
        
		//print list of extracted n-grams
		//System.out.println("\n========ABSTRACT==NGRAMS=============");
		//nextrac.displayList(ngram_count);
		//nextrac.displayList(ngram_title);
		//System.out.println("\n===========TITLE==NGRAMS=============");
		//nextrac.displayList(ngram_title_count);
		
		
		nextrac.considerOccurance(ngram_count, pathVars);
		nextrac.considerOccurance(ngram_title_count, pathVars);
		
		
		System.out.println("\n===========NGRAMS==EXPORT===============\n");		
		nextrac.exportFile(pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.NGRAM_FEATURES, ngram_count);
		System.out.println("..."+ ngram_count.size()+" unique Abstract ngrams saved.");
		nextrac.exportFile(pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.TITLE_NGRAMS, ngram_title_count);
		System.out.println("... "+ ngram_title_count.size() +" unique Title ngrams saved.");		
		System.out.println("\n========================================\n");		
               
	}
	
	
	/**
	 * Removes from feature list all features with 
	 * frequency not statistically relevant (2 or less)
	 * @param list to be cleaned
	 */
	
	private void considerOccurance(HashMap<String,Integer> list, PathConstants vars){
		//going over the list of annotations and removing the
		//statistically not significant features - frequency less than 2

		Iterator <Integer> iterator = list.values().iterator();

		while(iterator.hasNext()){
			Integer key = iterator.next();

			if(key < Integer.parseInt(vars.FEATURE_MIN_FREQ)){
				iterator.remove();				
			}
		}
	}
	
	private void addNGram(ArrayList<String> str, HashMap<String,Integer> list_count, HashMap<Map<String,String>,Integer> list, PathConstants pathVars){
		
		for(int i = 0; i < str.size(); i++){
			String currentNGram = str.get(i);
			
			if(list_count.containsKey(currentNGram)){
				int count = list_count.get(currentNGram);
				list_count.put(currentNGram, count+1);

				/*if(list.containsKey(currentNGram)){		
					int cnt = list.get(currentNGram).get(certainty);
					list.get(currentNGram).put(certainty, cnt+1);
				}
				else{
					list.get(currentNGram).put(certainty, 1);
				}*/
			}
			else {
				if(currentNGram.length() >= Integer.parseInt(pathVars.FEATURE_MIN_LENGTH)){
					list_count.put(currentNGram, 1);
					
				/*	list.put(currentNGram, new HashMap<String, Integer>());
					list.get(currentNGram).put(certainty, 1);*/
				}
			}
		}
	}
	
	/**
	 * Extracts n-grams from the content field
	 * and populates mapping with n-gram +count
	 * @param str
	 * @param id
	 * @param gram
	 */
	
	public ArrayList<String> nGrams(String str, PathConstants pathVar){

		//cleaning further chars on sentence		
		str = str.replace("/", "");
		str = str.replace("\\", "");		
		str = str.replace(" ", "-");
		//Tokenize the sentence
		String[] words = StringUtils.split(str,"-"); 
		ArrayList<String> ngramList = new ArrayList<String>();

		int ngram =Integer.parseInt(pathVar.NGRAM_SIZE);

		if(Boolean.valueOf(pathVar.NGRAM_STOP)){
			words = StringUtils.split(removeStopList(words, pathVar)," ");
		}		

		for(int i=0; i < words.length - (ngram - 1); i++){
			switch(pathVar.NGRAM_SIZE){
			case "1":
				ngramList.add(words[i].toLowerCase());
				break;
			case "2":
				ngramList.add(words[i].toLowerCase()+" "+words[i+1].toLowerCase());
				break;
			case "3":
				ngramList.add(words[i].toLowerCase()+" "+words[i+1].toLowerCase()+" "+words[i+2].toLowerCase());
				break;				
			}			
		}
		
		return ngramList;
	}
	
	/**
	 * Removes the stopwords from ngrams list
	 * 
	 * @param str list of ngrams
	 * @param pathVar constants from 
	 * @return
	 */
	
	public String removeStopList(String[] str, PathConstants pathVar){
		
		String pathStop = "stopList.txt";
		String[] stop = null;
		StringBuilder cleaned = new StringBuilder();
		
		try{
			
			BufferedReader reader = new BufferedReader(new FileReader(pathStop));
			
			String line = null;	
			
			while((line = reader.readLine()) != null){
				stop = StringUtils.split(line,",");
				line = reader.readLine();
			}
			
			reader.close();
			
		}catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 		
		
		for(int i = 0; i < str.length; i++){
			for(int j = 0; j < stop.length; j++){
				
				if(str[i].equalsIgnoreCase(stop[j])){
					str[i] = str[i].replace(str[i],"*");					
				}				
			}
			if(!(str[i].contentEquals("*"))){
				cleaned.append(str[i]).append(" ");				
			}
		}		
		return cleaned.toString().replace("  ", " ");
	}
	
	/**
	 * Evaluates the level of certainty... 
	 * TBD!!!
	 * @param list
	 * @return
	 */
	
	public String getCertainty(HashMap<String,Map<String,Integer>> list){
		
		ArrayList<Object> gramsAr = new ArrayList<Object>(list.entrySet());
		//String certainty;

		Iterator<?> itr = gramsAr.iterator();
		while(itr.hasNext()){
			String str = itr.next().toString();
			String[] splitted = StringUtils.split(str,"=");

			int relevance = 0;
			int count = 0;


			try{
				count = list.get(splitted[0]).get(certainty);
			} catch(Exception e){
				e.printStackTrace();
			}

			//relevance = count * getWeight();

			if(relevance == 1)			
				list.get(splitted[0]).put("fairly relevant", list.get(splitted[0]).get(certainty));							
			else if (relevance == 2)
				list.get(splitted[0]).put("relevant", list.get(splitted[0]).get(certainty));
			else
				list.get(splitted[0]).put("very relevant", list.get(splitted[0]).get(certainty));

		}
		
		return certainty;		
	}
	
	/**
	 * Displays the keys and values of the
	 * maps created with n-grams and counts.
	 * @param hash  HashMap containing n-grams
	 */
	@Override
	public void displayList(HashMap hash){
		super.displayList(hash);
			//sum = sum + hash.get(str);		
		System.out.println("\n=======================================\n");
		System.out.println("Number of unique n-grams: "+hash.size());
		System.out.println("\n=======================================\n");
	}
	
	
	/**
	 * Accessor and mutator methods for the export
	 * string with list values - so vector class
	 * can access its content.
	 * @return string with list of values.
	 */
	/*public static String getNgramCount() {
		//ngramCount = exportContent(ngram_count);
		return ngramCount;	
	}
	public void setNgramCount(String ngramCount) {
		this.ngramCount = ngramCount;
	}
	public static String getNgram() {
		//ngram = exportContent(ngrams);
		return ngram;
	}
	public void setNgram(String ngram) {
		this.ngram = ngram;
	}	*/
	
	
}
