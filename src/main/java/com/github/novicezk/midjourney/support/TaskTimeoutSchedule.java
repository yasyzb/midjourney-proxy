package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskTimeoutSchedule {
	private final TaskQueueHelper taskQueueHelper;
	private final ProxyProperties properties;
	private final List<DiscordInstance> discordInstances;

	@Scheduled(fixedRate = 30000L)
	public void checkTasks() {
		long currentTime = System.currentTimeMillis();
		long timeout = TimeUnit.MINUTES.toMillis(this.properties.getQueue().getTimeoutMinutes());
		List<Task> tasks = this.taskQueueHelper.findRunningTask(new TaskCondition())
				.filter(t -> currentTime - t.getStartTime() > timeout)
				.toList();
		for (Task task : tasks) {
			if (Set.of(TaskStatus.FAILURE, TaskStatus.SUCCESS).contains(task.getStatus())) {
				log.warn("task status is failure/success but is in the queue, end it. id: {}", task.getId());
			} else {
				log.debug("task timeout, id: {}", task.getId());
				task.fail("任务超时");
			}
			Future<?> future = this.taskQueueHelper.getRunningFuture(task.getId());
			if (future != null) {
				future.cancel(true);
			}
			this.taskQueueHelper.changeStatusAndNotify(task, task.getStatus());
			String instanceId = (String) task.getProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID);
			this.taskQueueHelper.taskStoreService.incBy(Constants.KEY_CONCURRENT_PREFIX + instanceId, 1); // 并发额度
			this.taskQueueHelper.taskStoreService.deleteCommon(Constants.KEY_RUNNING_PREFIX + task.getId());
		}
	}

	// 每月1号，0:01:00执行
	@Scheduled(cron = "0 1 0 1 * ?")
	public void resetFastConcurrentTimes() {
		log.info("reset fast/concurrent times");
		for (DiscordInstance instance : discordInstances) {
			String id = instance.getInstanceId();
			Integer concurrent = instance.getAccount().getConcurrent();
			Integer fastTime = instance.getAccount().getFastTime();
			this.taskQueueHelper.taskStoreService.set(Constants.KEY_FAST_PREFIX + id, fastTime * 60 * 60);
			this.taskQueueHelper.taskStoreService.set(Constants.KEY_CONCURRENT_PREFIX + id, concurrent);
		}
	}
}
