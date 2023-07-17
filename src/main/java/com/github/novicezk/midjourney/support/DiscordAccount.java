package com.github.novicezk.midjourney.support;

import lombok.Data;

@Data
public class DiscordAccount {
	private String guildId;
	private String channelId;
	private String userToken;
	/**
	 * 你的机器人token.
	 */
	private String botToken;
	private String sessionId = "9c4055428e13bcbf2248a6b36084c5f3";
	private String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";
	/**
	 * 是否使用user_token连接wss，默认启用.
	 */
	private boolean userWss = true;
}
