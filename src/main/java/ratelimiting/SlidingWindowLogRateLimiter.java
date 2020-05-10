package ratelimiting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
	Inspired by - https://medium.com/@saisandeepmopuri/system-design-rate-limiter-and-data-modelling-9304b0d18250

	Data-Structure for metadata (map)
	--------
	"userid_metadata": {
		"requests": 2,
	    "window_time": 30
	}

	Data-Structure for timestamps (sorted_set)
	----------
	"userid_timestamps": sorted_set([
	    "ts1": "ts1",
	    "ts2": "ts2"
	])
 */

public class SlidingWindowLogRateLimiter {
    static Logger logger = LoggerFactory.getLogger(SlidingWindowLogRateLimiter.class.getName());

    public static void main(String[] args) {
        SlidingWindowLog rateLimiter = new SlidingWindowLog(logger);

        String userId = "1";
        rateLimiter.addUser(userId, 2, 1);

        for (int i = 0; i < 10; i++) {
            boolean shouldAllowRequest = rateLimiter.shouldAllowRequest(userId);
            String out = shouldAllowRequest ? "True" : "False";
            logger.info("shouldAllowRequest: " + out);
        }

        rateLimiter.removeUser(userId);

        rateLimiter.terminate();
    }
}

class SlidingWindowLog {
    Jedis jedis;
    Logger logger;
    private String REQUESTS = "requests";
    private String WINDOW_LENGTH = "window_time";
    private String METADATA_SUFFIX = "_metadata";
    private String TIMESTAMPS = "_timestamps";

    SlidingWindowLog(Logger l) {
        jedis = new Jedis("localhost");
        logger = l;
        logger.info("Connection to server successfully: " + jedis.ping());
    }

    void terminate() {
        jedis.close();
    }

    // Adds a new user's rate of requests to be allowed
    void addUser(String userId, int requests, int windowLengthInSec) {
        Map<String, String> map = new HashMap<>();
        map.put(REQUESTS, String.valueOf(requests));
        map.put(WINDOW_LENGTH, String.valueOf(windowLengthInSec));
        jedis.hmset(userId + METADATA_SUFFIX, map);
    }

    // Removes a user's metadata and timestamps
    void removeUser(String userId) {
        jedis.del(userId + METADATA_SUFFIX, userId + TIMESTAMPS);
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

        // Step 2: remove all the timestamps from the previous bucket
        Long deleted = jedis.zremrangeByScore(userId + TIMESTAMPS, 0, oldestPossibleEntry);
        logger.info("deleted: " + deleted);

        // Step 3: add current timestamp
        int currentRequestCount = addTimeStampAndReturnSize(userId, currentTimestamp);

        logger.info(currentRequestCount + " : " + maxRequests);

        print(userId);

        return currentRequestCount <= maxRequests;
    }

    // Atomically add an element to the timestamps and return the total number of requests
    // in the current window time.
    int addTimeStampAndReturnSize(String userId, long timestamp) {
        // Transaction holds an optimistic lock over the redis entries userId + METADATA_SUFFIX
        // and userId + TIMESTAMPS. The changes in addNewTimestampAndReturnTotalCount
        // are committed only if none of these entries get changed through out
        Pipeline p = jedis.pipelined();
        Response<String> transactionMultiResponse = p.multi();
        p.zadd(userId + TIMESTAMPS, timestamp, String.valueOf(timestamp));
        Response<Long> count = p.zcount(userId + TIMESTAMPS, Double.MIN_VALUE, Double.MAX_VALUE);
        p.exec();
        p.sync();

        if (!transactionMultiResponse.get().equals("OK")) {
            logger.error("Error in setting transaction : Kuch to gadbad hai baba");
        }

        return count.get().intValue();
    }

    void print(String userId) {
        Set<String> timeStamps = jedis.zrange(userId + TIMESTAMPS, 0, -1);
        logger.info("DEBUG: " + String.valueOf(timeStamps));
    }

}