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
package analyzer.segmentation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.ContentInterface;
import analyzer.content.FacebookContentObject;
import analyzer.content.PlainTextContentObject;
import analyzer.content.TextObject;
import analyzer.content.TwitterContentObject;
import analyzer.content.socialmedia.Comment;
import analyzer.content.socialmedia.EventInformation;
import analyzer.content.socialmedia.GroupInformation;
import analyzer.content.socialmedia.LikeInformation;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.content.socialmedia.StatusMessage;
import analyzer.database.DataFactory;
import analyzer.database.FeedbackInterface;
import analyzer.database.User;
import analyzer.ner.NamedEntity;
import analyzer.ner.NerFactory;
import analyzer.ner.NerInterface;
import analyzer.process.TextAnalyzer;
import analyzer.sorting.SortingFactory;

/**
 * Class that specifically parses and orders ngrams from any inputted file
 * 
 * @author forsstho
 * 
 */
public class SegmentationParser implements SegmentationInterface {
	int ngramsize;
	public LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> ncurrentgrams;
	public LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> mediaGrams;
	Config config;
	NerInterface ner;
	LinkedHashMap<String, Ngram> ngram;
	LinkedHashMap<String, Ngram> mediaGram;
	LinkedHashMap<String, Ngram> grams;
	LinkedHashMap<String, Sentence> sentences = new LinkedHashMap<String, Sentence>();

	public SegmentationParser(Config config) {
		if (config.prop.containsKey("ngram")) {
			try {
				this.ngramsize = Integer.parseInt(config.prop
						.getProperty("ngram"));
			} catch (Exception e) {
				TextAnalyzer.logger
				.error("error parsing ngramsize, use  ngramsize = number, continuing with ngramsize = 1");
				this.ngramsize = 1;
			}
		} else {
			this.ngramsize = 1;
		}
		ncurrentgrams = new LinkedHashMap<Integer, LinkedHashMap<String, Ngram>>();
		mediaGrams = new LinkedHashMap<Integer, LinkedHashMap<String, Ngram>>();
		this.config = config;
		this.ner = NerFactory.getNer(config);
		parseNgram(ContentFactory.getParser(config));
		sentenceSegmentation(ContentFactory.getParser(config));
	}

	public void addAll(LinkedHashMap<String, Ngram> ngram, Ngram gram) {
		if (ngram.containsKey(gram.toString())) {
			Ngram temp = ngram.get(gram.toString());
			temp.frequency++;
			temp.source.addAll(gram.source);
			ngram.put(gram.toString(), temp);
		} else {
			ngram.put(gram.toString(), gram);
		}
	}

	@Override
	public void parseNgram(ContentInterface cI) {
		String datePattern = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		Date date = null;
		int cutoff = 0;
		if (config.prop.containsKey("dateLimit")
				&& config.prop.containsKey("cutoff")
				&& config.prop.getProperty("cutoff").equals("absolute")) {
			cutoff = 1;
			try {
				date = sdf.parse(config.prop.getProperty("dateLimit"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (config.prop.containsKey("dateLimit")
				&& config.prop.containsKey("cutoff")
				&& config.prop.getProperty("cutoff").equals("linear")) {
			cutoff = -1;
			try {
				date = sdf.parse(config.prop.getProperty("dateLimit"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			cutoff = 0;
		}
		for (int currentNgramSize = ngramsize; currentNgramSize >= 1; currentNgramSize--) {
			ngram = new LinkedHashMap<String, Ngram>();
			mediaGram = new LinkedHashMap<String, Ngram>();
			grams = new LinkedHashMap<String, Ngram>();
			boolean ignoreCase = false;
			if (config.prop.containsKey("ignoreCase")
					&& config.prop.getProperty("ignoreCase").equals("yes")) {
				ignoreCase = true;
			}
			if (cI instanceof FacebookContentObject) {
				FacebookContentObject co = (FacebookContentObject) cI;
				for (EventInformation info : co.eventInformation) {
					if (cutoff == 1) {
						if (date != null && info.date != null
								&& info.date.after(date)) {
							if (ignoreCase) {
								buildNgrams(ngram,
										info.description.toLowerCase(),
										currentNgramSize, info.weight,
										info.date, null);
								buildNgrams(ngram,
										info.eventName.toLowerCase(),
										currentNgramSize, info.weight,
										info.date, null);
							} else {
								buildNgrams(ngram, info.description,
										currentNgramSize, info.weight,
										info.date, null);
								buildNgrams(ngram, info.eventName,
										currentNgramSize, info.weight,
										info.date, null);
							}
						}
					} else if (cutoff == 0) {
						if (ignoreCase) {
							buildNgrams(ngram, info.description.toLowerCase(),
									currentNgramSize, info.weight, info.date,
									null);
							buildNgrams(ngram, info.eventName.toLowerCase(),
									currentNgramSize, info.weight, info.date,
									null);
						} else {
							buildNgrams(ngram, info.description,
									currentNgramSize, info.weight, info.date,
									null);
							buildNgrams(ngram, info.eventName,
									currentNgramSize, info.weight, info.date,
									null);
						}
					} else {
					}
				}
				for (GroupInformation info : co.groupInformation) {
					try {
						if (cutoff == 1) {
							if (date != null && info.date != null
									&& info.date.after(date)) {
								if (ignoreCase) {
									buildNgrams(ngram,
											info.description.toLowerCase(),
											currentNgramSize, 1, info.date,
											null);
									buildNgrams(ngram, info.name.toLowerCase(),
											currentNgramSize, info.weight,
											info.date, null);
								} else {
									buildNgrams(ngram, info.description,
											currentNgramSize, 1, info.date,
											null);
									buildNgrams(ngram, info.name,
											currentNgramSize, info.weight,
											info.date, null);
								}
							}
						} else if (cutoff == 0) {

							if (ignoreCase) {
								buildNgrams(ngram,
										info.description.toLowerCase(),
										currentNgramSize, 1, info.date, null);
								buildNgrams(ngram, info.name.toLowerCase(),
										currentNgramSize, info.weight,
										info.date, null);
							} else {
								buildNgrams(ngram, info.description,
										currentNgramSize, 1, info.date, null);
								buildNgrams(ngram, info.name, currentNgramSize,
										info.weight, info.date, null);
							}
						} else {

						}

					} catch (Exception e) {

					}
				}
				for (LikeInformation info : co.likeInformation) {
					try {
						if (cutoff == 1) {
							if (date != null && info.date != null
									&& info.date.after(date)) {
								if (ignoreCase) {
									buildNgrams(ngram, info.name.toLowerCase(),
											currentNgramSize, info.weight,
											info.date, null);
									buildNgrams(ngram,
											info.category.toLowerCase(),
											currentNgramSize, 1, info.date,
											null);
								} else {
									buildNgrams(ngram, info.name,
											currentNgramSize, info.weight,
											info.date, null);
									buildNgrams(ngram, info.category,
											currentNgramSize, 1, info.date,
											null);
								}
							}

						} else if (cutoff == 0) {
							if (ignoreCase) {
								buildNgrams(ngram, info.name.toLowerCase(),
										currentNgramSize, info.weight,
										info.date, null);
								buildNgrams(ngram, info.category.toLowerCase(),
										currentNgramSize, 1, info.date, null);
							} else {
								buildNgrams(ngram, info.name, currentNgramSize,
										info.weight, info.date, null);
								buildNgrams(ngram, info.category,
										currentNgramSize, 1, info.date, null);
							}
						} else {

						}
					} catch (Exception e) {

					}
				}
				for (MediaInformation info : co.photoInformation) {
					try {
						if (cutoff == 1) {
							if (date != null && info.date != null
									&& info.date.after(date)) {
								if (ignoreCase) {
									buildNgrams(ngram,
											info.description.toLowerCase(),
											currentNgramSize, info.weight,
											info.date, info.photoUID);
									for (Comment c : info.comments) {
										buildNgrams(ngram,
												c.message.toLowerCase(),
												currentNgramSize, c.weight,
												c.createdTimeStamp,
												info.photoUID);
									}
								} else {
									buildNgrams(ngram, info.description,
											currentNgramSize, info.weight,
											info.date, info.photoUID);
									for (Comment c : info.comments) {
										buildNgrams(ngram, c.message,
												currentNgramSize, c.weight,
												c.createdTimeStamp,
												info.photoUID);
									}
								}
							}

						} else if (cutoff == 0) {
							if (ignoreCase) {
								buildNgrams(ngram,
										info.description.toLowerCase(),
										currentNgramSize, info.weight,
										info.date, info.photoUID);
								for (Comment c : info.comments) {
									buildNgrams(ngram, c.message.toLowerCase(),
											currentNgramSize, c.weight,
											c.createdTimeStamp, info.photoUID);
								}
							} else {
								buildNgrams(ngram, info.description,
										currentNgramSize, info.weight,
										info.date, info.photoUID);
								for (Comment c : info.comments) {
									buildNgrams(ngram, c.message,
											currentNgramSize, c.weight,
											c.createdTimeStamp, info.photoUID);
								}
							}
						} else {

						}
					} catch (Exception e) {

					}
				}

				for (MediaInformation info : co.videoInformation) {
					try {
						if (cutoff == 1) {
							if (date != null && info.date != null
									&& info.date.after(date)) {

								if (ignoreCase) {
									buildNgrams(ngram,
											info.description.toLowerCase(),
											currentNgramSize, info.weight,
											info.date, info.photoUID);
									for (Comment c : info.comments) {
										buildNgrams(ngram,
												c.message.toLowerCase(),
												currentNgramSize, c.weight,
												c.createdTimeStamp,
												info.photoUID);
									}
								} else {
									buildNgrams(ngram, info.description,
											currentNgramSize, info.weight,
											info.date, info.photoUID);
									for (Comment c : info.comments) {
										buildNgrams(ngram, c.message,
												currentNgramSize, c.weight,
												c.createdTimeStamp,
												info.photoUID);
									}
								}
							}

						} else if (cutoff == 0) {
							if (ignoreCase) {
								buildNgrams(ngram,
										info.description.toLowerCase(),
										currentNgramSize, info.weight,
										info.date, info.photoUID);
								for (Comment c : info.comments) {
									buildNgrams(ngram, c.message.toLowerCase(),
											currentNgramSize, c.weight,
											c.createdTimeStamp, info.photoUID);
								}
							} else {
								buildNgrams(ngram, info.description,
										currentNgramSize, info.weight,
										info.date, info.photoUID);
								for (Comment c : info.comments) {
									buildNgrams(ngram, c.message,
											currentNgramSize, c.weight,
											c.createdTimeStamp, info.photoUID);
								}
							}
						} else {

						}
					} catch (Exception e) {

					}
				}
				for (StatusMessage info : co.statusMessageInformation) {
					try {
						if (cutoff == 1) {
							if (date != null && info.date != null
									&& info.date.after(date)) {
								if (ignoreCase) {
									buildNgrams(ngram,
											info.message.toLowerCase(),
											currentNgramSize, info.weight,
											info.date, null);
									for (Comment c : info.comments) {
										buildNgrams(ngram,
												c.message.toLowerCase(),
												currentNgramSize, c.weight,
												c.createdTimeStamp, null);
									}
								} else {
									buildNgrams(ngram, info.message,
											currentNgramSize, info.weight,
											info.date, null);
									for (Comment c : info.comments) {
										buildNgrams(ngram, c.message,
												currentNgramSize, c.weight,
												c.createdTimeStamp, null);
									}
								}
							}

						} else if (cutoff == 0) {
							if (ignoreCase) {
								buildNgrams(ngram, info.message.toLowerCase(),
										currentNgramSize, info.weight,
										info.date, null);
								for (Comment c : info.comments) {
									buildNgrams(ngram, c.message.toLowerCase(),
											currentNgramSize, c.weight,
											c.createdTimeStamp, null);
								}
							} else {
								buildNgrams(ngram, info.message,
										currentNgramSize, info.weight,
										info.date, null);
								for (Comment c : info.comments) {
									buildNgrams(ngram, c.message,
											currentNgramSize, c.weight,
											c.createdTimeStamp, null);
								}
							}
						} else {

						}
					} catch (Exception e) {

					}
				}

			} else if (cI instanceof PlainTextContentObject) {
				PlainTextContentObject ptco = (PlainTextContentObject) cI;
				for (TextObject to : ptco.content) {
					buildNgrams(ngram, to.text, currentNgramSize, 1,
							to.timeStamp, null);
				}
			} else if (cI instanceof TwitterContentObject) {
				TwitterContentObject tco = (TwitterContentObject) cI;
				for (MediaInformation info : tco.photoInformation) {
					String source = info.source;
					HashMap<String, Double> mediaGrams2 = new HashMap<String, Double>();
					grams = new LinkedHashMap<String, Ngram>();
					try {
						StringTokenizer st = new StringTokenizer(
								info.description);
						String text = "";
						while (st.hasMoreTokens()) {
							String check = st.nextToken();
							if (!check.contains("#")) {
								if (st.hasMoreTokens()) {
									text += check + " ";
								} else {
									text += check;
								}
							}
							if (check.contains("#")) {
								String newtext = check;
								newtext = newtext.replace("#", "");
								if (st.hasMoreTokens()) {
									text += newtext + " ";
								} else {
									text += newtext;
								}
							}
						}
						if (ignoreCase) {
							buildNgrams(grams, text.toLowerCase(),
									currentNgramSize, info.weight, source,
									info.date, info.photoUID);
							buildNgrams(ngram, text.toLowerCase(),
									currentNgramSize, info.weight, source,
									info.date, info.photoUID);
							buildNgrams(mediaGram, text.toLowerCase(),
									currentNgramSize, info.weight, source,
									info.date, info.photoUID);
						} else {

							buildNgrams(grams, text, currentNgramSize,
									info.weight, source, info.date,
									info.photoUID);
							buildNgrams(ngram, text, currentNgramSize,
									info.weight, source, info.date,
									info.photoUID);
							buildNgrams(mediaGram, text, currentNgramSize,
									info.weight, source, info.date,
									info.photoUID);

						}

					} catch (Exception e) {
							int fas=3;
					}
					for (Ngram gram : grams.values()) {
						if (mediaGrams2.containsKey(gram.toString())) {
							mediaGrams2.put(gram.toString(),
									(double) (gram.frequency + 1));

						} else {
							mediaGrams2.put(gram.toString(),
									(double) gram.frequency);
						}

					}
					info.mediaFreq.put(currentNgramSize, mediaGrams2);
				}
				for (MediaInformation info : tco.videoInformation) {
					String source = info.source;
					grams = new LinkedHashMap<String, Ngram>();
					HashMap<String, Double> mediaGrams2 = new HashMap<String, Double>();
					try {
						if (ignoreCase) {
							buildNgrams(grams, info.description.toLowerCase(),
									currentNgramSize, info.weight, source,
									info.date, info.photoUID);
							buildNgrams(ngram, info.description.toLowerCase(),
									currentNgramSize, info.weight, source,
									info.date, info.photoUID);
							buildNgrams(mediaGram,
									info.description.toLowerCase(),
									currentNgramSize, info.weight, source,
									info.date, info.photoUID);

						} else {
							buildNgrams(grams, info.description,
									currentNgramSize, info.weight, source,
									info.date, info.photoUID);
							buildNgrams(ngram, info.description,
									currentNgramSize, info.weight, source,
									info.date, info.photoUID);
							buildNgrams(mediaGram, info.description,
									currentNgramSize, info.weight, source,
									info.date, info.photoUID);

						}

					} catch (Exception e) {

					}
					for (Ngram gram : grams.values()) {
						mediaGrams2.put(gram.toString(), gram.gettfidf());
					}
					info.mediaFreq.put(currentNgramSize, mediaGrams2);
				}
				for (StatusMessage info : tco.statusMessageInformation) {
					try {

						if (ignoreCase) {
							buildNgrams(ngram, info.message.toLowerCase(),
									currentNgramSize, info.weight, null,
									info.date, null);

						} else {
							buildNgrams(ngram, info.message, currentNgramSize,
									info.weight, null, info.date, null);

						}
					} catch (Exception e) {

					}
				}
			}

			else {
				TextAnalyzer.logger
				.error("ContentObject of wrong type. Exiting.");
				// System.exit(0);
			}
			ncurrentgrams.put(currentNgramSize, ngram);
			mediaGrams.put(currentNgramSize, mediaGram);
			// System.out.println("size of ngram: " + ngram.size());
			// System.out.println("size of mediaGram: " + mediaGram.size());
		}
	}

	/**
	 * if feedback property is set we only add those not matched to ngrams
	 * 
	 * @param ngram2
	 * @param info
	 * @param currentNgramSize
	 * @param weight
	 * @param timeStamp
	 * @return
	 */
	
	//TODO: changed things in this method
	private void buildNgrams(LinkedHashMap<String, Ngram> tempgram,
			String info, int currentNgramSize, int weight, Date timeStamp,
			String photoUID) {
		Vector<Vector<String>> sentences = new Vector<Vector<String>>();
		Vector<String> words = new Vector<String>();
		FeedbackInterface fi = null;
		ContentInterface ci = null;
		if (config.prop.containsKey("feedback")
				&& config.prop.getProperty("feedback").equals("mysql")
				&& config.prop.getProperty("contentType").equals("facebook")) {
			fi = DataFactory.getFeedback(config);
			ci = ContentFactory.getParser(config);
		}
		if (fi != null) {
			if (info != null && !info.equals("")) {
				User user = fi.readContent(ci.getID());

				if (info != null && !info.equals("")) {
					StringTokenizer st = new StringTokenizer(info);
					while (st.hasMoreTokens()) {
						String token = st.nextToken();						
						if(!(token.equals("-") || token.equals("--") || token.equals("—") ||
								token.equals("!") || token.equals("?") || 
								token.equals(".") || token.equals(":") || 
								token.equals(";") || token.equals("â€¦")|| 
								token.equals("âœ“")|| token.equals("â†’")|| 
								token.equals("âœ�")|| token.equals("âœ˜")|| 
								token.equals("â˜¼")|| token.equals("â†“")|| 
								token.equals("â†‘")|| token.equals("â™«")|| 
								token.equals("âœˆ")|| token.equals("=")|| 
								token.equals("â€”")|| 
								token.equals("â€“")||
								token.equals("+"))) {
							if(token.endsWith(".") || token.endsWith(",") || 
									token.endsWith(";") || token.endsWith(":") || 
									token.endsWith("!") || token.endsWith("?")) {
								words.add(token);
								sentences.add(words);
								words = new Vector<String>();
							}else {
								if(!isURL(token)) {
									words.add(token);
									}
							}
						}
						else {
							sentences.add(words);
							words = new Vector<String>();
						}
					}
					for(Vector<String> ngramwords : sentences) {
						if (ngramwords.size() >= currentNgramSize) {
							for (int i = 0; i < ngramwords.size(); i++) {
								if (i + currentNgramSize <= ngramwords.size()) {
									Ngram gram = new Ngram(currentNgramSize);
									gram.timestamp = timeStamp;
									gram.weight = weight;
									gram.setPhotoUID(photoUID);
									String checkString = "";
									for (int j = 0; j < currentNgramSize; j++) {
										gram.gram[j] = ngramwords.get(i + j);
										if (j != currentNgramSize - 1) {
											checkString += gram.gram[j] + " ";
										} else {
											checkString += gram.gram[j];
										}

									}
									if (checkIntegrity(gram)) {

										if (!user.negativeWords.keySet().contains(
												checkString)) {
											addAll(tempgram, gram);
										}
									}
								}
							}
						}
					}
				}

			}
		} else {
			if (info != null && !info.equals("")) {
				StringTokenizer st = new StringTokenizer(info);
				while (st.hasMoreTokens()) {
					String token = st.nextToken();						
					if(!(token.equals("-") || token.equals("--") || token.equals("—") ||
							token.equals("!") || token.equals("?") || 
							token.equals(".") || token.equals(":") || 
							token.equals(";") || token.equals("â€¦")|| 
							token.equals("âœ“")|| token.equals("â†’")|| 
							token.equals("âœ�")|| token.equals("âœ˜")|| 
							token.equals("â˜¼")|| token.equals("â†“")|| 
							token.equals("â†‘")|| token.equals("â™«")|| 
							token.equals("âœˆ")|| token.equals("=")|| 
							token.equals("â€”")|| 
							token.equals("â€“")||
							token.equals("+"))) {
						words.add(token);
					}
					else {
						sentences.add(words);
						words = new Vector<String>();
					}
					if(token.endsWith(".") || token.endsWith(",") || 
							token.endsWith(";") || token.endsWith(":") || 
							token.endsWith("!") || token.endsWith("?")) {
						words.add(token);
						sentences.add(words);
						words = new Vector<String>();
					}
					else {
						if(!isURL(token)) {
							words.add(token);
							}
					}
				}
				for(Vector<String> ngramwords : sentences) {
					if (ngramwords.size() >= currentNgramSize) {
						for (int i = 0; i < ngramwords.size(); i++) {
							if (i + currentNgramSize <= ngramwords.size()) {
								Ngram gram = new Ngram(currentNgramSize);
								gram.weight = weight;
								gram.setPhotoUID(photoUID);
								gram.timestamp = timeStamp;
								for (int j = 0; j < currentNgramSize; j++) {
									gram.gram[j] = ngramwords.get(i + j);
								}
								if (checkIntegrity(gram)) {
									addAll(tempgram, gram);
								}
							}
						}
					}
				}
			}
		}
	}
	/**
	 * 
	 * @param tempgram
	 * @param info
	 * @param currentNgramSize
	 * @param weight
	 * @param source
	 * @param timeStamp
	 * @param photoUID
	 */
	
	//TODO: changed things in this method
	private void buildNgrams(LinkedHashMap<String, Ngram> tempgram,
			String info, int currentNgramSize, int weight, String source,
			Date timeStamp, String photoUID) {
		
		Vector<Vector<String>> sentences = new Vector<Vector<String>>();
		Vector<String> words = new Vector<String>();
		FeedbackInterface fi = null;
		ContentInterface ci = null;
		if (config.prop.containsKey("feedback")
				&& config.prop.getProperty("feedback").equals("mysql")
				&& config.prop.getProperty("contentType").equals("facebook")) {
			fi = DataFactory.getFeedback(config);
			ci = ContentFactory.getParser(config);
		}
		if (fi != null) {
			if (info != null && !info.equals("")) {
				User user = fi.readContent(ci.getID());

				if (info != null && !info.equals("")) {
					String breakFormatted = formatSentenceBreaks(info);
					StringTokenizer st = new StringTokenizer(breakFormatted);
					while (st.hasMoreTokens()) {
						String token = st.nextToken();						
						if(!(token.equals("-") || token.equals("--") || token.equals("—") ||
								token.equals("!") || token.equals("?") || 
								token.equals(".") || token.equals(":") || 
								token.equals(";") || token.equals("â€¦")|| 
								token.equals("âœ“")|| token.equals("â†’")|| 
								token.equals("âœ�")|| token.equals("âœ˜")|| 
								token.equals("â˜¼")|| token.equals("â†“")|| 
								token.equals("â†‘")|| token.equals("â™«")|| 
								token.equals("âœˆ")|| token.equals("=")|| 
								token.equals("â€”")|| 
								token.equals("â€“")||
								token.equals("+"))) {
							if(token.endsWith(".") || token.endsWith(",") || 
									token.endsWith(";") || token.endsWith(":") || 
									token.endsWith("!") || token.endsWith("?")) {
								if(!isURL(token)) {
									words.add(token);
								}	
								sentences.add(words);
								words = new Vector<String>();
							}
							else {
								if(!isURL(token)) {
									words.add(token);
									}
							}
						}
						else {
							sentences.add(words);
							words = new Vector<String>();
						}
						
					}
					for(Vector<String> ngramwords : sentences) {
						if (ngramwords.size() >= currentNgramSize) {
							for (int i = 0; i < ngramwords.size(); i++) {
								if (i + currentNgramSize <= ngramwords.size()) {
									Ngram gram = new Ngram(currentNgramSize);
									gram.source.add(source);
									gram.weight = weight;
									gram.setPhotoUID(photoUID);
									gram.timestamp = timeStamp;
									String checkString = "";
									for (int j = 0; j < currentNgramSize; j++) {
										gram.gram[j] = ngramwords.get(i + j);
										if (j != currentNgramSize - 1) {
											checkString += gram.gram[j] + " ";
										} else {
											checkString += gram.gram[j];
										}

									}
									if (checkIntegrity(gram)) {

										if (!user.negativeWords.keySet().contains(
												checkString)) {
											addAll(tempgram, gram);
										}
									}
								}
							}
						}
					}
				}

			}
		} else {
			if (info != null && !info.equals("")) {
				String breakFormatted = formatSentenceBreaks(info);
				StringTokenizer st = new StringTokenizer(breakFormatted);
				while (st.hasMoreTokens()) {
					String token = st.nextToken();						
					if(!(token.equals("-") || token.equals("--") || token.equals("—") ||
							token.equals("!") || token.equals("?") || 
							token.equals(".") || token.equals(":") || 
							token.equals(";") || token.equals("â€¦")|| 
							token.equals("âœ“")|| token.equals("â†’") || 
							token.equals("âœ�")|| token.equals("âœ˜")|| 
							token.equals("â˜¼")|| token.equals("â†“")|| 
							token.equals("â†‘")|| token.equals("â™«")|| 
							token.equals("âœˆ")|| token.equals("=")|| 
							token.equals("â€”")|| 
							token.equals("â€“")||
							token.equals("+"))) {
						if(token.endsWith(".") || token.endsWith(",") || 
								token.endsWith(";") || token.endsWith(":") || 
								token.endsWith("!") || token.endsWith("?")) {
							if(!isURL(token)) {
								words.add(token);
							}	
							sentences.add(words);
							words = new Vector<String>();
						}else {
							if(!isURL(token)) {
							words.add(token);
							}
						}					
					}
					else {
						sentences.add(words);
						words = new Vector<String>();
					}
					
				}
				sentences.add(words);
				words=new Vector<String>();
				for(Vector<String> ngramwords : sentences) {
					if (ngramwords.size() >= currentNgramSize) {
						for (int i = 0; i < ngramwords.size(); i++) {
							if (i + currentNgramSize <= ngramwords.size()) {
								Ngram gram = new Ngram(currentNgramSize);
								gram.source.add(source);
								gram.weight = weight;
								gram.setPhotoUID(photoUID);
								gram.timestamp = timeStamp;
								for (int j = 0; j < currentNgramSize; j++) {
									gram.gram[j] = ngramwords.get(i + j);
								}
								if (checkIntegrity(gram)) {
									addAll(tempgram, gram);
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	
	private String formatSentenceBreaks(String input)
	{
		String output="";
		String regex = "\\b(https?|ftp|http|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		input = input.replaceAll(regex, " ");
		input = input.replaceAll("—|…", " ");
		List<Character> breaks = Arrays.asList('.', ',','!','<','>','=',':','?','|', '_', '-','^', '~','(',')','[',']','"', '–', '>');
		for(int i=0; i<input.length(); i++)
		{
			if(breaks.contains(input.charAt(i)))
				output+=input.charAt(i)+" ";
			else
				output+=input.charAt(i);
			
		}
		output=output.trim();
		return output;		
	}
	
	
	//TODO: added method for check if text contains URL
	private boolean isURL(String text) {
		String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if(matcher.find()) {
			return true;
		}
		return false;
	}
	//TODO: added method for replacing last occurance of character
	private String replaceLast(String string, String from, String to) {
	     int lastIndex = string.lastIndexOf(from);
	     if (lastIndex < 0) return string;
	     String tail = string.substring(lastIndex).replaceFirst(from, to);
	     return string.substring(0, lastIndex) + tail;
	}
	//TODO: updated checkIntergrity method
	private boolean checkIntegrity(Ngram gram) {
		
		for (int i = 0; i < gram.gram.length; i++) {
			String word = gram.gram[i];
	
			//Contains characters in the middle
			word = word.replaceAll("[^a-zA-Z0-9]*$", "");
			//word = word.replace("—|…", "");
			//Starts with special chars
			if(word.length()>2)
			{
				String first = word.substring(0, 2);
				first = first.replaceAll("@|#|\"|“|…|-|/|—|â€”|â€™|'", "");
				word = first+word.substring(2, word.length());
				//Ends with special chars
				word = replaceLast(word, "(â€”â€”)(--)(-)(â€”)(,)(.)(;)(:)", "");
			}
			word = word.trim();
			if(word.equals(" ") || word.equals("  ") || word.equals("")) {
				return false;
			}
			gram.gram[i] = word;
		}
		return true;
	}

	public void loadNER(Vector<Ngram> vect) {
		for (Ngram ngram : vect) {
			for (NamedEntity namedEntity : ner.getEntities().values()) {
				if (ngram.toString().toLowerCase()
						.equals(namedEntity.namedEntity)) {
					ngram.matchesNER = true;
				}
			}
		}
	}

	@Override
	public LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> getncurrentGrams() {
		return ncurrentgrams;
	}

	@Override
	public void setncurrentgrams(
			LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> a) {
		ncurrentgrams = a;

	}

	@Override
	public Vector<Ngram> getCombinedgrams() {
		Vector<Ngram> fullVector = new Vector<Ngram>();
		for (int gramsize : ncurrentgrams.keySet()) {
			LinkedHashMap<String, Ngram> currentVector = ncurrentgrams
					.get(gramsize);
			for (Ngram s : currentVector.values()) {
				boolean isvalid = true;
				for (Ngram checkgram : fullVector) {
					if (checkgram.toString().contains(s.toString())) {
						isvalid = false;
					}
				}
				if (isvalid) {
					fullVector.add(s);
				}
			}
		}
		if (config.prop.contains("feedback")
				&& config.prop.contains("ngramSort")
				&& config.prop.getProperty("ngramSort").equals("ner")) {
			SortingFactory.getSorter().sortByNer(fullVector);
		} else if (config.prop.contains("feedback")
				&& config.prop.contains("ngramSort")) {
			SortingFactory.getSorter().sortByDefault(fullVector);
		}

		Collections.reverse(fullVector);
		return fullVector;
	}

	@Override
	public LinkedHashMap<Integer, LinkedHashMap<String, Centroid>> ngramToCentroids() {
		String userid = ContentFactory.getID(config);
		LinkedHashMap<Integer, LinkedHashMap<String, Centroid>> ngramCentroids = new LinkedHashMap<Integer, LinkedHashMap<String, Centroid>>();
		for (int size : ncurrentgrams.keySet()) {
			LinkedHashMap<String, Centroid> centroids = new LinkedHashMap<String, Centroid>();
			LinkedHashMap<String, Ngram> ngrams = ncurrentgrams.get(size);
			int i = 0;
			for (Ngram gram : ngrams.values()) {
				String id = userid + gram.toString();
				Centroid centroid = new Centroid(id, gram.toString(),
						gram.gettfidf());
				centroid.ngramsize = gram.gramsize;
				centroid.ngramsize = gram.gramsize;
				centroid.timestamp = gram.timestamp;
				centroid.setPhotoUID(gram.getPhotoUID());
				centroids.put(centroid.getTag(), centroid);
			}
			ngramCentroids.put(size, centroids);
		}
		return ngramCentroids;
	}

	@Override
	public LinkedHashMap<Integer, Vector<Centroid>> getMediaGrams() {
		String userid = ContentFactory.getID(config);
		LinkedHashMap<Integer, Vector<Centroid>> photoCentroids = new LinkedHashMap<Integer, Vector<Centroid>>();
		if (mediaGrams != null) {
			for (int size : mediaGrams.keySet()) {
				Vector<Centroid> centroids = new Vector<Centroid>();
				LinkedHashMap<String, Ngram> ngrams = mediaGrams.get(size);
				for (String name : ngrams.keySet()) {
					String id = name;
					Centroid centroid = new Centroid(id, ngrams.get(name)
							.toString(), ngrams.get(name).gettfidf(),
							ngrams.get(name).source);
					centroid.ngramsize = ngrams.get(name).gramsize;
					centroid.timestamp = ngrams.get(name).timestamp;
					centroid.setPhotoUID(ngrams.get(name).getPhotoUID());

					centroids.add(centroid);
				}
				photoCentroids.put(size, centroids);
			}
		} else {
			System.out.println("mediaGrams are null");
		}
		return photoCentroids;
	}

	@Override
	public LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> getncurrentMediaGrams() {
		return mediaGrams;
	}

	@Override
	public void sentenceSegmentation(ContentInterface contentInterface) {
		// Sentence segmentation is only implemented for twitter content at the
		// moment
		if (contentInterface instanceof TwitterContentObject) {
			TwitterContentObject tco = (TwitterContentObject) contentInterface;
			for (MediaInformation info : tco.photoInformation) {
				findSentences(info.description, info.source, info.date);
			}
		} else {

		}
	}

	private void findSentences(String description, String source, Date timestamp) {
		if (description != null) {
			String sentence = "";
			Sentence temp;
			description = description.replace("--", "-");
			description = description.replace("@", "");
			description = description.replace("\"", "");
			description = description.replace("#", "");
			char currentChar = '@';
			for (int i = 0; i < description.length(); i++) {
				char previouschar = currentChar;
				currentChar = description.charAt(i);
				if (sentence.length() == 0
						&& (currentChar == ' ' || currentChar == '—'
						|| currentChar == '"' || currentChar == '\'')) {
					continue;
				}

				if ((description.length() >= i + 2 && i > 0)
						&& description.substring(i - 1, i).matches("[0-9]")
						&& description.substring(i + 1, i + 2).matches("[0-9]")
						&& (currentChar == '.' || currentChar == ',')) {
					sentence += currentChar;
				} else if (currentChar == '.' || currentChar == ','

						|| currentChar == ':' || currentChar == '!'
						|| currentChar == '?' || currentChar == ';'
						|| currentChar == '-') {

					if (sentences.containsKey(sentence)) {
						temp = sentences.get(sentence);
					} else {
						temp = new Sentence();
					}
					if (source != null) {
						temp.sources.add(source);
						temp.englishSentence = sentence;
						if (timestamp != null) {
							temp.timestamp = timestamp;
						}
						temp.frequency++;
						if (sentence.length() > 8 && sentence.contains(" ")) {
							sentences.put(sentence, temp);
						}
					}
					sentence = "";
				} else {
					sentence += currentChar;
				}
			}
		}
	}

	@Override
	public LinkedHashMap<String, Sentence> getSentences() {
		return sentences;
	}
}
