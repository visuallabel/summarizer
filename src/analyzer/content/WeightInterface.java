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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import analyzer.content.socialmedia.HashTag;
import analyzer.content.socialmedia.MediaInformation;

/**
 * Interface for deciding weight parsers structure.
 * 
 * @author forsstho
 * 
 */
public interface WeightInterface {
	/**
	 * method that is used to format the content into a vector
	 */
	public Vector<TextObject> formatContent();

	/**
	 * Find the words that should be significant
	 */
	public HashMap<String, Double> findDateSignificanceWords();

	public HashMap<String, Double> findCounterSignificanceWords();

	public double getNumberOfFilesInCluster();

	public Vector<MediaInformation> buildMediaObjects();

	public LinkedHashMap<String, HashTag> calculateHashtags();

	public LinkedHashMap<String, HashTag> getHashTags();

}
