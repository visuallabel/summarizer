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
package analyzer.process;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.ContentInterface;
import analyzer.content.FacebookContentObject;
import analyzer.content.PlainTextContentObject;
import analyzer.content.TwitterContentObject;
import analyzer.content.WeightFactory;
import analyzer.content.WeightInterface;

/**
 * Class used to link different parts of analysis to source links. For example
 * we link hashtags to pictures.
 * 
 * @author forsstho
 * 
 */
public class SourceLinker {
	ContentInterface co;
	WeightInterface wi;

	public SourceLinker(Config c) {
		co = ContentFactory.getParser(c);
		if (co instanceof TwitterContentObject) {
			wi = WeightFactory.getParser(c);
			// linkTwitterPicture();
		} else if (co instanceof FacebookContentObject) {
			// TODO: decide if we are going to do something for facebook
		} else if (co instanceof PlainTextContentObject) {
			// TODO: decide if we are going to do something for plain text
		}
	}
	/*
	 * private void linkTwitterPicture() { LinkedHashMap<String, HashTag>
	 * hashtags = wi.getHashTags(); TwitterContentObject tco =
	 * (TwitterContentObject) co; Vector<MediaInformation> photos =
	 * tco.photoInformation; Vector<MediaInformation> videos =
	 * tco.videoInformation; Vector<StatusMessage> status =
	 * tco.statusMessageInformation; for(String tag : hashtags.keySet()) { for
	 * (MediaInformation info : photos) { info. } }
	 * 
	 * }
	 */
}
