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
package analyzer.parser;

import analyzer.config.Config;

/**
 * Factory pattern used to decide which XML parse to use based on the
 * contentType config option
 * 
 * @author forsstho
 * 
 */

public class ParserFactory {
	static ParserInterface PI;

	public static ParserInterface getParser(Config config) {
		if (PI == null) {
			if (config.prop.get("contentType").equals("facebook")) {
				PI = new FacebookXMLSaxParser(config);
			} else if (config.prop.get("contentType").equals("normal")) {
				PI = new PlainTextParser(config);
			} else if (config.prop.get("contentType").equals("twitter")) {
				PI = new TwitterXMLSaxParser(config);
			}
		}
		return PI;
	}

	public static void resetParser() {
		PI = null;
	}
}
