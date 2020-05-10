package lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/*
https://lettuce.io/core/release/reference/#asynchronous-api
also known as Pipelining
 */
public class LuaScriptEval {
    static Logger logger = LoggerFactory.getLogger(LuaScriptEval.class.getName());

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //Connecting to Redis server on localhost
        RedisClient redisClient = RedisClient.create("redis://localhost:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        logger.info("Connected to server successfully");

        // asynchronous API (using multi)
        RedisAsyncCommands<String, String> asyncCommands = connection.async();
		RedisFuture<String> result = asyncCommands.eval(" return redis.call('ping') ", ScriptOutputType.VALUE);
		System.out.println("Real Custom Command[dp.DATE]: " + result.get());

        connection.close();
        redisClient.shutdown();
    }
}