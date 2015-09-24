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
import java.util.HashMap;
import java.util.StringTokenizer;

import analyzer.config.Config;
import analyzer.process.TextAnalyzer;

/**
 * Class for adding userfeedback to the mysql database
 * 
 * @author forsstho
 * 
 */
public class MySQLFeedback implements FeedbackInterface {
	HashMap<String, User> users = new HashMap<String, User>();
	private Statement statement = null;
	private ResultSet resultSet = null;
	User user = null;

	public MySQLFeedback(Config config) {
	}

	@Override
	public User readContent(String userid) {
		if (user == null) {
			user = createConnection(userid);
		}
		return user;
	}

	public User createConnection(String userid) {
		User user = new User();
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			TextAnalyzer.logger.error("SQL exception 1: " + e.getMessage());
			System.exit(0);
			return null;
		}
		Connection connection = null;

		try {
			connection = DriverManager.getConnection(Keys.MYSQL_DB,
					Keys.MYSQL_UN, Keys.MYSQL_PW);

		} catch (SQLException e) {
			TextAnalyzer.logger.error("SQL exception 2: " + e.getMessage());
			System.exit(0);
			return null;
		}

		if (connection != null) {
			try {
				statement = connection.createStatement();
			} catch (SQLException e) {
				TextAnalyzer.logger.error("SQL exception 3: " + e.getMessage());
				System.exit(0);
			}
			try {
				String query = "select * from D2I.userfeedback where userid ="
						+ userid + " and feedback < 0";
				resultSet = statement.executeQuery(query);
			} catch (SQLException e) {

				TextAnalyzer.logger.error("SQL exception 4: " + e.getMessage());
				System.exit(0);
			}
			try {
				while (resultSet.next()) {
					String userId = resultSet.getString("userid");
					int ngram = resultSet.getInt("ngram");
					String text = resultSet.getString("text");
					int feedback = resultSet.getInt("feedback");
					user.userId = userId;
					user.negativeWords.put(text, ngram);
					user.feedback = feedback;
				}
			} catch (SQLException e) {
				TextAnalyzer.logger.error("SQL exception 5: " + e.getMessage());
				System.exit(0);
			}
		} else {
			TextAnalyzer.logger
					.error("Failed to create mysql database connection!");
			System.exit(0);
		}
		return user;
	}

	@Override
	public boolean addFeedback(String userid, String text, int feedback) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			TextAnalyzer.logger.error("SQL feedback exception 1: "
					+ e.getMessage());
			return false;
		}
		Connection connection = null;

		try {
			connection = DriverManager.getConnection(Keys.MYSQL_DB,
					Keys.MYSQL_UN, Keys.MYSQL_PW);

		} catch (SQLException e) {
			TextAnalyzer.logger.error("SQL feedback exception 2: "
					+ e.getMessage());
			return false;
		}
		StringTokenizer st = new StringTokenizer(text);
		int ngram = 0;
		while (st.hasMoreTokens()) {
			ngram++;
			st.nextToken();
		}
		if (connection != null) {

			try {
				int hash = (userid + text).hashCode();
				String preQueryStatement = "INSERT  INTO  userfeedback  VALUES  ("
						+ userid
						+ ",'"
						+ text
						+ "',"
						+ ngram
						+ ","
						+ feedback
						+ ", null,'" + hash + "')";
				statement.executeUpdate(preQueryStatement);
			} catch (Exception e) {
				TextAnalyzer.logger
						.info("couldn't insert userfeedback (maybe already exists)");
				return false;
			}
		}
		return true;

	}
}
