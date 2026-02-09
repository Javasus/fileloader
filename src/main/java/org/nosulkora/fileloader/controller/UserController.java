package org.nosulkora.fileloader.controller;

import org.nosulkora.fileloader.entity.User;
import org.nosulkora.fileloader.repository.UserRepository;

import java.util.List;
import java.util.Objects;

public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String name) {
        User user = new User();
        user.setName(name);
        return userRepository.save(user);
    }

    public User updateUser(Integer id, String name) {
        User user = getUserById(id);
        if (Objects.nonNull(user)) {
            user.setName(name);
            return userRepository.update(user);
        }
        return null;
    }

    public User getUserById(Integer id) {
        return userRepository.getById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.getAll();
    }

    public boolean deleteUserById(Integer id) {
        return userRepository.deleteById(id);
    }
}
