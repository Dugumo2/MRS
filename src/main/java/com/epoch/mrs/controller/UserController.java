package com.epoch.mrs.controller;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.epoch.mrs.annotation.OperationLog;
import com.epoch.mrs.domain.dto.LoginDTO;
import com.epoch.mrs.domain.enums.AdminStatus;
import com.epoch.mrs.domain.enums.UserStatus;
import com.epoch.mrs.domain.vo.Result;
import com.epoch.mrs.domain.po.User;
import com.epoch.mrs.domain.vo.ReviewUserVo;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
                        .setApplicationTime(LocalDateTime.now())
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
    @OperationLog("用户登录")
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

        if(user.getStatus() == UserStatus.PENDING){
            return Result.fail("用户正在审核中，请耐心等待");
        } else if (user.getStatus() == UserStatus.REJECTED) {
            // 从 Redis 获取剩余时间
            String redisKey = "user:reject:" + user.getId();
            Long expire = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return Result.fail("用户审核被拒绝，请" + expire + "秒后重试");
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
    @OperationLog("用户登出")
    @PostMapping("/logout")
    public Result logout() {
        StpUtil.logout();
        log.info("用户退出登录成功");
        return Result.ok();
    }

    /**
     * 修改密码
     * @param newPassword 新密码
     * @param rawPassword 旧密码
     * @return
     */
    @OperationLog("修改密码")
    @PutMapping("/password")
    public Result changePassword(@RequestParam("rawPassword") String rawPassword, @RequestParam("newPassword") String newPassword) {
        int userId = StpUtil.getLoginIdAsInt();
        User user = userService.lambdaQuery().eq(User::getId, userId).one();
        String originalPassword = user.getPassword();
        if (!BCrypt.checkpw(rawPassword, originalPassword)) {
            log.info("原密码错误，请重新输入          ");
            return Result.fail("原密码错误，请重新输入");
        }
        String encodedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(encodedPassword);
        userService.updateById(user);
        log.info("密码修改成功");
        return Result.ok("密码修改成功");
    }


    /**
     * 获取用户列表，可以按时间排序并筛选未审核用户
     * @param sortBy 排序字段，可选值：applicationTime, reviewTime
     * @param order 排序方式，可选值：asc, desc
     * @param status 筛选用户状态，可选值：0,1,2(PENDING, APPROVED, REJECTED)
     * @return
     */
    @GetMapping("/reviewList")
    public Result getReviewList(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) UserStatus status) {

        // 获取所有用户列表
        List<User> userList = userService.lambdaQuery().list();

        // 如果需要筛选状态
        if (status != null) {
            userList = userList.stream()
                    .filter(user -> user.getStatus() == status)
                    .collect(Collectors.toList());
        }

        // 如果需要排序
        if (sortBy != null) {
            Comparator<User> comparator = null;
            if ("applicationTime".equals(sortBy)) {
                comparator = Comparator.comparing(User::getApplicationTime);
            } else if ("reviewTime".equals(sortBy)) {
                comparator = Comparator.comparing(User::getReviewTime);
            }

            if (comparator != null) {
                if ("desc".equalsIgnoreCase(order)) {
                    comparator = comparator.reversed();
                }
                userList = userList.stream()
                        .sorted(comparator)
                        .collect(Collectors.toList());
            }
        }

        // 转换为 VO 对象
        List<ReviewUserVo> reviewList = BeanUtil.copyToList(userList, ReviewUserVo.class);

        // 返回结果
        return Result.ok(reviewList);
    }


    @OperationLog("审核用户")
    @PostMapping("/review")
    public Result reviewUser(Integer userId ,Integer status){
        User user = userService.lambdaQuery().eq(User::getId, userId).one();
        if(!(status.equals(1) || status.equals(2))){
            return Result.fail("请输入正确的状态");
        }
        if(status.equals(2)) {
            // 审核拒绝逻辑 - 存入 Redis 并设置 5 分钟有效期
            String redisKey = "user:reject:" + user.getId(); // Redis Key 格式
            stringRedisTemplate.opsForValue().set(redisKey, user.getId().toString(), 5, TimeUnit.MINUTES);
        }
        user.setStatus(UserStatus.of(status))
                .setReviewTime(LocalDateTime.now());
        userService.updateById(user);
        return Result.ok("更新用户状态成功");
    }

}
