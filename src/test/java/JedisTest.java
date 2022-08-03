import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class JedisTest {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Test
    public void testRedis() {
        stringRedisTemplate.opsForValue().set("name","10",30, TimeUnit.SECONDS);

    }
}
