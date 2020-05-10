package ratelimiting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.time.Instant;
import java.util.*;

/*
	Inspired by - https://medium.com/@saisandeepmopuri/system-design-rate-limiter-and-data-modelling-9304b0d18250

	Data-Structure for metadata (map)
	--------
	"userid_metadata": {
		"requests": 2,
	    "window_length": 30
	}

	Data-Structure for counts (map)
	----------
	"userid_counts": {
	    "bucket1": 2,
	    "bucket2": 3
	}
 */

public class SlidingWindowCounterRateLimiter {
    static Logger logger = LoggerFactory.getLogger(SlidingWindowCounterRateLimiter.class.getName());

    public static void main(String[] args) throws InterruptedException {
        SlidingWindowCounter rateLimiter = new SlidingWindowCounter(logger, 1);

        String userId = "1";
        rateLimiter.addUser(userId, 10, 3);

//		10 per window
//		   bucket size = 5,
//			   hence 2 buckets;
        for (int i = 0; i < 50; i++) {
            Thread.sleep(300);
            boolean shouldAllowRequest = rateLimiter.shouldAllowRequest(userId);
            String out = shouldAllowRequest ? "True" : "False";
            logger.info("shouldAllowRequest: " + out);
        }

        rateLimiter.removeUser(userId);
        rateLimiter.terminate();
    }
}

class SlidingWindowCounter {
    private Jedis jedis;
    private Logger logger;
    private String REQUESTS = "requests";
    private String WINDOW_LENGTH = "window_length";
    private String METADATA_SUFFIX = "_metadata";
    private String COUNTS = "_counts";
    private int bucketSize;

    SlidingWindowCounter(Logger logger, int bucketSize) {
        jedis = new Jedis("localhost");
        this.logger = logger;
        this.bucketSize = bucketSize;
        logger.info("Connection to server successfully: " + jedis.ping());
    }

    void terminate() {
        jedis.close();
    }

    // Adds a new user's rate of requests to be allowed
    void addUser(String userId, int requests, int windowSize) {
        Map<String, String> map = new HashMap<>();
        map.put(REQUESTS, String.valueOf(requests));
        map.put(WINDOW_LENGTH, String.valueOf(windowSize));
        jedis.hmset(userId + METADATA_SUFFIX, map);
    }

    // Removes a user's metadata and timestamps
    void removeUser(String userId) {
        jedis.del(userId + METADATA_SUFFIX, userId + COUNTS);
    }

    boolean shouldAllowRequest(String userId) {
        // Step 1: get the user metadata storing the number of requests per window time
        Map<String, String> userMetaData = jedis.hgetAll(userId + METADATA_SUFFIX);
        if (userMetaData == null) {
            logger.error("invalid user !!");
            return false;
        }
        int maxRequests = Integer.parseInt(userMetaData.get(REQUESTS));
        int windowLength = Integer.parseInt(userMetaData.get(WINDOW_LENGTH));

        Instant now = Instant.now();
        long currentTimestamp = now.toEpochMilli();
        long oldestPossibleEntry = now.minusSeconds(windowLength).toEpochMilli();
        logger.info("currentTimestamp: " + currentTimestamp);
        logger.info("oldestPossibleEntry: " + oldestPossibleEntry);

        // Step 2: evict older entries
        String[] bucketsToBeDeleted = jedis.hkeys(userId + COUNTS).stream()
            .filter(bucket -> Long.parseLong(bucket) < oldestPossibleEntry)
            .toArray(String[]::new);

        logger.info("bucketsToBeDeleted: " + Arrays.asList(bucketsToBeDeleted));

        if (bucketsToBeDeleted.length > 0) {
            jedis.hdel(userId + COUNTS, bucketsToBeDeleted);
        }

        // transaction holds an optimistic lock over the redis entries
        // userId + self.METADATA_SUFFIX, userId + self.COUNTS.
        // The changes in incrementCount are committed only
        // if none of these entries get changed.

        // Step 3: add current timestamp
        String currentBucket = getBucket(currentTimestamp, windowLength);
        int currentRequestCount = incrementCount(userId, currentBucket);

        logger.info(currentRequestCount + " : " + maxRequests);

        print(userId);

        return currentRequestCount <= maxRequests;
    }

    // Atomically increments hash key val by unit and returns. Uses optimistic locking
    // over userId + self.COUNTS redis key.
    private int incrementCount(String userId, String bucket) {
        Pipeline p = jedis.pipelined();
        p.multi();
        p.hincrBy(userId + COUNTS, bucket, 1);
        Response<List<String>> out = p.hvals(userId + COUNTS);
        p.exec();
        p.sync();
//        jedis.wait();

        return out.get().stream()
            .map(Integer::parseInt)
            .reduce(0, Integer::sum);
    }

    private void print(String userId) {
        Map<String, String> counts = jedis.hgetAll(userId + COUNTS);
        logger.info("DEBUG: " + counts);
    }

    private String getBucket(long timestamp, int windowLength) {
        int factor = windowLength / bucketSize;
        long f = (timestamp / factor) * factor;
        return String.valueOf(f);
    }
}