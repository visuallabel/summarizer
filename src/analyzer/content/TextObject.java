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
package analyzer.content;

import java.util.Date;

/**
 * Class for giving strings of text different weights. Can be unigrams or
 * ngrams.
 * 
 * @author forsstho
 * 
 */
public class TextObject {
	public enum TextObjectType {
		LIKEINFO, MEDIAINFO, STATUSINFO, EVENTINFO, GROUPINFO, RELATIONSHIPINFO, COMMENT, OTHER;
	}

	public TextObjectType textType;
	public int frequency;
	public String text;
	public Date timeStamp;

	public TextObject(int i, String words, TextObjectType mediainfo, Date date) {
		frequency = i;
		text = words;
		textType = mediainfo;
		this.timeStamp = date;
	}

	public TextObject() {

	}

	public String getContent() {
		return text;
	}

	@Override
	public String toString() {
		return text;
	}

}
