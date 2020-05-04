# Redis-Playground
###### REmote DIctionary Server 😎

### Java Redis Client
- Jedis - https://github.com/xetorthio/jedis

### Redis Doc
- Doc articles - https://redis.io/documentation
- List of Redis Commands - https://redis.io/commands

### Pipelining
- https://redis.io/topics/pipelining
- possible to send multiple commands to the server without waiting for the replies at all, and finally read the replies in a single step.

### Lua scripts using eval
- https://redis.io/commands/eval
- Can be used to write atomic Lua scripts to achieve super low latency

### Todo
- LRU cache (https://redis.io/topics/lru-cache)
- simple counter
- windowed counter
- Rate limiting using Token Bucket
- Rate limiting using Leaky Bucket
- Rate limiting using Sliding Window counter

### Sketch Algorithms using Redis-Bloom 
- https://redislabs.com/redis-best-practices/bloom-filter-pattern/
- Redis-Bloom - https://redislabs.com/redis-enterprise/redis-bloom/
- Bloom Filter
- Cuckoo Filter
- Count-Mins-Sketch
- TopK

### Advanced Config
- Sentinel - https://redis.io/topics/sentinel
- Replication - https://redis.io/topics/replication
- Partitioning - https://redis.io/topics/partitioning

