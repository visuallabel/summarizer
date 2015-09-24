package analyzer.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import analyzer.content.socialmedia.HashTag;
import analyzer.ner.NamedEntity;
import analyzer.segmentation.Centroid;
import analyzer.segmentation.Ngram;

public class Sorter implements SortingInterface {
	@Override
	public Vector<Ngram> orderNgramsByNer(
			LinkedHashMap<String, Ngram> orderedGrams) {
		Vector<Ngram> ngram = new Vector<Ngram>(orderedGrams.values());
		Collections.sort(ngram, new Comparator<Ngram>() {

			@Override
			public int compare(Ngram o1, Ngram o2) {
				if (o1.matchesNER == true && o2.matchesNER == false) {
					return -1;
				} else if (o1.matchesNER == false && o2.matchesNER == true) {
					return 1;
				} else {
					if (o1.frequency < o2.frequency) {
						return 1;
					} else if (o1.frequency > o2.frequency) {
						return -1;
					} else {
						if (o1.frequency == o2.frequency) {
							if (o1.gettfidf() < o2.gettfidf()) {
								return 1;
							} else if (o1.gettfidf() > o2.gettfidf()) {
								return -1;
							}
						}
						return 0;
					}
				}

			}
		});
		return ngram;
	}

	public Vector<Ngram> orderNgramsByDefault(
			LinkedHashMap<String, Ngram> orderedGrams) {
		Vector<Ngram> ngram = new Vector<Ngram>(orderedGrams.values());
		Collections.sort(ngram, new Comparator<Ngram>() {
			@Override
			public int compare(Ngram o1, Ngram o2) {
				if (o1.gettfidf() < o2.gettfidf()) {
					return -1;
				} else if (o1.gettfidf() > o2.gettfidf()) {
					return 1;
				}
				return 0;
			}
		});
		return ngram;
	}

	@Override
	public void sortByNer(Vector<Ngram> ngrams) {
		Collections.sort(ngrams, new Comparator<Ngram>() {
			@Override
			public int compare(Ngram o1, Ngram o2) {
				if (o1.matchesNER == true && o2.matchesNER == false) {
					return 1;
				} else if (o1.matchesNER == false && o2.matchesNER == true) {
					return -1;
				} else {
					if (o1.frequency > o2.frequency) {
						return 1;
					} else if (o1.frequency < o2.frequency) {
						return -1;
					} else {
						if (o1.frequency == o2.frequency) {
							if (o1.gettfidf() > o2.gettfidf()) {
								return 1;
							} else if (o1.gettfidf() < o2.gettfidf()) {
								return -1;
							}
						}
						return 0;
					}
				}

			}
		});
	}

	@Override
	public void sortByDefault(Vector<Ngram> ngrams) {
		Collections.sort(ngrams, new Comparator<Ngram>() {
			@Override
			public int compare(Ngram o1, Ngram o2) {

				if (o1.gettfidf() > o2.gettfidf()) {
					return 1;
				} else if (o1.gettfidf() < o2.gettfidf()) {
					return -1;
				}

				return 0;
			}
		});
	}

	@Override
	public void sortByTfidf(Vector<Centroid> ngrams) {
		Collections.sort(ngrams, new Comparator<Centroid>() {
			@Override
			public int compare(Centroid o1, Centroid o2) {

				if (o1.getTfidf() > o2.getTfidf()) {
					return 1;
				} else if (o1.getTfidf() < o2.getTfidf()) {
					return -1;
				}

				return 0;
			}
		});
	}

	@Override
	public ArrayList<Map.Entry<String, Double>> sortByValue(
			LinkedHashMap<String, Double> tfidf) {

		ArrayList<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>(
				tfidf.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> a,
					Map.Entry<String, Double> b) {
				return a.getValue().compareTo(b.getValue());
			}
		});

		Collections.reverse(entries);
		return entries;
	}

	public ArrayList<Entry<String, Double>> sortByValue2(
			LinkedHashMap<String, Double> mediaTfidf) {
		ArrayList<Map.Entry<String, Double>> mediaEntries = new ArrayList<Map.Entry<String, Double>>(
				mediaTfidf.entrySet());
		Collections.sort(mediaEntries,
				new Comparator<Map.Entry<String, Double>>() {
					public int compare(Map.Entry<String, Double> a,
							Map.Entry<String, Double> b) {
						return a.getValue().compareTo(b.getValue());
					}
				});
		Collections.reverse(mediaEntries);
		return mediaEntries;
	}

	// @Override
	// public List<Map.Entry<String, HashTag>> sortHashtagsByValue(
	// LinkedHashMap<String, HashTag> hashtags) {
	// List<Map.Entry<String, HashTag>> hashentries = new
	// ArrayList<Map.Entry<String, HashTag>>(
	// hashtags.entrySet());
	// Collections.sort(hashentries,
	// new Comparator<Map.Entry<String, HashTag>>() {
	// public int compare(Map.Entry<String, HashTag> a,
	// Map.Entry<String, HashTag> b) {
	// return a.getValue().compareTo(b.getValue());
	// }
	// });
	// Collections.reverse(hashentries);
	// return hashentries;
	// }

	@Override
	public LinkedHashMap<String, HashTag> sortHashByNerAndFrequency(
			LinkedHashMap<String, HashTag> hash) {
		ArrayList<Map.Entry<String, HashTag>> entries = new ArrayList<Map.Entry<String, HashTag>>(
				hash.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, HashTag>>() {
			public int compare(Map.Entry<String, HashTag> a,
					Map.Entry<String, HashTag> b) {
				if (a.getValue().matchesNer && !b.getValue().matchesNer) {
					return -1;
				} else if (!a.getValue().matchesNer && b.getValue().matchesNer) {
					return 1;
				} else {
					if (a.getValue().frequency < b.getValue().frequency) {
						return 1;
					} else if (a.getValue().frequency > b.getValue().frequency) {
						return -1;
					} else {
						return 0;
					}
				}

			}
		});
		LinkedHashMap<String, HashTag> sortedMap = new LinkedHashMap<String, HashTag>();
		for (Map.Entry<String, HashTag> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	@Override
	public LinkedHashMap<String, NamedEntity> sortNerByFrequency(
			LinkedHashMap<String, NamedEntity> ner) {

		ArrayList<Map.Entry<String, NamedEntity>> entries = new ArrayList<Map.Entry<String, NamedEntity>>(
				ner.entrySet());
		Collections.sort(entries,
				new Comparator<Map.Entry<String, NamedEntity>>() {
					public int compare(Map.Entry<String, NamedEntity> a,
							Map.Entry<String, NamedEntity> b) {
						if (a.getValue().frequency < b.getValue().frequency) {
							return 1;
						} else if (a.getValue().frequency > b.getValue().frequency) {
							return -1;
						} else {
							return 0;
						}
					}
				});
		LinkedHashMap<String, NamedEntity> sortedMap = new LinkedHashMap<String, NamedEntity>();
		for (Map.Entry<String, NamedEntity> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}
