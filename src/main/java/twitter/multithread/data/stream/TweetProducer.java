package main.java.twitter.multithread.data.stream;



import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;

import static main.java.twitter.multithread.data.stream.TwitterConfiguration.apiInstance;

public class TweetProducer extends Thread {

	// Load data in queue
	private BlockingQueue<String> tweetsQueue;

	public TweetProducer(BlockingQueue<String> tweetsQueue) {
		super("TWEETS PRODUCER");
		this.tweetsQueue = tweetsQueue;
	}

	public void run() {

		while (true) {
			try {

				// Read a all public tweets
				InputStream streamResult = apiInstance.tweets().sampleStream().backfillMinutes(0)
						.tweetFields(TwitterConfiguration.getTweetfields()).execute();

				BufferedReader reader = new BufferedReader(new InputStreamReader(streamResult));
				String tweetJson = reader.readLine();

				// Loop through each tweet and add to queue
				while (tweetJson != null && !tweetJson.isEmpty()) {

					// the producer will add an element into the shared queue.
					tweetsQueue.put(tweetJson);
					System.out.println(getName() + " Tweet added to queue " + tweetJson);
					Thread.sleep(20);

					// Goto next tweet
					tweetJson = reader.readLine();
				}
				System.out.println("@@@@@@@@@ TWEET QUEUE SIZE PRODUCED @@@@@@@@ " + tweetsQueue.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
