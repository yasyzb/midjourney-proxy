package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.enums.BlendDimensions;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.service.DiscordService;
import eu.maxschuster.dataurl.DataUrl;
import lombok.Getter;

import java.util.List;

public class DiscordInstance implements DiscordService {
	@Getter
	private final String instanceId;
	@Getter
	private final DiscordAccount account;
	private final DiscordService service;

	public DiscordInstance(String instanceId, DiscordAccount account, DiscordService service) {
		this.instanceId = instanceId;
		this.account = account;
		this.service = service;
	}

	@Override
	public Message<Void> imagine(String prompt) {
		return this.service.imagine(prompt);
	}

	@Override
	public Message<Void> info() {
		return this.service.info();
	}

	@Override
	public Message<Void> upscale(String messageId, int index, String messageHash, int messageFlags) {
		return this.service.upscale(messageId, index, messageHash, messageFlags);
	}

	@Override
	public Message<Void> variation(String messageId, int index, String messageHash, int messageFlags) {
		return this.service.variation(messageId, index, messageHash, messageFlags);
	}

	@Override
	public Message<Void> reroll(String messageId, String messageHash, int messageFlags) {
		return this.service.reroll(messageId, messageHash, messageFlags);
	}

	@Override
	public Message<Void> describe(String finalFileName) {
		return this.service.describe(finalFileName);
	}

	@Override
	public Message<Void> blend(List<String> finalFileNames, BlendDimensions dimensions) {
		return this.service.blend(finalFileNames, dimensions);
	}

	@Override
	public Message<String> upload(String fileName, DataUrl dataUrl) {
		return this.service.upload(fileName, dataUrl);
	}

	@Override
	public Message<String> sendImageMessage(String content, String finalFileName) {
		return this.service.sendImageMessage(content, finalFileName);
	}
}
