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

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import analyzer.content.TextObject;
import analyzer.content.TextObject.TextObjectType;

public class StatusMessage extends FacebookInformationObject {
	public String from;
	public String message;
	public int likeCount;
	public Vector<Comment> comments = new Vector<Comment>();
	public int weight = 1;
	public Vector<String> hashtags = new Vector<String>();
	ArrayList<TextObject> commentList = new ArrayList<TextObject>();

	public void getComments() {
		for (Comment c : comments) {
			String[] info = c.getContent();
			for (String word : info) {
				commentList.add(new TextObject(c.weight,
						removeDelimeters(word), TextObjectType.COMMENT,
						this.date));
			}
		}
	}

	public ArrayList<TextObject> getContent() {
		try {
			StringTokenizer st = new StringTokenizer(message);
			while (st.hasMoreTokens()) {
				commentList.add(new TextObject(weight, removeDelimeters(st
						.nextToken()), TextObjectType.STATUSINFO, this.date));
			}
			getComments();
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
