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

import java.util.LinkedHashMap;
import java.util.Vector;

import analyzer.segmentation.Centroid;
import analyzer.segmentation.Ngram;
import analyzer.weighting.TFIDFInterface;

/**
 * Interface that simply decides the format of the outputting classes. Output is
 * divided into centroid output, summary output and topic model output.
 * 
 * @author forsstho
 * 
 */
public interface OutputInterface {
	public void outputResults(TFIDFInterface tfidf);

	public void outputSpecificResults(TFIDFInterface tfidf, String outputPath);

	public LinkedHashMap<Integer, LinkedHashMap<String, Ngram>> getNgrams();

	Vector<Centroid> getSpecificOutput(int size);
}
