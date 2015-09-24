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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import analyzer.config.Config;
import analyzer.content.ContentFactory;
import analyzer.content.ContentInterface;
import analyzer.content.FacebookContentObject;
import analyzer.content.TwitterContentObject;
import analyzer.content.socialmedia.HashTag;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.ner.NamedEntity;
import analyzer.output.ObjectOutput;
import analyzer.process.TextAnalyzer;
import analyzer.segmentation.Centroid;
import analyzer.segmentation.SegmentationInterface;
import analyzer.segmentation.Sentence;

/**
 * Class for inserting tags into a MySQL server
 * 
 * @author forsstho
 * 
 */
public class MySqlTags implements TagInterface {
	String userid;
	// private List<Centroid> centroids;
	private LinkedHashMap<Integer, LinkedHashMap<String, Centroid>> ncurrentgrams;
	ContentInterface ci;

	public MySqlTags(Config config, SegmentationInterface ngi) {
		ci = ContentFactory.getParser(config);
		if (ci instanceof FacebookContentObject) {
			this.userid = ci.getID();
			ncurrentgrams = ngi.ngramToCentroids();

			// centroids = TFIDFFactory.getCentroids();
		}
	}

	public MySqlTags() {

	}

	@Override
	public void addTags() {
		if (ci instanceof FacebookContentObject) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				TextAnalyzer.logger.error("SQL insert exception 1: "
						+ e.getMessage());
				System.exit(0);
			}
			Connection connection = null;

			try {
				connection = DriverManager.getConnection(Keys.MYSQL_DB,
						Keys.MYSQL_UN, Keys.MYSQL_PW);

			} catch (SQLException e) {
				TextAnalyzer.logger.error("SQL insert exception 2: "
						+ e.getMessage());
				System.exit(0);
			}
			if (connection != null) {
				for (int size : ncurrentgrams.keySet()) {
					LinkedHashMap<String, Centroid> centroids = ncurrentgrams
							.get(size);
					for (Centroid cent : centroids.values()) {
						try {
							String text = cent.getTag();
							double tfidf = cent.getTfidf();
							Statement statement = connection.createStatement();
							String preQueryStatement = "INSERT  INTO  tags  VALUES  ("
									+ userid
									+ ",'"
									+ text
									+ "',"
									+ size
									+ ","
									+ tfidf
									+ ", null, '"
									+ cent.getUniqueID()
									+ "')";
							statement.executeUpdate(preQueryStatement);
						} catch (Exception e) {

						}
					}
				}
				for (LinkedHashMap<String, Centroid> grams : ncurrentgrams
						.values()) {
					for (Centroid cent : grams.values()) {
						try {
							int ngram = 1;
							String text = cent.getTag();
							double tfidf = cent.getTfidf();
							Statement statement = connection.createStatement();
							String preQueryStatement = "INSERT  INTO  tags  VALUES  ("
									+ userid
									+ ",'"
									+ text
									+ "',"
									+ ngram
									+ ","
									+ tfidf
									+ ", null, '"
									+ cent.getUniqueID()
									+ "')";
							statement.executeUpdate(preQueryStatement);
						} catch (Exception e) {

						}
					}
				}
			}
		} else if (ci instanceof TwitterContentObject) {

		}
	}

	@Override
	public void addTwitterTag(ObjectOutput oo) {
		String userid = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			TextAnalyzer.logger.error("SQL insert twitter exception 1: "
					+ e.getMessage());
//			e.printStackTrace();
		}
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(Keys.MYSQL_TWITTER_DB,
					Keys.MYSQL_UN, Keys.MYSQL_PW);

		} catch (SQLException e) {
			TextAnalyzer.logger.error("SQL insert twitter exception 2: "
					+ e.getMessage());
//			e.printStackTrace();
		}
		if (connection != null) {
			TwitterContentObject tco = null;
			if (ci instanceof TwitterContentObject) {
				tco = (TwitterContentObject) ci;
			}
			if (tco == null) {
				System.out.println("tco still null");
			}
			Statement statement = null;
			String preQueryStatement = null;

			try {
				statement = connection.createStatement();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			userid = tco.getID();
			if (userid != null) {
				try {
					preQueryStatement = "INSERT IGNORE  INTO  users(userid)  VALUES('"
							+ userid + "')";
					statement.executeUpdate(preQueryStatement);
				} catch (Exception e) {
					// System.out.println("userid insertion exception: "
					// + preQueryStatement);
					// e.printStackTrace();
				}
			} else {
				System.out.println("userid is stil null.");
			}

			// insert grams
			Vector<Centroid> centroids = oo.getSinglePhotoOutput();
			HashMap<String, Date> pictures = new HashMap<String, Date>();

			for (Centroid c : centroids) {
				for (String s : c.getSource()) {
					pictures.put(s, c.timestamp);
				}
				try {
					preQueryStatement = "INSERT  INTO  grams(userid, text, originaltext, tfidf, ngramsize)  VALUES(\""
							+ userid
							+ "\",\""
							+ c.getTag()
							+ "\",\""
							+ c.getOriginalTag()
							+ "\","
							+ c.getTfidf()
							+ ","
							+ c.ngramsize + ")";
					statement.executeUpdate(preQueryStatement);
				} catch (Exception e) {
					// System.out.println("grams insert exception: "
					// + preQueryStatement);
//					 e.printStackTrace();
				}
			}

			for (String s : pictures.keySet()) {
				try {
					preQueryStatement = "INSERT INTO pictures(pictureurl)  VALUES(\""
							+ s + "\")";
					statement.executeUpdate(preQueryStatement);
				} catch (Exception e) {
					// System.out.println("pictures insert exception: "
					// + preQueryStatement);
//					 e.printStackTrace();
				}
			}
			// insert userpictures
			for (String s : pictures.keySet()) {
				try {
					Timestamp timestamp = new Timestamp(pictures.get(s)
							.getTime());
					preQueryStatement = "INSERT INTO userpictures(userid, pictureurl, timestamp)  VALUES(\""
							+ userid
							+ "\",\""
							+ s
							+ "\",\""
							+ timestamp
							+ "\")";
					statement.executeUpdate(preQueryStatement);
				} catch (Exception e) {
//					 e.printStackTrace();
				}
			}

			// insert hashtags

			LinkedHashMap<String, HashTag> tags = oo.getHashtags();
			for (String tag : tags.keySet()) {
				try {
					preQueryStatement = "INSERT IGNORE INTO hashtags(tagtext)  VALUES(\""
							+ tag + "\")";
					statement.executeUpdate(preQueryStatement);
				} catch (Exception e) {
					// System.out.println("hashtags insert exception: "
					// + preQueryStatement);
//					 e.printStackTrace();
				}
			}

			// insert named entities
			LinkedHashMap<String, NamedEntity> entities = oo.getNamedEntities();

			if (entities != null) {
				for (String text : entities.keySet()) {
					try {
						preQueryStatement = "INSERT IGNORE INTO namedentity(netext, netype)  VALUES(\""
								+ text
								+ "\",\""
								+ entities.get(text).getType()
								+ "\")";

						statement.executeUpdate(preQueryStatement);
					} catch (Exception e) {
						// System.out.println("namedentity insert exception: "
						// + preQueryStatement);
//						 e.printStackTrace();
					}
				}
			}
			for (NamedEntity ent : entities.values()) {
				for (String s : ent.url) {
					try {
						Timestamp timestamp = ent.timestamp==null? null : new Timestamp(ent.timestamp.getTime());
						if (timestamp==null || timestamp.toString() == null
								|| timestamp.toString().equals("null")) {
							preQueryStatement = "INSERT IGNORE INTO picturenamedentities(netext, pictureurl)  VALUES(\""
									+ ent.namedEntity + "\",\"" + s + "\")";

							statement.executeUpdate(preQueryStatement);
						} else {
							preQueryStatement = "INSERT IGNORE INTO picturenamedentities(netext, pictureurl, timestamp)  VALUES(\""
									+ ent.namedEntity
									+ "\",\""
									+ s
									+ "\",\""
									+ timestamp.toString() + "\")";

							statement.executeUpdate(preQueryStatement);
						}
					} catch (Exception e) {
						 System.out
						 .println("picturenamedentities insert exception: "
						 + preQueryStatement);
						 e.printStackTrace();
					}
				}
			}
			for (HashTag tag : tags.values()) {
				preQueryStatement = "";
				try {
					if (tag != null) {
						for (String source : tag.sources) {
							if (tag.timestamp != null) {
								Timestamp timestamp = new Timestamp(
										tag.timestamp.getTime());
								if (timestamp.toString() == null
										|| timestamp.toString().equals("null")) {
									preQueryStatement = "INSERT IGNORE INTO picturehashtags(tagtext, pictureurl, frequency, timestamp)  VALUES(\""
											+ tag.tag
											+ "\",\""
											+ source
											+ "\",\""
											+ tag.frequency
											+ "\",\""
											+ timestamp.toString() + "\")";
									statement.executeUpdate(preQueryStatement);
								}
							} else {
								preQueryStatement = "INSERT IGNORE INTO picturehashtags(tagtext, pictureurl, frequency)  VALUES(\""
										+ tag.tag
										+ "\",\""
										+ source
										+ "\",\""
										+ tag.frequency + "\")";
								statement.executeUpdate(preQueryStatement);
							}
						}
					}

				} catch (Exception e) {
					// System.out.println("picturehashtags insert exception: "
					// + preQueryStatement);
//					 e.printStackTrace();
				}

			}
			// insert sentences

			LinkedHashMap<String, Sentence> sentences = oo.getSentences();
			for (Sentence ent : sentences.values()) {
				for (String s : ent.sources) {
					try {
						Timestamp timestamp = new Timestamp(
								ent.timestamp.getTime());
						if (timestamp.toString() == null
								|| timestamp.toString().equals("null")) {
							preQueryStatement = "INSERT IGNORE INTO sentences(userid, pictureurl, text)  VALUES(\""
									+ userid
									+ "\",\""
									+ s
									+ "\",\""
									+ ent.englishSentence + "\")";
							statement.executeUpdate(preQueryStatement);
						} else {
							preQueryStatement = "INSERT IGNORE INTO sentences(userid, pictureurl, timestamp, text)  VALUES(\""
									+ userid
									+ "\",\""
									+ s
									+ "\",\""
									+ timestamp.toString()
									+ "\",\""
									+ ent.englishSentence + "\")";
							statement.executeUpdate(preQueryStatement);
						}
					} catch (Exception e) {
//						 E.PRINTSTACKTRACE();
					}
				}
			}
			// insert picturegrams
			Vector<Centroid> centroids2 = oo.getSinglePhotoOutput();
			for (Centroid c : centroids2) {
				preQueryStatement = "";
				for (String s : c.getSource()) {
					try {
						if (s != null) {
							if (c.timestamp != null) {
								Timestamp timestamp = new Timestamp(
										c.timestamp.getTime());

								if (!(timestamp.toString() == null)
										&& !timestamp.toString().equals("null")) {
									String text = c.getTag().replace("\"", "");
									preQueryStatement = "INSERT IGNORE INTO picturegrams(pictureurl, text, timestamp, tfidf, ngramsize)  VALUES(\""
											+ s
											+ "\",\""
											+ text
											+ "\",\""
											+ timestamp.toString()
											+ "\",\""
											+ c.getTfidf()
											+ "\",\""
											+ c.ngramsize + "\")";
									statement.executeUpdate(preQueryStatement);
								}
							} else {
								preQueryStatement = "INSERT IGNORE INTO picturegrams(pictureurl, text, tfidf, ngramsize)  VALUES(\""
										+ s
										+ "\",\""
										+ c.getTag()
										+ "\",\""
										+ c.getTfidf()
										+ "\",\""
										+ c.ngramsize
										+ "\")";
								statement.executeUpdate(preQueryStatement);

							}
						}

					} catch (Exception e) {
						// System.out.println("picturegrams insert exception: "
						// + preQueryStatement);
//						 e.printStackTrace();
					}
				}
			}
			// insert raw text
			for (MediaInformation mi : tco.photoInformation) {
				try {
					Timestamp timestamp = null;
					if(mi.date!=null)
						timestamp=new Timestamp(mi.date.getTime());
					else 
						timestamp = new Timestamp(new Date().getTime());

					String text = mi.description.replace("\"", "'");
					text = text.replace("“", "'");
					text=text.replaceAll("[^\\x00-\\x7F]", "");
					if (timestamp.toString() == null
							|| timestamp.toString().equals("null")) {
						preQueryStatement = "INSERT  INTO raw(userid, description)  VALUES(\""
								+ userid + "\",\"" + text + "\")";
						statement.executeUpdate(preQueryStatement);
					} else {
						preQueryStatement = "INSERT IGNORE INTO raw(userid, description, timestamp, photouid, serviceid, pictureurl)  VALUES(\""
								+ userid
								+ "\",\""
								+ text
								+ "\",\""
								+ timestamp.toString()
								+ "\",\""
								+ mi.photoUID
								+ "\",\"" + mi.serviceId + "\", \""+mi.source+"\")";
						statement.executeUpdate(preQueryStatement);
					}
				} catch (Exception e) {
					 System.out.println("raw insert exception: "
					 + preQueryStatement);
					 System.out.println(e);
					// e.printStackTrace();
				}
			}
		}
	}

	@Override
	public TwitterPicture getTwitterPictureInformation(String url) {
		TwitterPicture tp = new TwitterPicture();
		tp.pictureurl = url;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		Connection connection = null;

		try {
			connection = DriverManager.getConnection(Keys.MYSQL_TWITTER_DB,
					Keys.MYSQL_UN, Keys.MYSQL_PW);

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		if (connection != null) {
			try {
				statement = connection.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			// unigram
			String query = null;
			try {
				query = "select * from twitter.picturegrams where pictureurl ='"
						+ url + "' and ngramsize=1 order by tfidf desc";
				resultSet = statement.executeQuery(query);
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
			}
			try {
				while (resultSet.next()) {
					Centroid cent = new Centroid();
					cent.ngramsize = 1;
					cent.timestamp = resultSet.getDate("timestamp");
					cent.setTag(resultSet.getString("text"));
					cent.setTfidf(resultSet.getDouble("tfidf"));
					tp.unigrams.add(cent);
				}
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
//				e.printStackTrace();

			}
			// twogram
			try {
				query = "select * from twitter.picturegrams where pictureurl ='"
						+ url + "' and ngramsize=2 order by tfidf desc";
				resultSet = statement.executeQuery(query);
			} catch (SQLException e) {

//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				while (resultSet.next()) {
					Centroid cent = new Centroid();
					cent.ngramsize = 2;
					cent.timestamp = resultSet.getDate("timestamp");
					cent.setTag(resultSet.getString("text"));
					cent.setTfidf(resultSet.getDouble("tfidf"));
					tp.unigrams.add(cent);
				}
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			// trigram
			try {
				query = "select * from twitter.picturegrams where pictureurl ='"
						+ url + "' and ngramsize=3 order by tfidf desc";
				resultSet = statement.executeQuery(query);
			} catch (SQLException e) {

//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				while (resultSet.next()) {
					Centroid cent = new Centroid();
					cent.ngramsize = 3;
					cent.timestamp = resultSet.getDate("timestamp");
					cent.setTag(resultSet.getString("text"));
					cent.setTfidf(resultSet.getDouble("tfidf"));
					tp.unigrams.add(cent);
				}
			} catch (SQLException e) {
//				System.out.println("failed: " + query + " url:" + url);
//				e.printStackTrace();
			}

			try {
				query = "select * from twitter.picturenamedentities where pictureurl ='"
						+ url + "' order by frequency,netext";
				resultSet = statement.executeQuery(query);

			} catch (SQLException e) {

//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				while (resultSet.next()) {
					NamedEntity ne = new NamedEntity();
					ne.frequency = resultSet.getInt("frequency");
					ne.namedEntity = resultSet.getString("netext");
					ne.timestamp = resultSet.getDate("timestamp");
					tp.ne.add(ne);
				}
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				query = "select * from twitter.sentences where pictureurl ='"
						+ url + "' order by text";
				resultSet = statement.executeQuery(query);

			} catch (SQLException e) {

//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				while (resultSet.next()) {
					Sentence se = new Sentence();
					se.englishSentence = resultSet.getString("text");
					se.timestamp = resultSet.getDate("timestamp");
				}
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}

		} else {
			TextAnalyzer.logger
					.error("Failed to create mysql database connection!");

		}
		return tp;
	}

	public TwitterPicture getTwitterPictureInformation(String url,
			Connection connection) {
		TwitterPicture tp = new TwitterPicture();
		tp.pictureurl = url;
		Statement statement = null;
		ResultSet resultSet = null;
		if (connection != null) {
			try {
				statement = connection.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			// unigram
			String query = null;
			try {
				query = "select * from twitter.picturegrams where pictureurl ='"
						+ url + "' and ngramsize=1 order by tfidf desc";
				resultSet = statement.executeQuery(query);
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
			}
			try {
				while (resultSet.next()) {
					Centroid cent = new Centroid();
					cent.ngramsize = 1;
					cent.timestamp = resultSet.getDate("timestamp");
					cent.setTag(resultSet.getString("text"));
					cent.setTfidf(resultSet.getDouble("tfidf"));
					tp.unigrams.add(cent);
				}
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			// twogram
			try {
				query = "select * from twitter.picturegrams where pictureurl ='"
						+ url + "' and ngramsize=2 order by tfidf desc";
				resultSet = statement.executeQuery(query);
			} catch (SQLException e) {

//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				while (resultSet.next()) {
					Centroid cent = new Centroid();
					cent.ngramsize = 2;
					cent.timestamp = resultSet.getDate("timestamp");
					cent.setTag(resultSet.getString("text"));
					cent.setTfidf(resultSet.getDouble("tfidf"));
					tp.twograms.add(cent);
				}
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			// trigram
			try {
				query = "select * from twitter.picturegrams where pictureurl ='"
						+ url + "' and ngramsize=3 order by tfidf desc";
				resultSet = statement.executeQuery(query);
			} catch (SQLException e) {

//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				while (resultSet.next()) {
					Centroid cent = new Centroid();
					cent.ngramsize = 3;
					cent.timestamp = resultSet.getDate("timestamp");
					cent.setTag(resultSet.getString("text"));
					cent.setTfidf(resultSet.getDouble("tfidf"));
					tp.trigrams.add(cent);
				}
			} catch (SQLException e) {
//				System.out.println("failed: " + query + " url:" + url);
//				e.printStackTrace();
			}

			try {
				query = "select * from twitter.picturenamedentities where pictureurl ='"
						+ url + "' order by frequency,netext";
				resultSet = statement.executeQuery(query);

			} catch (SQLException e) {

//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				while (resultSet.next()) {
					NamedEntity ne = new NamedEntity();
					ne.frequency = resultSet.getInt("frequency");
					ne.namedEntity = resultSet.getString("netext");
					ne.timestamp = resultSet.getDate("timestamp");
					tp.ne.add(ne);
				}
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				query = "select * from twitter.sentences where pictureurl ='"
						+ url + "' order by text";
				resultSet = statement.executeQuery(query);

			} catch (SQLException e) {

//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				while (resultSet.next()) {
					Sentence se = new Sentence();
					se.englishSentence = resultSet.getString("text");
					se.timestamp = resultSet.getDate("timestamp");
					tp.sentences.add(se);
				}
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				query = "select * from twitter.picturehashtags where pictureurl ='"
						+ url + "' order by frequency desc";
				resultSet = statement.executeQuery(query);

			} catch (SQLException e) {

//				System.out.println("failed: " + query);
//				e.printStackTrace();
			}
			try {
				while (resultSet.next()) {
					String tagtext = resultSet.getString("tagtext");
					if (tp.hashtags.containsKey(tagtext)) {
						HashTag ht = tp.hashtags.get(tagtext);
						ht.sources.add(resultSet.getString("pictureurl"));
						tp.hashtags.put(ht.tag, ht);
					} else {
						HashTag ht = new HashTag();
						ht.tag = resultSet.getString("tagtext");
						ht.sources.add(resultSet.getString("pictureurl"));
						ht.frequency = resultSet.getInt("frequency");
						ht.timestamp = resultSet.getDate("timestamp");
						tp.hashtags.put(ht.tag, ht);
					}
				}
			} catch (SQLException e) {
//				System.out.println("failed2: " + query);
//				e.printStackTrace();
			}
			try {
				query = "select * from twitter.raw where pictureurl ='" + url
						+ "'";
				resultSet = statement.executeQuery(query);

			} catch (SQLException e) {

//				System.out.println("failed3: " + query);
//				e.printStackTrace();
			}
			try {
				while (resultSet.next()) {
					String tweettext = resultSet.getString("description");
					tp.tweetText = tweettext;
					tp.photoUID = resultSet.getString("photouid");
					tp.serviceID = resultSet.getString("serviceid");
					tp.timestamp = resultSet.getDate("timestamp");
				}
			} catch (SQLException e) {
//				System.out.println("failed3: " + query);
//				e.printStackTrace();
			}
			try {
				query = "select * from twitter.picsomtags where pictureurl ='"
						+ url + "'";
				resultSet = statement.executeQuery(query);

			} catch (SQLException e) {
//				System.out.println("failed4: " + query);
//				e.printStackTrace();
			}
			try {
				while (resultSet.next()) {
					String tag = resultSet.getString("tag");
					tp.backendid = resultSet.getString("backendid");
					tp.photoUID = resultSet.getString("photouid");
					tp.visualobjectid = resultSet.getString("visualobjectid");
					tp.picsomTags.add(tag);
				}
			} catch (SQLException e) {
//				System.out.println("failed5: " + query);
//				e.printStackTrace();
			}
		} else {
			TextAnalyzer.logger
					.error("Failed to create mysql database connection!");
		}
		return tp;
	}

	@Override
	public TwitterProfile getTwitterProfileInformation(String userid) {
		Statement statement = null;
		ResultSet resultSet = null;
		TwitterProfile tp = new TwitterProfile();
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
		e.printStackTrace();
			return null;
		}
		Connection connection = null;
		Connection connection2 = null;

		try {
			connection = DriverManager.getConnection(Keys.MYSQL_TWITTER_DB,
					Keys.MYSQL_UN, Keys.MYSQL_PW);
			connection2 = DriverManager.getConnection(Keys.MYSQL_TWITTER_DB,
					Keys.MYSQL_UN, Keys.MYSQL_PW);

		} catch (SQLException e) {
//			e.printStackTrace();
			return null;
		}

		if (connection != null) {
			try {
				statement = connection.createStatement();
			} catch (SQLException e) {
//				e.printStackTrace();
			}
			String query = null;
			try {
				query = "select * from twitter.userpictures where userid ='"
						+ userid + "'";
				resultSet = statement.executeQuery(query);
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
			}
			try {
				while (resultSet.next()) {
					String picture = resultSet.getString("pictureurl");
					TwitterPicture temp = getTwitterPictureInformation(picture,
							connection2);
					temp.pictureurl = picture;
					Date date = resultSet.getDate("timestamp");
					temp.timestamp = date;
					tp.pictures.add(temp);
				}
			} catch (SQLException e) {
//				System.out.println("failed2: " + query);
			}

			try {
				query = "select * from twitter.raw where userid ='" + userid
						+ "'";
				resultSet = statement.executeQuery(query);

			} catch (SQLException e) {

//				System.out.println("failed: " + query);
			}
			try {
				while (resultSet.next()) {
					Centroid cent = new Centroid();
					String desc = resultSet.getString("description");
					Date timestamp = resultSet.getDate("timestamp");
					cent.setTag(desc);
					cent.timestamp = timestamp;
					tp.description.add(cent);
				}
			} catch (SQLException e) {
//				System.out.println("failed2: " + query);
			}

		} else {
			System.out.println("failed to create connection");
		}
		try {
			connection.close();
			connection2.close();
		} catch (SQLException e) {
//			 e.printStackTrace();
		}
		return tp;
	}

	public User createConnection(String userid) {
		User user = new User();

		return user;
	}

	@Override
	public boolean addPicsomTag(String url, String tag, String photouid,
			String backendid, String visualobjectid) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			TextAnalyzer.logger.error("Picsom insert exception 1: "
					+ e.getMessage());
		}
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(Keys.MYSQL_TWITTER_DB,
					Keys.MYSQL_UN, Keys.MYSQL_PW);

		} catch (SQLException e) {
			TextAnalyzer.logger.error("Picsom insert exception 2: "
					+ e.getMessage());
		}
		if (connection != null) {
			Statement statement = null;
			String preQueryStatement = null;

			try {
				statement = connection.createStatement();
			} catch (SQLException e1) {
//				e1.printStackTrace();
			}
			// insert pictures
			try {
				preQueryStatement = "INSERT  INTO  pictures(pictureurl)  VALUES(\""
						+ url + "\")";
				statement.executeUpdate(preQueryStatement);
			} catch (Exception e) {
				// System.out.println(preQueryStatement);
				// System.out.println("error inserting picture " + url);
				// e.printStackTrace();
			}
			// insert picsom tags
			try {
				preQueryStatement = "INSERT  INTO  picsomtags(pictureurl, tag, photouid, backendid, visualobjectid)  VALUES(\""
						+ url
						+ "\",\""
						+ tag
						+ "\",\""
						+ photouid
						+ "\",\""
						+ backendid + "\",\"" + visualobjectid + "\")";
				statement.executeUpdate(preQueryStatement);
			} catch (Exception e) {
				// System.out.println(preQueryStatement);
				// System.out.println("error inserting picsom tag " + tag);
				// e.printStackTrace();
			}
			try {
				connection.close();
				return true;
			} catch (SQLException e) {
			}
		}
		return false;
	}

	@Override
	public boolean addPicsomTag(String tag, String photouid, String backendid,
			String visualobjectid) {
		String url = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			TextAnalyzer.logger.error("Picsom insert exception 1: "
					+ e.getMessage());
		}
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(Keys.MYSQL_TWITTER_DB,
					Keys.MYSQL_UN, Keys.MYSQL_PW);

		} catch (SQLException e) {
			TextAnalyzer.logger.error("Picsom insert exception 2: "
					+ e.getMessage());
		}
		if (connection != null) {
			Statement statement = null;
			String preQueryStatement = null;

			String query = null;
			ResultSet resultSet = null;

			try {
				statement = connection.createStatement();
			} catch (SQLException e1) {
//				e1.printStackTrace();
			}
			// get url from photoUID
			try {
				query = "select pictureurl from twitter.raw where photouid ='"
						+ photouid + "' limit 1";
				resultSet = statement.executeQuery(query);
			} catch (SQLException e) {
//				System.out.println("failed: " + query);
			}
			try {
				while (resultSet.next()) {
					url = resultSet.getString("pictureurl");

				}
			} catch (SQLException e) {
//				System.out.println("failed2: " + query);
			}

			// insert pictures
			if (url != null) {
				try {
					preQueryStatement = "INSERT  INTO  pictures(pictureurl)  VALUES(\""
							+ url + "\")";
					statement.executeUpdate(preQueryStatement);
				} catch (Exception e) {
					// System.out.println(preQueryStatement);
					// System.out.println("error inserting picture " + url);
					// e.printStackTrace();
				}
				// insert picsom tags
				try {
					preQueryStatement = "INSERT  INTO  picsomtags(pictureurl, tag, photouid, backendid, visualobjectid)  VALUES(\""
							+ url
							+ "\",\""
							+ tag
							+ "\",\""
							+ photouid
							+ "\",\""
							+ backendid
							+ "\",\""
							+ visualobjectid
							+ "\")";
					statement.executeUpdate(preQueryStatement);
				} catch (Exception e) {
					// System.out.println(preQueryStatement);
					// System.out.println("error inserting picsom tag " + tag);
					// e.printStackTrace();
				}
				try {
					connection.close();
					return true;
				} catch (SQLException e) {
				}
			}
		}
		return false;
	}
}
