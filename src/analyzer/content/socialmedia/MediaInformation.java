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
package analyzer.content.socialmedia;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import analyzer.content.TextObject;
import analyzer.content.TextObject.TextObjectType;
import analyzer.segmentation.Centroid;

public class MediaInformation extends FacebookInformationObject {
	public String description;
	public String from;
	public int likeCount;
	public String city;
	public String country;
	public String placeName;
	public Vector<Comment> comments = new Vector<Comment>();
	public Vector<String> hashtags = new Vector<String>();
	public int weight = 1;
	public String timestmap;
	public String source;
	Vector<TextObject> commentList = new Vector<TextObject>();
	public HashMap<Integer, HashMap<String, Double>> mediaFreq = new HashMap<Integer, HashMap<String, Double>>();
	public HashMap<Integer, HashMap<String, Centroid>> mediaTfidf = new HashMap<Integer, HashMap<String, Centroid>>();
	// refers to phototag element in facebook xml
	public Vector<String> tagList = new Vector<String>();
	public String photoUID = "";
	public String serviceId = "";

	@Override
	public String toString() {
		String s = from + " " + city + " " + country + " " + placeName;

		return s;
	}

	public void setTagList(Vector<String> s) {
		tagList = s;
	}

	public void getComments() {
		try {
			for (Comment c : comments) {
				String[] info = c.getContent();
				for (String word : info) {
					commentList.add(new TextObject(c.weight,
							removeDelimeters(word), TextObjectType.COMMENT,
							this.date));
				}
			}
		} catch (Exception e) {

		}
	}

	public Vector<TextObject> getContent() {
		try {
			StringTokenizer st = new StringTokenizer(description);
			while (st.hasMoreTokens()) {
				String next = st.nextToken();
				commentList.add(new TextObject(weight, removeDelimeters(next),
						TextObjectType.MEDIAINFO, this.date));
			}
		} catch (Exception e) {

		}
		getComments();
		try {
			/*
			 * commentList.add(new TextObject(1, from, TextObjectType.MEDIAINFO,
			 * this.date)); commentList.add(new TextObject(1, city,
			 * TextObjectType.MEDIAINFO, this.date)); commentList.add(new
			 * TextObject(1, country, TextObjectType.MEDIAINFO, this.date));
			 * commentList.add(new TextObject(1, placeName,
			 * TextObjectType.MEDIAINFO, this.date));
			 */
		} catch (Exception e) {

		}
		return commentList;
	}

	public String removeDelimeters(String text) {
		text = text.replace(".", "");
		text = text.replace(",", "");
		text = text.replace("!", "");
		text = text.replace("?", "");
		text = text.replace(":", "");
		text = text.replace("\"", "");
		return text;
	}

}
