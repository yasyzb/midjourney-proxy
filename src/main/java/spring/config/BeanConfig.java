package spring.config;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.DiscordServiceImpl;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.service.store.InMemoryTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.store.RedisTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.translate.BaiduTranslateServiceImpl;
import com.github.novicezk.midjourney.service.translate.GPTTranslateServiceImpl;
import com.github.novicezk.midjourney.support.DiscordAccount;
import com.github.novicezk.midjourney.support.DiscordHelper;
import com.github.novicezk.midjourney.support.DiscordInstance;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskMixin;
import com.github.novicezk.midjourney.wss.WebSocketStarter;
import com.github.novicezk.midjourney.wss.bot.BotMessageListener;
import com.github.novicezk.midjourney.wss.bot.BotWebSocketStarter;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import com.github.novicezk.midjourney.wss.user.UserMessageListener;
import com.github.novicezk.midjourney.wss.user.UserWebSocketStarter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(ProxyProperties.class)
public class BeanConfig {
	@Resource
	private ApplicationContext applicationContext;

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	List<DiscordAccount> discordAccounts(ProxyProperties properties) {
		List<DiscordAccount> accounts = properties.getAccounts();
		if (CharSequenceUtil.isNotBlank(properties.getDiscord().getChannelId())) {
			accounts.add(properties.getDiscord());
		}
		return accounts;
	}

	@Bean
	List<DiscordInstance> discordInstances(List<DiscordAccount> discordAccounts, DiscordHelper discordHelper,
			RestTemplate restTemplate) {
		String serverUrl = discordHelper.getServer();
		List<DiscordInstance> instances = new ArrayList<>();
		for (DiscordAccount account : discordAccounts) {
			DiscordServiceImpl discordService = new DiscordServiceImpl(account.getGuildId(), account.getChannelId(),
					account.getUserToken(),
					account.getSessionId(), account.getUserAgent(), serverUrl + "/api/v9/interactions",
					serverUrl + "/api/v9/channels/" + account.getChannelId() + "/attachments",
					serverUrl + "/api/v9/channels/" + account.getChannelId() + "/messages",
					ResourceUtil.readUtf8Str("api-params/imagine.json"),
					ResourceUtil.readUtf8Str("api-params/upscale.json"),
					ResourceUtil.readUtf8Str("api-params/variation.json"),
					ResourceUtil.readUtf8Str("api-params/reroll.json"),
					ResourceUtil.readUtf8Str("api-params/describe.json"),
					ResourceUtil.readUtf8Str("api-params/blend.json"),
					ResourceUtil.readUtf8Str("api-params/message.json"),
					ResourceUtil.readUtf8Str("api-params/info.json"), restTemplate);
			instances.add(new DiscordInstance(account.getChannelId(), account, discordService));
		}
		return instances;
	}

	@Bean
	TranslateService translateService(ProxyProperties properties) {
		return switch (properties.getTranslateWay()) {
			case BAIDU -> new BaiduTranslateServiceImpl(properties.getBaiduTranslate());
			case GPT -> new GPTTranslateServiceImpl(properties);
			default -> prompt -> prompt;
		};
	}

	@Bean
	public TaskStoreService taskStoreService(ProxyProperties proxyProperties,
			RedisConnectionFactory redisConnectionFactory) {
		ProxyProperties.TaskStore.Type type = proxyProperties.getTaskStore().getType();
		Duration timeout = proxyProperties.getTaskStore().getTimeout();
		return switch (type) {
			case IN_MEMORY -> new InMemoryTaskStoreServiceImpl(timeout);
			case REDIS -> new RedisTaskStoreServiceImpl(timeout, taskRedisTemplate(redisConnectionFactory),
					taskRedisIntTemplate(redisConnectionFactory));
		};
	}

	@Bean
	public RedisTemplate<String, Task> taskRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Task> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Task.class));
		return redisTemplate;
	}

	@Bean
	public RedisTemplate<String, Integer> taskRedisIntTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Integer> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new IntegerRedisSerializer());
		return redisTemplate;
	}

	@Bean
	Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(ProxyProperties properties) {
		if (properties.isIncludeTaskExtended()) {
			return builder -> {
			};
		}
		return builder -> builder.mixIn(Task.class, TaskMixin.class);
	}

	@Bean
	List<MessageHandler> messageHandlers() {
		return this.applicationContext.getBeansOfType(MessageHandler.class).values().stream().toList();
	}

	@Bean
	Map<String, WebSocketStarter> webSocketStarterMap(List<DiscordAccount> discordAccounts, ProxyProperties properties,
			DiscordHelper discordHelper) {
		ProxyProperties.ProxyConfig proxy = properties.getProxy();
		Map<String, WebSocketStarter> webSocketStarterMap = new HashMap<>();
		discordAccounts.forEach(x -> {
			if (x.isUserWss()) {
				UserMessageListener userMessageListener = new UserMessageListener(x.getChannelId(), messageHandlers());
				webSocketStarterMap.put(x.getChannelId(), new UserWebSocketStarter(proxy.getHost(), proxy.getPort(),
						x.getUserToken(), x.getUserAgent(), userMessageListener, discordHelper));
			} else {
				BotMessageListener botMessageListener = new BotMessageListener(x.getChannelId(), messageHandlers());
				webSocketStarterMap.put(x.getChannelId(), new BotWebSocketStarter(proxy.getHost(), proxy.getPort(),
						x.getBotToken(), botMessageListener, discordHelper));
			}
		});
		return webSocketStarterMap;
	}

}
