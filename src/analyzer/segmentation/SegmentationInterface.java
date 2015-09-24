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

import java.util.LinkedHashMap;
import java.util.Vector;

import analyzer.content.ContentInterface;

/**
 * Interface that decides the ngram parsers structure
 * 
 * @author forsstho
 * 
 */
public interface SegmentationInterface {
	public void parseNgram(ContentInterface cI);

	public LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> getncurrentGrams();

	public LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> getncurrentMediaGrams();

	public Vector<Ngram> getCombinedgrams();

	public LinkedHashMap<Integer, LinkedHashMap<String, Centroid>> ngramToCentroids();

	public LinkedHashMap<Integer, Vector<Centroid>> getMediaGrams();

	void setncurrentgrams(LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> a);

	void sentenceSegmentation(ContentInterface contentInterface);

	public LinkedHashMap<String, Sentence> getSentences();
}
