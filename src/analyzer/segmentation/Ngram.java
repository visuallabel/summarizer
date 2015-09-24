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
package analyzer.segmentation;

import java.util.Date;
import java.util.HashSet;

/**
 * Class that represents one ngram object. Ngrams length is decided in the
 * config file.
 */
public class Ngram {
	public double frequency = 1;
	public String[] gram;
	public double[] tfidf;
	public boolean matchesNER = false;
	public int weight = 1;
	public double singleTFIDF;
	public HashSet<String> source = new HashSet<String>();
	public int gramsize;
	public Date timestamp;
	private String photoUID;

	public Ngram(int gramsize) {
		gram = new String[gramsize];
		tfidf = new double[gramsize];
		this.gramsize = gramsize;
	}

	@Override
	public String toString() {
		String text = gram[0];
		for (int i = 1; i < gram.length; i++) {
			text += " " + gram[i];
		}
		return text;
	}

	public double gettfidf() {
		if (singleTFIDF > 0) {
			return singleTFIDF * weight;
		}
		double value = 0;
		for (int i = 0; i < tfidf.length; i++) {
			value += tfidf[i];
		}
		return value * weight;
	}

	public void setSingleTfidf(double num) {
		singleTFIDF = num;
	}

	public double getSingleTfidf() {
		return singleTFIDF;
	}

	public String getPhotoUID() {
		return photoUID;
	}

	public void setPhotoUID(String photoUID) {
		this.photoUID = photoUID;
	}
}
