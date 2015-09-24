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

import java.util.LinkedHashMap;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.ner.NamedEntityRecognition;
import analyzer.output.OutputInterface;
import analyzer.segmentation.Centroid;
import analyzer.segmentation.Ngram;
import analyzer.weighting.TFIDFInterface;

/**
 * Class that was supposed to output summaries. Summaries has not been used so
 * its not implemented
 * 
 * @author forsstho
 * 
 */

public class SummaryOutput implements OutputInterface {
	Config config;

	public SummaryOutput(Config config, NamedEntityRecognition ner) {
		this.config = config;
	}

	@Override
	public void outputResults(TFIDFInterface tfidf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outputSpecificResults(TFIDFInterface tfidf, String outputPath) {
		// TODO Auto-generated method stub

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
