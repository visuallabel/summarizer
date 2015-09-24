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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import analyzer.config.Config;

/**
 * Class for parsing the command line options using Apache CLI. We have quite a
 * lot of options, this should be refactored so it is up-to-date
 * 
 * @author
 * 
 */
public class CommandLineOptionsParser {
	Options options;
	Config config;

	public CommandLineOptionsParser(String[] args, Config config) {
		// create Options object
		this.config = config;
		options = new Options();
		// TODO: write long help message
		options.addOption("help", false, "shows help message");
		options.addOption(
				"config",
				true,
				"path to the config file, config file will be created during first run if not available");
		options.addOption("file", true, "path to the file that will be parsed");
		options.addOption("summary", false, "change from centroids to summary");
		options.addOption("dateLimit", true,
				"limit parsing so that only information after the date is used (yyyy/mm/dd)");
		options.addOption("combine", true,
				"Which type of combine should be used? Same as was used in mead or simple?");
		options.addOption(
				"limitcounter",
				true,
				"limit parsing so that only posts with equal or greater amount of likes or shares are included");
		options.addOption("stopwords", true,
				"path to stopwords that will have significance 0");
		options.addOption("contentType", true,
				"type of file to parse: facebook or plain text");
		options.addOption("idfDictionary", true, "path to the idf dictionary");
		options.addOption("NER", true,
				"path to the Stanford compatible Named Entity Recognition classifier");
		options.addOption("ngram", true,
				"number of ngrams to match for (must be 2 or greater to work)");
		options.addOption("output", true, "path and name of output file");
		options.addOption("cutoff", true,
				"absolute or linear cutoff for datelimit");
		options.addOption("stemming", true,
				"reduce all words to their base form yes/no");
		options.addOption(
				"ignoreCase",
				false,
				"ignore word casing, ignore words that don't exist. Without this words that dont exist get a inverse document frequency of 0.1 centroids are case sensitive.");
		parse(args);
	}

	public void parse(String[] args) {
		// TODO: proof this file for wrong input
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("help")) {
				printHelpInfo();
				System.exit(0);
			}
			if (cmd.hasOption("-summary")) {
				TextAnalyzer.logger.info("Analysis done by summary.");
				config.prop.put("summary", "yes");
			} else {
				TextAnalyzer.logger.info("Analysis done by centroids.");
				config.prop.put("summary", "no");
			}
			if (cmd.hasOption("config")) {
				String text = cmd.getOptionValue("config");
				if (text != null && text != "") {
					TextAnalyzer.logger.info("Config file found: " + text);
					config.loadConfig(text);
				} else {
					System.out
							.println("Config file argument missing, try -config path or -help");
				}
				if (cmd.hasOption("file")) {
					text = cmd.getOptionValue("file");
					if (text != null) {
						TextAnalyzer.logger.info("Parsing file found: " + text);
						config.prop.setProperty("file", text);
					} else {
						TextAnalyzer.logger
								.info("No file argument found, please try -file path or -help");
						System.out
								.println("No file argument found, please try -file path or -help");
					}
				} else {
					if (!config.prop.containsKey("file")) {
						TextAnalyzer.logger
								.info("Missing file argument, you must add a file to parse! (-file path)");
						System.out
								.println("Missing file argument, you must add a file to parse! (-file path)");
						System.exit(0);
					}
				}
				if (cmd.hasOption("dateLimit")) {
					System.out.println("date limit found...");
					text = cmd.getOptionValue("dateLimit");
					if (text != null) {
						TextAnalyzer.logger.info("Analysis limited by date.");
						try {
							config.prop.put("dateLimit", text);
						} catch (Exception e) {
							TextAnalyzer.logger
									.error("Wrong date format entered, try -limitd yyyy-mm-dd or -help");
							// System.out
							// .println("Wrong date format entered, try -limitd yyyy-mm-dd or -help");
							System.exit(0);
							// e.printStackTrace();
						}
					} else {

					}
				} else {
				}
				if (cmd.hasOption("limitcounter")) {
					text = cmd.getOptionValue("limitcounter");
					if (text != null) {
						try {
							TextAnalyzer.logger
									.info("Analysis limited by likes.");
							config.prop.put("counterLimit",
									Integer.parseInt(text));
						} catch (Exception e) {
							TextAnalyzer.logger
									.error("Limitcounter a valid number, try -limit number or -help");
							// System.out
							// .println("Limitcounter not a valid number, try -limit number or -help");
							System.exit(0);
						}
					} else {
						TextAnalyzer.logger
								.error("No count entered, try -limitl number or -help");
						// System.out
						// .println("No count entered, try -limitl number or -help");
					}
				}
				if (cmd.hasOption("contentType")) {
					text = cmd.getOptionValue("contentType");
					try {
						// System.out.println("Processing type: " + text);
						config.prop.put("contentType", text);
					} catch (Exception e) {
						TextAnalyzer.logger
								.error("Contenttype problem. Exiting.");
						System.out.println("Contenttype problem. Exiting.");
						System.exit(0);
					}
				}
				// if (cmd.hasOption("extractSize")) {
				// text = cmd.getOptionValue("extractSize");
				// try {
				// // System.out.println("Processing type: " + text);
				// int num = Integer.parseInt(text);
				// if (num > 0) {
				// config.prop.put("contentType", num);
				// }
				// } catch (Exception e) {
				// TextAnalyzer.logger
				// .error("ExtractSize problem, must be set to integer (1 or larger). Exiting");
				// // System.out
				// //
				// .println("ExtractSize problem, must be set to integer (1 or larger). Exiting");
				// System.exit(0);
				// }
				// }

				if (cmd.hasOption("ngram")) {
					text = cmd.getOptionValue("ngram");
					try {
						config.prop.put("ngram", text);
					} catch (Exception e) {
						TextAnalyzer.logger
								.error("Ngram parsing problem. Exiting.");
						System.out.println("Ngram parsing problem. Exiting.");
						System.exit(0);
					}
				}
				if (cmd.hasOption("combine")) {
					text = cmd.getOptionValue("combine");
					try {
						config.prop.put("combine", text);
					} catch (Exception e) {
						TextAnalyzer.logger
								.error("Combine parsing failed. Exiting.");
						System.out.println("Combine parsing failed. Exiting.");
						System.exit(0);
					}
				}
				if (cmd.hasOption("output")) {
					text = cmd.getOptionValue("output");
					try {
						config.prop.put("output", text);
					} catch (Exception e) {
						TextAnalyzer.logger.error("Wrong output path or name");
						System.out.println("Wrong output path or name");
						System.exit(0);
					}
				}
				if (cmd.hasOption("idfDictionary")) {
					text = cmd.getOptionValue("idfDictionary");
					try {
						// System.out.println("dictionary location: " + text);
						config.prop.put("idfdictionary", text);
					} catch (Exception e) {
						TextAnalyzer.logger
								.error("Dictionary path problem. Exiting.");
						// System.out.println("Dictionary path problem. Exiting.");
						System.exit(0);
					}
				}
				if (cmd.hasOption("cutoff")) {
					System.out.println("cutoff found");
					text = cmd.getOptionValue("cutoff");
					try {
						config.prop.put("cutoff", text);
					} catch (Exception e) {
						TextAnalyzer.logger
								.error("Cutoff parsing problem. Use absolute or linear.");
						// System.out
						// .println("cutoff problem. Use absolute or linear. Exiting.");
						System.exit(0);
					}
				}
				if (cmd.hasOption("stopwords")) {
					text = cmd.getOptionValue("stopwords");
					try {
						// System.out.println("stopwords path: " + text);
						config.prop.put("stopwords", text);
					} catch (Exception e) {
						TextAnalyzer.logger.error("Stopword parsing problem.");
						// System.out
						// .println("Stopword parsing problem. Exiting.");
						System.exit(0);
					}
				}
				if (cmd.hasOption("NER")) {
					text = cmd.getOptionValue("NER");
					config.prop.put("NER", text);
				}
				if (cmd.hasOption("stemming")) {
					text = cmd.getOptionValue("stemming");
					config.prop.put("stemming", text);
				}

				if (cmd.hasOption("ignoreCase")) {
					config.prop.put("ignoreCase", "yes");
				}
			} else {
				TextAnalyzer.logger
						.error("Missing config file argument, creating new config file.");
				// System.out
				// .println("Missing config file argument, creating new config file.");
				config.createNewConfig();
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void printHelpInfo() {
		// TODO: help!
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"java -jar -summary -file /home/user/test.xml -config /home/user/config.txt -limitd  2013/09/16 -limitl 5",
						options);
		System.out.println("");
	}

}
