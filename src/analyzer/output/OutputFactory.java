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
package analyzer.output;

import analyzer.config.Config;
import analyzer.segmentation.SegmentationInterface;

/**
 * Factory for output. Together with the OutputInterface we have a Factory
 * pattern so that we automatically can choose the output type.
 * 
 * @author forsstho
 * 
 */
public class OutputFactory {
	public static OutputInterface getParser(Config config, SegmentationInterface ngi) {
		if (config.prop.get("outputType") != null
				&& config.prop.get("outputType").equals("file")) {
			return new FileOutput(config, ngi);
		} else {
			return new ObjectOutput(config, ngi);
		}
	}
}
