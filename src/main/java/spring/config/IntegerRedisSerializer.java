package spring.config;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class IntegerRedisSerializer implements RedisSerializer<Integer> {

    private final Charset charset;

    /**
     * Creates a new
     * {@link org.springframework.data.redis.serializer.StringRedisSerializer} using
     * {@link StandardCharsets#UTF_8 UTF-8}.
     */
    public IntegerRedisSerializer() {
        this(StandardCharsets.UTF_8);
    }

    /**
     * Creates a new
     * {@link org.springframework.data.redis.serializer.StringRedisSerializer} using
     * the given {@link Charset} to encode and decode strings.
     *
     * @param charset must not be {@literal null}.
     */
    public IntegerRedisSerializer(Charset charset) {

        Assert.notNull(charset, "Charset must not be null!");
        this.charset = charset;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.data.redis.serializer.RedisSerializer#deserialize(byte[])
     */
    @Override
    public Integer deserialize(@Nullable byte[] bytes) {
        return bytes == null ? null : Integer.valueOf(new String(bytes, charset));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.data.redis.serializer.RedisSerializer#serialize(java.lang
     * .Object)
     */
    @Override
    public byte[] serialize(@Nullable Integer string) {
        return (string == null ? null : String.valueOf(string).getBytes(charset));
    }
}