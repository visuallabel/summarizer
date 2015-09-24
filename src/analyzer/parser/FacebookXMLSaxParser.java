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
package analyzer.parser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.FacebookContentObject;
import analyzer.content.socialmedia.Comment;
import analyzer.content.socialmedia.EventInformation;
import analyzer.content.socialmedia.GroupInformation;
import analyzer.content.socialmedia.GroupInformation.groupPrivacy;
import analyzer.content.socialmedia.LikeInformation;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.content.socialmedia.RelationshipInformation;
import analyzer.content.socialmedia.StatusMessage;
import analyzer.process.TextAnalyzer;
import analyzer.translation.TranslatorFactory;
import analyzer.translation.TranslatorInterface;
import analyzer.translation.yandex.Language;

public class FacebookXMLSaxParser implements ParserInterface {
	private HashMap<Language, Integer> identifiedLanguages = new HashMap<Language, Integer>();

	private enum CurrentInfo {
		GROUP, LIKE, VIDEO, PHOTO, STATUS, BIO, EVENT, RELATIONSHIP, EVENTLOCATION, GROUPLOCATION, STATUSCOMMENT, PHOTOCOMMENT, PHOTOLOCATION, VIDEOCOMMENT, VIDEOLOCATION, PHOTOTAG;
	}

	/**
	 * Sax parser used to parse a facebook profile in XML format. Saves content
	 * in format decided in analyzer.Content.SocialMedia
	 * 
	 * @param config
	 */
	public FacebookXMLSaxParser(Config config) {
		this.config = config;
		cO = (FacebookContentObject) ContentFactory.getParser(config);
		gI = new GroupInformation();
		lI = new LikeInformation();
		sM = new StatusMessage();
		mD = new MediaInformation();
		eI = new EventInformation();
		rI = new RelationshipInformation();
	}

	final String datePattern = "yyyy-MM-dd'T'hh:mm:ssZ";
	final SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
	private FacebookContentObject cO;
	private CurrentInfo currentEnum;
	private Config config;
	private GroupInformation gI;
	private LikeInformation lI;
	private MediaInformation mD;
	private EventInformation eI;
	private RelationshipInformation rI;
	private StatusMessage sM;
	private Comment comment;
	private String type = "";
	private int weight;
	/*
	 * private String objectText = ""; private Date objectTimeStamp = null;
	 * private int objectLikeCount = -1;
	 */
	SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser saxParser;

	@Override
	public void parseToContentObject() {
		try {
			saxParser = factory.newSAXParser();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		}
		DefaultHandler handler = new DefaultHandler() {
			/**
			 * Parse the start elements, choosing enum and creating appropriate
			 * object.
			 */
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				try {
					String input = attributes.getValue("weight");
					weight = Integer.parseInt(input);
				} catch (Exception e) {
					weight = 1;
				}
				if (qName.equalsIgnoreCase("groupList")) {
					currentEnum = CurrentInfo.GROUP;
				} else if (qName.equalsIgnoreCase("likeList")) {
					currentEnum = CurrentInfo.LIKE;
				} else if (qName.equalsIgnoreCase("photoDescriptionList")) {
					currentEnum = CurrentInfo.PHOTO;
				} else if (qName.equalsIgnoreCase("statusMessageList")) {
					currentEnum = CurrentInfo.STATUS;
				} else if (qName.equalsIgnoreCase("userDetails")) {
					currentEnum = CurrentInfo.BIO;
				} else if (qName.equalsIgnoreCase("videoDescriptionList")) {
					currentEnum = CurrentInfo.VIDEO;
				} else if (qName.equalsIgnoreCase("eventList")) {
					currentEnum = CurrentInfo.EVENT;
				} else if (qName.equalsIgnoreCase("location")
						&& currentEnum == CurrentInfo.EVENT) {
					currentEnum = CurrentInfo.EVENTLOCATION;
				} else if (qName.equalsIgnoreCase("location")
						&& currentEnum == CurrentInfo.GROUP) {
					currentEnum = CurrentInfo.GROUPLOCATION;
				} else if (qName.equalsIgnoreCase("location")
						&& currentEnum == CurrentInfo.PHOTO) {
					currentEnum = CurrentInfo.PHOTOLOCATION;
				} else if (qName.equalsIgnoreCase("location")
						&& currentEnum == CurrentInfo.VIDEO) {
					currentEnum = CurrentInfo.VIDEOLOCATION;
				} else if (qName.equalsIgnoreCase("commentlist")
						&& currentEnum == CurrentInfo.STATUS) {
					currentEnum = CurrentInfo.STATUSCOMMENT;
				} else if (qName.equalsIgnoreCase("commentlist")
						&& currentEnum == CurrentInfo.PHOTO) {
					currentEnum = CurrentInfo.PHOTOCOMMENT;
				} else if (qName.equalsIgnoreCase("commentlist")
						&& currentEnum == CurrentInfo.VIDEO) {
					currentEnum = CurrentInfo.VIDEOCOMMENT;
				} else if (qName.equalsIgnoreCase("relationshipList")
						&& currentEnum == CurrentInfo.BIO) {
					currentEnum = CurrentInfo.RELATIONSHIP;
				} else if (qName.equalsIgnoreCase("photoTag")
						&& currentEnum == CurrentInfo.PHOTO) {
					currentEnum = CurrentInfo.PHOTOTAG;
				}
				if (qName.equalsIgnoreCase("group")) {
					gI = new GroupInformation();
					gI.weight = weight;
				} else if (qName.equalsIgnoreCase("like")) {
					lI = new LikeInformation();
					lI.weight = weight;
				} else if (qName.equalsIgnoreCase("statusmessage")) {
					sM = new StatusMessage();
					sM.weight = weight;
				} else if (qName.equalsIgnoreCase("photodescription")
						|| qName.equalsIgnoreCase("videodescription")) {
					mD = new MediaInformation();
					mD.weight = weight;
				} else if (qName.equalsIgnoreCase("event")) {
					eI = new EventInformation();
					eI.weight = weight;
				} else if (qName.equalsIgnoreCase("RELATIONSHIP")) {
					rI = new RelationshipInformation();
				} else if (qName.equalsIgnoreCase("comment")) {
					comment = new Comment();
					comment.weight = weight;
				}
				type = qName.toLowerCase();
			}

			/**
			 * Parse the end elements. Adds object to appropriate vector.
			 */
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				if (qName.equalsIgnoreCase("group")) {
					cO.groupInformation.add(gI);
				} else if (qName.equalsIgnoreCase("like")) {
					cO.likeInformation.add(lI);
				} else if (qName.equalsIgnoreCase("statusmessage")) {
					cO.statusMessageInformation.add(sM);
				} else if (qName.equalsIgnoreCase("relationship")) {
					cO.relationships.add(rI);
				} else if (qName.equalsIgnoreCase("event")) {
					cO.eventInformation.add(eI);
				} else if (qName.equalsIgnoreCase("videodescription")) {
					cO.videoInformation.add(mD);

				} else if (qName.equalsIgnoreCase("photodescription")) {
					cO.photoInformation.add(mD);
				} else if (qName.equalsIgnoreCase("comment")) {
					if (currentEnum == CurrentInfo.STATUSCOMMENT) {
						sM.comments.add(comment);
					}
				} else if (qName.equalsIgnoreCase("commentlist")
						&& currentEnum == CurrentInfo.STATUSCOMMENT) {
					currentEnum = CurrentInfo.STATUS;
				} else if (qName.equalsIgnoreCase("commentlist")
						&& currentEnum == CurrentInfo.PHOTOCOMMENT) {
					currentEnum = CurrentInfo.PHOTO;
				} else if (qName.equalsIgnoreCase("commentlist")
						&& currentEnum == CurrentInfo.VIDEOCOMMENT) {
					currentEnum = CurrentInfo.VIDEO;
				} else if (qName.equalsIgnoreCase("location")
						&& currentEnum == CurrentInfo.EVENTLOCATION) {
					currentEnum = CurrentInfo.EVENT;
				} else if (qName.equalsIgnoreCase("location")
						&& currentEnum == CurrentInfo.GROUPLOCATION) {
					currentEnum = CurrentInfo.GROUP;
				} else if (qName.equalsIgnoreCase("location")
						&& currentEnum == CurrentInfo.PHOTOLOCATION) {
					currentEnum = CurrentInfo.PHOTO;
				} else if (qName.equalsIgnoreCase("location")
						&& currentEnum == CurrentInfo.VIDEOLOCATION) {
					currentEnum = CurrentInfo.VIDEO;
				} else if (qName.equalsIgnoreCase("relationshipList")
						&& currentEnum == CurrentInfo.RELATIONSHIP) {
					currentEnum = CurrentInfo.BIO;
				} else if (qName.equalsIgnoreCase("photoTag")
						&& currentEnum == CurrentInfo.PHOTOTAG) {
					currentEnum = CurrentInfo.PHOTO;

				}
			}

			/**
			 * Parse the element values. Element value is the value between a
			 * start and end tag
			 */
			public void characters(char ch[], int start, int length)
					throws SAXException {
				String elementValue = new String(ch, start, length);
				if (!elementValue.startsWith("\n")) {
					if (type.equals("name")) {
						switch (currentEnum) {
						case GROUP:
							gI.name = elementValue;
							break;
						case GROUPLOCATION:
							gI.locationName = elementValue;
							break;
						case LIKE:
							lI.name = elementValue;
							break;
						case PHOTOLOCATION:
							mD.placeName = elementValue;
							break;
						case EVENTLOCATION:
							eI.locationName = elementValue;
							break;
						case EVENT:
							eI.eventName = elementValue;
							break;
						case BIO:
							elementValue = elementValue.replace(",", " ");
							String names[] = elementValue.split(" ");
							if (names.length == 3) {
								cO.lastName = names[0];
								cO.firstName = names[1];
								cO.middleName = names[2];
								break;
							} else if (names.length == 2) {
								cO.lastName = names[0];
								cO.firstName = names[1];
								break;
							} else if (names.length == 1)
								cO.firstName = names[0];
							break;
						}
					} else if (type.equals("category")) {
						switch (currentEnum) {
						case LIKE:
							lI.category = elementValue;
							break;
						}
					} else if (type.equals("createdtimestamp")) {
						switch (currentEnum) {
						case STATUSCOMMENT:
							try {
								comment.createdTimeStamp = sdf
										.parse(elementValue);
								break;
							} catch (ParseException e) {
								// System.out.println("supposed a date: "+elementValue);
							}
						case PHOTOCOMMENT:
							try {
								comment.createdTimeStamp = sdf
										.parse(elementValue);
								break;
							} catch (ParseException e) {
								// e.printStackTrace();
							}
						case VIDEOCOMMENT:
							try {
								comment.createdTimeStamp = sdf
										.parse(elementValue);
								break;
							} catch (ParseException e) {
								// e.printStackTrace();
							}
						case PHOTO:
							if (mD != null) {
								if (mD.date == null) {
									try {
										mD.date = sdf.parse(elementValue);
										break;
									} catch (ParseException e) {
										// e.printStackTrace();
									}
								}
							}
						case VIDEO:
							if (mD != null) {
								if (mD.date == null) {
									try {
										mD.date = sdf.parse(elementValue);
										break;
									} catch (ParseException e) {
										// e.printStackTrace();
									}
								}
							}
						}
					} else if (type.equals("description")) {
						switch (currentEnum) {
						case GROUP:
							gI.description = elementValue;
							break;
						case VIDEO:
							mD.description = elementValue;
							break;
						case PHOTO:
							mD.description = elementValue;
							break;
						}
					} else if (type.equals("from")) {
						switch (currentEnum) {
						case VIDEOCOMMENT:
							comment.from = elementValue;
							break;
						case VIDEO:
							mD.from = elementValue;
							break;
						case STATUSCOMMENT:
							comment.from = elementValue;
							break;
						case STATUS:
							sM.from = elementValue;
							break;
						case PHOTOCOMMENT:
							comment.from = elementValue;
							break;
						case PHOTO:
							mD.from = elementValue;
							break;
						}
					} else if (type.equals("owner")) {
						switch (currentEnum) {
						case GROUP:
							gI.owner = elementValue;
							break;
						case EVENT:
							eI.owner = elementValue;
							break;
						}
					} else if (type.equals("message")) {
						switch (currentEnum) {
						case VIDEOCOMMENT:
							comment.message = elementValue;
							break;
						case STATUSCOMMENT:
							comment.message = elementValue;
							break;
						case STATUS:
							sM.message = elementValue;
							break;
						case PHOTOCOMMENT:
							comment.message = elementValue;
							break;
						}
					} else if (type.equals("likecount")) {
						switch (currentEnum) {
						case VIDEOCOMMENT:
							comment.likeCount = Integer.parseInt(elementValue);
							break;
						case VIDEO:
							mD.likeCount = Integer.parseInt(elementValue);
							break;
						case STATUSCOMMENT:
							comment.likeCount = Integer.parseInt(elementValue);
							break;
						case STATUS:
							try {
								sM.likeCount = Integer.parseInt(elementValue);
							} catch (Exception e) {
							}
							break;
						case PHOTOCOMMENT:
							comment.likeCount = Integer.parseInt(elementValue);
							break;
						case PHOTO:
							mD.likeCount = Integer.parseInt(elementValue);
							break;
						}
					} else if (type.equals("updatedtimestamp")) {
						switch (currentEnum) {
						case GROUP:
							try {
								gI.date = sdf.parse(elementValue);
								break;
							} catch (ParseException e) {
								// e.printStackTrace();
							}
						case STATUS:
							try {
								sM.date = sdf.parse(elementValue);
								break;
							} catch (ParseException e) {
								// e.printStackTrace();
							}
						case PHOTO:
							try {
								mD.date = sdf.parse(elementValue);
								break;
							} catch (ParseException e) {
								// e.printStackTrace();
							}
						case VIDEO:
							try {
								mD.date = sdf.parse(elementValue);
								break;
							} catch (ParseException e) {
								// e.printStackTrace();
							}
						case EVENT:
							try {
								eI.date = sdf.parse(elementValue);
								break;
							} catch (ParseException e) {
								// e.printStackTrace();
							}
						case BIO:
							try {
								cO.date = sdf.parse(elementValue);
								break;
							} catch (ParseException e) {
								// e.printStackTrace();
							}
						}
					} else if (type.equals("groupprivacy")) {
						switch (currentEnum) {
						case GROUP:
							try {
								gI.privacy = groupPrivacy.valueOf(elementValue);
								break;
							} catch (Exception e) {

							}
						case EVENT:
							try {
								eI.privacy = analyzer.content.socialmedia.EventInformation.groupPrivacy
										.valueOf(elementValue);
								break;
							} catch (Exception e) {

							}
						}
					} else if (type.equals("rsvpstatus")) {
						switch (currentEnum) {
						case EVENT:
							try {
								eI.status = analyzer.content.socialmedia.EventInformation.rsvpStatus
										.valueOf(elementValue);
								break;
							} catch (Exception e) {

							}
						}
					} else if (type.equals("country")) {
						switch (currentEnum) {
						case GROUPLOCATION:
							gI.locationCountry = elementValue;
							break;
						case PHOTOLOCATION:
							mD.country = elementValue;
							break;
						}
					} else if (type.equals("state")) {
						switch (currentEnum) {
						case GROUPLOCATION:
							gI.locationState = elementValue;
							break;
						}
					} else if (type.equals("city")) {
						switch (currentEnum) {
						case GROUPLOCATION:
							gI.locationCity = elementValue;
							break;
						case PHOTOLOCATION:
							mD.city = elementValue;
							break;
						}
					} else if (type.equals("starttimestamp")) {
						switch (currentEnum) {
						case EVENT:
							try {
								eI.startTime = sdf.parse(elementValue);
								break;
							} catch (ParseException e) {
								// e.printStackTrace();
							}
						}
					} else if (type.equals("relationshipType")) {
						switch (currentEnum) {
						case RELATIONSHIP:
							rI.relationshipType = elementValue;
							break;
						}
					} else if (type.equals("with")) {
						switch (currentEnum) {
						case RELATIONSHIP:
							rI.with = elementValue;
							break;
						}
					} else if (type.equals("endttimestamp")) {
						switch (currentEnum) {
						case EVENT:
							try {
								eI.endTime = sdf.parse(elementValue);
								break;
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					} else if (type.equals("birthday")) {
						switch (currentEnum) {
						case BIO:
							try {
								cO.birthDay = sdf.parse(elementValue);
								break;
							} catch (ParseException e) {
								// e.printStackTrace();
							}
						}
					} else if (type.equals("bio")) {
						switch (currentEnum) {
						case BIO:
							cO.bio = elementValue;
							break;
						}
					} else if (type.equals("gender")) {
						switch (currentEnum) {

						case BIO:
							try {
								cO.gender = analyzer.content.FacebookContentObject.Gender
										.valueOf(elementValue);
								break;
							} catch (Exception e) {

							}
						}
					} else if (type.equals("hometown")) {
						switch (currentEnum) {
						case BIO:
							cO.homeTown = elementValue;
							break;
						}
					} else if (type.equals("political")) {
						switch (currentEnum) {
						case BIO:
							cO.politicalView = elementValue;
							break;
						}
					} else if (type.equals("religion")) {
						switch (currentEnum) {
						case BIO:
							cO.religion = elementValue;
							break;
						}
					} else if (type.equals("userid")) {
						switch (currentEnum) {
						case BIO:
							String id = cO.getID();
							id = elementValue;
							break;
						}
					} else if (type.equals("value")) {
						switch (currentEnum) {
						case PHOTOTAG:
							mD.tagList.add(elementValue);
						}
					}
				}
			}
		};
		if (config != null) {
			try {
				saxParser.parse((String) config.prop.get("file"), handler);
			} catch (SAXException e) {
				// System.out
				// .println("The file to run specified seems to be broken or not specified. Exiting.");
				TextAnalyzer.logger
						.error("The file to run specified seems to be broken or not specified. Exiting.");
				System.exit(0);
			} catch (IOException e) {
				// e.printStackTrace();
				// System.out
				// .println("The file specified for parsing was not found. Exiting.");
				TextAnalyzer.logger
						.error("The file specified for parsing was not found. Exiting.");
				System.exit(0);
			}
		} else {
			// System.out.println("No config set in parser. Exiting.");
			TextAnalyzer.logger.error("No config set in parser. Exiting.");
			System.exit(0);
		}
	}

	@Override
	public FacebookContentObject getObject() {
		return cO;
	}

	@Override
	public void translateToEnglish() {

		// TextAnalyzer.logger.info("");
		// System.out.println("translating...");
		if (config.prop.containsKey("translation")) {
			TranslatorInterface TI = TranslatorFactory.getParser(config);

			for (GroupInformation info : cO.groupInformation) {
				if (info != null) {
					info.description = TI
							.identifyAndTranslateOneText(info.description);
				}
			}
			for (LikeInformation info : cO.likeInformation) {
				try {
					if (info != null) {
						if (info.name != null) {
							info.name = TI
									.identifyAndTranslateOneText(info.name);

						}
					}
				} catch (Exception e) {

				}
			}
			for (MediaInformation info : cO.videoInformation) {
				if (info != null) {
					if (info.description != null) {
						info.description = TI
								.identifyAndTranslateOneText(info.description);
					}
					for (Comment c : info.comments) {
						if (c != null) {
							if (c.message != null) {
								c.message = TI
										.identifyAndTranslateOneText(c.message);
							}
						}
					}
				}
			}
			for (MediaInformation info : cO.photoInformation) {
				if (info != null) {
					if (info.description != null) {
						info.description = TI
								.identifyAndTranslateOneText(info.description);
					}
					for (Comment c : info.comments) {
						if (c != null) {
							if (c.message != null) {
								c.message = TI
										.identifyAndTranslateOneText(c.message);
							}
						}
					}
					Vector<String> tags = new Vector<String>();
					for (String tag : info.tagList) {
						if (tag != null) {
							tags.add(TI.identifyAndTranslateOneText(tag));
						}
					}
					info.tagList = tags;
				}
			}
			for (EventInformation info : cO.eventInformation) {
				if (info != null) {
					if (info.description != null) {
						info.description = TI
								.identifyAndTranslateOneText(info.description);
					}
				}
			}
			for (StatusMessage info : cO.statusMessageInformation) {
				if (info != null) {
					if (info.message != null) {
						info.message = TI
								.identifyAndTranslateOneText(info.message);

					}
					for (Comment c : info.comments) {
						if (c != null) {
							if (c.message != null) {
								c.message = TI
										.identifyAndTranslateOneText(c.message);
							}
						}
					}
				}
			}
		} else {
			TextAnalyzer.logger.info("No translation engine defined.");
			// System.out.println("No translation engine defined.");
		}
	}

	@Override
	public HashMap<Language, Integer> getLanguages() {
		return identifiedLanguages;
	}
}
