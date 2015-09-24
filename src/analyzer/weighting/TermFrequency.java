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

import java.util.HashMap;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.TextObject;
import analyzer.content.TextObject.TextObjectType;
import analyzer.content.WeightFactory;
import analyzer.content.WeightInterface;
import analyzer.content.socialmedia.MediaInformation;

/**
 * OLD DEPRECATED CLASS NO LONGER USED!
 * 
 * 
 * Class that counts how many times each word appears in the content
 * 
 */
public class TermFrequency {
	private HashMap<String, Double> frequency = new HashMap<String, Double>();
	private WeightInterface wi;
	private Vector<TextObject> objects;
	String combineType;
	Config c;

	public TermFrequency(String combineType, Config c) {
		this.c = c;
		wi = WeightFactory.getParser(c);
		this.combineType = combineType;
		// calculateTermFrequency();
	}

	public void calculateTermFrequency() {
		// TODO: decide if we are keeping stemming as option
		objects = wi.formatContent();
		if (c.prop.containsKey("stemming")
				&& c.prop.getProperty("stemming").equals("yes")) {
			for (TextObject object : objects) {
				PorterStemmer stemmer = new PorterStemmer();
				Vector<String> stemmerObject = new Vector<String>();
				stemmerObject.add(object.toString());
				Vector<String> words = stemmer.startStemming(stemmerObject);
				if (frequency.containsKey(object.toString())) {
					double number = frequency.get(object.toString());
					frequency.put(words.get(0), (number + object.frequency));
				} else {
					frequency.put(words.get(0), (double) object.frequency);
				}
			}
			double freqDivideBy = wi.getNumberOfFilesInCluster();
			if (freqDivideBy > 1) {
				for (String key : frequency.keySet()) {
					double number = frequency.get(key);
					number = (number / freqDivideBy);
					if (combineType != null) {
						if (combineType.equals("mead")) {
							number = number / 4;
						}
					}
					frequency.put(key, number);
				}
			}
		} else {
			if (c.prop.getProperty("contentType").equals("twitter")) {
				Vector<MediaInformation> mediaObjects = ContentFactory
						.getParser(c).getMedia();
				for (MediaInformation mi : mediaObjects) {
					HashMap<String, Double> mediaFreq = new HashMap<String, Double>();

					Vector<TextObject> to = mi.getContent();
					for (TextObject t : to) {
						if (c.prop.getProperty("ignoreCase").equals("yes")) {
							if (!t.text.contains("#")) {
								if (!mediaFreq
										.containsKey(t.text.toLowerCase())) {
									mediaFreq.put(t.text.toLowerCase(),
											(double) 1);
								} else {

									mediaFreq
											.put(t.text.toLowerCase(),
													mediaFreq.get(t.text
															.toLowerCase()) + 1);
								}
							}
							else if (t.text.contains("#")) {
								String hash = t.text.replace("#", "");
								if (!mediaFreq
										.containsKey(hash.toLowerCase())) {
									mediaFreq.put(hash.toLowerCase(),
											(double) 1);
								} else {

									mediaFreq
											.put(hash.toLowerCase(),
													mediaFreq.get(hash.toLowerCase()) + 1);
								}
							}
						} else {
							if (!t.text.contains("#")) {
								if (!mediaFreq.containsKey(t.text)) {
									mediaFreq.put(t.text, (double) 1);
								} else {

									mediaFreq.put(t.text,
											mediaFreq.get(t.text) + 1);
								}
							}
							else if (t.text.contains("#")) {
								String hash = t.text.replace("#", "");
								if (!mediaFreq.containsKey(hash)) {
									mediaFreq.put(hash, (double) 1);
								} else {

									mediaFreq.put(hash,
											mediaFreq.get(hash) + 1);
								}
							}
						}
					}
					mi.mediaFreq.put(1, mediaFreq);
				}
				for (TextObject object : objects) {
					if (object.textType.equals(TextObjectType.STATUSINFO)) {
						if (frequency.containsKey(object.toString())) {
							double number = frequency.get(object.toString());
							frequency.put(object.toString(),
									(number + object.frequency));
						} else {
							frequency.put(object.toString(),
									(double) object.frequency);
						}
					}
				}
			} else {
				for (TextObject object : objects) {
					if (frequency.containsKey(object.toString())) {
						double number = frequency.get(object.toString());
						frequency.put(object.toString(),
								(number + object.frequency));
					} else {
						frequency
								.put(object.toString(), (double) object.frequency);
					}
				}
				double freqDivideBy = wi.getNumberOfFilesInCluster();
				if (freqDivideBy > 1) {
					for (String key : frequency.keySet()) {
						double number = frequency.get(key);
						number = (number / freqDivideBy);
						if (combineType.equals("mead")) {
							number = number / 4;
						}
						frequency.put(key, number);
					}
				}
			}
		}
	}

	public HashMap<String, Double> getFrequency() {
		return frequency;
	}

	public HashMap<String, Double> getOtherFrequency() {
		HashMap<String, Double> otherFrequency = new HashMap<String, Double>();
		if (c.prop.getProperty("contentType").equals("twitter")) {
			for (TextObject object : objects) {
				if (!object.textType.equals(TextObjectType.STATUSINFO)) {
					if (otherFrequency.containsKey(object.toString())) {
						double number = otherFrequency.get(object.toString());
						otherFrequency.put(object.toString(),
								(number + object.frequency));
					} else {
						otherFrequency.put(object.toString(),
								(double) object.frequency);
					}
				}
			}
		}
		// TODO: only for twitter, empty for others?
		return otherFrequency;
	}
}
