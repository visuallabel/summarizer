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

import analyzer.config.Config;
import analyzer.process.TextAnalyzer;

/**
 * Factory pattern used to automatically decide which weight implementation to
 * use base on confid option contentType.
 * 
 * @author forsstho
 * 
 */
public class WeightFactory {
	private static WeightInterface wI;
	private static Config config;

	/**
	 * Factory that returns class for extraction of words to be weighted
	 */
	public static WeightInterface getParser(Config c) {
		config = c;
		if (wI == null) {
			String prop = (String) config.prop.get("contentType");
			if (prop.equals("facebook")) {
				wI = new FacebookWeight(config);
			} else if (config.prop.get("contentType").equals("normal")) {
				wI = new PlainTextWeight(config);
			} else if (config.prop.get("contentType").equals("twitter")) {
				wI = new TwitterWeight(config);
			} else {
				TextAnalyzer.logger
						.error("faulty contentType in  wightfactory");
				// System.out.println("faulty contentType... Exiting.");
				System.exit(0);
			}
		}
		return wI;
	}

	public static void resetWeight() {
		wI = null;
	}

	public static void setConfig(Config c) {
		config = c;
	}
}
