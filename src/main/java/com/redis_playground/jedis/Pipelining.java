package com.redis_playground.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.Set;

public class Pipelining {
    static Logger logger = LoggerFactory.getLogger(Pipelining.class.getName());

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost");
        logger.info("Connection to server successfully: " + jedis.ping());

//      Notice we do not get direct access to the command responses,
//      instead we're given a Response instance from which we can request the underlying response after the pipeline has been synced.

        String userOneId = "4352523";
        String userTwoId = "4849888";

        Pipeline p = jedis.pipelined();
        p.sadd("searched#" + userOneId, "paris");
        p.zadd("ranking", 126, userOneId);
        p.zadd("ranking", 325, userTwoId);
        Response<Boolean> pipeExists = p.sismember("searched#" + userOneId, "paris");
        Response<Set<String>> pipeRanking = p.zrange("ranking", 0, -1);
        p.sync();

        boolean exists = pipeExists.get();
        Set<String> ranking = pipeRanking.get();
        logger.info("exists: " + exists);
        logger.info("ranking: " + ranking);

        jedis.close();
    }
}