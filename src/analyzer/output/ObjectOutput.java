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
package analyzer.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.TwitterContentObject;
import analyzer.content.socialmedia.HashTag;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.ner.NamedEntity;
import analyzer.ner.NerFactory;
import analyzer.ner.NerInterface;
import analyzer.postprocessing.PPFactory;
import analyzer.segmentation.Centroid;
import analyzer.segmentation.Ngram;
import analyzer.segmentation.SegmentationInterface;
import analyzer.segmentation.Sentence;
import analyzer.sorting.SortingFactory;
import analyzer.translation.TranslatorFactory;
import analyzer.translation.TranslatorInterface;
import analyzer.weighting.TFIDFFactory;
import analyzer.weighting.TFIDFInterface;

/**
 * Class used to build the object containing all relevant output. This is used
 * when we want to keep the results in memory instead of outputting to a file.
 * 
 * @author forsstho
 * 
 */
public class ObjectOutput implements OutputInterface {
	public List<Entry<String, HashTag>> hashtags;
	private LinkedHashMap<String, NamedEntity> namedEntities;
	private LinkedHashMap<Integer, LinkedHashMap<String, Centroid>> ngramCentroids;
	private LinkedHashMap<Integer, Vector<Centroid>> photoCentroids;
	private LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> ngrams;
	private LinkedHashMap<String, HashTag> hashtaglist = new LinkedHashMap<String, HashTag>();
	private Vector<MediaInformation> mediaInfo;
	public List<Entry<String, HashTag>> rHashtagsRetranslated;
	private LinkedHashMap<String, Sentence> sentences;
	Vector<Centroid> sortedOutput;
	TranslatorInterface ti;
	Config config;

	public ObjectOutput(Config config, SegmentationInterface ngi) {
		ti = TranslatorFactory.getParser(config);
		this.config = config;
		// Vector<Centroid> centroids = TFIDFFactory.getCentroids();
		NerInterface ner = NerFactory.getNer(config);
		if (ner != null && ner.getEntities().size() > 0) {
			namedEntities = ner.getEntities();
			// TODO: fix sorting for everything
			ArrayList<Map.Entry<String, Double>> entries = SortingFactory
					.getSorter().sortByValue(
							TFIDFFactory.getTFIDF(config).getTFIDF());
		}
		else 
		{
			namedEntities=new LinkedHashMap<String,NamedEntity>();
		}
		ngramCentroids = ngi.ngramToCentroids();
		ngrams = ngi.getncurrentGrams();
		// ngramCentroids.put(1, centroids);

		// Vector<Centroid> mediaCentroids = TFIDFFactory.getMediaCentroids();

		mediaInfo = ContentFactory.getParser(config).getMedia();
		PPFactory.getRedundancyParser(config).removeMediaRedundancy(mediaInfo);
		hashtags = TFIDFFactory.getHashTags();
		photoCentroids = ngi.getMediaGrams();
		sentences = ngi.getSentences();
		if (ContentFactory.getParser(config) instanceof TwitterContentObject) {
			// retranslate();

		}
	}

	/**
	 * Translate english text back to original language
	 */
	public void retranslate() {

		if (ti != null && ti.calculateRetranslation() != null) {
			for (int size : ngramCentroids.keySet()) {
				LinkedHashMap<String, Centroid> ngrams = ngramCentroids
						.get(size);
				for (Centroid cent : ngrams.values()) {
					String returned = ti.translateBack(cent.getTag());
					cent.setOriginalTag(returned);
					if (namedEntities != null) {
						for (String key : namedEntities.keySet()) {
							if (key.toLowerCase().equals(
									cent.getTag().toLowerCase())) {
								NamedEntity ne = namedEntities.get(key);
								ne.namedEntityOriginalLanguage = returned;
							}
						}
					}
				}

				for (int size2 : photoCentroids.keySet()) {
					Vector<Centroid> ngrams2 = photoCentroids.get(size2);
					for (Centroid cent2 : ngrams2) {
						String returned = ti.translateBack(cent2.getTag());
						cent2.setOriginalTag(returned);
						for (MediaInformation mi : mediaInfo) {
							HashMap<Integer, HashMap<String, Centroid>> s = mi.mediaTfidf;
							for (int size3 : s.keySet()) {
								HashMap<String, Centroid> media = s.get(size3);
								if (media.containsKey(cent2.getTag())) {
									Centroid c2 = media.get(cent2.getTag());
									c2.setOriginalTag(returned);
								}
							}
						}
					}
				}
			}
		}
	}

	public String retranslate(String text) {
		String returned = null;
		if (ti != null && ti.calculateRetranslation() != null) {

			returned = ti.translateBack(text);

		}
		return returned;
	}

	public LinkedHashMap<String, NamedEntity> getNamedEntities() {
		return namedEntities;
	}

	public void setHashtags() {
		try {
			Vector<MediaInformation> mi = getMedia();
			if (mi != null) {                    
				for (MediaInformation m : mi) {

					Vector<String> s = m.hashtags;
					if (s != null) {
						for (String text : s) {
							if (text != null) {
								text = text.replace(",", "");
								text = text.replace(".", "");
								text = text.replace("\"", "");
								if (!text.equals("")) {
									HashTag ht;
									if (hashtaglist.containsKey(text.toLowerCase())) {
										ht = hashtaglist.get(text.toLowerCase());
									} else {
										ht = new HashTag();
										ht.tag = text;
									}
									ht.frequency++;
									ht.sources.add(m.source);
									if (namedEntities.containsKey(text
											.toLowerCase())) {
										ht.matchesNer = true;
									}
									hashtaglist.put(text.toLowerCase(), ht);
								}
							}
						}
					}
				}
				hashtaglist = SortingFactory.getSorter()
						.sortHashByNerAndFrequency(hashtaglist);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public LinkedHashMap<String, HashTag> getHashtags() {
		return hashtaglist;
	}

	public void setNamedEntities(LinkedHashMap<String, NamedEntity> ent) {
		namedEntities = ent;
	}

	public LinkedHashMap<Integer, LinkedHashMap<String, Centroid>> getCentroids() {
		return ngramCentroids;
	}

	public void setCentroids(Vector<Centroid> cent) {
		sortedOutput = cent;
	}

	public LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> getNgrams() {
		return ngrams;
	}

	@Override
	public Vector<Centroid> getSpecificOutput(int size) {
		Vector<Centroid> sortedOutput = new Vector<Centroid>();
		LinkedHashMap<String, Centroid> cents = ngramCentroids.get(size);
		sortedOutput.addAll(cents.values());
		SortingFactory.getSorter().sortByTfidf(sortedOutput);
		Collections.reverse(sortedOutput);
		return sortedOutput;
	}

	public Vector<Centroid> getSingleOutput() {
		if (sortedOutput == null) {
			sortedOutput = new Vector<Centroid>();
			for (LinkedHashMap<String, Centroid> cents : ngramCentroids
					.values()) {
				sortedOutput.addAll(cents.values());
			}
			SortingFactory.getSorter().sortByTfidf(sortedOutput);
			Collections.reverse(sortedOutput);
		}
		return sortedOutput;
	}

	public Vector<Centroid> getSinglePhotoOutput() 
	{
		Vector<Centroid> locallySortedOutput = new Vector<Centroid>();
		for (Vector<Centroid> cents : photoCentroids.values()) {
			
			for(Centroid c : cents)
			{
				for(Centroid check : sortedOutput)
				{
					if(check.getTag().equals(c.getTag()))
					{
						locallySortedOutput.add(c);
						break;
					}
				}
				
			}
		}
	
		SortingFactory.getSorter().sortByTfidf(locallySortedOutput);
		Collections.reverse(locallySortedOutput);
		return locallySortedOutput;
	}

	public Vector<MediaInformation> getMedia() {
		return mediaInfo;
	}

	public LinkedHashMap<String, Sentence> getSentences() {
		return sentences;
	}

	public LinkedHashMap<Integer, Vector<Centroid>> getPhotoCentroids() {
		for (int num : photoCentroids.keySet()) {
			Vector<Centroid> cent = photoCentroids.get(num);
			SortingFactory.getSorter().sortByTfidf(cent);
			Collections.reverse(cent);
			photoCentroids.put(num, cent);
		}
		return photoCentroids;
	}

	@Override
	public void outputResults(TFIDFInterface tfidf) {
		System.out.println("results not implemented");

	}

	@Override
	public void outputSpecificResults(TFIDFInterface tfidf, String outputPath) {
		try {

			File outputFile = new File(outputPath);
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
			BufferedWriter bw = null;
			FileWriter fw = null;
			Vector<Centroid> s = null;
			fw = new FileWriter(outputFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			if (config.prop.containsKey("extractSize")
					&& config.prop.containsKey("ngram")) {
				int ngramsize = Integer.parseInt(config.prop
						.getProperty("ngram"));
				s = getSpecificOutput(ngramsize);
			} else {
				s = getSingleOutput();
			}

			for (Centroid c : s) {
				bw.write(c.getTag() + ";" + c.getTfidf() + "\n");
			}
			bw.close();
			System.out.println("CC completed to: " + outputPath);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
