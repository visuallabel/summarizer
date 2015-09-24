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

/**
 * Information object that acts as parent to each of the sub information objects
 * 
 * @author forsstho
 * 
 */
public class FacebookInformationObject implements
		Comparable<FacebookInformationObject> {
	public Date date = null;

	public FacebookInformationObject() {

	}

	@Override
	public int compareTo(FacebookInformationObject o) {
		if (date == null && o.date == null) {
			return 0;
		} else if (date == null && o.date != null) {
			return -1;
		} else if (date != null && o.date == null) {
			return 1;
		} else if (date.after(o.date)) {
			return 1;
		} else if (date.before(o.date)) {
			return -1;
		} else {
			return 0;
		}
	}

}
