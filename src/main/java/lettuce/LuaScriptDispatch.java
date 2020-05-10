package lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.Utf8StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.AsyncCommand;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.RedisCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/*
https://lettuce.io/core/release/reference/#asynchronous-api
also known as Pipelining
 */
public class LuaScriptDispatch {
    static Logger logger = LoggerFactory.getLogger(LuaScriptDispatch.class.getName());

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //Connecting to Redis server on localhost
        RedisClient redisClient = RedisClient.create("redis://localhost:6379");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        logger.info("Connected to server successfully");

        RedisCommand<String, String, String> command = new Command<>(CommandType.PING, new StatusOutput<>(new Utf8StringCodec()));
        AsyncCommand<String, String, String> async = new AsyncCommand<>(command);
        connection.dispatch(async);
		System.out.println("Real Custom Command[dp.DATE]: " + async.get());

        connection.close();
        redisClient.shutdown();
    }
}