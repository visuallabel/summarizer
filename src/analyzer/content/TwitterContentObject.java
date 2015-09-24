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

import java.util.Vector;

import analyzer.content.socialmedia.MediaInformation;
import analyzer.content.socialmedia.StatusMessage;

/**
 * Class that implements contentinterface for twitter
 * 
 * @author forsstho
 * 
 */

public class TwitterContentObject implements ContentInterface {
	public int favoritesCount;
	public int followersCount;
	public int friendsCount;
	public String name;
	public String screenName;
	public String userId;
	public Vector<MediaInformation> videoInformation;
	public Vector<MediaInformation> photoInformation;
	public Vector<StatusMessage> statusMessageInformation;

	public TwitterContentObject() {
		videoInformation = new Vector<MediaInformation>();
		photoInformation = new Vector<MediaInformation>();
		statusMessageInformation = new Vector<StatusMessage>();
	}

	@Override
	public String getID() {
		return userId;
	}

	@Override
	public Vector<MediaInformation> getMedia() {
		return photoInformation;
	}

}
