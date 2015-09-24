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
package analyzer.weighting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import analyzer.config.Config;
import analyzer.process.StopWordRemover;

/**
 * Class that loads a existing idf collection
 * 
 */
public class IDFCollection {
	private HashMap<String, Double> idfCollection = new HashMap<String, Double>();
	String dictionaryLocation;
	Config config;

	public IDFCollection(Config config) {
		this.config = config;
		dictionaryLocation = (String) config.prop.get("idfdictionary");
		loadIDF();
	}

	/**
	 * Load in the idf dictionary to a hashmap with String, Double keys
	 */
	private void loadIDF() {
		InputStream is = null;
		try {
			is = this.getClass().getResourceAsStream(dictionaryLocation);
			getStringFromInputStream(is);
		} catch (Exception e) {
			String filename = config.prop.getProperty("idfdictionary");
			File file = new File(filename);
			getStringFromInputStream(file);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}

	/**
	 * checks if an idf is a number, if it is we discard it
	 */
	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public HashMap<String, Double> getIdfCollection() {
		return idfCollection;
	}

	private void getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		String idf;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			StringTokenizer st = null;
			if (config.prop.getProperty("ignoreCase").equals("yes")) {
				while ((idf = br.readLine()) != null) {
					st = new StringTokenizer(idf);
					String key = st.nextToken();
					if (!isNumeric(key)) {
						double value = Double.parseDouble(st.nextToken());
						key = key.toLowerCase();
						if (idfCollection.get(key) != null) {
							double value2 = idfCollection.get(key);
							if (value2 < value) {
								idfCollection.put(key, value);
							}
						}
						idfCollection.put(key, value);
					}
				}
			} else {
				while ((idf = br.readLine()) != null) {
					st = new StringTokenizer(idf);
					String key = st.nextToken();
					if (!isNumeric(key)) {
						double value = Double.parseDouble(st.nextToken());
						idfCollection.put(key, value);
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void getStringFromInputStream(File file) {
		BufferedReader br = null;
		String idf;
		try {

			br = new BufferedReader(new FileReader(file));
			if (config.prop.getProperty("ignoreCase").equals("yes")) {
				while ((idf = br.readLine()) != null) {
					String ngram[] = idf.split(";");
					double value = Double.parseDouble(ngram[1]);
					ngram[0] = ngram[0].toLowerCase();
					if (idfCollection.get(ngram[0]) != null) {
						double value2 = idfCollection.get(ngram[0]);
						if (value2 < value) {
							idfCollection.put(ngram[0], value);
						}
					}
					idfCollection.put(ngram[0], value);

				}
			} else {
				while ((idf = br.readLine()) != null) {
					String ngram[] = idf.split(";");
					double value = Double.parseDouble(ngram[1]);
					idfCollection.put(ngram[0], value);

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public HashSet<String> getStorwords() {
		StopWordRemover sr = new StopWordRemover(config);
		return sr.getStopwordSet();
	}
}
