package analyzer.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import analyzer.content.socialmedia.HashTag;
import analyzer.ner.NamedEntity;
import analyzer.segmentation.Centroid;
import analyzer.segmentation.Sentence;

public class TwitterPicture {
	public String pictureurl;
	public Date timestamp;
	public String tweetText = "";
	public String uniqueID;
	public String photoUID;
	public String serviceID;
	public String backendid;
	public String visualobjectid;
	public ArrayList<Centroid> unigrams = new ArrayList<Centroid>();
	public ArrayList<Centroid> twograms = new ArrayList<Centroid>();
	public ArrayList<Centroid> trigrams = new ArrayList<Centroid>();
	public ArrayList<NamedEntity> ne = new ArrayList<NamedEntity>();
	public ArrayList<Sentence> sentences = new ArrayList<Sentence>();
	public LinkedHashMap<String, HashTag> hashtags = new LinkedHashMap<String, HashTag>();
	public ArrayList<String> picsomTags = new ArrayList<String>();
}
