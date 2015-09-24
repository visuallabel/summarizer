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

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.PlainTextContentObject;
import analyzer.content.WeightFactory;
import analyzer.content.WeightInterface;
import analyzer.content.socialmedia.HashTag;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.database.DataFactory;
import analyzer.database.FeedbackInterface;
import analyzer.database.TagInterface;
import analyzer.ner.NamedEntity;
import analyzer.ner.NamedEntityRecognition;
import analyzer.ner.NerFactory;
import analyzer.ner.NerInterface;
import analyzer.output.FileOutput;
import analyzer.output.ObjectOutput;
import analyzer.output.OutputFactory;
import analyzer.output.OutputInterface;
import analyzer.parser.ParserFactory;
import analyzer.parser.ParserInterface;
import analyzer.segmentation.Centroid;
import analyzer.segmentation.SegmentationFactory;
import analyzer.segmentation.SegmentationInterface;
import analyzer.sorting.SortingFactory;
import analyzer.translation.TranslatorFactory;
import analyzer.weighting.TFIDFFactory;
import analyzer.weighting.TFIDFInterface;
//import cc.mallet.pipe.CharSequence2TokenSequence;
//import cc.mallet.pipe.CharSequenceLowercase;
//import cc.mallet.pipe.Pipe;
//import cc.mallet.pipe.SerialPipes;
//import cc.mallet.pipe.TokenSequence2FeatureSequence;
//import cc.mallet.pipe.TokenSequenceRemoveStopwords;
//import cc.mallet.pipe.iterator.CsvIterator;
//import cc.mallet.topics.ParallelTopicModel;
//import cc.mallet.types.Alphabet;
//import cc.mallet.types.FeatureSequence;
//import cc.mallet.types.IDSorter;
//import cc.mallet.types.InstanceList;
//import cc.mallet.types.LabelSequence;

public class TextAnalyzer {
	public static Logger logger;
	private WeightInterface wI;
	private Config config;

	public TextAnalyzer() {
		logger = LogManager.getLogger("log");
		logger.info("Started logger");
	}

	/**
	 * runTopicModel. Topic modeling method, work in progress (not yet
	 * finished).
	 * 
	 * @param numberoftopics
	 * @param alpha_t
	 * @param beta_w
	 * @param filePath
	 * @param stopwordPath
	 * @param iterations
	 * @param threads
	 * @return
	 */
	// public Vector<TopicOutput> runTopicModel(int numberoftopics,
	// double alpha_t, double beta_w, String filePath,
	// String stopwordPath, int iterations, int threads) {
	// Vector<TopicOutput> topicOutput = new Vector<TopicOutput>();
	// try {
	// // "C:\\mallet\\stoplists\\en.txt"
	// // threads = 2
	// // beta_w = 0.01
	// // alpha_t = 0.01
	// // Note that the first parameter is passed as the sum over topics,
	// // while
	// // the second is the parameter for a single dimension of the
	// // Dirichlet
	// // prior.
	// // Run the model for 50 iterations and stop (this is for testing
	// // only,
	// // for real applications, use 1000 to 2000 iterations)
	// ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
	//
	// // Pipes: lowercase, tokenize, remove stopwords, map to features
	// pipeList.add(new CharSequenceLowercase());
	// pipeList.add(new CharSequence2TokenSequence(Pattern
	// .compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
	// pipeList.add(new TokenSequenceRemoveStopwords(
	// new File(stopwordPath), "UTF-8", false, false, false));
	// pipeList.add(new TokenSequence2FeatureSequence());
	//
	// InstanceList instances = new InstanceList(new SerialPipes(pipeList));
	//
	// // String text = "C:\\Users\\forsstho\\Desktop\\old stuff\\en";
	// String input = filePath;
	// /*
	// * Reader fileReader = new InputStreamReader(new FileInputStream(new
	// * File( args[0])), "UTF-8");
	// */
	// File file = new File(input);
	// if (file.isDirectory()) {
	// for (File files : file.listFiles()) {
	// Reader fileReader = new InputStreamReader(
	// new FileInputStream(files), "UTF-8");
	// instances.addThruPipe(new CsvIterator(fileReader, Pattern
	// .compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2,
	// 1));
	// fileReader.close();
	// }
	//
	// } else {
	// Reader fileReader = new InputStreamReader(new FileInputStream(
	// file), "UTF-8");
	// instances.addThruPipe(new CsvIterator(fileReader, Pattern
	// .compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1));
	// fileReader.close();
	// }
	// // data,
	// // label,
	// // name
	// // fields
	//
	// // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
	// // Note that the first parameter is passed as the sum over topics,
	// // while
	// // the second is the parameter for a single dimension of the
	// // Dirichlet
	// // prior.
	// int numTopics = numberoftopics;
	// ParallelTopicModel model = new ParallelTopicModel(numTopics,
	// alpha_t, beta_w);
	//
	// model.addInstances(instances);
	//
	// // Use two parallel samplers, which each look at one half the corpus
	// // and
	// // combine
	// // statistics after every iteration.
	// model.setNumThreads(threads);
	//
	// // Run the model for 50 iterations and stop (this is for testing
	// // only,
	// // for real applications, use 1000 to 2000 iterations)
	// model.setNumIterations(iterations);
	// model.estimate();
	//
	// // Show the words and topics in the first instance
	//
	// // The data alphabet maps word IDs to strings
	// Alphabet dataAlphabet = instances.getDataAlphabet();
	//
	// FeatureSequence tokens = (FeatureSequence)
	// model.getData().get(0).instance
	// .getData();
	// LabelSequence topics = model.getData().get(0).topicSequence;
	//
	// Formatter out = new Formatter(new StringBuilder(), Locale.US);
	// for (int position = 0; position < tokens.getLength(); position++) {
	// out.format("%s-%d ", dataAlphabet.lookupObject(tokens
	// .getIndexAtPosition(position)), topics
	// .getIndexAtPosition(position));
	// }
	//
	// // Estimate the topic distribution of the first instance,
	// // given the current Gibbs state.
	// double[] topicDistribution = model.getTopicProbabilities(0);
	//
	// // Get an array of sorted sets of word ID/count pairs
	// ArrayList<TreeSet<IDSorter>> topicSortedWords = model
	// .getSortedWords();
	//
	// // Show top 5 words in topics with proportions for the first
	// // document
	// for (int topic = 0; topic < numTopics; topic++) {
	// TopicOutput topicOut = new TopicOutput();
	// Iterator<IDSorter> iterator = topicSortedWords.get(topic)
	// .iterator();
	//
	// out = new Formatter(new StringBuilder(), Locale.US);
	// out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
	// // int rank = 0;
	// topicOut.distribution = topicDistribution[topic];
	// while (iterator.hasNext()) {
	//
	// IDSorter idCountPair = iterator.next();
	// // System.out.println("dataalhpabet: "
	// // + dataAlphabet.lookupObject(idCountPair.getID())
	// // + " weight " + idCountPair.getWeight());
	// topicOut.topicWords.put(
	// dataAlphabet.lookupObject(idCountPair.getID())
	// .toString(), idCountPair.getWeight());
	// // out.format("%s (%.0f) ",
	// // dataAlphabet.lookupObject(idCountPair.getID()),
	// // idCountPair.getWeight());
	// // rank++;
	// }
	//
	// topicOutput.add(topicOut);
	// }
	//
	// // Create a new instance with high probability of topic 0
	// /*
	// * StringBuilder topicZeroText = new StringBuilder();
	// * Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();
	// *
	// * int rank = 0; while (iterator.hasNext() && rank < 5) { IDSorter
	// * idCountPair = iterator.next();
	// * topicZeroText.append(dataAlphabet.lookupObject(idCountPair
	// * .getID()) + " "); rank++; }
	// *
	// * // Create a new instance named "test instance" with empty target
	// * and // source fields. InstanceList testing = new
	// * InstanceList(instances.getPipe()); testing.addThruPipe(new
	// * Instance(topicZeroText.toString(), null, "test instance", null));
	// *
	// * TopicInferencer inferencer = model.getInferencer(); double[]
	// * testProbabilities = inferencer.getSampledDistribution(
	// * testing.get(0), 10, 1, 5); System.out.println("0\t" +
	// * testProbabilities[0]);
	// */
	// } catch (Exception e) {
	//
	// }
	// return topicOutput;
	// }

	/**
	 * runCFOAnalysis API method for running on file specified outside the
	 * config. You also need to specify output path and name of output file.
	 * 
	 * @param configpath
	 * @param filePath
	 * @param outputPathAndName
	 */
	public void runCFOAnalysis(String configpath, File filePath,
			String outputPathAndName) {
		reset();

		config = new Config();
		if (filePath.isDirectory()) {

			for (File f : filePath.listFiles()) {
				if (f.isFile()) {

					config.loadConfig(configpath);
					config.prop.put("file", f.getAbsolutePath());
					StopWordRemover swr = new StopWordRemover(config);
					ParserInterface PI = ParserFactory.getParser(config);
					PI.parseToContentObject();
					PI.translateToEnglish();
					NamedEntityRecognition ner = new NamedEntityRecognition(
							config);
					SegmentationInterface ngi = SegmentationFactory
							.getParser(config);
					wI = WeightFactory.getParser(config);
					TFIDFInterface tfidf = TFIDFFactory.getTFIDF(config);
					tfidf.modifyWeight();

					swr.removeStopWords(tfidf.getTFIDF());
					swr.removeStopWordsFromNgram(ngi.getncurrentGrams());
					swr.removeStopWordsFromNgram(ngi.getncurrentMediaGrams());
					OutputInterface oi = OutputFactory.getParser(config, ngi);
					TagInterface ti = DataFactory.getTagInterface(config, ngi);
					if (ti != null) {
						ti.addTags();
					}
					oi.outputSpecificResults(tfidf,
							(outputPathAndName + f.getName()));
				}
			}
		} else if (filePath.isFile()) {
			config.loadConfig(configpath);
			config.prop.put("file", filePath.getAbsolutePath());
			StopWordRemover swr = new StopWordRemover(config);
			ParserInterface PI = ParserFactory.getParser(config);
			PI.parseToContentObject();
			PI.translateToEnglish();
			NerInterface ner = NerFactory.getNer(config);
			SegmentationInterface ngi = SegmentationFactory.getParser(config);
			wI = WeightFactory.getParser(config);
			TFIDFInterface tfidf = TFIDFFactory.getTFIDF(config);
			tfidf.modifyWeight();

			swr.removeStopWords(tfidf.getTFIDF());
			swr.removeStopWordsFromNgram(ngi.getncurrentGrams());
			swr.removeStopWordsFromNgram(ngi.getncurrentMediaGrams());
			OutputInterface oi = OutputFactory.getParser(config, ngi);
			TagInterface ti = DataFactory.getTagInterface(config, ngi);
			if (ti != null) {
				ti.addTags();
			}
			oi.outputSpecificResults(tfidf,
					(outputPathAndName + filePath.getName()));
		}
	}

	/**
	 * API method for content analysis. Used to build centroids for a whole
	 * category that later can be used in similarity analysis
	 * 
	 * @param configpath
	 * @param filePath
	 * @param outputPathAndName
	 */
	public void buildCombinedCentroids(String configpath, File filePath,
			String outputPathAndName) {
		System.out.println("starting combined centroids");
		reset();
		config = new Config();
		if (filePath.isDirectory()) {
			config.loadConfig(configpath);
			config.prop.put("file", filePath.getAbsolutePath());
			StopWordRemover swr = new StopWordRemover(config);
			ParserInterface PI = ParserFactory.getParser(config);
			PI.parseToContentObject();
			PI.translateToEnglish();
			SegmentationInterface ngi = SegmentationFactory.getParser(config);
			wI = WeightFactory.getParser(config);
			TFIDFInterface tfidf = TFIDFFactory.getTFIDF(config);
			tfidf.modifyWeight();
			// swr.removeStopWords(tfidf.getTFIDF());
			swr.removeStopWordsFromNgram(ngi.getncurrentGrams());
			swr.removeStopWordsFromNgram(ngi.getncurrentMediaGrams());
			OutputInterface oi = OutputFactory.getParser(config, ngi);
			TagInterface ti = DataFactory.getTagInterface(config, ngi);
			if (ti != null) {
				ti.addTags();
			}
			oi.outputSpecificResults(tfidf, outputPathAndName);
		}

	}

	public void buildCombinedCentroids(Config config, File filePath,
			String outputPathAndName) {
		System.out.println("starting combined centroids");
		reset();
		if (filePath.isDirectory()) {
			config.prop.put("file", filePath.getAbsolutePath());
			StopWordRemover swr = new StopWordRemover(config);
			ParserInterface PI = ParserFactory.getParser(config);
			PI.parseToContentObject();
			PI.translateToEnglish();
			SegmentationInterface ngi = SegmentationFactory.getParser(config);
			wI = WeightFactory.getParser(config);
			TFIDFInterface tfidf = TFIDFFactory.getTFIDF(config);
			tfidf.modifyWeight();
			// swr.removeStopWords(tfidf.getTFIDF());
			swr.removeStopWordsFromNgram(ngi.getncurrentGrams());
			swr.removeStopWordsFromNgram(ngi.getncurrentMediaGrams());
			OutputInterface oi = OutputFactory.getParser(config, ngi);
			TagInterface ti = DataFactory.getTagInterface(config, ngi);
			if (ti != null) {
				ti.addTags();
			}
			oi.outputSpecificResults(tfidf, outputPathAndName);
		}

	}

	/**
	 * runCAnalysis When you want to specify everything in the config file and
	 * use the API
	 * 
	 * @param configpath
	 */
	public void runCAnalysis(String configpath) {
		reset();
		config = new Config();
		config.loadConfig(configpath);
		StopWordRemover swr = new StopWordRemover(config);
		ParserInterface PI = ParserFactory.getParser(config);
		PI.parseToContentObject();
		PI.translateToEnglish();
		NerInterface ner = NerFactory.getNer(config);
		SegmentationInterface ngi = SegmentationFactory.getParser(config);
		wI = WeightFactory.getParser(config);
		TFIDFInterface tfidf = TFIDFFactory.getTFIDF(config);
		tfidf.modifyWeight();

		swr.removeStopWords(tfidf.getTFIDF());
		swr.removeStopWordsFromNgram(ngi.getncurrentGrams());
		swr.removeStopWordsFromNgram(ngi.getncurrentMediaGrams());
		OutputInterface oi = OutputFactory.getParser(config, ngi);
		TagInterface ti = DataFactory.getTagInterface(config, ngi);
		if (ti != null) {
			ti.addTags();
		}
		oi.outputResults(tfidf);
	}

	/**
	 * runAnalysisAllParams is a method that runs without a config file, you
	 * need to fill in all the needed fields in the program
	 */

	public void runAnalysisAllParams(String filePath, String outputPathAndName,
			String summary, String limitDate, String combine, int limitCounter,
			String stopwordpath, String contentType, String idfdictionary,
			String NER, String ignoreCase, int cr, String translation,
			int ngrams) {
		reset();
		config = new Config();
		config.prop.put("file", filePath);
		config.prop.put("contentType", contentType);
		config.prop.put("config", "analyzer_config.txt");
		config.prop.put("ignoreCase", ignoreCase);
		config.prop.put("summary", summary);
		config.prop.put("dateLimit", limitDate);
		config.prop.put("stopwords", stopwordpath);
		config.prop.put("ner", NER);
		config.prop.put("cr", cr);
		config.prop.put("idfdictionary", idfdictionary);
		config.prop.put("translation", translation);
		config.prop.put("combine", combine);
		config.prop.put("output", outputPathAndName);
		config.prop.put("counterLimit", limitCounter);
		config.prop.put("ngram", ngrams);
		StopWordRemover swr = new StopWordRemover(config);
		ParserInterface PI = ParserFactory.getParser(config);
		PI.parseToContentObject();
		PI.translateToEnglish();
		NerInterface ner = NerFactory.getNer(config);
		SegmentationInterface ngi = SegmentationFactory.getParser(config);
		wI = WeightFactory.getParser(config);
		TFIDFInterface tfidf = TFIDFFactory.getTFIDF(config);
		tfidf.modifyWeight();
		// swr.removeStopWords(tfidf.getTFIDF());
		swr.removeStopWordsFromNgram(ngi.getncurrentGrams());
		swr.removeStopWordsFromNgram(ngi.getncurrentMediaGrams());
		OutputInterface oi = OutputFactory.getParser(config, ngi);
		TagInterface ti = DataFactory.getTagInterface(config, ngi);
		if (ti != null) {
			ti.addTags();
		}
		oi.outputResults(tfidf);
	}

	static OutputInterface OI;

	/**
	 * 
	 * main is the method that chooses what to do based upon what options we
	 * have added in the config
	 */

	public static void main(String args[]) {
		TextAnalyzer ta = new TextAnalyzer();

		if (args.length > 0) {
			ta.logger = LogManager.getLogger("log");
			ta.logger.info("Started logger");
			ta.config = new Config();
			StopWordRemover swr = new StopWordRemover(ta.config);

			CommandLineOptionsParser clop = new CommandLineOptionsParser(args,
					ta.config);
			ta.config.saveConfig();
			ParserInterface PI = ParserFactory.getParser(ta.config);
			PI.parseToContentObject();
			PI.translateToEnglish();
			NerInterface ner = NerFactory.getNer(ta.config);
			SegmentationInterface ngi = SegmentationFactory
					.getParser(ta.config);
			ta.wI = WeightFactory.getParser(ta.config);
			TFIDFInterface tfidf = TFIDFFactory.getTFIDF(ta.config);
			tfidf.modifyWeight();
			swr.removeStopWordsFromNgram(ngi.getncurrentGrams());
			swr.removeStopWordsFromNgram(ngi.getncurrentMediaGrams());
			// swr.removeStopWords(tfidf.getTFIDF());
			OutputInterface oi = OutputFactory.getParser(ta.config, ngi);
			TagInterface ti = DataFactory.getTagInterface(ta.config, ngi);
			if (ti != null) {
				ti.addTags();
			}
			if (oi instanceof FileOutput) {
				oi.outputResults(tfidf);
			}

			System.out.println("Completed.");
		} else {
			System.out.println("No arguments given. Exiting.");
		}
	}

	/**
	 * addFeedback Assumes that the id we have used is the same as the one we
	 * will be using when returning feedback
	 */
	public boolean addFeedback(String text, int feedback, String userid) {
		boolean success = false;
		FeedbackInterface fi = DataFactory.getFeedback(config);
		success = fi.addFeedback(userid, text, feedback);
		return success;
	}

	/**
	 * API method for running analysis on filePath with config file configpath
	 * 
	 * @param configpath
	 * @param filePath
	 * @return
	 */
	public ObjectOutput runCFAnalysis(String configpath, String filePath) {
		reset();
		if (config == null) {
			config = new Config();
			config.loadConfig(configpath);

		}
		config.prop.put("file", filePath);
		StopWordRemover swr = new StopWordRemover(config);
		// config.saveConfig();
		ParserInterface PI = ParserFactory.getParser(config);
		PI.parseToContentObject();
		PI.translateToEnglish();
		SegmentationInterface ngi = SegmentationFactory.getParser(config);
		wI = WeightFactory.getParser(config);
		TFIDFInterface tfidf = TFIDFFactory.getTFIDF(config);
		tfidf.modifyWeight();
		swr.removeStopWordsFromNgram(ngi.getncurrentGrams());
		swr.removeStopWordsFromNgram(ngi.getncurrentMediaGrams());
		// swr.removeStopWords(tfidf.getTFIDF());
		if (!(ContentFactory.getParser(config) instanceof PlainTextContentObject)) {
			swr.removeStopWords(tfidf.getMediaTFIDF());
			swr.removeMediaStopWords(ContentFactory.getParser(config)
					.getMedia());
		}

		// OutputInterface oi = OutputFactory.getParser(config, ner, ngi);
		// oi.outputResults(tfidf);

		OutputInterface oi = OutputFactory.getParser(config, ngi);
		TagInterface ti = DataFactory.getTagInterface(config, ngi);
		if (ti != null) {
			ti.addTags();
		}
		if (oi instanceof ObjectOutput) {
			return (ObjectOutput) oi;
		} else {
			return null;
		}
	}

	/**
	 * API-call that is mostly used to parse files
	 * 
	 * @param config
	 * @param filePath
	 * @return
	 */

	public ObjectOutput runCFAnalysis(Config config, String filePath) {
		reset();
		config.prop.put("file", filePath);
		StopWordRemover swr = new StopWordRemover(config);
		// config.saveConfig();
		ParserInterface PI = ParserFactory.getParser(config);
		PI.parseToContentObject();
		PI.translateToEnglish();
		SegmentationInterface ngi = SegmentationFactory.getParser(config);
		wI = WeightFactory.getParser(config);
		TFIDFInterface tfidf = TFIDFFactory.getTFIDF(config);
		tfidf.modifyWeight();
		swr.removeStopWordsFromNgram(ngi.getncurrentGrams());
		swr.removeStopWordsFromNgram(ngi.getncurrentMediaGrams());
		// swr.removeStopWords(tfidf.getTFIDF());
		if (!(ContentFactory.getParser(config) instanceof PlainTextContentObject)) {
			swr.removeStopWords(tfidf.getMediaTFIDF());
			swr.removeMediaStopWords(ContentFactory.getParser(config)
					.getMedia());
		}

		// OutputInterface oi = OutputFactory.getParser(config, ner, ngi);
		// oi.outputResults(tfidf);

		OutputInterface oi = OutputFactory.getParser(config, ngi);
		TagInterface ti = DataFactory.getTagInterface(config, ngi);
		if (ti != null) {
			ti.addTags();
		}
		if (oi instanceof ObjectOutput) {
			return (ObjectOutput) oi;
		} else {
			return null;
		}
	}

	public ObjectOutput runCFTwitterAnalysis(String config, String filePath) {
		TextAnalyzer ta = new TextAnalyzer();
		LinkedHashMap<String, NamedEntity> entities = null;
		LinkedHashMap<String, NamedEntity> ner = null;

		ObjectOutput out = ta.runCFAnalysis(config, filePath);
		if (out != null) {
			entities = out.getNamedEntities();

			if (entities != null) {
				ner = new LinkedHashMap<String, NamedEntity>();
				for (String text : entities.keySet()) {
					ner.put(text.toLowerCase(), entities.get(text));
				}
				ner = SortingFactory.getSorter().sortNerByFrequency(ner);
				out.setNamedEntities(ner);
			}
			out.setHashtags();
			LinkedHashMap<String, HashTag> hashtags = out.getHashtags();
			Vector<Centroid> s = out.getSingleOutput();
			HashSet<String> duplicates = new HashSet<String>();
			if(!s.isEmpty())
				duplicates.add(s.get(0).getTag());
			boolean isfound = false;
			for (Centroid c : s) {
				isfound = false;
				for (String compare : duplicates) {
					if (compare.equals(c.getTag())) {
						isfound = true;
					}
				}
				if (!isfound) {
					duplicates.add(c.getTag());
				}
			}
			Vector<Centroid> newCentroids = new Vector<Centroid>();
			if (ner == null) {
				for (Centroid c : s) {
					if (!hashtags.containsKey(c.getTag().toLowerCase())
							&& duplicates.contains(c.getTag())
							&& !c.getTag().contains("http://")) {
						newCentroids.add(c);
					}
				}
			} else {
				for (Centroid c : s) {
					if (
							//!(hashtags.containsKey(c.getTag().toLowerCase())) &&
							!ner.containsKey(c.getTag().toLowerCase())
							&& duplicates.contains(c.getTag())
							&& !c.getTag().contains("http://")) {
						newCentroids.add(c);
					}
				}
			}
			out.setCentroids(newCentroids);

			
			DataFactory.addTwitterTags(out);
		} else {
			logger.error("output object was null in runCFTwitterAnalysis , maybe some path parameter wrong?");
		}
		return out;
	}

	public void reset() {
		WeightFactory.resetWeight();
		ContentFactory.resetContent();
		ParserFactory.resetParser();
		TFIDFFactory.resetFactory();
		SegmentationFactory.reset();
		TranslatorFactory.resetFactory();
		DataFactory.resetFactory();
		SortingFactory.reset();

	}

}
