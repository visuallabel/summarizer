package analyzer.segmentation;

import java.util.Date;
import java.util.Vector;

public class Sentence {
	public String englishSentence = "";
	public Vector<String> sources = new Vector<String>();
	public String uniqueID;
	public int frequency;
	public Date timestamp;
}