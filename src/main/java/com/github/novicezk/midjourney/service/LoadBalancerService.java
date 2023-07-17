package com.github.novicezk.midjourney.service;

import com.github.novicezk.midjourney.support.DiscordInstance;

import java.util.Collection;

/**
 * @author NpcZZZZZZ
 * @version 1.0
 * @email 946123601@qq.com
 * @date 2023/6/28
 **/
public interface LoadBalancerService {

	Collection<DiscordInstance> getAllInstances();

	DiscordInstance chooseInstance();

	DiscordInstance getDiscordInstance(String instanceId);

}
