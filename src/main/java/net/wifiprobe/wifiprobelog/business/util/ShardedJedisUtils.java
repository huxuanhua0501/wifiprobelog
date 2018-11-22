package net.wifiprobe.wifiprobelog.business.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ShardedJedisUtils {
    public static ShardedJedisPool shardedJedisPool ;//切片连接池
    @Autowired
    Environment env;

    @PostConstruct
    public ShardedJedisPool init() {
        // 池基本配置
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(200);
        config.setMinIdle(10);
        config.setMaxWaitMillis(1000l);
        config.setTestOnBorrow(false);
        // slave链接
        List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
        shards.add(new JedisShardInfo(env.getProperty("spring.redis.host"),  Integer.parseInt(env.getProperty("spring.redis.port"))));
        // 构造池
        shardedJedisPool = new ShardedJedisPool(config, shards);
        return shardedJedisPool;
    }


    public ShardedJedis getShardedJedis() {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        return shardedJedis;
    }
}
