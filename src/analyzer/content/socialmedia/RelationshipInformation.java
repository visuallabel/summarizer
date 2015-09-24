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

import analyzer.content.TextObject;
import analyzer.content.TextObject.TextObjectType;

public class RelationshipInformation {
	public String relationshipType;
	public String with;
	ArrayList<TextObject> commentList = new ArrayList<TextObject>();

	@Override
	public String toString() {
		return relationshipType + " " + with;
	}

	public ArrayList<TextObject> getContent() {
		try {
			commentList.add(new TextObject(1, relationshipType,
					TextObjectType.RELATIONSHIPINFO, null));
			commentList.add(new TextObject(1, with,
					TextObjectType.RELATIONSHIPINFO, null));
		} catch (Exception e) {

		}
		return commentList;
	}
}
