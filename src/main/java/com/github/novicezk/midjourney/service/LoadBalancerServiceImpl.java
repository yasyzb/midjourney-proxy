package com.github.novicezk.midjourney.service;

import com.github.novicezk.midjourney.support.DiscordInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class LoadBalancerServiceImpl implements LoadBalancerService {
	private final AtomicInteger position = new AtomicInteger(0);
	private final List<DiscordInstance> discordInstances;

	@Override
	public Collection<DiscordInstance> getAllInstances() {
		return this.discordInstances;
	}

	@Override
	public DiscordInstance chooseInstance() {
		int pos = incrementAndGet();
		return this.discordInstances.get(pos % this.discordInstances.size());
	}

	@Override
	public DiscordInstance getDiscordInstance(String instanceId) {
		return this.discordInstances.stream().filter(instance -> instance.getInstanceId().equals(instanceId))
				.findFirst().orElseThrow(() -> new RuntimeException("No instances available"));
	}

	private int incrementAndGet() {
		int current;
		int next;
		do {
			current = this.position.get();
			next = current == Integer.MAX_VALUE ? 0 : current + 1;
		} while (!this.position.compareAndSet(current, next));
		return next;
	}
}
