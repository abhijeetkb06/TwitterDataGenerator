package main.java.twitter.multithread.data.stream;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;

import org.json.JSONException;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentExistsException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;

public class TweetConsumer extends Thread {

	// Read data to consume once data is loaded in queue
	private BlockingQueue<String> tweetsQueue;

	public TweetConsumer(BlockingQueue<String> tweetsQueue) {
		super("TWEET CONSUMER");
		this.tweetsQueue = tweetsQueue;
	}

	public void run() {
		try {
			boolean firstRun= true;
			while (true) {
				
				System.out.println("***************QUEUE SIZE************** "+tweetsQueue.size());

				// Remove the tweet from shared tweet queue and process
				String tweet = tweetsQueue.take();

				// Populate couchbase database
				loadDataInCouchbase(tweet);

				if (tweetsQueue.size() < 1 && !firstRun) {
					System.out.println("***TERMINATE***QUEUE SIZE************** "+tweetsQueue.size());
					// Stop thread execution once the tweet queue is exhausted
					Thread.currentThread().interrupt();
				}
				firstRun=false;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create documents in couchbase for each tweet.
	 * 
	 * @param tweet
	 */
	private void loadDataInCouchbase(String tweet) {

		try {
			// Extract AUTHOR_ID value to set as document key
			twitter4j.JSONObject obj = new twitter4j.JSONObject(tweet);
			String couchbaseDocID = obj.getJSONObject(TwitterConfiguration.DATA).getString(TwitterConfiguration.ID);
			JsonObject docVal = JsonObject.fromJson(tweet);

			// Get a bucket reference
			Bucket bucket = CouchbaseConfiguration.getCluster().bucket(CouchbaseConfiguration.getBucket());
			bucket.waitUntilReady(Duration.parse(CouchbaseConfiguration.PT10S));
			Scope scope = bucket.scope(CouchbaseConfiguration.getScope());
			Collection collCB = scope.collection(CouchbaseConfiguration.getCollection());

			// Insert document in couchbase
			collCB.insert(couchbaseDocID, docVal);

			System.out.println("TWEET CONSUMED \n");
			System.out.println(" Thread Name: " + Thread.currentThread().getName());
			// Data loaded
			System.out.println(docVal != null ? docVal.toString() : "Null object");
		} catch (DocumentExistsException ex) {
			System.err.println("The document already exists!");
		} catch (CouchbaseException ex) {
			System.err.println("Something else happened: " + ex);
		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
