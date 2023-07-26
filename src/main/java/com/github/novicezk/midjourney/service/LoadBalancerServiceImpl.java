package com.github.novicezk.midjourney.service;

import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.support.DiscordInstance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class LoadBalancerServiceImpl implements LoadBalancerService {
	private final TaskStoreService taskStoreService;

	private final AtomicInteger position = new AtomicInteger(0);
	private final List<DiscordInstance> discordInstances;
	private Lock lock = new ReentrantLock();
	private final Random random = new Random();

	@Override
	public Collection<DiscordInstance> getAllInstances() {
		return this.discordInstances;
	}

	@Override
	public DiscordInstance chooseInstance(boolean relax) {
		return chooseInstanceV2(relax);
	}

	private DiscordInstance chooseInstanceV1() {
		int pos = incrementAndGet();
		return this.discordInstances.get(pos % this.discordInstances.size());
	}

	private DiscordInstance chooseInstanceV2(boolean relax) {
		lock.lock();

		// 拿到有并发额度的instance
		List<Integer> concurrentValues = this.taskStoreService
				.mget(this.getInstanceIds(Constants.KEY_CONCURRENT_PREFIX));
		System.out.println("---" + this.discordInstances.size());
		System.out.println("===" + Constants.KEY_CONCURRENT_PREFIX + concurrentValues.get(0));
		List<String> queryAvailableIds = new ArrayList<>();
		List<DiscordInstance> ds = new ArrayList<>();
		int conIndex = -1;
		for (Integer concurrent : concurrentValues) {
			conIndex++;
			if (concurrent <= 0) {
				// 没有并发额度
				continue;
			}
			ds.add(this.discordInstances.get(conIndex));
			queryAvailableIds.add(Constants.KEY_FAST_PREFIX + this.discordInstances.get(conIndex).getInstanceId());
		}

		List<Integer> fastValues = this.taskStoreService.mget(queryAvailableIds);
		System.out.println("===" + queryAvailableIds.get(0) + fastValues.get(0));
		// fast instance id
		List<Integer> fastIds = new ArrayList<>();
		int fastIndex = -1;
		for (Integer v : fastValues) {
			fastIndex++;
			if (v <= 0) {
				continue;
			}
			fastIds.add(fastIndex);
		}

		lock.unlock();

		// 可用为空
		if (ds.size() == 0) {
			int index = this.random.nextInt(0);
			String instanceId = this.discordInstances.get(index).getInstanceId();
			this.taskStoreService.descBy(Constants.KEY_FAST_PREFIX + instanceId, 4); // fast额度
			this.taskStoreService.descBy(Constants.KEY_CONCURRENT_PREFIX + instanceId, 1); // 并发额度
			return this.discordInstances.get(index);
		}

		int returnIndex = 0;
		// relax || 没有fast额度
		if (relax || fastIds.size() == 0) {
			returnIndex = this.random.nextInt(ds.size());
		}
		// fast && 有fast额度
		returnIndex = this.random.nextInt(fastIds.size());
		this.taskStoreService.descBy(Constants.KEY_FAST_PREFIX + ds.get(returnIndex).getInstanceId(), 4); // fast额度
		this.taskStoreService.descBy(Constants.KEY_CONCURRENT_PREFIX + ds.get(returnIndex).getInstanceId(), 1); // 并发额度
		return ds.get(returnIndex);
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

	private List<String> getInstanceIds(String prefix) {
		List<String> ids = new ArrayList<>();
		for (DiscordInstance d : this.discordInstances) {
			ids.add(prefix + d.getInstanceId());
		}
		return ids;
	}
}
