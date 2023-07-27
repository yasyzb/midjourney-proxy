package com.github.novicezk.midjourney.service.store;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.stream.StreamUtil;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskStoreServiceImpl implements TaskStoreService {
	private final TimedCache<String, Task> taskMap;

	public InMemoryTaskStoreServiceImpl(Duration timeout) {
		this.taskMap = CacheUtil.newTimedCache(timeout.toMillis());
	}

	@Override
	public void save(Task task) {
		this.taskMap.put(task.getId(), task);
	}

	@Override
	public void delete(String key) {
		this.taskMap.remove(key);
	}

	@Override
	public Task get(String key) {
		return this.taskMap.get(key);
	}

	@Override
	public List<Task> list() {
		return ListUtil.toList(this.taskMap.iterator());
	}

	@Override
	public List<Task> list(TaskCondition condition) {
		return StreamUtil.of(this.taskMap.iterator()).filter(condition).toList();
	}

	@Override
	public Task findOne(TaskCondition condition) {
		return StreamUtil.of(this.taskMap.iterator()).filter(condition).findFirst().orElse(null);
	}

	@Override
	public List<Integer> mget(List<String> ids) {
		return new ArrayList<Integer>();
	}

	@Override
	public void descBy(String id, Integer num) {
	}

	@Override
	public void incBy(String id, Integer num) {
	}

	@Override
	public void set(String id, Integer num) {
	}
}
