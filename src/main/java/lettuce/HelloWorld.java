package lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class HelloWorld {
    static Logger logger = LoggerFactory.getLogger(HelloWorld.class.getName());

    public static void main(String[] args) {
        //Connecting to Redis server on localhost
        RedisClient redisClient = RedisClient.create("redis://localhost:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        logger.info("Connected to server successfully");

        // command API for synchronous execution
        RedisCommands<String, String> commands = connection.sync();

        //set the data in redis string
        commands.set("foo", "bar");

        // Get the stored data and print it
        logger.info("Stored string in redis:: " + commands.get("foo"));

        connection.close();
        redisClient.shutdown();
    }
}