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
import java.util.Date;
import java.util.StringTokenizer;

import analyzer.content.TextObject;
import analyzer.content.TextObject.TextObjectType;

public class EventInformation extends FacebookInformationObject {
	public String locationName;
	public String owner;
	public String description;
	public String eventName;
	public Date startTime;
	public Date endTime;
	public rsvpStatus status;
	public groupPrivacy privacy;
	public int weight;
	ArrayList<TextObject> commentList = new ArrayList<TextObject>();

	public enum rsvpStatus {
		ATTENDING, UNSURE, NOT_REPLIED, DECLINED;
	}

	public enum groupPrivacy {
		SECRET, OPEN, CLOSED;
	}

	@Override
	public String toString() {
		return locationName + " " + owner;
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

	public ArrayList<TextObject> getContent() {
		StringTokenizer st = null;
		try {
			st = new StringTokenizer(description);
			while (st.hasMoreTokens()) {
				commentList.add(new TextObject(weight, removeDelimeters(st
						.nextToken()), TextObjectType.EVENTINFO, this.date));
			}
		} catch (Exception e) {

		}
		try {
			st = new StringTokenizer(eventName);
			while (st.hasMoreTokens()) {
				commentList.add(new TextObject(weight, removeDelimeters(st
						.nextToken()), TextObjectType.EVENTINFO, this.date));
			}
			commentList.add(new TextObject(1, locationName,
					TextObjectType.EVENTINFO, this.date));
			commentList.add(new TextObject(1, owner, TextObjectType.EVENTINFO,
					this.date));
		} catch (Exception e) {

		}
		return commentList;
	}
}
