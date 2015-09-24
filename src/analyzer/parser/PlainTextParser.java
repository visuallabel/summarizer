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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.PlainTextContentObject;
import analyzer.content.TextObject;
import analyzer.content.TextObject.TextObjectType;
import analyzer.process.TextAnalyzer;
import analyzer.translation.TranslatorFactory;
import analyzer.translation.TranslatorInterface;
import analyzer.translation.yandex.Language;

/**
 * Parser class for .txt files with no structure. The parser splits the text
 * into sentences if such are found.
 * 
 * @author forsstho
 * 
 */
public class PlainTextParser implements ParserInterface {
	private int numberOfFiles;
	private PlainTextContentObject cO;
	private Config config;
	private String textToParse;
	private HashMap<Language, Integer> identifiedLanguages = new HashMap<Language, Integer>();

	public PlainTextParser(Config config) {
		cO = (PlainTextContentObject) ContentFactory.getParser(config);
		this.config = config;
	}

	@Override
	public void parseToContentObject() {
		cO.content.clear();
		File file = new File((String) config.prop.get("file"));
		BufferedReader br = null;
		try {
			textToParse = "";
			String input;
			if (!file.isDirectory()) {
				br = new BufferedReader(new FileReader(file));
				numberOfFiles = 1;
				while ((input = br.readLine()) != null) {
					textToParse += input;
					if (textToParse.charAt(textToParse.length() - 1) != ' ') {
						textToParse += " ";
					}
				}
			} else {

				File[] files = file.listFiles();
				numberOfFiles = files.length;
				for (File f : files) {
					br = new BufferedReader(new FileReader(f));
					while ((input = br.readLine()) != null) {
						textToParse += input;
						if (textToParse.charAt(textToParse.length() - 1) != ' ') {
							textToParse += " ";
						}
					}
				}
			}
			if (config.prop.containsKey("translation")) {
				translateToEnglish();
			}
			tokenize(textToParse);
		} catch (IOException e) {
			TextAnalyzer.logger
					.error("Failed, check your config, probably wrong paths!");
			System.exit(0);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				TextAnalyzer.logger.error(ex.getMessage());
			}
		}
	}

	private void tokenize(String textToParse) {
		String ngramSize = config.prop.getProperty("ngram");
		int size = 1;
		if (ngramSize != null) {
			size = Integer.parseInt(ngramSize);
		}
		/*
		 * textToParse = textToParse.replace(".", " "); textToParse =
		 * textToParse.replace(",", " "); textToParse = textToParse.replace("?",
		 * " "); textToParse = textToParse.replace("!", " "); textToParse =
		 * textToParse.replace(":", " "); textToParse =
		 * textToParse.replace("null", "");
		 */
		cO.content = loadWords(textToParse, size);
		cO.numberOfFiles = numberOfFiles;
	}

	public Vector<TextObject> loadWords(String text, int ngramSize) {
		Vector<TextObject> terms = new Vector<TextObject>();
		if (config.prop.getProperty("ignoreCase").equals("yes")) {
			text = text.toLowerCase();
			String[] sentences = text.split(" ; ");
			for (int j = 0; j < sentences.length; j++) {
				ArrayList<String> sentenceWords = new ArrayList<String>();
				sentences[j] = sentences[j].replace("\0", " ");
				StringTokenizer st = new StringTokenizer(sentences[j]);
				while (st.hasMoreTokens()) {
					sentenceWords.add(st.nextToken());
				}
				if (sentenceWords.size() >= ngramSize) {
					for (int i = 0; i < sentenceWords.size(); i++) {
						if (sentenceWords.size() - i >= ngramSize) {
							String term = "";
							for (int k = i; k < ngramSize + i; k++) {
								if (k != (ngramSize + i - 1)) {
									term += sentenceWords.get(k) + " ";
								} else {
									term += sentenceWords.get(k);
								}
							}
							terms.add(new TextObject(1, term,
									TextObjectType.OTHER, null));
						} else {
							break;
						}
					}
				}
			}
		} else {
			String[] sentences = text.split(" ; ");
			for (int j = 0; j < sentences.length; j++) {
				ArrayList<String> sentenceWords = new ArrayList<String>();
				sentences[j] = sentences[j].replace("\0", " ");
				StringTokenizer st = new StringTokenizer(sentences[j]);
				while (st.hasMoreTokens()) {
					sentenceWords.add(st.nextToken());
				}
				if (sentenceWords.size() >= ngramSize) {
					for (int i = 0; i < sentenceWords.size(); i++) {
						if (sentenceWords.size() - i >= ngramSize) {
							String term = "";
							for (int k = i; k < ngramSize + i; k++) {
								if (k != (ngramSize + i - 1)) {
									term += sentenceWords.get(k) + " ";
								} else {
									term += sentenceWords.get(k);
								}
							}
							terms.add(new TextObject(1, term,
									TextObjectType.OTHER, null));
						} else {
							break;
						}
					}
				}
			}
		}
		return terms;
	}

	@Override
	public Object getObject() {
		return cO;
	}

	@Override
	public void translateToEnglish() {
		TextAnalyzer.logger.info("Translating text to English.");
		TranslatorInterface TI = TranslatorFactory.getParser(config);
		Vector<String> sentences = new Vector<String>();
		textToParse.split(".");
		for (String sentence : sentences) {
			sentence = TI.identifyAndTranslateOneText(sentence);
		}
	}

	@Override
	public HashMap<Language, Integer> getLanguages() {
		return identifiedLanguages;
	}
}
