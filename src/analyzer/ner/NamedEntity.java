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
package analyzer.ner;

import java.util.Date;
import java.util.HashSet;

public class NamedEntity {
	public enum EntityType {
		LOCATION, ORGANIZATION, PERSON;
	}

	private EntityType type;
	public String namedEntity;
	public String namedEntityOriginalLanguage;
	public double tfidf = -1;
	public HashSet<String> url = new HashSet<String>();
	public int frequency = 0;
	public Date timestamp;

	@Override
	public String toString() {
		return namedEntity;
	}

	public void setType(EntityType type) {
		this.type = type;
	}

	public EntityType getType() {
		return type;
	}

}
