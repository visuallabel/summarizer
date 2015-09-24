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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.ContentInterface;
import analyzer.content.FacebookContentObject;
import analyzer.content.PlainTextContentObject;
import analyzer.content.TextObject;
import analyzer.content.TwitterContentObject;
import analyzer.content.socialmedia.Comment;
import analyzer.content.socialmedia.EventInformation;
import analyzer.content.socialmedia.GroupInformation;
import analyzer.content.socialmedia.LikeInformation;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.content.socialmedia.RelationshipInformation;
import analyzer.content.socialmedia.StatusMessage;
import analyzer.ner.NamedEntity.EntityType;
import analyzer.process.TextAnalyzer;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;

/**
 * Named entity recognition class based on the Stanford Named Entity
 * recognitioner.
 * 
 * @author forsstho
 * 
 */
public class NamedEntityRecognition implements NerInterface {
	public String serializedClassifier = "";
	// TODO: make sure we are using the same type of list through the whole
	// program. Now we use arraylist here and vector in other places
	public LinkedHashMap<String, NamedEntity> namedEntities = new LinkedHashMap<String, NamedEntity>();
	ArrayList<String> names = new ArrayList<String>();
	AbstractSequenceClassifier classifier = null;

	public NamedEntityRecognition(Config config) {
		ContentInterface cO = ContentFactory.getParser(config);
		if (config.prop.containsKey("ner")) {
			serializedClassifier = config.prop.getProperty("ner");
			classifier = CRFClassifier
					.getClassifierNoExceptions(serializedClassifier);

			if (cO instanceof FacebookContentObject) {
				findNames((FacebookContentObject) cO);
				findFacebookNer(cO);
				removeNamesAndFriends();
			} else if (cO instanceof PlainTextContentObject) {
				findPlainNer(cO);
			} else if (cO instanceof TwitterContentObject) {
				findTwitterNer(cO);
			} else {
				TextAnalyzer.logger
						.info("Unrecognized ner type, skipping ner.");
			}
		} else {
		}
	}

	public void findTwitterNer(ContentInterface cO) {
		TwitterContentObject tO = (TwitterContentObject) cO;
		Vector<MediaInformation> videoInformation = tO.videoInformation;
		Vector<MediaInformation> photoInformation = tO.photoInformation;
		Vector<StatusMessage> statusMessageInformation = tO.statusMessageInformation;
		if (videoInformation != null) {
			for (MediaInformation media : videoInformation) {
				if (media != null && media.description != null) {
					String namedEntities = findNamedEntities(media.description);
					tokenizeNamedEtities(namedEntities, media.source,
							media.date);
				}
			}
		}
		if (photoInformation != null) {
			for (MediaInformation media : photoInformation) {
				if (media != null && media.description != null) {
					
					String stripped = media.description.replace("#", "");
					stripped=stripped.replace("@", "");
					
					String namedEntities = findNamedEntities(stripped);
					tokenizeNamedEtities(namedEntities, media.source,
							media.date);
				}
			}
		}
		if (statusMessageInformation != null) {
			for (StatusMessage status : statusMessageInformation) {
				if (status != null && status.message != null) {
					String namedEntities = findNamedEntities(status.message);
					tokenizeNamedEtities(namedEntities, null, status.date);

				}
			}
		}
	}

	public void findPlainNer(ContentInterface cO) {
		PlainTextContentObject pO = (PlainTextContentObject) cO;
		for (TextObject text : pO.content) {
			if (text != null) {
				String namedEntities = findNamedEntities(text.toString());
				// only take person, organization and location tokens
				tokenizeNamedEtities(namedEntities, null, null);
			}
		}
	}

	public void findFacebookNer(ContentInterface cO) {
		FacebookContentObject fO = (FacebookContentObject) cO;
		Vector<MediaInformation> videoInformation = fO.videoInformation;
		Vector<MediaInformation> photoInformation = fO.photoInformation;
		Vector<LikeInformation> likeInformation = fO.likeInformation;
		Vector<StatusMessage> statusMessageInformation = fO.statusMessageInformation;
		Vector<GroupInformation> groupInformation = fO.groupInformation;
		Vector<EventInformation> eventInformation = fO.eventInformation;
		if (videoInformation != null) {
			for (MediaInformation media : videoInformation) {
				if (media != null && media.description != null) {
					String namedEntities = findNamedEntities(media.description);
					tokenizeNamedEtities(namedEntities, media.source,
							media.date);
				}
			}
		}
		if (photoInformation != null) {
			for (MediaInformation media : photoInformation) {
				if (media != null && media.description != null) {
					String namedEntities = findNamedEntities(media.description);
					tokenizeNamedEtities(namedEntities, media.source,
							media.date);
				}
			}
		}
		if (statusMessageInformation != null) {
			for (StatusMessage status : statusMessageInformation) {
				if (status != null && status.message != null) {
					String namedEntities = findNamedEntities(status.message);
					tokenizeNamedEtities(namedEntities, null, status.date);
				}
			}
		}
		if (likeInformation != null) {
			for (LikeInformation info : likeInformation) {
				if (info != null && info.name != null) {
					String namedEntities = findNamedEntities(info.name);
					tokenizeNamedEtities(namedEntities, null, info.date);
				}
			}
		}
		if (groupInformation != null) {
			for (GroupInformation info : groupInformation) {
				if (info != null) {
					String namedEntities = findNamedEntities(info.toString());
					tokenizeNamedEtities(namedEntities, null, info.date);
				}
			}
		}
		if (eventInformation != null) {
			for (EventInformation info : eventInformation) {
				if (info != null && info.description != null) {
					String namedEntities = findNamedEntities(info.description);
					tokenizeNamedEtities(namedEntities, null, info.date);
				}
			}
		}
	}

	/*
	 * public void formatTextThenClassify() { String text = ""; // TODO input
	 * and format text // find named entities String namedEntities =
	 * findNamedEntities(text); // only take person, organization and location
	 * tokens tokenizeNamedEtities(namedEntities); // remove names found in the
	 * profile from before removeNamesAndFriends(); }
	 */

	private void removeNamesAndFriends() {
		if (namedEntities.size() > 0) {
			Iterator<NamedEntity> it = namedEntities.values().iterator();
			while (it.hasNext()) {
				String next = it.next().namedEntity;
				for (String name : names) {
					if (name.contains(next)) {
						namedEntities.remove(next);
					}
				}
			}
		} else {
			TextAnalyzer.logger.info("No named entities found.");
			// System.out.println("No named entities found.");
		}
	}

	/**
	 * We extract locations and organizations, but not names
	 * 
	 * @param entities
	 */
	private void tokenizeNamedEtities(String entities, String source,
			Date timestamp) {
		entities = entities.replace(".", " ");
		entities = entities.replace("!", " ");
		entities = entities.replace(",", " ");
		entities = entities.replace("?", " ");
		StringTokenizer st = new StringTokenizer(entities);
		int nerType = 0;
		int previousNerType = 0;
		String nerText = "";
		while (st.hasMoreTokens()) {
			String text = st.nextToken();
			if (text.contains("'")) {
				int char1 = text.indexOf("'");
				text = text.substring(0, char1);
			}
			if (text.contains("--")) {
				text = text.replace("--", " ");
			}
			if (text.startsWith("-")) {
				text = text.replace("-", "");
			}
			if (text.contains(":")) {
				text = text.replace(":", "");
			}
			if (text.contains("`")) {
				text = text.replace("`", "");
			}
			if (text.contains("/PERSON")) {
				nerType = 1;
				text = text.replace("/PERSON", "");
				text = text.replace("/O", "");
				if (previousNerType == nerType) {
					nerText += " " + text;

				} else if (previousNerType == 0) {
					nerText = text;
				} else {
					NamedEntity ner;
					if (nerText.equals("") && !text.equals("")
							&& !text.equals(" ")) {
						if (text.startsWith(" ")) {
							text = text.replaceFirst(" ", "");
						}
						if (namedEntities.containsKey(text)) {
							ner = namedEntities.get(text);
						} else {
							ner = new NamedEntity();
							ner.timestamp = timestamp;
						}
						if (source != null) {
							ner.url.add(source);
						}
						ner.namedEntity = text;
						ner.frequency++;
						if (nerType == 1) {
							ner.setType(EntityType.PERSON);
						} else if (nerType == 3) {
							ner.setType(EntityType.ORGANIZATION);
						} else if (nerType == 2) {
							ner.setType(EntityType.LOCATION);
						}
						namedEntities.put(text, ner);
					} else {
						if (namedEntities.containsKey(nerText)) {
							ner = namedEntities.get(nerText);
						} else {
							ner = new NamedEntity();
							ner.timestamp = timestamp;
						}
						if (source != null) {
							ner.url.add(source);
						}
						ner.namedEntity = nerText;
						ner.frequency++;
						if (nerType == 1) {
							ner.setType(EntityType.PERSON);
						} else if (nerType == 3) {
							ner.setType(EntityType.ORGANIZATION);
						} else if (nerType == 2) {
							ner.setType(EntityType.LOCATION);
						}
						namedEntities.put(nerText, ner);
						nerText = "";
					}
				}
			} else if (text.contains("/LOCATION")) {
				nerType = 2;
				text = text.replace("/LOCATION", "");
				text = text.replace("/O", "");
				NamedEntity ner;
				if (previousNerType == nerType) {
					nerText += " " + text;
				} else if (previousNerType == 0) {
					nerText = text;
				} else {
					if (nerText.equals("") && !text.equals("")
							&& !text.equals(" ")) {
						if (namedEntities.containsKey(text)) {
							ner = namedEntities.get(text);
						} else {
							ner = new NamedEntity();
							ner.timestamp = timestamp;
						}
						if (source != null) {
							ner.url.add(source);
						}
						ner.namedEntity = text;
						ner.frequency++;
						if (nerType == 1) {
							ner.setType(EntityType.PERSON);
						} else if (nerType == 3) {
							ner.setType(EntityType.ORGANIZATION);
						} else if (nerType == 2) {
							ner.setType(EntityType.LOCATION);
						}
						namedEntities.put(text, ner);
					} else {
						if (namedEntities.containsKey(nerText)) {
							ner = namedEntities.get(nerText);
						} else {
							ner = new NamedEntity();
							ner.timestamp = timestamp;
						}
						if (source != null) {
							ner.url.add(source);
						}
						ner.namedEntity = nerText;
						ner.frequency++;
						if (nerType == 1) {
							ner.setType(EntityType.PERSON);
						} else if (nerType == 3) {
							ner.setType(EntityType.ORGANIZATION);
						} else if (nerType == 2) {
							ner.setType(EntityType.LOCATION);
						}
						if (!nerText.equals("")) {
							if (nerText.startsWith(" ")) {
								nerText = nerText.substring(1,
										nerText.length() - 1);
								ner.namedEntity = nerText;
							}
							namedEntities.put(nerText, ner);
						}
						nerText = "";
					}
				}

			} else if (text.contains("/ORGANIZATION")) {
				nerType = 3;
				text = text.replace("/ORGANIZATION", "");
				text = text.replace("/O", "");
				if (previousNerType == nerType) {
					nerText += " " + text;

				} else if (previousNerType == 0) {
					nerText = text;
				} else {
					NamedEntity ner = new NamedEntity();
					ner.timestamp = timestamp;
					if (nerText.equals("") && !text.equals("")
							&& !text.equals(" ")) {
						if (namedEntities.containsKey(text)) {
							ner = namedEntities.get(text);
						} else {
							ner = new NamedEntity();
							ner.timestamp = timestamp;
						}

						ner.namedEntity = text;
						ner.frequency++;
						if (nerType == 1) {
							ner.setType(EntityType.PERSON);
						} else if (nerType == 3) {
							ner.setType(EntityType.ORGANIZATION);
						} else if (nerType == 2) {
							ner.setType(EntityType.LOCATION);
						}
						if (!nerText.equals("")) {
							if (nerText.startsWith(" ")) {
								nerText = nerText.substring(1,
										nerText.length() - 1);
								ner.namedEntity = nerText;
							}
							namedEntities.put(text, ner);
						}
					} else {
						if (namedEntities.containsKey(nerText)) {
							ner = namedEntities.get(nerText);
						} else {
							ner = new NamedEntity();
							ner.timestamp = timestamp;
						}
						ner.namedEntity = nerText;
						ner.frequency++;
						if (nerType == 1) {
							ner.setType(EntityType.PERSON);
						} else if (nerType == 3) {
							ner.setType(EntityType.ORGANIZATION);
						} else if (nerType == 2) {
							ner.setType(EntityType.LOCATION);
						}
						if (!nerText.equals("")) {
							if (nerText.startsWith(" ")) {
								nerText = nerText.substring(1,
										nerText.length() - 1);
								ner.namedEntity = nerText;
							}
							namedEntities.put(nerText, ner);
						}
						nerText = "";
					}
				}

			} else if (text.contains("/O")) {
				previousNerType = 0;
				if (nerText != "") {
					NamedEntity ner;
					if (namedEntities.containsKey(nerText)) {
						ner = namedEntities.get(nerText);
					} else {
						ner = new NamedEntity();
						ner.timestamp = timestamp;
					}
					if (source != null) {
						ner.url.add(source);
					}
					ner.namedEntity = nerText;
					ner.frequency++;
					if (nerType == 1) {
						ner.setType(EntityType.PERSON);
					} else if (nerType == 3) {
						ner.setType(EntityType.ORGANIZATION);
					} else if (nerType == 2) {
						ner.setType(EntityType.LOCATION);
					}
					if (!nerText.equals("") && nerText != null
							&& !nerText.equals(" ")) {
						if (nerText.startsWith(" ")) {

							nerText = nerText
									.substring(1, nerText.length() - 1);
							ner.namedEntity = nerText;
						}
						namedEntities.put(nerText, ner);
					}
					nerText = "";
				}
			}
			previousNerType = nerType;
		}
		if (nerText != "") {
			NamedEntity ner;
			if (namedEntities.containsKey(nerText)) {
				ner = namedEntities.get(nerText);
			} else {
				ner = new NamedEntity();
				ner.timestamp = timestamp;
			}
			if (source != null) {
				ner.url.add(source);
			}
			ner.namedEntity = nerText;
			ner.frequency++;
			if (nerType == 1) {
				ner.setType(EntityType.PERSON);
			} else if (nerType == 3) {
				ner.setType(EntityType.ORGANIZATION);
			} else if (nerType == 2) {
				ner.setType(EntityType.LOCATION);
			}
			if (nerText != "") {
				if (nerText.startsWith(" ")) {
					if((nerText.length()-1)>1)
					{
						nerText = nerText.substring(1, nerText.length() - 1);
					}
					ner.namedEntity = nerText;
				}
				namedEntities.put(nerText, ner);
			}
		}
	}

	public String findNamedEntities(String text) {
		//System.out.println("input: "+text);
		String output = classifier.classifyToString(text);
		//System.out.println("output: "+output);
		return output;
	}

	private void findNames(FacebookContentObject cO) {
		if (cO.firstName != null && cO.firstName != "") {
			names.add(cO.firstName.toLowerCase());
		} else if (cO.firstName != null && cO.firstName != "") {
			names.add(cO.lastName.toLowerCase());
		} else if (cO.firstName != null && cO.firstName != "") {
			names.add(cO.middleName.toLowerCase());
		}
		for (StatusMessage sm : cO.statusMessageInformation) {
			if (sm.from != null && sm.from != "") {
				names.add(sm.from.toLowerCase());
			}
			for (Comment c : sm.comments) {
				if (c.from != null && c.from != "") {
					names.add(c.from.toLowerCase());
				}
			}
		}
		for (EventInformation e : cO.eventInformation) {
			if (e.owner != null && e.owner != "") {
				names.add(e.owner.toLowerCase());

			}
		}
		/*
		 * Adding group information to Named Entities while removing names of
		 * creator
		 */
		for (GroupInformation e : cO.groupInformation) {
			if (e.owner != null && e.owner != "") {
				names.add(e.owner.toLowerCase());
			}
			if (e.name != null && e.name != "") {
				NamedEntity ner = new NamedEntity();
				ner.timestamp = e.date;
				ner.namedEntity = e.name;
				ner.frequency++;
				namedEntities.put(e.name, ner);

			}
		}
		/*
		 * Adding names of Like information to Named Entities while removing
		 * others
		 */

		for (LikeInformation e : cO.likeInformation) {
			if (e.name != null && e.name != "") {
				NamedEntity ner = new NamedEntity();
				ner.timestamp = e.date;
				ner.namedEntity = e.name;
				ner.frequency++;
				namedEntities.put(e.name, ner);
			}
		}
		for (MediaInformation e : cO.videoInformation) {
			if (e.from != null && e.from != "") {
				names.add(e.from.toLowerCase());

			}
			for (Comment c : e.comments) {
				if (c.from != null && c.from != "") {
					names.add(c.from.toLowerCase());
				}
			}
		}
		for (MediaInformation e : cO.photoInformation) {
			if (e.from != null && e.from != "") {
				names.add(e.from.toLowerCase());
			}
			for (Comment c : e.comments) {
				if (c.from != null && c.from != "") {
					names.add(c.from.toLowerCase());
				}
			}
		}
		for (RelationshipInformation e : cO.relationships) {
			if (e.with != null && e.with != "") {
				names.add(e.with.toLowerCase());
			}
		}

	}

	@Override
	public LinkedHashMap<String, NamedEntity> getEntities() {
		return namedEntities;
	}
}
