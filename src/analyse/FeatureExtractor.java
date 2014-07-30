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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import configure.PathConstants;


/**
 * This class extracts and parses domain 
 * annotation features from doc instances
 *   
 * @author halmeida
 */

public class FeatureExtractor extends Extractor{	
	
	public FeatureExtractor(){
				
		this.id = "<PMID Version=1>";
		this.endId = "</PMID>";
		this.endFile = "</PubmedArticleSet>";
		this.openAbst = "<AbstractText>";
		this.closeAbst = "</AbstractText>";
		this.abstractLabel = "<AbstractText ";
		this.openEC = "<RegistryNumber>EC ";
		this.closeEC = "</RegistryNumber>";
		this.classTag = "<TRIAGE>";
		this.openJournal = "<Title>";
		this.closeJournal = "</Title>";
		this.openTitle = "<ArticleTitle>";
		this.closeTitle = "</ArticleTitle>";		
	}
		
	
	public static void main(String[] args) {
		
		PathConstants pathVars = new PathConstants();
		
		String AnCorpus = pathVars.HOME_DIR + pathVars.CORPUS_DIR +  pathVars.TRAIN_DIR + pathVars.TRAINING_FILE;
		FeatureExtractor fextrac = new FeatureExtractor();	
				
		//store all features, type and count
		HashMap<Map<String,String>,Integer> abstract_count = new HashMap<Map<String,String>,Integer>();
		//store all features, type and classification
		HashMap<Map<String,String>,String> abstract_type = new HashMap<Map<String,String>,String>();
		
		//store title features, type and classification
		HashMap<Map<String,String>,String> title_type = new HashMap<Map<String,String>,String>();
		//store title features, type and count
		HashMap<Map<String,String>, Integer> title_count = new HashMap<Map<String,String>, Integer>();
		//store title features, whole journal title content and classification
		HashMap<Map<String,String>,String> title_content = new HashMap<Map<String,String>,String>();
		
		//store title content and EC numbers
		ArrayList<String> ec_numbers = new ArrayList<String>();
				
		fextrac.initialize();
		int jTitle = 0;
				
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(AnCorpus));			

			//---------------------------
			// repeat until all lines of the file are read
			//---------------------------
			String line = null;
			String features = null;
			// String id = null;			


			while((line = reader.readLine()) != null){

				line = line.replaceAll("\t","");
				line = line.replace("\"", "");

				//find paper ID and store it
				if (line.contains(fextrac.getid())){
					line = line.replace(fextrac.getid(), "");
				//	id = line.replace(fextrac.getendId(), "");		

					//continue reading
					features = reader.readLine();
					features = features.replaceAll("\t","");
										
					String journal = "";

					//continue reading until the end of file
					while(!(features.contentEquals(fextrac.getendFile()))){											
						
						//find relevant doc section - Journal title
						if(features.contains(fextrac.getOpenJournal())){
							
							features = features.replace(fextrac.getOpenJournal(),"");
							features = features.replace(fextrac.getCloseJournal(), "");
							features = fextrac.removeSpecialChar(features);
														
							//separating only the journal title content													
							journal = fextrac.removeTags(features);
							//counting # of journal titles					
							jTitle++;
							
							features = reader.readLine();
							features = features.replaceAll("\t","");								
						}						
												
						//find relevant doc section - Article title
						if(features.contains(fextrac.getOpenTitle())){
							
							features = features.replace(fextrac.getOpenTitle(),"");
							features = features.replace(fextrac.getCloseTitle(), "");
							features = fextrac.removeSpecialChar(features);
							
							//separating the title by annotations													
							String title_annotation = features;																				
							
							//extracting annotations and inserting them on lists
							fextrac.annotations(title_annotation, title_count, title_type, pathVars);
							fextrac.addContent(title_annotation, journal, title_content);							
							
							features = reader.readLine();
							features = features.replaceAll("\t","");							
						}						
						
						if(features.contains(fextrac.getAbstractLabel())){
							
							String temp = "";
							String newAbs = fextrac.getopenAbst();						
							
							//handling cases when the tag is already within abstract content
							if(features.contains("</Abstract>")){
								temp = temp + fextrac.processAbstract(features);
							}
							else{												
								do{							
									temp = temp + fextrac.processAbstract(features);								
									features = reader.readLine();							
								}while(!(features.contains("</Abstract>")));
							}							
							newAbs = newAbs + temp;
							features = newAbs + fextrac.getcloseAbst();							
						}
						
						//find relevant doc section - Abstract
						if(features.contains(fextrac.getopenAbst())){
							
							features = features.replace(fextrac.getopenAbst(),"");
							features = features.replace(fextrac.getcloseAbst(), "");
							features = fextrac.removeSpecialChar(features);
							
							//handle lines in which abstract text tag
							//is separated from the actual text
							if(features.isEmpty()){
								features = reader.readLine();
								features = features.replaceAll("\t","");
								features = features.replace(fextrac.getopenAbst(),"");
								features = features.replace(fextrac.getcloseAbst(), "");
								features = fextrac.removeSpecialChar(features);
							}																					
							
							features = fextrac.removeAbstractTags(features);
							
							//gathering abstract annotations
							String abstrac = features;
							
							//extract annotations and insert them on lists
							fextrac.annotations(abstrac, abstract_count, abstract_type, pathVars);					

							features = reader.readLine();
							features = features.replaceAll("\t","");
							//features = features.replaceAll("\\s+", "");
						}
						
						//identifying EC number
						if(features.contains(fextrac.getOpenEC())){
							features = features.replace(fextrac.getOpenEC(), "");
							features = features.replace(fextrac.getCloseEC(), "");
							features = fextrac.removeSpecialChar(features);
							
							ec_numbers.add(features);
							
							features = reader.readLine();
							features = features.replaceAll("\t","");
						}
						
						//find classification of the document	
						if(features.contains(fextrac.getClassTag())){	
														
							//adding classification to the list of annotations
							String classif = fextrac.getClassif(features);
							fextrac.addClass(classif, abstract_type);							
							fextrac.addClass(classif, title_type);
							fextrac.addClass(classif, title_content);													
							
							features = reader.readLine();
							features = features.replaceAll("\t","");
						}

						features = reader.readLine();
						features = features.replaceAll("\t","");						

					}					

				}				

			}

			reader.close();

		}
		catch (FileNotFoundException e) {
			e.printStackTrace();			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//Use for sample output
		//System.out.println("\n===========TITLE==ANNOTATIONS=============");
		//fextrac.displayList(title_count);		
		//fextrac.displayList(title_type);
		//fextrac.displayList(title_content);
		//System.out.println("\n========ABSTRACT==ANNOTATIONS=============");
		//fextrac.displayList(abstract_count);		
		//fextrac.displayList(abstract_type);		
		
		//Before exporting, take into account the 
		//occurence of all extracted features 
		fextrac.considerOccurence(abstract_count, pathVars);
		fextrac.considerOccurence(title_count, pathVars);
		
		
		System.out.println("\n===========FEATURE==EXPORT===============");			
		fextrac.exportList(pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.ECNUM_FEATURES, ec_numbers);
		System.out.println("..."+ ec_numbers.size()+" EC numbers saved.");				
		fextrac.exportFile(pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.ANNOTATION_FEATURES, abstract_count);
		System.out.println("..."+ abstract_count.size()+" unique Abstract annotations saved.");
		fextrac.exportFile(pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.TITLE_FEATURES, title_count);
		System.out.println("..."+ title_count.size() +" unique Title annotations saved.");
		fextrac.exportFile(pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.JOURNAL_TITLE_FEATURES, title_content);
		System.out.println("..."+jTitle+" Journal titles saved.");
		System.out.println("\n=========================================\n");
		
	}		

	/**
	 * Identifies the classification on doc
	 * 
	 * @param clas text containing classification (after char removal)
	 * @return classification of doc
	 */	
	private String getClassif(String clas) {
		
		//parsing the not edited text into HTML using Jsoup
		Document doc = Jsoup.parseBodyFragment(clas);
		//saving the text as an Jsoup element, with a main tag (the HTML body), 
		//attributes and child nodes (TRIAGE tags)
		Element text = doc.body();
		
		Elements classification = text.getElementsByTag("TRIAGE");
				
		return classification.text();		
	}
	
	/**
	 * Inserts the classification 
	 * on the list of features
	 * 
	 * @param class information to insert on list
	 * @param list list of features used
	 */	
	private void addClass(String element, HashMap<Map<String,String>, String> list){
		//going over list to insert
		//classif on document instances		
		Iterator<Map<String, String>>it = list.keySet().iterator();
		
		while(it.hasNext()){		
			Map<String,String> str = it.next();
								
			if(list.get(str).contains("positive") || list.get(str).contains("negative")){
					
			}
			else list.put(str, element);
		}
	}
	
	
	/**
	 * Removes from feature list all features with 
	 * frequency not statistically relevant (2 or less)
	 * @param list to be cleaned
	 */	
	private void considerOccurence(HashMap<Map<String,String>,Integer> list, PathConstants vars){
		//going over the list of annotations and removing the
		//features with occurance lower than specified.
		
		Iterator<Map<String, String>> iterator = list.keySet().iterator();
							
		while(iterator.hasNext()){
			Map<String, String> key = iterator.next();
			int valor = list.get(key).intValue();			
			
			if(valor < Integer.parseInt(vars.FEATURE_MIN_FREQ)){
				iterator.remove();				
			}
		}		
	}	
	
	
	/**
	 * Extract the annotations from a determined section
	 * of the document and add them to the specified lists.
	 * 
	 * @param annotation cleaned and splitted line with annotation
	 * @param count list that holds annotation, its type and its count
	 * @param type list that holds annotation, its type and its classification
	 */	
	private void annotations(String annot, HashMap<Map<String, String>, Integer> count, HashMap<Map<String,String>,String> type, PathConstants pathVars) {		
		HashMap<String,String> features = loadAnnotationEntities();
		PathConstants pathVar = new PathConstants(); 
		NgramExtractor nextrac = new NgramExtractor();
		ArrayList<String> content = new ArrayList<String>();

		//parsing the not edited text into HTML using Jsoup
		Document doc = Jsoup.parseBodyFragment(annot);
		//saving the text as an Jsoup element, with a main tag (the HTML body), 
		//attributes and child nodes (annotation tags)
		Element annotations = doc.body();

		//iterating over list of entities
		for(Map.Entry<String,String> value : features.entrySet()){

			String an_type = value.getKey();
			String an_level = value.getValue();

			//for each entity, find the annotations on abstract
			Elements annots = annotations.getElementsByTag(an_type);			

			//for each annotation found, 
			for(Element an : annots){

				//grabbing annotation content:
				//if the annotation is made on the sentence level:
				if(an_level.contains("sentence")){

					//checkingh if sentence contains inner annotations
					if(an.childNodeSize() != 0){

						//going over list of inner annotations
						for(Element child : an.children()){

							//if child is sentence (sentence inside of sentence),  
							//then add annotations as ngrams on this
							if(features.get(child.nodeName()).contains("sentence")) {
								content.addAll(nextrac.nGrams(child.text(), pathVar));
								insertAnnotation(content, an.nodeName(), count, type, pathVars);
							}
							//adding annotations on sentence as they are - no ngrams on this
							else {
								content.add(child.text());	
								insertAnnotation(content, an.nodeName(), count, type, pathVars);
							}
						}
						
						//removing inner annotations from sentence, they are already added
						Element tempAnnot = an.clone();
						tempAnnot.children().remove();

						//splitting content in ngrams to whats left on the sentence
						content.addAll(nextrac.nGrams(tempAnnot.text(), pathVar));
						insertAnnotation(content, an.nodeName(), count, type, pathVars);
					}			

				}
				else {
					//keeping original annotation content for other cases					
					content.add(an.text()); 
					insertAnnotation(content, an.nodeName(), count, type, pathVars);
				}
			}

		}

	}	
	
	
	/**
	 * Insert annotation (or ngram list of annotation) 
	 * on lists, used on @annotations method 
	 * @param content content of annotation
	 * @param an_type type extracted from text (entity)
	 * @param count list of annotations and their count
	 * @param type  list of annotations and their type
	 */	
	private void insertAnnotation(ArrayList<String> content, String an_type, HashMap<Map<String, String>, Integer> count, HashMap<Map<String,String>,String> type, PathConstants pathVars){
		
		//iterating over list of annotations
		for(int i = 0; i < content.size(); i++){

			if(content.get(i).length() >= Integer.parseInt(pathVars.FEATURE_MIN_LENGTH)){

				//creating the list key as: content - type mapping
				Map<String, String> an_content = new HashMap<String, String>();				
				an_content.put(content.get(i), an_type);

				//for each annotation (or ngram on annotation)
				//insert content and related type
				if(count.containsKey(an_content)){						
					try{
						int cnt = count.get(an_content);								
						count.put(an_content, cnt+1);

					}catch(Exception e){
						count.put(an_content, 1);															
					}
				}					
				else{					
					count.put(an_content, 1);					
				}
				//populating list of feature_an_types, with:
				//feature--an_type--class				
				type.put(an_content, "");
			}
		}
		
		content.clear();
		
	}

	
	/**
	 * Inserts the text (e.g.title) content into   
	 * a list of features (e.g.title features)
	 *  
	 * @param annot text with the annotations to be handled
	 * @param wContent whole field to be added on the list of features
	 * @param list features used
	 * 
	 */	
	private void addContent(String annot, String wContent, HashMap<Map<String,String>,String> list) {

		HashMap<String,String> features = loadAnnotationEntities();
		ArrayList<String> content = new ArrayList<String>();
		NgramExtractor nextrac = new NgramExtractor();
		PathConstants pathVar = new PathConstants();

		//parsing not edited text into HTML using Jsoup
		Document doc = Jsoup.parseBodyFragment(annot);
		//saving the text as an Jsoup element, with a main tag (the HTML body), 
		//attributes and child nodes (annotation tags)
		Element annotations = doc.body();

		//iterating over annotation types
		for(Map.Entry<String,String> value : features.entrySet()){

			String an_type = value.getKey();
			String an_level = value.getValue();

			//for each annotation type, find all related annotations on the abstract
			Elements annots = annotations.getElementsByTag(an_type);			

			//for each annotation type, 
			for(Element an : annots){

				//grab annotation content								
				if(an_level.contains("sentence"))
					//splitting in ngrams for sentence level annotations
					content = nextrac.nGrams(an.text(), pathVar);
				else 
					//keeping original annotation for other cases
					content.add(an.text());

				//iterating over list of annotations
				for(int i = 0; i < content.size(); i++){
					
					Map<String,String> an_content = new HashMap<String,String>();
					an_content.put(content.get(i), wContent);
					
					//populating list of feature_an_types, with:
					//feature--an_type--class
					list.put(an_content, "");									
				}
				content.clear();
			}
		}
	}

	
	/**
	 * Loads list of entities from external file
	 * 
	 * @param str list of entities
	 * @param pathVar constants from 
	 * @return
	 */	
	public HashMap<String,String> loadAnnotationEntities(){
		
		String pathEntities = "entities.txt";		
		HashMap<String,String> values = new HashMap<String,String>();
						
		try{			
			BufferedReader reader = new BufferedReader(new FileReader(pathEntities));
			
			String line = null;	
			
			while((line = reader.readLine()) != null){				
                
				String[] value = StringUtils.split(line, " ");
				values.put(value[0].toLowerCase(), value[1].toLowerCase());				
			}
			
			reader.close();
			
		}catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }		
		//String[] entities = values.toArray(new String[values.size()]);
		
		return values;
	}

	
	/**
	 * Handles the content of annotations; when
	 * there is multiple elements, they are 
	 * concatenated after extracted 
	 * 
	 * @param str list of annotation elements
	 * @return single string with all elements
	 */	
	public String contentToString(String[] str){
		String cont = "";
		
		for(int i = 0; i < str.length; i++){
				if(cont.contentEquals("")){
					cont = cont + str[i];	
				}
				else cont = cont+" "+ str[i];
				
			}		
		
		return cont;
	}
	
	

}
