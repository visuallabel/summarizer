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

import java.util.Date;
import java.util.StringTokenizer;

/**
 * Class representing comments in a profile.
 * 
 * @author forsstho
 * 
 */
public class Comment {
	public Date createdTimeStamp;
	public String from;
	public String message;
	public int likeCount;
	public int weight;

	@Override
	public String toString() {
		return from + " " + message;
	}

	public String[] getContent() {
		StringTokenizer st = new StringTokenizer(message);
		String content[] = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens()) {
			content[i] = st.nextToken();
			i++;
		}
		return content;
	}
}
