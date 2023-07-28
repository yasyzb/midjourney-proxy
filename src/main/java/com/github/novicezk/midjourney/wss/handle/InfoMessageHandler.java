package com.github.novicezk.midjourney.wss.handle;

import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.ContentParseData;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * imagine消息处理.
 * 开始(create): **cat** - <@1012983546824114217> (Waiting to start)
 * 进度(update): **cat** - <@1012983546824114217> (0%) (relaxed)
 * 完成(create): **cat** - <@1012983546824114217> (relaxed)
 */
@Slf4j
@Component
public class InfoMessageHandler extends MessageHandler {
    @Override
    public void handle(MessageType messageType, DataObject message) {
        if (MessageType.CREATE == messageType) {
            return;
        } else if (MessageType.UPDATE == messageType) {
            String description = getMessageEmbedDescription(message);
            if (description.isEmpty()) {
                return;
            }
            Integer remain = getRemainFastTimeSeconds(description);
            if (remain.equals(-1)) {
                return;
            }
            String instanceId = message.getString("channel_id");
            if (instanceId.isEmpty()) {
                return;
            }
            this.taskQueueHelper.taskStoreService.set(Constants.KEY_FAST_PREFIX + instanceId, remain);
        }
    }

    @Override
    public void handle(MessageType messageType, Message message) {
    }

    private Integer getRemainFastTimeSeconds(String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (!line.startsWith("**Fast Time Remaining**:")) {
                continue;
            }
            String[] times = line.replace("**Fast Time Remaining**:", "").trim().split(" ");
            if (times.length < 2) {
                return -1;
            }

            String remain = times[0].split("/")[0].trim();
            String unit = times[1].trim();

            float f = Float.parseFloat(remain);
            if (unit.equals("minutes")) {
                return Math.round(f * 60);
            } else if (unit.equals("hours")) {
                return Math.round(f * 60 * 60);
            } else {
                System.out.println("invalid unit:" + unit);
                return -1;
            }
        }
        return -1;
    }

}
