package main;

import main.java.twitter.multithread.data.stream.TweetConsumer;
import main.java.twitter.multithread.data.stream.TweetProducer;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This initiates the process of Twitter data load into Couchbase.
 * 
 * @author abhijeetbehera
 */
public class LaunchTwitterDataStreaming {

	public static void main(String[] args) {

		BlockingQueue<String> sharedTweetsQueue = new LinkedBlockingQueue<String>();

		// Create number of tweet producer threads
		Thread[] tweetProducer = new Thread[1];
		Arrays.stream(tweetProducer).forEach(p -> {
			p = new Thread(new TweetProducer(sharedTweetsQueue));
			p.setName("PRODUCER THREAD " + p);
			p.start();
        });

		// Create number of tweet consumer threads
		Thread[] tweetConsumer = new Thread[4];// amount of threads
		Arrays.stream(tweetConsumer).forEach(c -> {
			c = new Thread(new TweetConsumer(sharedTweetsQueue));
			c.setName("CONSUMER THREAD " + c);
			c.start();
        });
	}
}
