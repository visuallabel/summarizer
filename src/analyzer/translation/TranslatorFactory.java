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
package analyzer.translation;

import analyzer.config.Config;

public class TranslatorFactory {
	static TranslatorInterface TI = null;

	public static TranslatorInterface getParser(Config config) {
		if (TI == null) {
			if (config.prop.containsKey("translation")
					&& config.prop.get("translation").equals("yandex")) {
				return new YandexTranslator(config);
			} else {
				return null;
			}
		} else {
			return TI;
		}
	}

	public static void resetFactory() {
		TI = null;
	}
}
