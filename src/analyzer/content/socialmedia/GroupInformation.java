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

import analyzer.content.TextObject;
import analyzer.content.TextObject.TextObjectType;

public class GroupInformation extends FacebookInformationObject {
	public String name;
	public String description;
	public String owner;
	public groupPrivacy privacy;
	public int weight;
	ArrayList<TextObject> commentList = new ArrayList<TextObject>();

	public enum groupPrivacy {
		SECRET, OPEN, CLOSED;
	}

	public String locationName;
	public String locationCountry;
	public String locationState;
	public String locationCity;

	@Override
	public String toString() {
		return description + " " + locationName + ", " + locationCountry
				+ ",  " + locationState + ", " + locationCity;
	}

	public ArrayList<TextObject> getContent() {

		StringTokenizer st;
		try {
			if (name != null) {
				st = new StringTokenizer(name);
				while (st.hasMoreTokens()) {
					commentList
							.add(new TextObject(weight, removeDelimeters(st
									.nextToken()), TextObjectType.GROUPINFO,
									this.date));
				}
			}
		} catch (Exception e) {

		}
		try {
			if (description != null) {
				st = new StringTokenizer(description);
				while (st.hasMoreTokens()) {
					commentList
							.add(new TextObject(1, removeDelimeters(st
									.nextToken()), TextObjectType.GROUPINFO,
									this.date));
				}
			}
		} catch (Exception e) {

		}
		try {
			commentList.add(new TextObject(1, owner, TextObjectType.GROUPINFO,
					this.date));
			commentList.add(new TextObject(1, locationName,
					TextObjectType.GROUPINFO, this.date));
			commentList.add(new TextObject(1, locationCountry,
					TextObjectType.GROUPINFO, this.date));
			commentList.add(new TextObject(1, locationState,
					TextObjectType.GROUPINFO, this.date));
			commentList.add(new TextObject(1, locationCity,
					TextObjectType.GROUPINFO, this.date));
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
