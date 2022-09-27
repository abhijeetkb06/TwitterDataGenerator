package main.java.twitter.multithread.data.stream;

import java.util.HashSet;
import java.util.Set;

import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;

public class TwitterConfiguration {
	
	// Twitter fields
	public static final String DATA = "data";
	public static final String AUTHOR_ID = "author_id";
	public static final String CREATED_AT = "created_at";
	public static final String ID = "id";

	private static final String bearerToken = "AAAAAAAAAAAAAAAAAAAAAISZfQEAAAAATS8aOJLcL9v6GXQVWpSEBU0zGZE%3DEaBjbF9WzVw5Vg48IppemO5vam5WmdZ48w16ZWO5eQIyqP4CoG";
	
	private static final TwitterConfiguration twitterConfiguration = new TwitterConfiguration();
	
	/**
	 * Set the credentials for the twitter APIs
	 */
	public static final TwitterApi apiInstance = new TwitterApi(new TwitterCredentialsBearer(bearerToken));

	private TwitterConfiguration() {
		setTwitterAttributes();
	}

	public static TwitterConfiguration getInstance() {
		return twitterConfiguration;
	}
	
	private static Set<String> tweetFields =null;
	
	public static Set<String> getTweetfields() {
		return tweetFields;
	}

	/**
	 * Set necessary tweet fields for json.
	 * 
	 * @return
	 */
	private static final Set<String> setTwitterAttributes() {
		tweetFields = new HashSet<>();
		tweetFields.add(AUTHOR_ID);
		tweetFields.add(ID);
		tweetFields.add(CREATED_AT);
		return tweetFields;
	}
}
