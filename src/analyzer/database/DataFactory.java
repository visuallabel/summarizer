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
package analyzer.database;

import analyzer.config.Config;
import analyzer.output.ObjectOutput;
import analyzer.process.TextAnalyzer;
import analyzer.segmentation.SegmentationInterface;

/**
 * Factory pattern for automatically deciding feedback and tag implementations
 * 
 * @author forsstho
 * 
 */
public class DataFactory {

	static FeedbackInterface ti = null;
	static TagInterface tagI = null;

	public static FeedbackInterface getFeedback(Config config) {

		if (ti == null) {
			if (config.prop.containsKey("feedback")
					&& config.prop.get("feedback").equals("json")) {
				ti = new JsonFeedback(config);
			} else if (config.prop.containsKey("feedback")
					&& config.prop.getProperty("feedback").equals("mysql")) {
				ti = new MySQLFeedback(config);
			} else {
				TextAnalyzer.logger
						.error("wrong feedback type, must have value json or mysql");
			}
		}
		return ti;
	}

	public static TagInterface getTagInterface(Config config,
			SegmentationInterface ngi) {
		if (tagI == null) {
			if (config.prop.containsKey("feedback")
					&& config.prop.get("feedback").equals("mysql")) {
				tagI = new MySqlTags(config, ngi);
			}
		}
		return tagI;
	}

	public static void resetFactory() {
		ti = null;
		tagI = null;
	}

	public static void addTwitterTags(ObjectOutput out) {
		if (tagI != null) {
			tagI.addTwitterTag(out);
		}
	}

	public static boolean addPicsomTag(String url, String tag, String photouid,
			String backendid, String visualobjectid) {
		MySqlTags mst = new MySqlTags();
		return mst.addPicsomTag(url, tag, photouid, backendid, visualobjectid);
	}

	public static boolean addPicsomTag(String tag, String photouid,
			String backendid, String visualobjectid) {
		MySqlTags mst = new MySqlTags();
		return mst.addPicsomTag(tag, photouid, backendid, visualobjectid);
	}

	public static TwitterProfile getProfile(String userid) {
		MySqlTags mst = new MySqlTags();
		return mst.getTwitterProfileInformation(userid);
	}
}
