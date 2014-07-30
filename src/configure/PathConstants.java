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
***
* This class re-uses https://code.google.com/p/semlinker/source/browse/trunk/src/configure/NistKBPConfiguration.java
* The code authors: Eric Charton http://www.echarton.com twitter.com/ericcharton
*                   Marie-Jean Meurs http://mjmrsc.com/research/ twitter.com/mjmrsc
*                   
* This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
*/

package configure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Variables used by the software 
 * 
 * @author Marie-Jean Meurs
 * @since 2013
 *  
 */
public class PathConstants {	

	/**
	 * Default constructor
	 */
	public PathConstants() {
		initVars();
	}

	/**
	 * Constructor with custom parameter file.
	 * @param configfile
	 */
	public PathConstants(String configfile) {
		CONFIG_FILE = configfile;
		initVars();
	}


	public static String CONFIG_FILE = "config.cfg";
	public HashMap<String, String> CONFIG_MAP = new HashMap<String, String>();

	//Input files
	public String HOME_DIR;
	public String CORPUS_DIR; 
	public String TRAIN_DIR; 
	public String TEST_DIR;
	public String FEATURE_DIR;	
	public String OUTPUT_MODEL;
	public String TRAINING_FILE;
	public String TEST_FILE;
	public String ARFF_TRAIN;
	public String ARFF_TEST;
	public String STOP_LIST;
	
	//Output files
	public String JOURNAL_TITLE_FEATURES;
	public String ECNUM_FEATURES;
	public String ANNOTATION_FEATURES;
	public String TITLE_FEATURES;
	public String NGRAM_FEATURES;
	public String TITLE_NGRAMS;
	
	//Feature setup
	public String USE_TEXT_SIZE;
	public String USE_JOURNAL_TITLE_FEATURE;
	public String USE_ECNUM_FEATURE;
	public String FEATURE_MIN_FREQ;
	public String FEATURE_MIN_LENGTH;
	
	//Feature setup - Annotations
	public String USE_ANNOTATION_FEATURE;
	public String USE_ANNOTATION_TYPE;
	public String USE_TITLE_FEATURE;
	
	
	//Feature setup - Ngrams
	public String USE_NGRAM_FEATURE;
	public String USE_TITLE_NGRAMS;
	public String NGRAM_STOP;
	public String NGRAM_SIZE;
	public String USE_WEIGHTED_NGRAM;
	public String WEIGHT;

	//Task setup
	public String EXP_TYPE;	
	public String NB_PARAMS;		
	

	private void initVars() {
		String text = null;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));
			while ((text = reader.readLine()) != null) {
				if (! text.startsWith("#")) {
					String label = text.split("=")[0];
					String value = text.split("=")[1];
					CONFIG_MAP.put(label, value);
				}
			}
			reader.close();
		} catch (IOException ex) {
			Logger.getLogger(PathConstants.class.getName()).log(Level.SEVERE, null, ex);
		}
		HOME_DIR = CONFIG_MAP.get("HOME_DIR");
		CORPUS_DIR = CONFIG_MAP.get("CORPUS_DIR"); 
		TRAIN_DIR = CONFIG_MAP.get("TRAIN_DIR"); 
		TEST_DIR = CONFIG_MAP.get("TEST_DIR");
		FEATURE_DIR = CONFIG_MAP.get("FEATURE_DIR");		
		OUTPUT_MODEL = CONFIG_MAP.get("OUTPUT_MODEL");
		TRAINING_FILE = CONFIG_MAP.get("TRAINING_FILE");
		TEST_FILE = CONFIG_MAP.get("TEST_FILE");
		ARFF_TRAIN = CONFIG_MAP.get("ARFF_TRAIN");
		ARFF_TEST = CONFIG_MAP.get("ARFF_TEST");
		STOP_LIST = CONFIG_MAP.get("STOP_LIST");
		
		JOURNAL_TITLE_FEATURES = CONFIG_MAP.get("JOURNAL_TITLE_FEATURES");
		ECNUM_FEATURES = CONFIG_MAP.get("ECNUM_FEATURES");	
		ANNOTATION_FEATURES = CONFIG_MAP.get("ANNOTATION_FEATURES");
		TITLE_FEATURES = CONFIG_MAP.get("TITLE_FEATURES");
		NGRAM_FEATURES = CONFIG_MAP.get("NGRAM_FEATURES");
		TITLE_NGRAMS = CONFIG_MAP.get("TITLE_NGRAMS");
		
		USE_TEXT_SIZE = CONFIG_MAP.get("USE_TEXT_SIZE");
		USE_JOURNAL_TITLE_FEATURE = CONFIG_MAP.get("USE_JOURNAL_TITLE_FEATURE");	
		USE_ECNUM_FEATURE = CONFIG_MAP.get("USE_ECNUM_FEATURE");
		FEATURE_MIN_FREQ = CONFIG_MAP.get("FEATURE_MIN_FREQ");
		FEATURE_MIN_LENGTH = CONFIG_MAP.get("FEATURE_MIN_LENGTH");
		
		USE_ANNOTATION_FEATURE = CONFIG_MAP.get("USE_ANNOTATION_FEATURE");
		USE_ANNOTATION_TYPE = CONFIG_MAP.get("USE_ANNOTATION_TYPE");		
		USE_TITLE_FEATURE = CONFIG_MAP.get("USE_TITLE_FEATURE");
		
		USE_NGRAM_FEATURE = CONFIG_MAP.get("USE_NGRAM_FEATURE");
		USE_TITLE_NGRAMS = CONFIG_MAP.get("USE_TITLE_NGRAMS");
		NGRAM_STOP = CONFIG_MAP.get("NGRAM_STOP");		
		NGRAM_SIZE = CONFIG_MAP.get("NGRAM_SIZE");
		USE_WEIGHTED_NGRAM = CONFIG_MAP.get("USE_WEIGHTED_NGRAM");
		WEIGHT = CONFIG_MAP.get("WEIGHT");
				
		EXP_TYPE = CONFIG_MAP.get("EXP_TYPE");		
		NB_PARAMS = CONFIG_MAP.get("NB_PARAMS");		
		
	}
}
