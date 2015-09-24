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
import analyzer.content.ContentInterface;
import analyzer.content.FacebookContentObject;
import analyzer.content.PlainTextContentObject;
import analyzer.content.TwitterContentObject;
import analyzer.content.socialmedia.HashTag;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.ner.NamedEntity;
import analyzer.ner.NerFactory;
import analyzer.ner.NerInterface;
import analyzer.postprocessing.PPFactory;
import analyzer.process.TextAnalyzer;
import analyzer.segmentation.Centroid;
import analyzer.segmentation.Ngram;
import analyzer.segmentation.SegmentationInterface;
import analyzer.sorting.SortingFactory;
import analyzer.translation.TranslatorFactory;
import analyzer.translation.TranslatorInterface;
import analyzer.translation.yandex.Language;
import analyzer.weighting.TFIDFFactory;
import analyzer.weighting.TFIDFInterface;

/**
 * Class for outputting centroid parsing to files.
 * 
 * @author forsstho
 * 
 */
public class FileOutput implements OutputInterface {
	Config config;
	NerInterface ner;
	LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> ncurrentgrams;
	SegmentationInterface ngi;
	public List<Entry<String, HashTag>> hashtags;
	private LinkedHashMap<String, NamedEntity> namedEntities;
	private LinkedHashMap<Integer, LinkedHashMap<String, Centroid>> ngramCentroids;
	private LinkedHashMap<Integer, Vector<Centroid>> photoCentroids;
	private Vector<MediaInformation> mediaInfo;
	public List<Entry<String, HashTag>> rHashtagsRetranslated;
	TranslatorInterface ti;

	public FileOutput(Config config, SegmentationInterface ngi) {
		NerInterface ner2 = NerFactory.getNer(config);
		this.config = config;
		if (ner2 != null) {
			this.ner = ner2;
		}
		if (ngi != null) {
			this.ncurrentgrams = ngi.getncurrentGrams();
			this.ngi = ngi;
		}

	}

	@Override
	public void outputResults(TFIDFInterface tfidf) {
		ContentInterface ci = ContentFactory.getParser(config);
		if (ci instanceof FacebookContentObject
				|| ci instanceof PlainTextContentObject) {
			List<Map.Entry<String, Double>> centroids = tfidf.getEntries();
			double size = 100;
			try {
				String compressratio = (String) config.prop.get("cr");
				if (!compressratio.equals("")) {
					size = Double.parseDouble(compressratio);
				}
			} catch (Exception e) {
				TextAnalyzer.logger
						.error("Failed to parse compressratio, leaving at 100%");
				// System.out
				// .println("failed to parse compressratio, leaving at 100%");
			}
			size = size / 100;
			try {
				File file, file2;
				String filepath3;
				String path = (String) config.prop.get("output");
				if (path == null || path.equals(" ") || path.equals("")) {
					file = new File("output.txt");
					file2 = new File("namedEntities.txt");
					filepath3 = "";
					// System.out.println("No output defined, printing to output.txt");
					TextAnalyzer.logger
							.info("No output defined, printing to output.txt");
					config.prop.setProperty("output", "output.txt");
				} else {
					if (!path.endsWith("/") && !path.endsWith("\\")) {
						file = new File(path);
						file2 = new File(file.getParent() + File.separator
								+ "namedEntities.txt");
						filepath3 = file.getParent() + File.separator;
					} else {
						file = new File(path + File.separator + "output.txt");
						file2 = new File(path + File.separator
								+ "namedEntities.txt");
						filepath3 = path + File.separator;
					}

				}
				if (!file.exists()) {
					file.createNewFile();
				}
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				for (int i = 0; i < (centroids.size() * size); i++) {
					Entry<String, Double> e = centroids.get(i);
					bw.write(e.getKey() + ";" + e.getValue() + "\n");
				}
				bw.close();
				fw.close();
				TextAnalyzer.logger.info("Output to " + file.getAbsolutePath()
						+ " completed.");
				if (ner != null && ner.getEntities().size() > 0) {

					fw = new FileWriter(file2.getAbsoluteFile());
					bw = new BufferedWriter(fw);
					for (NamedEntity text : ner.getEntities().values()) {
						bw.write(text.namedEntity + "\n");
					}
					bw.close();
					fw.close();
					TextAnalyzer.logger.info("Named entity output to "
							+ file2.getAbsolutePath() + " completed.");
					// System.out.println("Named entity output to "
					// + file2.getAbsolutePath() + " completed.");
					if (ncurrentgrams != null) {
						for (int gramsize : ncurrentgrams.keySet()) {
							LinkedHashMap<String, Ngram> outputVector = ncurrentgrams
									.get(gramsize);
							try {

								file = new File(filepath3 + gramsize + "gram"
										+ ".txt");
								if (!file.exists()) {
									file.createNewFile();
								}

								fw = new FileWriter(file.getAbsoluteFile());
								bw = new BufferedWriter(fw);
								for (Ngram s : outputVector.values()) {

									bw.write(s.toString() + "\t" + s.matchesNER
											+ "\t" + s.frequency + "\t"
											+ s.gettfidf() + "\n");

								}

								bw.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						Vector<Ngram> outputVector = ngi.getCombinedgrams();
						file = new File(filepath3 + "combinedgrams" + ".txt");
						if (!file.exists()) {
							file.createNewFile();
						}
						fw = new FileWriter(file.getAbsoluteFile());
						bw = new BufferedWriter(fw);
						for (Ngram gram : outputVector) {
							bw.write(gram.toString() + "\t" + gram.matchesNER
									+ "\t" + gram.frequency + "\t"
									+ gram.gettfidf() + "\n");
						}
						TextAnalyzer.logger.info("ngrams outputted to "
								+ file.getAbsolutePath());
						// System.out.println("ngrams outputted to "
						// + file.getAbsolutePath());
					}
				}
				bw.close();
				fw.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (ci instanceof TwitterContentObject) {
			try {
				ti = TranslatorFactory.getParser(config);
				// Vector<Centroid> centroids = TFIDFFactory.getCentroids();
				NerInterface ner = NerFactory.getNer(config);
				if (ner != null && ner.getEntities().size() > 0) {
					namedEntities = ner.getEntities();
					// TODO: fix sorting for everything
					ArrayList<Map.Entry<String, Double>> entries = SortingFactory
							.getSorter().sortByValue(
									TFIDFFactory.getTFIDF(config).getTFIDF());
				}
				ngramCentroids = ngi.ngramToCentroids();
				// ngramCentroids.put(1, centroids);

				// Vector<Centroid> mediaCentroids =
				// TFIDFFactory.getMediaCentroids();
				photoCentroids = ngi.getMediaGrams();

				mediaInfo = ContentFactory.getParser(config).getMedia();
				PPFactory.getRedundancyParser(config).removeMediaRedundancy(
						mediaInfo);
				hashtags = TFIDFFactory.getHashTags();
				String filename = config.prop.getProperty("output");
				File file = null;
				if (filename != null) {
					if (filename.endsWith(".txt")) {
						filename = filename.replace(".txt", "");
					}
					filename += "mediaCentroids.txt";
					file = new File(filename);
				} else {
					System.out
							.println("no file name found, using mediaCentroids.txt");
					file = new File("mediaCentroids.txt");
				}
				if (!file.exists()) {
					file.createNewFile();
				}
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				for (MediaInformation m : mediaInfo) {

					HashMap<Integer, HashMap<String, Centroid>> s = m.mediaTfidf;
					bw.write("ngram;translatedngram;tfidf;source\n");
					for (int i : s.keySet()) {
						HashMap<String, Centroid> k = s.get(i);
						for (String d : k.keySet()) {
							String retranslate = retranslate(d);
							bw.write(d + ";" + retranslate + ";"
									+ k.get(d).getTfidf() + ";" + m.source
									+ "\n");

						}
					}

				}
				bw.close();
				fw.close();
				if (filename != null) {
					if (filename.endsWith(".txt")) {
						filename = filename.replace(".txt", "");
					}
					filename += "hashtags.txt";
					file = new File(filename);
				} else {
					System.out
							.println("no file name found, using defaultHashtags.txt");
					file = new File("defaultHashtags.txt");
				}

				// if file doesnt exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}
				fw = new FileWriter(file.getAbsoluteFile());
				bw = new BufferedWriter(fw);

				for (MediaInformation m : mediaInfo) {
					Vector<String> s = m.hashtags;
					for (String text : s) {
						bw.write(text + "; " + m.source + "\n");
					}
				}
				bw.close();
				fw.close();
				if (filename != null) {
					if (filename.endsWith(".txt")) {
						filename = filename.replace(".txt", "");
					}
					filename += "ner.txt";
					file = new File(filename);
				} else {
					System.out
							.println("no file name found, using defaultNER.txt");
					file = new File("defaultNER.txt");
				}
				fw = new FileWriter(file.getAbsoluteFile());
				bw = new BufferedWriter(fw);

				LinkedHashMap<String, NamedEntity> entities = namedEntities;

				bw.write("Named Entity;Name Entity original; location;frequency;source\n");
				for (NamedEntity ent : entities.values()) {
					String retranslate = retranslate(ent.namedEntity);
					bw.write(ent.namedEntity + ";" + retranslate + ";"
							+ ent.getType().toString() + ";" + ent.frequency
							+ ";");
					// bw.write(ent.namedEntity + ";" + ent.getType().toString()
					// + ";" + ent.frequency + ";");
					for (String s : ent.url) {
						bw.write(s + ";");
					}
					bw.write("\n");
				}
				bw.close();
				if (filename != null) {
					if (filename.endsWith(".txt")) {
						filename = filename.replace(".txt", "");
					}
					filename += "allCentroids.txt";
					file = new File(filename);
				} else {
					System.out
							.println("no file name found, using allCentroids.txt");
					file = new File("allCentroids.txt");
				}
				fw = new FileWriter(file.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				Vector<Centroid> s = getSingleOutput();
				int j = 0;
				bw.write("Centroid;Orig centroid; tfidf\n");
				for (Centroid c : s) {
					if (j < 100) {
						String retranslate = retranslate(c.getTag());
						bw.write(c.getTag() + ";" + retranslate + ";"
								+ c.getTfidf() + "\n");
						// bw.write(c.getTag() + ";" + c.getTfidf() + "\n");
					} else {
						break;
					}

				}
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void outputSpecificResults(TFIDFInterface tfidf, String outputPath) {
		List<Map.Entry<String, Double>> centroids = tfidf.getEntries();
		double size = 100;
		try {
			String compressratio = (String) config.prop.get("cr");
			if (compressratio != null && !compressratio.equals("")) {
				size = Double.parseDouble(compressratio);
			}
		} catch (Exception e) {
			TextAnalyzer.logger
					.error("failed to parse compressratio, leaving at 100%");
			// System.out
			// .println("failed to parse compressratio, leaving at 100%");
		}
		size = size / 100;
		try {
			File file, file2;
			String filepath3;
			String path = outputPath;
			if (path == null || path.equals(" ") || path.equals("")) {
				file = new File("output.txt");
				file2 = new File("namedEntities.txt");
				filepath3 = "";
				TextAnalyzer.logger
						.info("No output defined, printing to output.txt");
				// System.out.println("No output defined, printing to output.txt");
				config.prop.setProperty("output", "output.txt");
			} else {
				if (!path.endsWith("/") && !path.endsWith("\\")) {
					file = new File(path);
					file2 = new File(file.getParent() + File.separator
							+ "namedEntities.txt");
					filepath3 = file.getParent() + File.separator;
				} else {
					file = new File(path + File.separator + "output.txt");
					file2 = new File(path + File.separator
							+ "namedEntities.txt");
					filepath3 = path + File.separator;
				}

			}
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < (centroids.size() * size); i++) {
				Entry<String, Double> e = centroids.get(i);
				bw.write(e.getKey() + ";" + e.getValue() + "\n");
			}
			bw.close();
			fw.close();
			TextAnalyzer.logger.info("Output to " + file.getAbsolutePath()
					+ " completed.");
			// System.out.println("Output to " + file.getAbsolutePath()
			// + " completed.");
			if (ner != null && ner.getEntities().size() > 0) {
				fw = new FileWriter(file2.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				for (NamedEntity ne : ner.getEntities().values()) {
					bw.write(ne.namedEntity + "\n");
				}
				bw.close();
				fw.close();
				TextAnalyzer.logger.info("Named entity output to "
						+ file2.getAbsolutePath() + " completed.");
				// System.out.println("Named entity output to "
				// + file2.getAbsolutePath() + " completed.");
				for (int gramsize : ncurrentgrams.keySet()) {
					LinkedHashMap<String, Ngram> outputVector = ncurrentgrams
							.get(gramsize);
					try {

						file = new File(filepath3 + gramsize + "gram" + ".txt");
						if (!file.exists()) {
							file.createNewFile();
						}

						fw = new FileWriter(file.getAbsoluteFile());
						bw = new BufferedWriter(fw);
						for (Ngram s : outputVector.values()) {

							bw.write(s.toString() + "\t" + s.matchesNER + "\t"
									+ s.frequency + "\t" + s.gettfidf() + "\n");

						}

						bw.close();
					} catch (IOException e) {
						TextAnalyzer.logger.error(e.getMessage());

					}
				}
				if (ngi != null) {
					Vector<Ngram> outputVector = ngi.getCombinedgrams();
					file = new File(filepath3 + "combinedgrams" + ".txt");
					if (!file.exists()) {
						file.createNewFile();
					}
					fw = new FileWriter(file.getAbsoluteFile());
					bw = new BufferedWriter(fw);
					for (Ngram gram : outputVector) {
						bw.write(gram.toString() + "\t" + gram.matchesNER
								+ "\t" + gram.frequency + "\t"
								+ gram.gettfidf() + "\n");
					}
					TextAnalyzer.logger.info("ngrams outputted to "
							+ file.getAbsolutePath());
					// System.out.println("ngrams outputted to "
					// + file.getAbsolutePath());
				}
			}
			bw.close();
			fw.close();

		} catch (IOException e) {
			TextAnalyzer.logger.error(e.getMessage());
		}
		tfidf.clear();
	}

	public String retranslate(String text) {
		String returned = null;
		if (ti != null && ti.calculateRetranslation() != null
				&& ti.calculateRetranslation() != Language.ENGLISH) {
			returned = ti.translateBack(text);
		}
		return returned;
	}

	public Vector<Centroid> getSingleOutput() {
		Vector<Centroid> sortedOutput = new Vector<Centroid>();
		for (LinkedHashMap<String, Centroid> cents : ngramCentroids.values()) {
			sortedOutput.addAll(cents.values());
		}
		SortingFactory.getSorter().sortByTfidf(sortedOutput);
		Collections.reverse(sortedOutput);
		return sortedOutput;
	}

	@Override
	public LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> getNgrams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<Centroid> getSpecificOutput(int size) {
		// TODO Auto-generated method stub
		return null;
	}
}
