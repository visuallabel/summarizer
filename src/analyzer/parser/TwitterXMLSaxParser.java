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
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.TwitterContentObject;
import analyzer.content.socialmedia.Comment;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.content.socialmedia.StatusMessage;
import analyzer.process.TextAnalyzer;
import analyzer.translation.TranslatorFactory;
import analyzer.translation.TranslatorInterface;
import analyzer.translation.yandex.Language;

/**
 * Sax parser class for parsing twitter input. Requires quite a lot of
 * maintenance but is faster than other types of xml parsing.
 * 
 * @author forsstho
 * 
 */

public class TwitterXMLSaxParser implements ParserInterface {
	private TwitterContentObject tco;
	private SAXParserFactory factory = SAXParserFactory.newInstance();
	private SAXParser saxParser;
	private Config config;
	private CurrentInfo currentEnum;
	private StatusMessage sM;
	private MediaInformation mD;
	private String type = "";
	final String datePattern = "yyyy-MM-dd'T'hh:mm:ssZ";
	final SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
	private HashMap<Language, Integer> identifiedLanguages = new HashMap<Language, Integer>();

	public TwitterXMLSaxParser(Config config) {
		tco = (TwitterContentObject) ContentFactory.getParser(config);
		this.config = config;
	}

	private enum CurrentInfo {
		USERDETAILS, VIDEO, PHOTO, STATUS, STATUSCOMMENT, PHOTOCOMMENT, PHOTOLOCATION, VIDEOCOMMENT, VIDEOLOCATION, PHOTOTAG;
	}

	/**
	 * parseToContentObject Parse the inputted content into the structure
	 * defined in analyzer.Content.SocialMedia
	 */
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
			private StringBuilder chars = new StringBuilder();
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				chars.setLength(0);
				try {
					String input = attributes.getValue("weight");
				} catch (Exception e) {
				}
				if (qName.equalsIgnoreCase("photoDescriptionList")) {
					currentEnum = CurrentInfo.PHOTO;
				} else if (qName.equalsIgnoreCase("statusMessageList")) {
					currentEnum = CurrentInfo.STATUS;
				} else if (qName.equalsIgnoreCase("videoDescriptionList")) {
					currentEnum = CurrentInfo.VIDEO;
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
				} else if (qName.equalsIgnoreCase("photoTag")
						&& currentEnum == CurrentInfo.PHOTO) {
					currentEnum = CurrentInfo.PHOTOTAG;
				}
				if (qName.equalsIgnoreCase("statusmessage")) {
					sM = new StatusMessage();
				} else if (qName.equalsIgnoreCase("photodescription")
						|| qName.equalsIgnoreCase("videodescription")) {
					mD = new MediaInformation();
				}
				type = qName.toLowerCase();
			}

			/**
			 * Parse the end elements. Adds object to appropriate vector.
			 */
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				
				String elementValue = chars.toString();
				if (!elementValue.startsWith("\n")) 
				{
					if (type.equalsIgnoreCase("name")) {
						try {
							tco.name = elementValue;
						} catch (Exception e) {

						}
					} else if (type.equalsIgnoreCase("createdtimestamp")) {
						switch (currentEnum) {
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
						default:
							break;
						}
					} else if (type.equalsIgnoreCase("description")) {
						switch (currentEnum) {
						case VIDEO:
							mD.description = elementValue;
							parseForUrl(elementValue);
							parseForHashTags(1, elementValue);
							break;
						case PHOTO:
							mD.description = elementValue;
							parseForUrl(elementValue);
							parseForHashTags(1, elementValue);
							break;
						default:
							break;
						}
					} else if (type.equalsIgnoreCase("from")) {
						switch (currentEnum) {
						case VIDEO:
							mD.from = elementValue;
							break;
						case STATUS:
							sM.from = elementValue;
							break;
						case PHOTO:
							mD.from = elementValue;
							break;
						default:
							break;
						}
					} else if (type.equalsIgnoreCase("photoUID")) {
						mD.photoUID = elementValue;
					} else if (type.equalsIgnoreCase("serviceId")) {
						mD.serviceId = elementValue;
					} else if (type.equalsIgnoreCase("message")) {
						switch (currentEnum) {
						case STATUS:
							sM.message = elementValue;
							parseForHashTags(2, elementValue);
							break;
						default:
							break;
						}
					} else if (type.equalsIgnoreCase("updatedtimestamp")) {
						switch (currentEnum) {
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
						default:
							break;

						}
					} 
					else if (type.equalsIgnoreCase("twitterId")) {
						tco.userId = elementValue;

					}
				} 
				if (type.equalsIgnoreCase("value")) {
					switch (currentEnum) {
					case PHOTOTAG:
						mD.tagList.add(elementValue);
					default:
						break;
					}
				} else if (type.equalsIgnoreCase("favoritesCount")) {
					try {
						tco.favoritesCount = Integer.parseInt(elementValue);
					} catch (Exception e) {

					}
				} else if (type.equalsIgnoreCase("followersCount")) {
					try {
						tco.followersCount = Integer.parseInt(elementValue);
					} catch (Exception e) {

					}
				} else if (type.equalsIgnoreCase("friendsCount")) {
					try {
						tco.friendsCount = Integer.parseInt(elementValue);
					} catch (Exception e) {

					}
				} else if (type.equalsIgnoreCase("twitterId")) {
					try {

						// tco.userId = elementValue;

					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (type.equalsIgnoreCase("screenName")) {
					try {
						tco.screenName = elementValue;
					} catch (Exception e) {

					}
				}
			
				
				
				if (qName.equalsIgnoreCase("statusmessage")) {
					tco.statusMessageInformation.add(sM);
				} else if (qName.equalsIgnoreCase("videodescription")) {
					tco.videoInformation.add(mD);

				} else if (qName.equalsIgnoreCase("photodescription")) {
					tco.photoInformation.add(mD);
				}
			}

			/**
			 * Parse the element values. Element value is the value between a
			 * start and end tag
			 */
			public void characters(char ch[], int start, int length)
					throws SAXException {
				String elementValue = new String(ch, start, length);
				if(!elementValue.startsWith("\n"))
					chars.append(ch,start,length);
				
				
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
	public Object getObject() {
		// TODO Maybe remove this method from interface?
		return tco;
	}

	@Override
	public void translateToEnglish() {
		if (config.prop.containsKey("translation")) {

			TranslatorInterface TI = TranslatorFactory.getParser(config);

			for (MediaInformation info : tco.videoInformation) {
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
			for (MediaInformation info : tco.photoInformation) {
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

			for (StatusMessage info : tco.statusMessageInformation) {
				if (info != null) {
					if (info.message != null) {
						info.message = TI
								.identifyAndTranslateOneText(info.message);

					}
				}
			}
		} else {
			TextAnalyzer.logger.info("No translation engine defined.");
			// System.out.println("No translation engine defined.");
		}

	}

	/**
	 * parseForUrl parses a description or text for picture urls
	 */
	public void parseForUrl(String url) {
		String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		// TODO: there seems to be some urls that are not matched correctly (or
		// then they are wrong to start with?)
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(url);
		while (matcher.find()) {
			mD.source = matcher.group();
		}
		if (mD.source != null) {
			mD.description = mD.description.replace(mD.source, "");
		}
	}

	/**
	 * parseForHashTags Method added specifically for twitter anaylsis, could
	 * maybe be extended to include others aswell. We parse each hashtag and
	 * create a hashtag object with a link to url (if found) and frequency.
	 * 
	 * @param type
	 *            says if we are checking mediainformation of statusmessages for
	 *            hashtags
	 * @param text
	 */
	public void parseForHashTags(int type, String text) {
		StringTokenizer st = new StringTokenizer(text);

		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.toString().startsWith("#")) {
				String tag = token.toString().replace("#", " ");
				String[] tags = tag.split(" ");
				if (type == 1) {
					for (int i = 0; i < tags.length; i++) {
						String tagtext = tags[i];
						tagtext = tagtext.replace(" ", "");
						mD.hashtags.add(tagtext);
					}

				} else if (type == 2) {
					for (int i = 0; i < tags.length; i++) {
						String tagtext = tags[i];
						tagtext = tagtext.replace(" ", "");
						sM.hashtags.add(tagtext);
					}
				}
			}
		}
	}

	@Override
	public HashMap<Language, Integer> getLanguages() {
		return identifiedLanguages;
	}

}
