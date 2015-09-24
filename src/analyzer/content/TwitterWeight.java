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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.socialmedia.HashTag;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.content.socialmedia.StatusMessage;

/**
 * Implementation of weightinterface for twitter profile weighting.
 * 
 * @author forsstho
 * 
 */
public class TwitterWeight implements WeightInterface {
	public LinkedHashMap<String, HashTag> hashtags = new LinkedHashMap<String, HashTag>();
	private TwitterContentObject tco;
	private Vector<TextObject> contentWords;
	Vector<MediaInformation> mediaObjectList;
	Config config;

	public TwitterWeight(Config config) {
		this.config = config;
		tco = (TwitterContentObject) ContentFactory.getParser(config);
	}

	@Override
	public Vector<TextObject> formatContent() {
		contentWords = new Vector<TextObject>();
		if (tco.statusMessageInformation.size() > 0) {
			for (StatusMessage sm : tco.statusMessageInformation) {
				contentWords.addAll(sm.getContent());
			}
		}
		if (tco.photoInformation.size() > 0) {
			for (MediaInformation info : tco.photoInformation) {
				contentWords.addAll(info.getContent());
			}
		}
		if (tco.videoInformation.size() > 0) {
			for (MediaInformation info : tco.videoInformation) {
				contentWords.addAll(info.getContent());
			}
		}
		return contentWords;
	}

	@Override
	public HashMap<String, Double> findDateSignificanceWords() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, Double> findCounterSignificanceWords() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getNumberOfFilesInCluster() {
		return 1;
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

	@Override
	public LinkedHashMap<String, HashTag> calculateHashtags() {
		for (MediaInformation mi : tco.photoInformation) {
			// System.out.println("hashtag number: " + mi.hashtags.size());
			for (String tag : mi.hashtags) {
				if (!hashtags.containsKey(tag)) {
					HashTag hash = new HashTag();
					hash.timestamp = mi.date;
					hash.frequency = 1;
					hash.sources.add(mi.source);

					hash.tag = tag;
					hashtags.put(tag, hash);
				} else {
					HashTag hash = hashtags.get(tag);
					hash.frequency++;
					// hashtag.url.add(mi.source);

					if (!hash.sources.contains(mi.source)) {
						hash.sources.add(mi.source);
					}
					hashtags.put(tag, hash);
				}
			}
		}
		for (MediaInformation mi : tco.videoInformation) {
			// System.out.println("hashtag number: " + mi.hashtags.size());
			for (String tag : mi.hashtags) {
				if (!hashtags.containsKey(tag)) {
					HashTag hash = new HashTag();
					hash.timestamp = mi.date;
					hash.frequency = 1;
					if (!hash.sources.contains(mi.source)) {
						hash.sources.add(mi.source);
					}
					hash.tag = tag;
					hash.sources.add(mi.source);
					hashtags.put(tag, hash);
				} else {
					HashTag hash = hashtags.get(tag);
					hash.sources.add(mi.source);
					hash.frequency++;
					if (!hash.sources.contains(mi.source)) {
						hash.sources.add(mi.source);
					}
					hashtags.put(tag, hash);
				}
			}
		}
		// not including statusmessage hashtags at the moment, uncomment if you
		// want it included

		// for (StatusMessage si : tco.statusMessageInformation) {
		// // System.out.println("hashtag number status: " +
		// // si.hashtags.size());
		// for (String tag : si.hashtags) {
		// if (!hashtags.containsKey(tag)) {
		// HashTag hash = new HashTag();
		// hash.frequency = 1;
		// hash.timestamp = si.date;
		// hash.tag = tag;
		// hashtags.put(tag, hash);
		// } else {
		// HashTag hash = hashtags.get(tag);
		// hash.frequency++;
		// hashtags.put(tag, hash);
		// }
		// }
		// }
		return hashtags;
	}

	@Override
	public LinkedHashMap<String, HashTag> getHashTags() {
		return hashtags;
	}

	/** Not sure why this method is needed, maybe it isn't anymore?**/
	@Override
	public Vector<MediaInformation> buildMediaObjects() {
		ContentInterface ci = ContentFactory.getParser(config);
		Vector<MediaInformation> mediaObjectList = ci.getMedia();
		if (ci instanceof TwitterContentObject) {
			for (MediaInformation mi : mediaObjectList) {
				//removeHashTags(mi);
			}
		} else {
			// facebook and plain returns null
			return null;
		}
		return mediaObjectList;

	}
	//TODO
	/** Not sure why this method is needed, maybe it isn't anymore?**/
	public void removeHashTags(MediaInformation mediaObject) {
		StringTokenizer st = new StringTokenizer(mediaObject.description);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.toString().startsWith("#")) {
				mediaObject.description = mediaObject.description.replace(
						token, " ");
			}

		}
	}
}
