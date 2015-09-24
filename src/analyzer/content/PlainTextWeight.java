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
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.socialmedia.HashTag;
import analyzer.content.socialmedia.MediaInformation;

/**
 * Implementation of weightinterface for plaintext.
 * 
 * @author forsstho
 * 
 */
public class PlainTextWeight implements WeightInterface {
	private Config config;
	private PlainTextContentObject cO;

	public PlainTextWeight(Config config) {
		this.config = config;
		cO = (PlainTextContentObject) ContentFactory.getParser(config);
	}

	@Override
	public Vector<TextObject> formatContent() {
		return cO.content;
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
		return cO.numberOfFiles;

	}

	@Override
	public LinkedHashMap<String, HashTag> calculateHashtags() {
		// TODO parse hashtags
		return null;
	}

	@Override
	public LinkedHashMap<String, HashTag> getHashTags() {
		// TODO: Methord that return hashtags if found, no hashtags are parsed
		// for plaintext at the moment
		return null;
	}

	@Override
	public Vector<MediaInformation> buildMediaObjects() {
		return null;

	}

}
