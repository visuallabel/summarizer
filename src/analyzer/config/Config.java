/*Copyright 2013 Thomas Forss, Shuhua Liu, Arcada Ab Oy, developed within Digile D2I project

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package analyzer.config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Vector;

import analyzer.process.TextAnalyzer;

/**
 * Config file that will be used to run different options. The config options
 * are saved in a Properties file.
 * 
 * The arguments found in the config: file - path to the file that will be
 * parsed path - where the config file will be stored output - path to location
 * where output will be saved cr - compression rate 1-100%: how large summary or
 * how much of centroids idfdictionary - location where idfdictionary is located
 * contentType - facebook/normal/twitter: defines the format for input dateLimit
 * - increase significance of the content created after date counterLimit -
 * increase significance of the content with equal or greater counterLimit
 * stopwords - path to file with stop words, significance will be put to 0
 * translation - set to yandex if in use ignoreCase - if yes ignores cases
 * feedback - currently only mysql implementet, outputs feedback from facebook
 * analysis to database combine - set to normal or mead, mead combine imitates
 * the experiments we did in back when using mead ngramSort - arrange ngrams by
 * feedback or normal ner - added if we want to use named entitiy recognition
 */
public class Config {
	// URL url = Config.class.getClassLoader().getResource();
	public String defaultIDF;
	public Properties prop = new Properties();

	public void loadConfig(String s) {
		FileInputStream fis = null;
		InputStream is = null;
		try {
			try {
				is = this.getClass().getResourceAsStream(s);
				prop.load(is);
			} catch (Exception e) {
				fis = new FileInputStream(s);
				prop.load(fis);

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
				if (is != null)
					is.close();
			} catch (IOException e) {

			}
		}
	}

	public void saveConfig() {
		FileOutputStream fis = null;
		try {
			if (prop.containsKey("config")) {
				fis = new FileOutputStream(prop.getProperty("config"));
				// System.out.println("printing to: "+prop.getProperty("config"));
				prop.store(fis, null);
				TextAnalyzer.logger.info("Config printed to "
						+ prop.getProperty("config"));
			} else {
				fis = new FileOutputStream("config.properties");
				prop.store(fis, null);
				TextAnalyzer.logger.info("Config printed to config.properties");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates new config, asks user to add properties
	 */
	public void createNewConfig() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Path to file to parse:");
		String input = "";
		try {
			input = br.readLine();
			prop.put("file", input);
			System.out
					.println("Path to save config, for default write nothing");
			input = br.readLine();
			if (!input.equals("")) {
				prop.put("config", input);
			} else if (!prop.containsKey("config")) {
				prop.put("config", "analyzer_config.txt");
			}
			System.out
					.println("Path to save output, for default write nothing");
			input = br.readLine();
			if (input != null) {
				prop.put("output", input);
			} else if (!prop.containsKey("output")) {
				prop.put("output", "output.txt");
			}
			System.out.println("Choose compression ratio 1 - 100 %:");
			input = br.readLine();
			prop.put("cr", input);
			System.out
					.println("Increase significance of content created after date? (yyyy-MM-dd)");
			System.out.println("Leave empty to not alter significance:");
			input = br.readLine();
			if (input != null) {
				prop.put("dateLimit", input);
			}
			System.out
					.println("Increase significance of content with counter limit equal to or above?");
			System.out.println("Leave empty to not alter significance:");
			input = br.readLine();
			if (input != null) {
				prop.put("counterLimit", input);
			}
			System.out
					.println("Idf dictionary location, for default write nothing:");
			input = br.readLine();
			if (input == null || input.equals("")) {
				prop.put("idfdictionary", "/enidf.txt");
			} else {
				prop.put("idfdictionary", input);
			}
			System.out.println("Content type facebook/twitter/normal :");

			// TODO:change when normal is completed
			input = br.readLine();
			if (input == null) {
				System.out
						.println("Wrong argument added, trying to run as plain text content.");
				prop.put("contentType", "normal");
			} else if (input.equalsIgnoreCase("normal")) {
				prop.put("contentType", "normal");
			} else if (input.equalsIgnoreCase("facebook")) {
				prop.put("contentType", "facebook");
			} else if (input.equalsIgnoreCase("twitter")) {
				prop.put("contentType", "facebook");
			} else {
				prop.put("contentType", "normal");
			}
			System.out
					.println("stop words path, for no stop word list leave empty:");
			input = br.readLine();
			prop.put("stopwords", input);
			System.out
					.println("Path to Stanford compatible Named Entity Recognition classifier:");
			input = br.readLine();
			if (input != null && input != "") {
				prop.put("ner", input);
			}
			System.out
					.println("TFIDF ignoring word casing and non-existing words? yes/no");
			input = br.readLine();
			if (input != null) {
				if (input.equalsIgnoreCase("yes")) {
					prop.put("ignoreCase", "yes");
				} else if (input.equalsIgnoreCase("no")) {
					prop.put("ignoreCase", "no");
				}
			}
			saveConfig();
			System.out
					.println("Config file completed, to run with the config file add argument: -config path");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * check if config is correct
	 * 
	 * @return
	 */
	public Vector<String> checkConfig() {
		Vector<String> fix = new Vector<String>();
		if (prop.get("path") == null) {
			fix.add("path");
		} else if (prop.get("file") == null) {
			fix.add("file");
		} else if (prop.get("output") == null) {
			fix.add("output");
		}
		return fix;
	}

	/**
	 * Fix config if it is missing or broken
	 */
	public void fixConfig() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		Vector<String> fix = checkConfig();
		for (String s : fix) {
			TextAnalyzer.logger.info("Missing value in config for " + s);
			try {
				input = br.readLine();
				if (checkProperty(s, input)) {
					prop.put(s, input);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Check particular property and see if value is legal
	 */
	public boolean checkProperty(String property, String value) {
		// TODO: checks so wrong information cant be entered
		return true;
	}
}
