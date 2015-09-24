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
package analyzer.content;

import java.util.Date;
import java.util.Vector;

import analyzer.content.socialmedia.EventInformation;
import analyzer.content.socialmedia.GroupInformation;
import analyzer.content.socialmedia.LikeInformation;
import analyzer.content.socialmedia.MediaInformation;
import analyzer.content.socialmedia.RelationshipInformation;
import analyzer.content.socialmedia.StatusMessage;

/**
 * Class that implements contentinterface for facebook
 * 
 * @author forsstho
 * 
 */
public class FacebookContentObject implements ContentInterface {
	public String bio;

	public enum Gender {
		MALE, FEMALE, OTHER;
	}

	public Gender gender;
	public String firstName;
	public String lastName;
	public String middleName;
	public String homeTown;
	public String locationName;
	public Date birthDay;
	public String relationsship;
	public String politicalView;
	public String religion;
	public Date date;
	private String userID;
	public Vector<RelationshipInformation> relationships = new Vector<RelationshipInformation>();

	public Vector<MediaInformation> videoInformation;
	public Vector<MediaInformation> photoInformation;
	public Vector<LikeInformation> likeInformation;
	public Vector<StatusMessage> statusMessageInformation;
	public Vector<GroupInformation> groupInformation;
	public Vector<EventInformation> eventInformation;

	public FacebookContentObject() {

		videoInformation = new Vector<MediaInformation>();
		photoInformation = new Vector<MediaInformation>();
		likeInformation = new Vector<LikeInformation>();
		statusMessageInformation = new Vector<StatusMessage>();
		groupInformation = new Vector<GroupInformation>();
		eventInformation = new Vector<EventInformation>();
	}

	public FacebookContentObject(Vector<MediaInformation> videoDescriptions,
			Vector<MediaInformation> photoDescriptions,
			Vector<LikeInformation> likeInformation,
			Vector<StatusMessage> statusMessageInformation,
			Vector<GroupInformation> groupInformation,
			Vector<EventInformation> eventInformation) {
		this.videoInformation = videoDescriptions;
		this.photoInformation = photoDescriptions;
		this.likeInformation = likeInformation;
		this.statusMessageInformation = statusMessageInformation;
		this.groupInformation = groupInformation;
		this.eventInformation = eventInformation;
	}

	@Override
	public String toString() {
		return gender + " " + firstName + " " + lastName + " " + middleName
				+ " " + homeTown + " " + locationName + " " + relationsship
				+ " " + politicalView + " " + religion;
	}

	public String[] getContent() {
		String text[] = new String[9];
		text[0] = gender.toString();
		text[1] = firstName;
		text[2] = lastName;
		text[3] = middleName;
		text[4] = homeTown;
		text[5] = locationName;
		text[6] = relationsship;
		text[7] = politicalView;
		text[8] = religion;
		return null;
	}

	@Override
	public String getID() {

		return userID;
	}

	@Override
	public Vector<MediaInformation> getMedia() {
		Vector<MediaInformation> mi2 = videoInformation;
		mi2.addAll(photoInformation);
		return mi2;
	}
}
