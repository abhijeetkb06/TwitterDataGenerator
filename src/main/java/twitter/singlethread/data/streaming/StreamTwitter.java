package main.java.twitter.singlethread.data.streaming;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentExistsException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.CounterResult;
import com.couchbase.client.java.kv.IncrementOptions;
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;

/**
 * Stream twitter data to couchbase.
 * 
 * @author abhijeetbehera
 */
public class StreamTwitter {
	
	// Twitter fields
	private static final String DATA = "data";
    private static final String CREATED_AT = "created_at";
	private static final String ID = "id";
	private static final String AUTHOR_ID = "author_id";
    
	//TODO: Update these variables to point to your Couchbase Capella instance and credentials.
	public final static Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
	public final static String bucketName="Demo";
	public final static String scopeName="CDW";
	public final static String collectionName="twitter-data";
	public final static String bearerToken = "AAAAAAAAAAAAAAAAAAAAAISZfQEAAAAATS8aOJLcL9v6GXQVWpSEBU0zGZE%3DEaBjbF9WzVw5Vg48IppemO5vam5WmdZ48w16ZWO5eQIyqP4CoG";
	public static final String PT10S = "PT10S";
 	
	public static void main(String[] args) {
		
		// Total tweets
		int totalTweets=0;
		
		/**
		 * Set the credentials for the twitter APIs
		 */
		TwitterApi apiInstance = new TwitterApi(new TwitterCredentialsBearer(bearerToken));

		// Set all the required tweet fields
		Set<String> tweetFields = setTwitterAttributes();

		try {
			
			// Read a all public tweets
			InputStream streamResult = apiInstance.tweets().sampleStream().backfillMinutes(0).tweetFields(tweetFields)
					.execute();

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(streamResult));
				String tweetJson = reader.readLine();
				
				// Loop through each tweet and store it as document
				while (tweetJson != null && !tweetJson.isEmpty()) {
					
					// populate couchbase database
					loadDataInCouchbase(tweetJson);
					
					// couct tweets loaded.
					totalTweets++;
					
					//Goto next tweet
					tweetJson = reader.readLine();
					
					System.out.println("$$$$$$Tweet No:"+ totalTweets);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e);
			}
		} catch (ApiException e) {
			System.err.println("Status code: " + e.getCode());
			System.err.println("Reason: " + e.getResponseBody());
			System.err.println("Response headers: " + e.getResponseHeaders());
			e.printStackTrace();
		}
		
		System.out.println("************ TWEETS PRODUCED ************** " + totalTweets);
	}

	/**
	 * Create documents in couchbase for each tweet.
	 * 
	 * @param line
	 */
	private static void loadDataInCouchbase(String line) {
		
		// Extract AUTHOR_ID value to set as document key
		twitter4j.JSONObject obj = new twitter4j.JSONObject(line);
		String id = obj.getJSONObject(DATA).getString(AUTHOR_ID);
		
		try {
			JsonObject jsonVal = JsonObject.fromJson(line);
			
			// Get a bucket reference
		    Bucket bucket = cluster.bucket(bucketName);
		    bucket.waitUntilReady(Duration.parse(PT10S)) ;
		 	Scope scope = bucket.scope(scopeName);
			Collection collCB = scope.collection(collectionName);

			CounterResult myID = collCB.binary().increment(id, IncrementOptions.incrementOptions().initial(1));
			System.out.println("************ myID.toString() ************** " + myID.toString());
			System.out.println("************ myID.cas()************** " + myID.cas());
			System.out.println("************ myID.content()************** " + myID.content());
			// Insert document in couchbase
			collCB.insert(myID.toString(), jsonVal);
			
			//Data loaded
			System.out.println(jsonVal != null ? jsonVal.toString() : "Null object");
		} catch (DocumentExistsException ex) {
			System.err.println("The document already exists!");
		} catch (CouchbaseException ex) {
			System.err.println("Something else happened: " + ex);
		}
	}

	/**
	 * Set necessary tweet fields for json.
	 * 
	 * @return
	 */
	private static Set<String> setTwitterAttributes() {
		Set<String> tweetFields = new HashSet<>();
		tweetFields.add(AUTHOR_ID);
		tweetFields.add(ID);
		tweetFields.add(CREATED_AT);
		return tweetFields;
	}
}

