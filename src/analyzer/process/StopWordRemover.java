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
package analyzer.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.ContentInterface;
import analyzer.content.FacebookContentObject;
import analyzer.content.PlainTextContentObject;
import analyzer.content.socialmedia.Comment;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.content.socialmedia.StatusMessage;
import analyzer.segmentation.Centroid;
import analyzer.segmentation.Ngram;

/**
 * Class used to remove stopwords of various files. Generally a stopword file is
 * a text file with one stop word on each row.
 * 
 * @author forsstho
 * 
 */
public class StopWordRemover {
	Config config;

	public StopWordRemover(Config config) {
		this.config = config;

	}

	public void removeStopWords(HashMap<String, Double> tfidf) {
		if (config.prop.containsKey("stopwords")) {
			String path = config.prop.getProperty("stopwords");
			if (!path.equals("")) {
				BufferedReader br = null;
				InputStream is = null;
				try {
					String input;
					try {
						is = this.getClass().getResourceAsStream(path);
						br = new BufferedReader(new InputStreamReader(is));
					} catch (Exception e) {
						br = new BufferedReader(new FileReader(path));
					}
					while ((input = br.readLine()) != null) {
						if (input.startsWith("/")) {
							input = input.substring(1, input.length() - 1);
						}
						if (tfidf.containsKey(input)) {
							tfidf.put(input, (double) 0);
						}
						if (tfidf.containsKey(input.toUpperCase())) {
							tfidf.put(input.toUpperCase(), (double) 0);
						}
						String firstUpper = input.substring(0, 1).toUpperCase()
								+ input.substring(1);
						if (tfidf.containsKey(firstUpper)) {
							tfidf.put(firstUpper, (double) 0);
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (br != null)
							br.close();
						if (is != null)
							is.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				removeNames(tfidf);
			} else {
				TextAnalyzer.logger.info("No stop word path entered.");
			}

		} else {
			TextAnalyzer.logger.info("No stop word path entered.");
		}
	}

	public void removeMediaStopWords(Vector<MediaInformation> mi) {
		if (config.prop.containsKey("stopwords")
				&& !(ContentFactory.getParser(config) instanceof PlainTextContentObject)) {
			String path = config.prop.getProperty("stopwords");
			if (!path.equals("")) {
				BufferedReader br = null;
				try {
					String input;
					br = new BufferedReader(new FileReader(path));
					while ((input = br.readLine()) != null) {
						for (MediaInformation m : mi) {
							HashMap<Integer, HashMap<String, Centroid>> mtfidf = m.mediaTfidf;
							for (int i : mtfidf.keySet()) {
								HashMap<String, Centroid> tfidf = mtfidf.get(i);
								if (input.startsWith("/")) {
									input = input.substring(1,
											input.length() - 1);
								}
								if (tfidf.containsKey(input)) {
									Centroid c = tfidf.get(input);
									c.setTfidf(0);
									tfidf.put(input, c);
								}
								if (tfidf.containsKey(input.toUpperCase())) {
									Centroid c = tfidf.get(input.toUpperCase());
									c.setTfidf(0);
									tfidf.put(input.toUpperCase(), c);
								}
								String firstUpper = input.substring(0, 1)
										.toUpperCase() + input.substring(1);
								if (tfidf.containsKey(firstUpper)) {
									Centroid c = tfidf.get(firstUpper);
									c.setTfidf(0);
									tfidf.put(firstUpper, c);
								}
								m.mediaTfidf.put(i, tfidf);
							}

						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (br != null)
							br.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			} else {
				TextAnalyzer.logger.info("No stop word path entered.");
			}

		} else {
			TextAnalyzer.logger.info("No stop word path entered.");
		}
	}

	public void removeNames(HashMap<String, Double> tfidf) {
		Vector<String> names = new Vector<String>();
		ContentInterface cI = ContentFactory.getParser(config);
		if (cI instanceof FacebookContentObject) {
			FacebookContentObject fi = (FacebookContentObject) cI;
			names.add(fi.firstName);
			names.add(fi.middleName);
			names.add(fi.lastName);

			for (MediaInformation info : fi.photoInformation) {
				try {
					String[] split = info.from.split(" ");
					for (int i = 0; i < split.length; i++) {
						if (!names.contains(split[i])) {
							names.add(split[i]);
						}
					}
				} catch (Exception e) {
					if (!names.contains(info.from)) {
						names.add(info.from);
					}
				}
				for (Comment c : info.comments) {
					try {
						String[] split = info.from.split(" ");
						for (int i = 0; i < split.length; i++) {
							if (!names.contains(split[i])) {
								names.add(split[i]);
							}
						}
					} catch (Exception e) {
						if (!names.contains(info.from)) {
							names.add(info.from);
						}
					}
				}
			}
			for (MediaInformation info : fi.videoInformation) {
				try {
					String[] split = info.from.split(" ");
					for (int i = 0; i < split.length; i++) {
						if (!names.contains(split[i])) {
							names.add(split[i]);
						}
					}
				} catch (Exception e) {
					if (!names.contains(info.from)) {
						names.add(info.from);
					}
				}
				for (Comment c : info.comments) {
					try {
						String[] split = info.from.split(" ");
						for (int i = 0; i < split.length; i++) {
							if (!names.contains(split[i])) {
								names.add(split[i]);
							}
						}
					} catch (Exception e) {
						if (!names.contains(info.from)) {
							names.add(info.from);
						}
					}
				}
			}
			for (StatusMessage info : fi.statusMessageInformation) {
				try {
					String[] split = info.from.split(" ");
					for (int i = 0; i < split.length; i++) {
						if (!names.contains(split[i])) {
							names.add(split[i]);
						}
					}
				} catch (Exception e) {
					if (!names.contains(info.from)) {
						names.add(info.from);
					}
				}
				for (Comment c : info.comments) {
					try {
						String[] split = info.from.split(" ");
						for (int i = 0; i < split.length; i++) {
							if (!names.contains(split[i])) {
								names.add(split[i]);
							}
						}
					} catch (Exception e) {
						if (!names.contains(info.from)) {
							names.add(info.from);
						}
					}
				}
			}
			for (String name : names) {
				if (tfidf.containsKey(name)) {
					tfidf.put(name, (double) 0);
				}
				if (name != null && tfidf.containsKey(name.toUpperCase())) {
					tfidf.put(name.toUpperCase(), (double) 0);
				}
				String firstUpper = "";
				if (name != null && name.length() > 1) {
					firstUpper = name.substring(0, 1).toUpperCase()
							+ name.substring(1);
				}
				if (tfidf.containsKey(firstUpper)) {
					tfidf.put(firstUpper, (double) 0);
				}
			}
		}
	}

	public void removeStopWordsFromNgram(
			HashMap<Integer, LinkedHashMap<String, Ngram>> currentgrams) {
		if (currentgrams != null) {

			if (config.prop.containsKey("stopwords")
					&& config.prop.containsKey("ngram")) {
				String path = config.prop.getProperty("stopwords");
				if (!path.equals("")) {

					BufferedReader br = null;
					try {

						Set<Integer> gramsizes = new HashSet<Integer>(
								currentgrams.keySet());
						String input;
						HashSet<String> stopwords = new HashSet<String>();
						InputStream is = null;
						try {
							is = this.getClass().getResourceAsStream(path);
							br = new BufferedReader(new InputStreamReader(is));
						} catch (Exception e) {
							br = new BufferedReader(new FileReader(path));
						}
						while ((input = br.readLine()) != null) {
							if (input.startsWith("/")) {
								input = input.substring(1, input.length() - 1);
							}
							stopwords.add(input);
						}

						for (int gramsize : gramsizes) {
							Vector<Ngram> removegrams = new Vector<Ngram>();
							LinkedHashMap<String, Ngram> grams = currentgrams
									.get(gramsize);

							// br = new BufferedReader(new FileReader(path));

							for (Ngram gram : grams.values()) {

								// check if first is stopword
								if (stopwords.contains(gram.gram[0]
										.toLowerCase())) {
									removegrams.add(gram);
								}
								// check if last is stopword
								else if (stopwords
										.contains(gram.gram[gram.gram.length - 1]
												.toLowerCase())) {
									removegrams.add(gram);
								} else if (stopwords.contains(gram.toString())) {
									removegrams.add(gram);
								}
								// check if containsstopword
								else {
									for (int i = 0; i < gram.gram.length; i++) {
										String word = gram.gram[i];
										if (stopwords.contains(word)) {
											gram.tfidf[i] = 0;
										}
									}
								}
							}
							for (Ngram gram : removegrams) {
								grams.remove(gram.toString());
							}

							currentgrams.put(gramsize, grams);

						}

					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if (br != null)
								br.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				} else {
					TextAnalyzer.logger.info("No stop word path entered.");

				}
			} else {
				TextAnalyzer.logger.info("No stop words defined.");

			}
		}

	}

	public HashSet<String> getStopwordSet() {
		HashSet<String> stopwords = new HashSet<String>();
		String path = config.prop.getProperty("stopwords");
		if (!path.equals("")) {
			BufferedReader br = null;
			try {
				String input;
				br = new BufferedReader(new FileReader(path));
				while ((input = br.readLine()) != null) {
					stopwords.add(input);
				}
			} catch (Exception e) {

			}
		}
		return stopwords;
	}
}
