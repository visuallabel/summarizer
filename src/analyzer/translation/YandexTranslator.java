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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

import analyzer.config.Config;
import analyzer.parser.ParserFactory;
import analyzer.parser.ParserInterface;
import analyzer.translation.yandex.Detect;
import analyzer.translation.yandex.Language;
import analyzer.translation.yandex.Translate;

public class YandexTranslator implements TranslatorInterface {
	HashMap<String, String> abbr = new HashMap<String, String>();
	HashMap<String, String> misspelled = new HashMap<String, String>();
	HashMap<String, String> slang = new HashMap<String, String>();
	Language lang;
	Language langRetranslate;
	boolean loaded = false;
	private String[] translated;
	HashMap<Language, Integer> identifiedLanguages;

	public YandexTranslator(Config config) {
		ParserInterface PI = ParserFactory.getParser(config);
		identifiedLanguages = PI.getLanguages();
	}

	public String identifyAndTranslateOneText(String text) {
		// System.out.println("translating : " + text);
		if (text != null && !text.equals("null") && !text.equals("")) {
			identifyLanguage(text);
			if (identifiedLanguages.containsKey(lang)) {
				int languageCount = identifiedLanguages.get(lang);
				languageCount++;
				identifiedLanguages.put(lang, languageCount);

			} else {
				if (lang != Language.ENGLISH) {
					identifiedLanguages.put(lang, 1);
				}
			}
			text = correctSpeech(text);
			if (lang != Language.ENGLISH) {
				text = translate(text);
			}
			// System.out.println("lang: " + lang + " text: " + text);
		} else {
			// System.out.println("not translated: " + text);
			return "";
		}
		return text;
	}

	public YandexTranslator(String fullText[]) {
		translated = new String[fullText.length];

		for (int i = 0; i < fullText.length; i++) {
			String translation = null;
			String text = fullText[i];
			identifyLanguage(text);
			if (identifiedLanguages.containsKey(lang)) {
				int languageCount = identifiedLanguages.get(lang);
				languageCount++;
				if (lang != Language.ENGLISH) {
					identifiedLanguages.put(lang, languageCount);
				}

			} else {
				if (lang != Language.ENGLISH) {
					identifiedLanguages.put(lang, 1);
				}
			}
			text = correctSpeech(text);
			if (lang != Language.ENGLISH) {
				translation = translate(text);
			}
			translated[i] = translation;
		}
	}

	/**
	 * Method that translates the text input to the identified language
	 */
	@Override
	public String translate(String text) {

		String translatedText = null;
		try {
			translatedText = Translate.execute(text, lang, Language.ENGLISH);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return translatedText;
	}

	/**
	 * Use yandex to identify language
	 */
	@Override
	public void identifyLanguage(String text) {
		try {
			lang = Detect.execute(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * replace the words found in the dictionaries with the correct spelling, if
	 * no dictionary is found we do nothing
	 */
	@Override
	public String correctSpeech(String text) {
		String fixedText = text;
		if (loadDictionaries()) {
			fixedText = "";
			StringTokenizer st = new StringTokenizer(text);
			while (st.hasMoreTokens()) {
				String fixee = st.nextToken();
				for (String fix : misspelled.keySet()) {
					if (fixee.equals(fix)) {
						fixee = misspelled.get(fix);
					}
				}
				for (String fix : abbr.keySet()) {
					if (fixee.equals(fix)) {
						fixee = misspelled.get(fix);
					}
				}
				for (String fix : slang.keySet()) {
					if (fixee.equals(fix)) {
						fixee = misspelled.get(fix);
					}
				}
				if (fixee.charAt(fixee.length() - 1) != ' ') {
					fixedText += fixee + " ";
				} else {
					fixedText += fixee;
				}
			}
		}
		return fixedText;
	}

	/**
	 * Method that compares the toString() method of Yandex language results to
	 * dictionaries. If we have a dictionary matching the language we load the
	 * dictionaries.
	 */
	public boolean loadDictionaries() {
		String language = "";
		try {
			language = lang.toString();
			InputStream in = this
					.getClass()
					.getClassLoader()
					.getResourceAsStream(
							"dictionaries/misspelled/" + language + ".txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			boolean run = true;
			String text = null;
			try {
				while ((text = br.readLine()) != null) {
					String[] splitText = text.split("\t");
					misspelled.put(splitText[0], splitText[1]);

				}
				in.close();
				br.close();
				in = this
						.getClass()
						.getClassLoader()
						.getResourceAsStream(
								"dictionaries/abbr/" + language + ".txt");
				br = new BufferedReader(new InputStreamReader(in));
				while ((text = br.readLine()) != null) {

					String[] splitText = text.split("\t");
					abbr.put(splitText[0], splitText[1]);

				}
				in.close();
				br.close();
				in = this
						.getClass()
						.getClassLoader()
						.getResourceAsStream(
								"dictionaries/slang/" + language + ".txt");
				br = new BufferedReader(new InputStreamReader(in));
				while ((text = br.readLine()) != null) {

					String[] splitText = text.split("\t");
					slang.put(splitText[0], splitText[1]);

				}
				in.close();
				br.close();
			} catch (Exception e) {

			} finally {
				in.close();
				br.close();
			}

		} catch (Exception e) {
			// TextAnalyzer.logger.info("Dictionaries for language " + language
			// + " are currently not available.");
			return false;
		}
		return true;
	}

	@Override
	public String[] returnTranslation() {
		return translated;
	}

	@Override
	public String translateBack(String text) {
		String translatedText = text;
		if (langRetranslate != null) {
			try {
				translatedText = Translate.execute(text, Language.ENGLISH,
						langRetranslate);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return translatedText;
	}

	@Override
	public Language calculateRetranslation() {
		if (langRetranslate == null) {
			int highestFreq = 0;

			for (Language ident : identifiedLanguages.keySet()) {

				int currentFreq = identifiedLanguages.get(ident);
				if (currentFreq > highestFreq) {

					highestFreq = currentFreq;
					langRetranslate = ident;

				}
			}
			if (highestFreq == 1) {
				this.langRetranslate = Language.ENGLISH;
			} else {
				System.out.println("language: "
						+ this.langRetranslate.toString());
			}
		}

		return this.langRetranslate;

	}
}
