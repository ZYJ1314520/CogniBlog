package com.example.cogniblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.cogniblog.common.BusinessException;
import com.example.cogniblog.dto.LoginRequest;
import com.example.cogniblog.dto.RegisterRequest;
import com.example.cogniblog.dto.UserUpdateRequest;
import com.example.cogniblog.entity.User;
import com.example.cogniblog.mapper.UserMapper;
import com.example.cogniblog.util.JwtTool;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtTool jwtTool;

    public Map<String, Object> register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(DigestUtil.md5Hex(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("user");
        userMapper.insert(user);

        // 生成 Token
        String token = jwtTool.generateToken(user.getId(), user.getUsername(), user.getRole());
        return buildResult(user, token);
    }

    public Map<String, Object> login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 验证密码
        String passwordHash = DigestUtil.md5Hex(request.getPassword());
        if (!passwordHash.equals(user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 生成 Token
        String token = jwtTool.generateToken(user.getId(), user.getUsername(), user.getRole());
        return buildResult(user, token);
    }

    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 密码脱敏
        user.setPassword(null);
        return user;
    }

    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    public void updateUser(UserUpdateRequest request) {
        User user = userMapper.selectById(request.getId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userMapper.updateById(user);
    }

    public String getUsernameById(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getUsername() : "未知用户";
    }

    public Long getUserIdByToken(String token) {
        if (token == null || !jwtTool.validateToken(token)) {
            return null;
        }
        return jwtTool.getUserId(token);
    }

    public boolean isAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        String oldHash = DigestUtil.md5Hex(oldPassword);
        if (!oldHash.equals(user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        user.setPassword(DigestUtil.md5Hex(newPassword));
        userMapper.updateById(user);
    }

    private Map<String, Object> buildResult(User user, String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("role", user.getRole());
        return result;
    }
}
