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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.ContentInterface;
import analyzer.content.WeightFactory;
import analyzer.content.WeightInterface;
import analyzer.content.socialmedia.HashTag;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.database.DataFactory;
import analyzer.database.FeedbackInterface;
import analyzer.database.User;
import analyzer.ner.NamedEntity;
import analyzer.ner.NerFactory;
import analyzer.ner.NerInterface;
import analyzer.segmentation.Centroid;
import analyzer.segmentation.Ngram;
import analyzer.segmentation.SegmentationFactory;
import analyzer.segmentation.SegmentationInterface;
import analyzer.sorting.SortingFactory;

/**
 * Class that puts together termfrequency and inverse document frequency
 * Depending on the content does specific things to it on the way
 * 
 */
public class TFIDFCalculator implements TFIDFInterface {
	private TermFrequency tf;
	private IDFCollection ic;
	private Config config;
	private LinkedHashMap<String, Double> tfidf = new LinkedHashMap<String, Double>();
	private LinkedHashMap<String, Double> mediaTfidf = new LinkedHashMap<String, Double>();
	private List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>();
	private List<Map.Entry<String, Double>> mediaEntries;
	private List<Map.Entry<String, HashTag>> hashentries;
	private LinkedHashMap<String, HashTag> hashtags;
	HashMap<String, Double> freq2;
	private WeightInterface wi;

	public TFIDFCalculator(Config config) {
		wi = WeightFactory.getParser(config);
		this.config = config;
		this.ic = TFIDFFactory.getIc(config);
		// calculateTfidf();
		calculateAllTfidf();
	}

	/**
	 * New method for calculating TFIDF for everything, use this one it includes
	 * unigram, ngram, hashtags, and ner.
	 */
	private void calculateAllTfidf() {
		this.tf = new TermFrequency(config.prop.getProperty("combine"), config);
		SegmentationInterface ngi = SegmentationFactory.getParser(config);
		getNgramTfidf(ngi.getncurrentGrams());
		getMediaTfidf(ngi.getncurrentMediaGrams());

	}
	//TODO: I think I have modified this method
	public void getHashTagTfidf() {
		HashMap<String, Double> idf = ic.getIdfCollection();

		if (config.prop.get("contentType").equals("twitter")) {
			if (config.prop.getProperty("ignoreCase").equals("yes")) {

				hashtags = WeightFactory.getParser(config).calculateHashtags();
				for (String word : hashtags.keySet()) {
					HashTag hash = hashtags.get(word);
					if (idf.containsKey(word)) {
						double value = hash.frequency * idf.get(word);
						if (value > 0) {
							hash.tfidf = value;
							hashtags.put(word, hash);

						} else {
							hash.tfidf = 0.0;
							hashtags.put(word, hash);
						}
					} else {
						hash.tfidf = hash.frequency * 0.1;
						hashtags.put(word, hash);
					}
				}
				// hashentries = SortingFactory.getSorter().sortHashtagsByValue(
				// hashtags);
				Vector<MediaInformation> mediaInfo = ContentFactory.getParser(
						config).getMedia();
				HashSet<String> stopwords = ic.getStorwords();
				for (MediaInformation m : mediaInfo) {
					for (int i = 1; i <= m.mediaFreq.size(); i++) {
						HashMap<String, Centroid> mtfidf = new HashMap<String, Centroid>();
						for (String tag : m.mediaFreq.get(i).keySet()) {
							StringTokenizer st = new StringTokenizer(tag);
							double value = 0;
							while (st.hasMoreTokens()) {
								String token = st.nextToken();
								if (idf.containsKey(token)) {
									if (stopwords.contains(token)) {

									} else {
										value += idf.get(token);
									}

								} else {

									if (stopwords.contains(token)) {

									} else {
										value += 0.1;
									}

								}
							}
							String id = ContentFactory.getID(config);
							Centroid c = new Centroid(id + tag, tag, value);
							
							if (m.photoUID != null) {
								c.setPhotoUID(m.photoUID);
							} else {
								c.setPhotoUID("null");
								System.out.println("photouid is null for centroid "+tag);
							}
							mtfidf.put(tag, c);
						}
						m.mediaTfidf.put(i, mtfidf);
					}
				}
			}
		}
	}

	public LinkedHashMap<Integer, Vector<Ngram>> getMediaTfidf(
			LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> linkedHashMap) {

		LinkedHashMap<Integer, Vector<Ngram>> freq = new LinkedHashMap<Integer, Vector<Ngram>>();
		for (int gram : linkedHashMap.keySet()) {
			LinkedHashMap<String, Ngram> orderedGrams = linkedHashMap.get(gram);
			// LinkedHashMap<String, Ngram> ngram =
			// for (Ngram grams : ngram) {
			// String key = grams.toString();
			// if (orderedGrams.containsKey(key)) {
			// orderedGrams.get(key).frequency += 1;
			// } else {
			// orderedGrams.put(key, grams);
			// }
			// }
			loadtfidf(orderedGrams);
			if (config.prop.containsKey("ngramSort")
					&& config.prop.getProperty("ngramSort").equals("ner")) {
				loadNER(orderedGrams, NerFactory.getNer(config));
				Vector<Ngram> ordered = SortingFactory.getSorter()
						.orderNgramsByNer(orderedGrams);
				freq.put(gram, ordered);
			} else {
				Vector<Ngram> ordered = SortingFactory.getSorter()
						.orderNgramsByDefault(orderedGrams);
				freq.put(gram, ordered);

			}

		}
		return freq;
	}

	public void loadNER(LinkedHashMap<String, Ngram> orderedGrams,
			NerInterface nerInterface) {
		if (config.prop.containsKey("ner")) {
			for (Ngram ngram : orderedGrams.values()) {
				for (NamedEntity namedEntity : nerInterface.getEntities()
						.values()) {
					if (ngram.toString().toLowerCase()
							.equals(namedEntity.namedEntity)) {
						ngram.matchesNER = true;
					}
				}
			}
		}
	}

	public LinkedHashMap<Integer, Vector<Ngram>> getNgramTfidf(
			LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> linkedHashMap) {

		LinkedHashMap<Integer, Vector<Ngram>> freq = new LinkedHashMap<Integer, Vector<Ngram>>();
		for (int gram : linkedHashMap.keySet()) {
			LinkedHashMap<String, Ngram> orderedGrams = linkedHashMap.get(gram);
			// LinkedHashMap<String, Ngram> ngram = linkedHashMap.get(gram);
			// for (Ngram grams : ngram) {
			// String key = grams.toString();
			// if (orderedGrams.containsKey(key)) {
			// Ngram temp = orderedGrams.get(key);
			// temp.frequency += 1;
			// orderedGrams.put(key, temp);
			// } else {
			// orderedGrams.put(key, grams);
			// }
			// }
			// Only used of we are running combined centroids (= more than one
			// file at the time)
			double freqDivideBy = wi.getNumberOfFilesInCluster();
			if (freqDivideBy > 1) {
				for (Ngram grams : orderedGrams.values()) {
					double number = grams.frequency;
					number = (number / freqDivideBy);
					grams.frequency = number;
				}
			}
			loadtfidf(orderedGrams);
			if (config.prop.containsKey("ngramSort")
					&& config.prop.getProperty("ngramSort").equals("ner")) {
				loadNER(orderedGrams, NerFactory.getNer(config));
				Vector<Ngram> ordered = SortingFactory.getSorter()
						.orderNgramsByNer(orderedGrams);
				freq.put(gram, ordered);
			} else {
				Vector<Ngram> ordered = SortingFactory.getSorter()
						.orderNgramsByDefault(orderedGrams);
				freq.put(gram, ordered);

			}

		}
		return freq;
	}

	/**
	 * OLD DEPRECATED METHOD Method for calculating TDIDF, method split up in
	 * three parts depending on if you are using facebook, twitter or plain text
	 * extraction
	 */
	public void calculateTfidf() {
		this.tf = new TermFrequency(config.prop.getProperty("combine"), config);
		tf.calculateTermFrequency();
		HashMap<String, Double> freq = tf.getFrequency();
		HashMap<String, Double> mediaFreq = tf.getOtherFrequency();
		HashMap<String, Double> idf = ic.getIdfCollection();
		User userFeedback = null;
		ContentInterface ci = ContentFactory.getParser(config);
		if (config.prop.containsKey("feedback")
				&& config.prop.get("contentType").equals("facebook")) {
			FeedbackInterface fi = DataFactory.getFeedback(config);
			userFeedback = fi.readContent(ci.getID());
			Set<String> keyset = userFeedback.negativeWords.keySet();
			if (config.prop.getProperty("ignoreCase").equals("yes")) {
				for (String word : freq.keySet()) {
					if (keyset.contains(word)) {
						tfidf.put(word, 0.0);
					} else {
						if (idf.containsKey(word)) {
							double value = freq.get(word) * idf.get(word);
							if (value > 0) {
								tfidf.put(word, value);
							} else {
							}
						} else {
							double value = freq.get(word) * 0.1;
							if (value > 0) {
								tfidf.put(word, value);
							} else {
							}
						}
					}
				}
			} else {
				for (String word : freq.keySet()) {
					if (keyset.contains(word)) {
						tfidf.put(word, 0.0);
					} else {
						if (idf.containsKey(word)) {
							double value = freq.get(word) * idf.get(word);
							tfidf.put(word, value);
						} else {
							double value = freq.get(word) * 0.1;
							tfidf.put(word, value);
						}
					}
				}
			}
		}
		if (config.prop.get("contentType").equals("twitter")) {
			if (config.prop.getProperty("ignoreCase").equals("yes")) {
				for (String word : freq.keySet()) {
					if (idf.containsKey(word)) {
						double value = freq.get(word) * idf.get(word);
						if (value > 0) {
							tfidf.put(word.toLowerCase(), value);
						} else {
						}
					}
				}

				for (String word : mediaFreq.keySet()) {
					if (idf.containsKey(word)) {
						double value = mediaFreq.get(word) * (idf.get(word));
						if (value > 0) {
							mediaTfidf.put(word.toLowerCase(), value);
						} else {

						}
					} else {
						double value = mediaFreq.get(word) * 0.1;
						tfidf.put(word, value);
					}
				}

			} else {
				for (String word : freq.keySet()) {
					if (idf.containsKey(word)) {
						double value = freq.get(word) * idf.get(word);
						tfidf.put(word, value);
					} else {

						double value = freq.get(word) * 0.1;
						tfidf.put(word, value);
					}
				}

				for (String word : mediaFreq.keySet()) {
					if (idf.containsKey(word)) {
						double value = mediaFreq.get(word) * (idf.get(word));
						mediaTfidf.put(word, value);
					} else {
						double value = freq.get(word) * 0.1;
						mediaTfidf.put(word, value);
					}
				}
			}
			hashtags = WeightFactory.getParser(config).calculateHashtags();
			for (String word : hashtags.keySet()) {
				HashTag hash = hashtags.get(word);
				if (idf.containsKey(word)) {
					double value = hash.frequency * idf.get(word);
					if (value > 0) {
						hash.tfidf = value;
						hashtags.put(word, hash);

					} else {
					}
				} else {
					hash.tfidf = hash.frequency * 0.1;
					hashtags.put(word, hash);
				}
			}
			// hashentries = SortingFactory.getSorter().sortHashtagsByValue(
			// hashtags);
			Vector<MediaInformation> mediaInfo = ContentFactory.getParser(
					config).getMedia();
			HashSet<String> stopwords = ic.getStorwords();
			for (MediaInformation m : mediaInfo) {
				for (int i = 1; i <= m.mediaFreq.size(); i++) {
					HashMap<String, Centroid> mtfidf = new HashMap<String, Centroid>();
					for (String tag : m.mediaFreq.get(i).keySet()) {
						StringTokenizer st = new StringTokenizer(tag);
						double value = 0;
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							if (idf.containsKey(token)) {
								if (stopwords.contains(token)) {

								} else {
									value += idf.get(token);
								}

							} else {

								if (stopwords.contains(token)) {

								} else {
									value += 0.1;
								}

							}
						}

						String id = ContentFactory.getID(config);
						Centroid c = new Centroid(id + tag, tag, value);
						if (m.photoUID != null) {
							c.setPhotoUID(m.photoUID);
						} else {
							c.setPhotoUID("null");
						}
						mtfidf.put(tag, c);
					}
					m.mediaTfidf.put(i, mtfidf);
				}
			}
		} else {
			if (config.prop.getProperty("ignoreCase").equals("yes")) {
				for (String word : freq.keySet()) {
					if (idf.containsKey(word)) {
						double value = freq.get(word) * idf.get(word);
						if (value > 0) {
							tfidf.put(word, value);
						} else {
						}
					} else {
						double value = freq.get(word) * 0.1;
						if (value > 0) {
							tfidf.put(word, value);
						} else {
						}
					}
				}
			} else {
				for (String word : freq.keySet()) {
					if (idf.containsKey(word)) {
						double value = freq.get(word) * idf.get(word);
						tfidf.put(word, value);
					} else {

						double value = freq.get(word) * 0.1;
						tfidf.put(word, value);
					}
				}
			}
		}
	}

	public List<Map.Entry<String, Double>> getEntries() {
		return entries;
	}

	@Override
	public void modifyWeight() {

		WeightInterface cI = WeightFactory.getParser(config);
		HashMap<String, Double> significant;
		String key = (String) config.prop.get("dateLimit");

		if (key != null && !key.equals("")
				&& config.prop.get("cutoff").equals("linear")) {
			significant = cI.findDateSignificanceWords();
			if (significant != null && significant.size() > 0) {
				for (String word : significant.keySet()) {
					if (tfidf.containsKey(word)) {
						double number = tfidf.get(word);
						number = number * (significant.get(word) + 1);
						tfidf.put(word, number);
					}
				}
			}
		}
		key = (String) config.prop.get("counterLimit");
		if (key != null && !key.equals("")) {
			significant = cI.findCounterSignificanceWords();
			for (String word : significant.keySet()) {
				if (tfidf.containsKey(word)) {
					double number = tfidf.get(word);
					number = number * (significant.get(word) + 1);
					tfidf.put(word, number);
				}
			}
		}

	}

	@Override
	public LinkedHashMap<String, Double> getTFIDF() {
		return tfidf;
	}

	@Override
	public void clear() {
		entries.clear();
		tfidf.clear();
	}

	// @Override
	// public Vector<Centroid> getCentroids() {
	// Vector<Centroid> centroids = new Vector<Centroid>();
	// for (Entry<String, Double> entry : entries) {
	// try {
	// String id = ContentFactory.getID(config) + entry.getKey();
	// Centroid centroid = new Centroid(id, entry.getKey(),
	// entry.getValue());
	// centroids.add(centroid);
	// } catch (Exception e) {
	//
	// }
	// }
	// return centroids;
	// }

	@Override
	public Vector<Centroid> getMediaCentroids() {
		Vector<Centroid> mediacentroids = new Vector<Centroid>();
		// System.out.println("entries size: " + entries.size());
		for (Entry<String, Double> entry : mediaEntries) {
			// System.out.println("entry: " + entry.getKey() + " "
			// + entry.getValue());
			String id = ContentFactory.getID(config) + entry.getKey();
			Centroid centroid = new Centroid(id, entry.getKey(),
					entry.getValue());
			mediacentroids.add(centroid);
		}
		return mediacentroids;
	}

	public void loadtfidf(LinkedHashMap<String, Ngram> orderedGrams) {
		HashMap<String, Double> idf = ic.getIdfCollection();
		for (String s : orderedGrams.keySet()) {
			// defining size of ngrams to be counted, used with idfs for special
			// ngram size
			Ngram temp = orderedGrams.get(s);
			if (config.prop.containsKey("extractSize")) {

				if (idf.containsKey(s)) {

					temp.setSingleTfidf(idf.get(s) * temp.frequency);
				} else {
					temp.setSingleTfidf(0.1 * temp.frequency);
				}
			} else {// when size of ngram not defined we count each word
					// separately
				for (int i = 0; i < temp.gram.length; i++) {
					if (idf.containsKey(temp.gram[i])) {
						temp.tfidf[i] += idf.get(temp.gram[i]) * temp.frequency;
					} else {
						temp.tfidf[i] += 0.1 * temp.frequency;
					}
				}
			}
		}
	}

	@Override
	public List<Entry<String, HashTag>> getHashTags() {
		return hashentries;
	}

	@Override
	public LinkedHashMap<String, Double> getMediaTFIDF() {
		return mediaTfidf;
	}
}