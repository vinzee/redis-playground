package ratelimiting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
	Inspired by - https://www.youtube.com/watch?v=NymIgA7Wa78

	Data-Structure (map)
	--------
	"userid_ratelimit_metadata": {
		"level": 15,
	    "max": 4,
	    "rate": 0.25, // requests per sec ?
	    "lastLeakAt": 15244948695958
	}

	// 4 requests per sec
	= leak rate = 1 / 4

 */

public class LeakyBucketRateLimiter {
    static Logger logger = LoggerFactory.getLogger(LeakyBucketRateLimiter.class.getName());

    public static void main(String[] args) throws InterruptedException {
        LeakyBucket rateLimiter = new LeakyBucket(logger);

        String userId = "1";
        rateLimiter.addUser(userId, 4);

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

class LeakyBucket {
    private Jedis jedis;
    private Logger logger;
	private String CURRENT_LEVEL = "level";
	private String MAX_LEVEL = "max";
	private String RATE = "rate";
	private String LAST_LEAK_AT = "lastLeakAt";
    private String METADATA_SUFFIX = "_ratelimit_metadata";

    LeakyBucket(Logger logger) {
        jedis = new Jedis("localhost");
        this.logger = logger;
        logger.info("Connection to server successfully: " + jedis.ping());
    }

    void terminate() {
        jedis.close();
    }

    // Adds a new user's rate of requests to be allowed
    void addUser(String userId, int requestsPerSecond) {
        Map<String, String> map = new HashMap<String, String>() {{
            put(CURRENT_LEVEL, Integer.toString(0));
            put(MAX_LEVEL, Integer.toString(requestsPerSecond));
            put(RATE, Integer.toString(1 / requestsPerSecond));
            put(LAST_LEAK_AT, Long.toString(Instant.now().toEpochMilli()));
        }};

        jedis.hmset(userId + METADATA_SUFFIX, map);
    }

    // Removes a user's metadata and timestamps
    void removeUser(String userId) {
        jedis.del(userId + METADATA_SUFFIX);
    }

    boolean shouldAllowRequest(String userId) {
        // Step 1: get the user metadata storing the number of requests per window time
        Map<String, String> userMetaData = jedis.hgetAll(userId + METADATA_SUFFIX);
        if (userMetaData == null) {
            logger.error("invalid user !!");
            return false;
        }
        int currentLevel = Integer.parseInt(userMetaData.get(CURRENT_LEVEL));
        int maxLevel = Integer.parseInt(userMetaData.get(MAX_LEVEL));
        int rate = Integer.parseInt(userMetaData.get(RATE));
        long lastLeakAt = Long.parseLong(userMetaData.get(LAST_LEAK_AT));

        // Step 2: evict older entries
        long currentTimestamp = Instant.now().toEpochMilli();
        logger.info("currentTimestamp: " + currentTimestamp);
        long timeDiff = (currentTimestamp - lastLeakAt) / 1000;
        logger.info("timeDiff: " + timeDiff);

        int leak = (int) timeDiff * rate;

        currentLevel = Math.max(0, currentLevel - leak);

        boolean out = false;

        // Step 3: incr count for current req
        if(currentLevel < maxLevel){
            ++currentLevel;
            out = true;
        }

        userMetaData.put(CURRENT_LEVEL, Integer.toString(currentLevel));
        userMetaData.put(LAST_LEAK_AT, Long.toString(currentTimestamp));

        logger.info(currentLevel + " : " + currentTimestamp);

        Pipeline p = jedis.pipelined();
        p.multi();
        p.hset(userId + METADATA_SUFFIX, userMetaData);
        p.exec();
        p.sync();

        print(userId);

        return out;
    }

    // Atomically increments hash key val by unit and returns. Uses optimistic locking
    // over userId + self.COUNTS redis key.

    private void print(String userId) {
        Map<String, String> counts = jedis.hgetAll(userId + METADATA_SUFFIX);
        logger.info("DEBUG: " + counts);
    }
}