package analyzer.sorting;

public class SortingFactory {
	static SortingInterface si = null;

	public static SortingInterface getSorter() {
		if (si == null) {
			return new Sorter();
		}
		return si;
	}

	public static void reset() {
		si = null;
	}

}
