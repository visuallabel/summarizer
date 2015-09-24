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
package analyzer.segmentation;

import java.util.Date;
import java.util.HashSet;

/**
 * Centroid class that represents one centroid from whatever we are parsing.
 * //TODO: refactor since this was added in a later stage
 * 
 * @author forsstho
 * 
 */
public class Centroid {
	private String tag;
	private String tagOriginalLanguage;
	private String uniqueID;
	private double tfidf;
	private HashSet<String> source = new HashSet<String>();
	public int ngramsize;
	public Date timestamp;
	private String photoUID;

	public Centroid() {

	}

	public Centroid(String idAndValue, String tagvalue, double tfidf) {
		if (idAndValue != null) {
			uniqueID = "" + idAndValue.hashCode();
		}
		tag = tagvalue;
		this.tfidf = tfidf;
	}

	public Centroid(String idAndValue, String tagvalue, double tfidf,
			HashSet<String> source2) {
		if (idAndValue != null) {
			uniqueID = "" + idAndValue.hashCode();
		}
		tag = tagvalue;
		this.tfidf = tfidf;
		this.source = source2;
	}

	public String getOriginalTag() {
		return tagOriginalLanguage;
	}

	public void setOriginalTag(String s) {
		tagOriginalLanguage = s;
	}

	public void setuniqueID(String id) {
		uniqueID = id;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public double getTfidf() {
		return tfidf;
	}

	public void setTfidf(double tfidf) {
		this.tfidf = tfidf;
	}

	public void setSource(HashSet<String> s) {
		source = s;
	}

	public HashSet<String> getSource() {
		return source;
	}

	public String getPhotoUID() {
		return photoUID;
	}

	public void setPhotoUID(String photoUID) {
		this.photoUID = photoUID;
	}

}
