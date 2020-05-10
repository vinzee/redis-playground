package lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
https://lettuce.io/core/release/reference/#asynchronous-api
also known as Pipelining
 */
public class LeaderBoard {
    static Logger logger = LoggerFactory.getLogger(LeaderBoard.class.getName());

    public static void main(String[] args) {
        //Connecting to Redis server on localhost
        RedisClient redisClient = RedisClient.create("redis://localhost:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        logger.info("Connected to server successfully");

        // command API for synchronous execution
        RedisCommands<String, String> sync = connection.sync();

        //set the data in redis string
        sync.set("foo", "bar");

        // Get the stored data and print it
        logger.info("Stored string in redis:: " + sync.get("foo"));

        connection.close();
        redisClient.shutdown();
    }
}