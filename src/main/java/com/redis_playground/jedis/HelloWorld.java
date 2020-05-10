package com.redis_playground.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class HelloWorld {
    static Logger logger = LoggerFactory.getLogger(HelloWorld.class.getName());

   public static void main(String[] args) {
      //Connecting to Redis server on localhost
      Jedis jedis = new Jedis("localhost");
      logger.info("Connection to server successfully");

      //check whether server is running or not
      logger.info("Server is running: " + jedis.ping());

      //set the data in redis string
      jedis.set("tutorial-name", "Redis tutorial");

      // Get the stored data and print it
      logger.info("Stored string in redis:: "+ jedis.get("tutorial-name"));

      jedis.close();
   }
}