package analyzer.ner;

import java.util.LinkedHashMap;

public interface NerInterface {
	public LinkedHashMap<String, NamedEntity> getEntities();

}
