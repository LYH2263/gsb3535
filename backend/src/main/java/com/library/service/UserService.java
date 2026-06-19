package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.domain.LibraryUser;
import com.library.dto.UserRequest;
import com.library.mapper.LibraryUserMapper;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final LibraryUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(LibraryUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<LibraryUser> listAll() {
        return userMapper.selectList(new LambdaQueryWrapper<>());
    }

    public LibraryUser getById(Long id) {
        return userMapper.selectById(id);
    }

    public LibraryUser findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<LibraryUser>().eq(LibraryUser::getUsername, username));
    }

    @Transactional
    public LibraryUser create(UserRequest request, String rawPassword) {
        LibraryUser user = new LibraryUser();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setPassword(passwordEncoder.encode(rawPassword));
        userMapper.insert(user);
        return user;
    }

    @Transactional
    public LibraryUser update(Long id, UserRequest request) {
        LibraryUser existing = userMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        if ("ADMIN".equals(existing.getRole())) {
            throw new RuntimeException("不允许编辑管理员角色账号");
        }
        existing.setUsername(request.getUsername());
        existing.setFullName(request.getFullName());
        existing.setRole(request.getRole());
        userMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public boolean delete(Long id) {
        LibraryUser user = userMapper.selectById(id);
        if (user != null && "ADMIN".equals(user.getRole())) {
            throw new RuntimeException("不允许删除管理员角色账号");
        }
        return userMapper.deleteById(id) > 0;
    }
}
