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
package analyzer.postprocessing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.PlainTextContentObject;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.process.StopWordRemover;
import analyzer.segmentation.Centroid;

public class RedundancyRemover implements RedundancyInterface {
	Config config;
	HashSet<String> stopwords;

	public RedundancyRemover(Config config) {
		this.config = config;
		StopWordRemover sr = new StopWordRemover(config);
		stopwords = sr.getStopwordSet();
	}

	@Override
	public void removeMediaRedundancy(Vector<MediaInformation> mi) {
		if (!(ContentFactory.getParser(config) instanceof PlainTextContentObject)) {
			HashSet<String> redundantString = new HashSet<String>();
			for (MediaInformation m : mi) {
				HashMap<Integer, HashMap<String, Centroid>> redundancyFreeMedia = new HashMap<Integer, HashMap<String, Centroid>>();
				HashMap<Integer, HashMap<String, Centroid>> redundantTFIDF = m.mediaTfidf;
				for (int size = redundantTFIDF.size(); size > 0; size--) {
					HashMap<String, Centroid> strings = redundantTFIDF
							.get(size);
					HashMap<String, Centroid> redundancyFreeMap = new HashMap<String, Centroid>();
					for (String key : strings.keySet()) {
						Centroid c = strings.get(key);
						if (!checkStartEnd(c)) {
							if (!redundantString.contains(key)) {
								boolean found = false;
								for (String red : redundantString) {
									if (red.contains(key)) {
										found = true;
										break;
									}
								}
								if (!found) {
									redundancyFreeMap.put(key, c);
									redundantString.add(key);
								}
							}
						}
					}
					redundancyFreeMedia.put(size, redundancyFreeMap);
				}
				m.mediaTfidf = redundancyFreeMedia;
			}
		}
	}

	public boolean checkStartEnd(Centroid c) {
		StringTokenizer st = new StringTokenizer(c.getTag());
		if (st.hasMoreTokens()) {
			String first = st.nextToken();
			if (stopwords.contains(first)) {
				return true;
			}
			String rest;
			while (st.hasMoreTokens()) {
				rest = st.nextToken();
				if (!st.hasMoreTokens()) {
					if (stopwords.contains(rest)) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
