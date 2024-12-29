package com.epoch.mrs.listener;

import com.epoch.mrs.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final IUserService userService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer, IUserService userService) {
        super(listenerContainer);
        this.userService = userService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        // 检查 Key 格式是否符合 "user:pending:<id>"
        if (expiredKey.startsWith("user:reject:")) {
            String userId = expiredKey.replace("user:reject:", "");

            // 删除用户记录
            userService.removeById(Long.parseLong(userId));
            log.info("被拒绝的用户 ID :" + userId + " 已被定时删除");
        }
    }
}