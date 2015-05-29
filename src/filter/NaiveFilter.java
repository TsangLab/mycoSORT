package filter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import configure.ConfigConstants;

/**
 * 
 * This class implements naive feature filtering methods 
 * to be used by the extractor processes pre-vector building
 *   
 * @author Hayda Almeida
 * @since 2015 
 *
 */
public class NaiveFilter {
	

	private boolean verbose = true;
	private String[] stopWords;
				

	/**
	 * Removes from feature list all features with 
	 * frequency not statistically relevant (2 or less)
	 * @param list to be cleaned
	 */	
	public void considerAnnotationOccurence(HashMap<Map<String,String>,Integer> list, ConfigConstants vars){
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
	 * Removes from feature list all features with 
	 * frequency not statistically relevant (2 or less)
	 * @param list to be cleaned
	 */	
	public void considerOccurence(HashMap<String,Integer> list, ConfigConstants vars){
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
	
	/**
	 * Load the list of PubMed stopwords
	 * @param path file with stopwords list
	 * @return list of stopwords
	 */
	public void loadStopWords(String path){
				
		StringBuilder cleaned = new StringBuilder();
		
		try{
			
			BufferedReader reader = new BufferedReader(new FileReader(path));
			
			String line = null;	
			//loading stop-words list
			while((line = reader.readLine()) != null){
				this.stopWords = StringUtils.split(line,",");
				System.out.println("");
				//line = reader.readLine();
			}
			
			reader.close();
			
		}catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 		
		
	}
	
	/**
	 * Removes stopwords from ngrams list
	 * 
	 * @param str list of ngrams
	 * @param constants 
	 * @return cleaned list of ngrams
	 */	
	public String removeStopList(String[] str){
				
		StringBuilder cleaned = new StringBuilder();			
		
		//iteraing over text to be cleaned
		for(int i = 0; i < str.length; i++){
			//iterating over stop-words list
			for(int j = 0; j < this.stopWords.length; j++){
				
				//when stop-word is encountered, replace it
				if(str[i].equalsIgnoreCase(this.stopWords[j])){
					str[i] = str[i].replace(str[i],"*");					
				}				
			}
			//retrieve the text without stop-words replacements
			if(!(str[i].contentEquals("*"))){
				cleaned.append(str[i]).append(" ");				
			}
		}		
		return cleaned.toString().replace("  ", " ");
	}
	
	public String[] getStopWords() {
		return stopWords;
	}

	public void setStopWords(String[] stopWords) {
		this.stopWords = stopWords;
	}

}
