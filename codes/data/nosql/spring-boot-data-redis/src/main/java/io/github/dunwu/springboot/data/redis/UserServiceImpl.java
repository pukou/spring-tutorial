package io.github.dunwu.springboot.data.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2019-10-14
 */
@Service
public class UserServiceImpl implements UserService {

    public static final String DEFAULT_KEY = "spring-boot:user";

    private final RedisTemplate redisTemplate;

    public UserServiceImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public User getUser(Long id) {
        return (User) redisTemplate.opsForHash().get(DEFAULT_KEY, id.toString());
    }

    @Override
    public void setUser(User user) {
        redisTemplate.opsForHash().put(DEFAULT_KEY, user.getId().toString(), user);
    }

    @Override
    public void batchSetUsers(Map<String, User> users) {
        redisTemplate.opsForHash().putAll(DEFAULT_KEY, users);
    }

}