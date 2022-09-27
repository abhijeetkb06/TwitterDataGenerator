package main.java.twitter.singlethread.data.streaming;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.couchbase.client.core.env.PasswordAuthenticator;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.manager.collection.CollectionManager;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class DMGetTweets{

	public static void main(String[] args) throws Exception {

		//Twitter4j Setup
		ConfigurationBuilder cb = new ConfigurationBuilder();
	   	cb.setDebugEnabled(true)
	   	  .setOAuthConsumerKey("svTiWgMweh9UEVYCAzX6qkLOF")
	   	  .setOAuthConsumerSecret("LaELGSQl6w96HWSUeGxRRyQchzG7A3ku7NL1VJDyN5kAql9oUS")
	   	  .setOAuthAccessToken("3427440317-2iXpeFcyaAa6xzmqS55OEdpwzikrNaaoKZTXUZL")
	   	  .setOAuthAccessTokenSecret("oZgl6OJNt6mpWH2lXjlcv7kpj00ul1bWmkCNP9ajBTbUE")
	   	  .setTweetModeExtended(true);
	   	TwitterFactory tf 		= new TwitterFactory(cb.build());
	   	Twitter twitter 		= tf.getInstance();

	   	//Couchbase Setup
//		PasswordAuthenticator authenticator = PasswordAuthenticator.builder().username("jairam").password("!oSJt2Q&]m4&").onlyEnablePlainSaslMechanism().build();
//		Cluster cluster 		= Cluster.connect("ec2-54-88-48-107.compute-1.amazonaws.com", ClusterOptions.clusterOptions(authenticator)); 
//		Bucket bucket 			= cluster.bucket("Demo");
//		Scope scope 			= bucket.scope("CDW");
//		Collection collCB 	= scope.collection("twitter-data");
//		Collection collDM 	= scope.collection("twitter-data");
			   	
	   	Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
	   	final Bucket bucket = cluster.bucket("Demo");
		Scope scope 			= bucket.scope("CDW");
		Collection collCB 	= scope.collection("twitter-data");

		Query queryCB = new Query("Couchbase");
//		Query queryDM = new Query("Dataminr");
		
		Date untilDate = new GregorianCalendar(2021, 4, 27).getTime();
		Date today = Calendar.getInstance().getTime();

		while (!today.equals(untilDate)) {
//			queryCB.sinceId(getBiggestId(cluster, "json_cbtweets"));
//			queryDM.sinceId(getBiggestId(cluster, "json_dmtweets"));

		  	QueryResult resultsCB = twitter.search(queryCB);
//		  	QueryResult resultsDM = twitter.search(queryDM);

	  	    for (Status status : resultsCB.getTweets()) {
	  	    	writeToCouchbase(status, collCB);
	  	    }
//	  	    for (Status status : resultsDM.getTweets()) {
//	  	    	writeToCouchbase(status, collDM);
//	  	    }
	  	    today = Calendar.getInstance().getTime();
	  	    System.out.println(today.toString());
		  	Thread.sleep(300000);
		}
		cluster.disconnect();
		System.out.println("done");
	}
	
	public static void writeToCouchbase(Status status, Collection collection) throws Exception {
		JsonObject doc1 = JsonObject.create();
		doc1.put("lang", status.getLang());
		doc1.put("tweet", status.getText());
		doc1.put("time", status.getCreatedAt().toInstant().toString());
		doc1.put("user", status.getUser().getName());
		doc1.put("rt", status.isRetweet());

		if (status.isRetweet())
			doc1.put("rt_t", status.getRetweetedStatus().getText());
		MutationResult upsertResult = collection.upsert(String.valueOf(status.getId()), doc1);
	}
	
	public static long getBiggestId(Cluster cluster, String scollection) throws Exception {
		long biggestId = 0;
		String sQuery = "SELECT META().id as id FROM Dataminr.Twitter." + scollection + " ORDER BY id DESC LIMIT 1";
		com.couchbase.client.java.query.QueryResult result = cluster.query(sQuery);
		for (JsonObject row : result.rowsAsObject()) 
		{
		  String sId = row.getString("id");
		  biggestId = Long.parseLong(sId);
		}
		return biggestId;
	}

}