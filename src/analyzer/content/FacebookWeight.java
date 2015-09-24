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
package analyzer.content;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.TextObject.TextObjectType;
import analyzer.content.socialmedia.EventInformation;
import analyzer.content.socialmedia.GroupInformation;
import analyzer.content.socialmedia.HashTag;
import analyzer.content.socialmedia.LikeInformation;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.content.socialmedia.RelationshipInformation;
import analyzer.content.socialmedia.StatusMessage;

/**
 * Implementation of weightinterface for facebook profile weighting.
 * 
 * @author forsstho
 * 
 */
public class FacebookWeight implements WeightInterface {
	private FacebookContentObject cO;
	private Config config;
	private HashMap<String, Double> significantDateWords;
	private HashMap<String, Double> significantCounterWords;

	public FacebookWeight(Config config) {
		this.config = config;
		cO = (FacebookContentObject) ContentFactory.getParser(config);
	}

	/**
	 * Put all the words from the facebook profile into a vector of words
	 */
	@Override
	public Vector<TextObject> formatContent() {
		int cutoff = 0;
		String datePattern = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		Date date = null;
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
		Vector<TextObject> contentWords = new Vector<TextObject>();
		String words = "";
		// StringTokenizer st;
		// words += cO.toString();
		String[] content = cO.getContent();
		try {
			for (String s : content) {
				contentWords.add(new TextObject(1, s, TextObjectType.OTHER,
						null));
			}
		} catch (Exception e) {

		}
		if (cO.groupInformation.size() > 0) {
			for (GroupInformation gI : cO.groupInformation) {
				words = "";
				if (cutoff == 1) {
					if (date != null && gI.date != null && gI.date.after(date)) {

						contentWords.addAll(gI.getContent());
					}

				} else if (cutoff == 0) {
					contentWords.addAll(gI.getContent());
				} else {
				}
			}
		}
		if (cO.eventInformation.size() > 0) {
			for (EventInformation eI : cO.eventInformation) {
				if (cutoff == 1) {
					if (date != null && eI.date != null && eI.date.after(date)) {
						contentWords.addAll(eI.getContent());
					}

				} else if (cutoff == 0) {
					contentWords.addAll(eI.getContent());
				} else {

				}

			}
		}
		if (cO.likeInformation.size() > 0) {
			for (LikeInformation lI : cO.likeInformation) {
				if (cutoff == 1) {
					if (date != null && lI.date != null && lI.date.after(date)) {
						contentWords.addAll(lI.getContent());
					}
				} else if (cutoff == 0) {
					contentWords.addAll(lI.getContent());
				} else {

				}

			}
		}
		if (cO.photoInformation.size() > 0) {
			for (MediaInformation lI : cO.photoInformation) {
				if (cutoff == 1) {
					if (date != null && lI.date != null && lI.date.after(date)) {
						contentWords.addAll(lI.getContent());
					}

				} else if (cutoff == 0) {
					contentWords.addAll(lI.getContent());
				} else {

				}

			}
		}
		if (cO.videoInformation.size() > 0) {
			for (MediaInformation lI : cO.videoInformation) {
				if (cutoff == 1) {
					if (date != null && lI.date != null && lI.date.after(date)) {
						contentWords.addAll(lI.getContent());

					}

				} else if (cutoff == 0) {
					contentWords.addAll(lI.getContent());

				} else {

				}
			}
		}
		if (cO.statusMessageInformation.size() > 0) {
			for (StatusMessage sm : cO.statusMessageInformation) {
				if (cutoff == 1) {
					if (date != null && sm.date != null && sm.date.after(date)) {
						contentWords.addAll(sm.getContent());
					}

				} else if (cutoff == 0) {
					contentWords.addAll(sm.getContent());
				} else {

				}

			}
		}
		if (cO.relationships.size() > 0) {
			for (RelationshipInformation rI : cO.relationships) {
				contentWords.addAll(rI.getContent());
			}
		}
		return contentWords;
	}

	public double linearDate(long time1, long time2) {
		double linearTime = (((double) time1 - (double) time2) / ((double) time1));
		return linearTime;
	}

	public void addInfo(String text, double time,
			HashMap<String, Double> significant) {
		text = replaceDelimeters(text);
		StringTokenizer st = new StringTokenizer(text);
		while (st.hasMoreTokens()) {
			String key = st.nextToken();
			if (significant.containsKey(key)) {
				if (significant.get(key) < time) {
					significant.put(key, time);
				}
			} else {
				significant.put(key, time);
			}
		}
	}

	@Override
	public HashMap<String, Double> findDateSignificanceWords() {
		if (config.prop.containsKey("dateLimit")
				&& config.prop.getProperty("cutoff").equalsIgnoreCase("linear")) {
			String datePattern = "yyyy-MM-dd";
			SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
			Date date = null;
			try {
				date = sdf.parse((String) config.prop.get("dateLimit"));
			} catch (ParseException e) {
				// e.printStackTrace();
			}
			significantDateWords = new HashMap<String, Double>();
			for (GroupInformation gI : cO.groupInformation) {
				if (gI.date != null && date != null && gI.date.after(date)) {
					double linearTime = linearDate(gI.date.getTime(),
							date.getTime());
					addInfo(gI.toString(), linearTime, significantDateWords);
				}
			}
			for (EventInformation eI : cO.eventInformation) {
				if (eI.date != null && date != null && eI.date.after(date)) {
					double linearTime = linearDate(eI.date.getTime(),
							date.getTime());
					addInfo(eI.toString(), linearTime, significantDateWords);
				}
			}
			for (LikeInformation lI : cO.likeInformation) {
				if (lI.date != null && date != null && lI.date.after(date)) {
					double linearTime = linearDate(lI.date.getTime(),
							date.getTime());
					addInfo(lI.toString(), linearTime, significantDateWords);
				}
			}
			for (MediaInformation mI : cO.photoInformation) {
				if (mI.date != null && date != null && mI.date.after(date)) {
					double linearTime = linearDate(mI.date.getTime(),
							date.getTime());
					addInfo(mI.toString(), linearTime, significantDateWords);
				}
			}
			for (MediaInformation mI : cO.videoInformation) {
				if (mI.date != null && date != null && mI.date.after(date)) {
					double linearTime = linearDate(mI.date.getTime(),
							date.getTime());
					addInfo(mI.toString(), linearTime, significantDateWords);
				}
			}
			for (StatusMessage sm : cO.statusMessageInformation) {
				if (sm.date != null && date != null) {
					double linearTime = linearDate(sm.date.getTime(),
							date.getTime());
					addInfo(sm.toString(), linearTime, significantDateWords);
				}
			}

			return significantDateWords;
		} else {
			return null;
		}
	}

	public String replaceDelimeters(String text) {
		text = text.replace(".", " ");
		text = text.replace("!", " ");
		text = text.replace("?", " ");
		text = text.replace(",", " ");
		text = text.replace(";", " ");
		text = text.replace(":", " ");
		text = text.replace("null", "");
		if (config.prop.getProperty("ignoreCase").equals("yes")) {
			text = text.toLowerCase();
		} else {
		}
		return text;
	}

	public double linearCounter(int counter1, int counter2) {
		double counter = 0;
		if (counter2 > 0 && counter1 != counter2) {
			counter = ((double) counter1 - (double) counter2)
					/ (double) counter1;
		}
		return counter;
	}

	@Override
	public HashMap<String, Double> findCounterSignificanceWords() {
		if (config.prop.containsKey("counterLimit")
				&& config.prop.getProperty("counterLimit") != null
				&& config.prop.containsKey("cutoff")
				&& config.prop.getProperty("cutoff").equalsIgnoreCase("linear")) {
			try {
				significantCounterWords = new HashMap<String, Double>();
				int likeCounter = Integer.parseInt((String) config.prop
						.get("counterLimit"));
				for (MediaInformation mI : cO.photoInformation) {
					if (mI.likeCount > likeCounter) {

						double linear = linearCounter(mI.likeCount, likeCounter);
						addInfo(mI.toString(), linear, significantCounterWords);
						// System.out.println("adding: " + mI.toString());
					}
				}
				for (MediaInformation mI : cO.videoInformation) {
					if (mI.likeCount > likeCounter) {
						int likeCount = mI.likeCount;
						double linear = linearCounter(likeCount, likeCounter);
						addInfo(mI.toString(), linear, significantCounterWords);
						// System.out.println("adding: " + mI.toString());
					}
				}
				for (StatusMessage sm : cO.statusMessageInformation) {
					if (sm.likeCount > likeCounter) {

						double linear = linearCounter(sm.likeCount, likeCounter);
						// /System.out.println("adding: " + sm.toString()
						// + " output: " + linear);
						addInfo(sm.toString(), linear, significantCounterWords);
					}
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
			return significantCounterWords;
		} else
			return null;
	}

	@Override
	public double getNumberOfFilesInCluster() {
		return 1;
	}

	@Override
	public LinkedHashMap<String, HashTag> calculateHashtags() {
		// TODO parse hashtags
		return null;
	}

	@Override
	public LinkedHashMap<String, HashTag> getHashTags() {
		// TODO: Methord that return hashtags if found, no hashtags are parsed
		// for facebook at the moment
		return null;
	}

	@Override
	public Vector<MediaInformation> buildMediaObjects() {
		return null;

	}

}
