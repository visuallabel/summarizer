package analyzer.sorting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Vector;

import analyzer.content.socialmedia.HashTag;
import analyzer.ner.NamedEntity;
import analyzer.segmentation.Centroid;
import analyzer.segmentation.Ngram;

public interface SortingInterface {
	public Vector<Ngram> orderNgramsByNer(
			LinkedHashMap<String, Ngram> orderedGrams);

	public Vector<Ngram> orderNgramsByDefault(
			LinkedHashMap<String, Ngram> orderedGrams);

	public void sortByTfidf(Vector<Centroid> ngrams);

	ArrayList<Entry<String, Double>> sortByValue(
			LinkedHashMap<String, Double> tfidf);

	void sortByNer(Vector<Ngram> ngrams);

	void sortByDefault(Vector<Ngram> ngrams);

	// List<Entry<String, HashTag>> sortHashtagsByValue(
	// LinkedHashMap<String, HashTag> hashtags);

	LinkedHashMap<String, HashTag> sortHashByNerAndFrequency(
			LinkedHashMap<String, HashTag> hash);

	LinkedHashMap<String, NamedEntity> sortNerByFrequency(
			LinkedHashMap<String, NamedEntity> ner);
}
