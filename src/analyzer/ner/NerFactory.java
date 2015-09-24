package analyzer.ner;

import analyzer.config.Config;

public class NerFactory {
	static NerInterface ner;

	public static NerInterface getNer(Config c) {
		if (ner == null) {
			ner = new NamedEntityRecognition(c);

		}
		return ner;

	}
}
