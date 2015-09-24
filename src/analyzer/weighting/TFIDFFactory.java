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

import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.socialmedia.HashTag;
import analyzer.segmentation.Centroid;

public class TFIDFFactory {
	static TermFrequency tf;
	static TFIDFInterface tfidf;
	static IDFCollection ic;
	static Vector<Centroid> centroids;
	static Config c;

	public static TFIDFInterface getTFIDF(Config config) {
		c = config;
		tfidf = new TFIDFCalculator(config);
		centroids = null;
		return tfidf;
	}

	public static TermFrequency getTF(Config config) {
		tf = new TermFrequency(config.prop.getProperty("combine"), config);
		return tf;

	}

	public static IDFCollection getIc(Config config) {
		if (ic == null) {
			ic = new IDFCollection(config);
		}
		return ic;
	}

	// public static Vector<Centroid> getCentroids() {
	// return tfidf.getCentroids();
	// }

	public static List<Entry<String, HashTag>> getHashTags() {
		if (tfidf != null) {
			return tfidf.getHashTags();
		}
		return null;
	}

	public static Vector<Centroid> getMediaCentroids() {
		return tfidf.getMediaCentroids();
	}

	public static void resetFactory() {
		tf = null;
		tfidf = null;
		centroids = null;
		c = null;
	}

}
