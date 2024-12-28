package com.epoch.mrs.controller;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.epoch.mrs.domain.dto.LoginDTO;
import com.epoch.mrs.domain.enums.AdminStatus;
import com.epoch.mrs.domain.enums.UserStatus;
import com.epoch.mrs.domain.vo.Result;
import com.epoch.mrs.domain.po.User;
import com.epoch.mrs.domain.vo.UserVo;
import com.epoch.mrs.service.IUserService;
import com.epoch.mrs.service.MailService;
import com.epoch.mrs.utils.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
public class UserController {

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IUserService userService;

    @Autowired
    private MailService mailService;

    /**
     * 用户名或邮箱是否已注册
     * @param type 是“username” 或 “email”
     * @param value 具体的用户名或邮件
     * @return
     */
    @GetMapping("/IsExists")
    public Result checkExists(@RequestParam String type, @RequestParam String value) {

        boolean exists = switch (type.toLowerCase()) {
            case "username" -> userService.lambdaQuery()
                    .eq(User::getUsername, value)
                    .exists();
            case "email" -> userService.lambdaQuery()
                    .eq(User::getEmail, value)
                    .exists();
            default -> throw new IllegalArgumentException("Invalid check type");
        };

        String message = type.equals("username") ? "用户名已存在" : "邮箱已被注册";
        return exists ? Result.fail(message) : Result.ok() ;
    }

    /**
     *
     * 发送邮件验证码
     * @param to 接受的邮件地址
     * @return
     */
    @PostMapping("/email")
    public Result sendMail(@RequestParam String to){
        log.info(to);
        try {
            mailService.sendVerificationCode(to);
        } catch (Exception e) {
            log.error("发送邮件失败");
            throw new RuntimeException(e);

        }
        return Result.ok("发送邮件成功");
    }

    /**
     * 用户注册
     * @param logName 用户名
     * @param password 用户密码
     * @param email 用户邮件
     * @param checkCode 验证码
     * @return
     * @throws Exception
     */
    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class)
    public Result register(@RequestParam String logName,@RequestParam String password,@RequestParam String email,@RequestParam String checkCode) throws Exception {

        String lockKey = "DMS:register:" + email;
        Boolean isLocked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 1, TimeUnit.MINUTES);

        if (!isLocked) {
            return Result.fail("请勿重复提交注册请求,请1分钟后重试");
        }

            try{
            // 获取Redis中存储的验证码
            String redisCode = stringRedisTemplate.opsForValue().get("MRS:code:" + email);

            // 检查Redis中的验证码是否存在以及是否匹配
            if (redisCode != null && redisCode.equals(checkCode)) {
                // 验证码正确
                log.debug("验证码正确！");
            } else {
                // 验证码不正确或已过期
                return Result.fail("验证码不正确或已过期");
            }

            // 验证邮箱格式
            if (!RegexUtils.isValidEmail(email)) {
                return Result.fail("邮箱格式不正确");
            }

            //将获取的密码加密
            String encodedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                User user = new User()
                        .setEmail(email)
                        .setPassword(encodedPassword)
                        .setUsername(logName)
                        .setReviewTime(LocalDateTime.now())
                        .setStatus(UserStatus.PENDING)
                        .setRole(AdminStatus.USER);

            userService.save(user);

            // 删除已使用的验证码
            stringRedisTemplate.delete("DMS:code:" + email);

            return Result.ok("用户创建成功");

        } catch (Exception e) {
            log.error("用户注册失败: " + e.getMessage());

                // 手动回滚事务
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            return Result.fail("注册失败，请稍后重试");
        }finally {
                // 释放锁
                stringRedisTemplate.delete(lockKey);
            }
    }

    /**
     * 用户登录
     * @param loginDTO 登录条件
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO) {
        String account = loginDTO.getAccount();
        String rawPassword = loginDTO.getPassword();


        // 查询用户 - 使用or条件同时匹配email和username
        User user = userService.lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(User::getEmail, account)
                        .or()
                        .eq(User::getUsername, account)
                )
                .one();

        if (user == null) {
            return Result.fail("用户不存在");
        }

        // 验证密码
        if (!BCrypt.checkpw(rawPassword, user.getPassword())) {
            return Result.fail("账号或密码错误");
        }

        // 登录成功，生成token
        StpUtil.login(user.getId());
        UserVo userVo = new UserVo().setSaTokenInfo(StpUtil.getTokenInfo())
                .setEmail(user.getEmail())
                .setUsername(user.getUsername());


        return Result.ok(userVo);
    }


    /**
     * 用户登出
     * @return
     */
    @PostMapping("/logout")
    public Result logout() {
        StpUtil.logout();
        return Result.ok();
    }

    /**
     * 修改密码
     * @param newPassword 新密码
     * @param rawPassword 旧密码
     * @return
     */
    @PutMapping("/password")
    public Result changePassword(@RequestParam("rawPassword") String rawPassword, @RequestParam("newPassword") String newPassword) {
        int userId = StpUtil.getLoginIdAsInt();
        User user = userService.lambdaQuery().eq(User::getId, userId).one();
        String originalPassword = user.getPassword();
        if (!BCrypt.checkpw(rawPassword, originalPassword)) {
            return Result.fail("原密码错误，请重新输入");
        }
        String encodedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(encodedPassword);
        userService.updateById(user);

        return Result.ok("密码修改成功");
    }

}
