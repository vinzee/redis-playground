package lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/*
https://lettuce.io/core/release/reference/#asynchronous-api
also known as Pipelining
 */
public class HelloWorldAsync {
    static Logger logger = LoggerFactory.getLogger(HelloWorldAsync.class.getName());

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //Connecting to Redis server on localhost
        RedisClient redisClient = RedisClient.create("redis://localhost:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        logger.info("Connected to server successfully");

        // asynchronous API (using multi)
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
        //set the data in redis string
        asyncCommands.lpush("tasks", "firstTask");
        asyncCommands.lpush("tasks", "secondTask");

        // Get the stored data and print it
        RedisFuture<String> redisFuture = asyncCommands.rpop("tasks");
        String val = redisFuture.get();
        logger.info("Stored string in redis:: " + val);

        connection.close();
        redisClient.shutdown();
    }
}