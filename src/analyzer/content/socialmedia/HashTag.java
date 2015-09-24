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
import java.util.HashSet;

/**
 * Class that represents one hash tag in a profile. Frequency is amount of times
 * it appears, url is the links it is linked to.
 * 
 * @author forsstho
 * 
 */
public class HashTag {
	public HashSet<String> sources = new HashSet<String>();
	public String tag = "";
	public int frequency = 0;
	public double tfidf = 0;
	public boolean matchesNer = false;
	public Date timestamp = null;
}
