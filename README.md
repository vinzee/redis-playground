# Redis-Playground
###### REmote DIctionary Server 😎

### Redis Doc
- Doc HomePage - https://redis.io/documentation
- Commands - https://redis.io/commands
- Data-types - https://redis.io/topics/data-types
- Redis Modules - https://redislabs.com/community/redis-modules-hub/

### Talks
- [Redis internals explained - by Christoph Strobl (Youtube Video)](https://www.youtube.com/watch?v=ctfDs7M35Ho)
- [Redis cluster explained - by Box (Youtube Video)](https://www.youtube.com/watch?v=NymIgA7Wa78)
- [RedisConf17 - Geospatial Indexing: The 10 Million QPS Redis Architecture Powering Lyft - Daniel H.](https://www.youtube.com/watch?v=cSFWlF96Sds)
- proxy based routing (twemproxy), consistently hashed
- bad design - multiple services talking to redis directly
- good design - multiple services talking to a frontend service which talks with redis
- moved from region based clusters to GeoHashing
- recommends s2 geohashing library
- [Redis at Lyft: 1,000 Instances](https://www.youtube.com/watch?v=U4WspAKekqM)
- migrated from twemproxy to envoy
- migrated from ketama to maglav (consistently hashing algos)

### Java Redis libraries
- Lettuce (sync, async, reactive API, thread-safe) (by Pivotal) 
- Redisson (sync, async, reactive API, thread-safe)
- Jedis (sync API only, not thread safe)
- JRedisBloom
- Spring Data Redis (abstract API over Jedis & Lettuce)

##### Lettuce
- https://lettuce.io/core/release/reference/#connecting-redis
- https://www.baeldung.com/java-redis-lettuce
- https://redislabs.com/blog/jedis-vs-lettuce-an-exploration/
- asynchronous support via the Java 8's CompletionStage interface and support for Reactive Streams
- uses asynchronous Netty for connections - better suited for sharing a connection with more than one thread.
- https://medium.com/@diego_pacheco/dispatching-custom-commands-with-lettuce-and-redis-modules-9093916b33d9

##### Redissoon
- https://redisson.pro/
- https://github.com/redisson/redisson
- higher-level client with another layer of abstraction
- Offers collections and other interfaces instead of raw Redis commands.
- uses asynchronous Netty for connections

##### Jedis
- https://github.com/xetorthio/jedis

##### JRedisBloom
- https://github.com/RedisBloom/JRedisBloom

##### Data structures in Redis
- KV, list, set, hash
- bitfields
- sorted set
- geo
- hyperloglog
- pubsub

### Pipelining
- https://redis.io/topics/pipelining
- possible to send multiple commands to the server without waiting for the replies at all, and finally read the replies in a single step.

### Lua scripts using eval
- https://redis.io/commands/eval
- enables Atomic operations 
- able to achieve super low latency

### Other Applications
- LRU cache (https://redis.io/topics/lru-cache)
- Leaderboard - https://redislabs.com/redis-enterprise/use-cases/leaderboards/
- Pubsub using redis Queues
- Shopping cart using redis

### RateLimiting
- Rate limiting using Token Bucket
- Rate limiting using Leaky Bucket ??
- Rate limiting using Sliding Window counter
- https://github.com/brandur/redis-cell
- it implements the generic cell rate algorithm (GCRA)

### Sketch Algorithms using Redis-Bloom 
- https://redislabs.com/redis-best-practices/bloom-filter-pattern/
- Redis-Bloom - https://redislabs.com/redis-enterprise/redis-bloom/
- Bloom Filter
- Cuckoo Filter
- Count-Mins-Sketch
- TopK

## Operation Modes
#### Master standalone
- no sharding, no replication
- no automatic failover
- static topology

#### Master/Replica standalone
- Master-Slave replication, no sharding
- no automatic failover
- static topology - one-time topology lookup which remains static afterward

#### Master/Replica + Sentinel
- https://redis.io/topics/sentinel
- Master-Slave replication, no sharding
- automatic master failover using Sentinel (high availability)
- Sentinel is the registry and notification source for topology events
- topology updates - are received by subscribing to all Sentinels and listening for Pub/Sub messages to trigger topology refreshing

#### Redis Cluster
- https://redis.io/topics/cluster-tutorial
- https://redis.io/topics/cluster-spec
- Replication + Sharding
- connects to specific seed nodes
- topology updates - periodic cluster topology updates need to be triggered manually
- partitions keys using slots
- automatic sharding

### Partitioning
- Partitioning - https://redis.io/topics/partitioning
- Option 1: Query routing (using Redis cluster)
- Option 2: Proxy assisted partitioning (using Twemproxy)
- Option 3: Client side partitioning

### Replication
##### Asynchronous Replication 
- https://redis.io/topics/replication
- supported by default
##### Synchronous Replication 
- using WAIT command
- WAIT - https://redis.io/commands/wait
##### Active-Active Replication using CRDTs 
https://redislabs.com/redis-enterprise/technology/active-active-geo-distribution/

### Redis Cluster

### Mac Redis Setup
```bash
brew install redis
```

Option1: Start as daemon (start redis and restart at login)
```bash
brew services start redis
```
Option2: If you don't want/need a background service you can just run:
```bash
redis-server /usr/local/etc/redis.conf
```
