package main.java.twitter.multithread.data.stream;

import com.couchbase.client.java.Cluster;

public class CouchbaseConfiguration {
	
	//TODO: Update these variables to point to your Couchbase Capella instance and credentials.
	private final static Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
	private final static String bucketName="Demo";
	private final static String scopeName="CDW";
	private final static String collectionName="twitter-data";
	
	public static final String PT10S = "PT10S";
	
    private static final CouchbaseConfiguration couchbaseInstance = new CouchbaseConfiguration(); 

    private CouchbaseConfiguration() {}

    public static CouchbaseConfiguration getInstance() {
        return couchbaseInstance;
    }
    
    public static Cluster getCluster() {
		return cluster;
	}

	public static String getBucket() {
		return bucketName;
	}

	public static String getScope() {
		return scopeName;
	}

	public static String getCollection() {
		return collectionName;
	}
}
