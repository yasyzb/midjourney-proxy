package com.github.novicezk.midjourney.service;

import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;

import java.util.List;

public interface TaskStoreService {

	void save(Task task);

	void delete(String id);

	Task get(String id);

	List<Task> list();

	List<Task> list(TaskCondition condition);

	Task findOne(TaskCondition condition);

	List<Integer> mget(List<String> ids);

	void descBy(String id, Integer num);

	void incBy(String id, Integer num);

	void set(String id, Integer num);

	void deleteCommon(String id);
}
