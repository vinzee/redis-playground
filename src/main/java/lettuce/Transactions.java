package lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/*
https://lettuce.io/core/release/reference/#asynchronous-api
also known as Pipelining
 */
public class Transactions {
    static Logger logger = LoggerFactory.getLogger(Transactions.class.getName());

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //Connecting to Redis server on localhost
        RedisClient redisClient = RedisClient.create("redis://localhost:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        logger.info("Connected to server successfully");

        // asynchronous API (using multi)
        RedisAsyncCommands<String, String> asyncCommands = connection.async();

        // start transaction
        asyncCommands.multi();

        RedisFuture<String> result1 = asyncCommands.set("key1", "value1");
        RedisFuture<String> result2 = asyncCommands.set("key2", "value2");
        RedisFuture<String> result3 = asyncCommands.set("key3", "value3");

        // execute transaction
        RedisFuture<TransactionResult> execResult = asyncCommands.exec();

        TransactionResult transactionResult = execResult.get();
        logger.info("our TransactionResult:: " + transactionResult.get(0) + transactionResult.get(1) + transactionResult.get(2));
        logger.info("our Results: " + result1 + result2 + result3);

        connection.close();
        redisClient.shutdown();
    }
}