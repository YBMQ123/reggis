import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.Set;

public class JedisTest {
    @Test
    public void testRedis(){
        //1.获取连接
        Jedis jedis=new Jedis("localhost",6379);
        //2.执行具体的操作
        jedis.set("password","12345");
        jedis.hset("hash1","age","20");
//        jedis.del("username");
        System.out.println(jedis.get("password"));
        String hget = jedis.hget("hash1", "age");
        System.out.println(hget);
        Set<String> keys = jedis.keys("*");
        for (String key : keys) {
            System.out.println(key);
        }
        //关闭连接
        jedis.close();
    }
}
