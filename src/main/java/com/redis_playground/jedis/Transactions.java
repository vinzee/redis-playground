package com.redis_playground.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class Transactions {
    static Logger logger = LoggerFactory.getLogger(Transactions.class.getName());

   public static void main(String[] args) {
      Jedis jedis = new Jedis("localhost");
      logger.info("Connection to server successfully: " + jedis.ping());

// Transactions guarantee atomicity and thread safety operations,
// which means that requests from other clients will never be handled concurrently during Redis transactions:

       String friendsPrefix = "friends#";
       String userOneId = "4352523";
       String userTwoId = "5552321";

       Transaction t = jedis.multi();
       t.sadd(friendsPrefix + userOneId, userTwoId);
       t.sadd(friendsPrefix + userTwoId, userOneId);
       t.exec();

       jedis.close();
   }
}