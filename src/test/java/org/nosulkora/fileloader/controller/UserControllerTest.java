package org.nosulkora.fileloader.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nosulkora.fileloader.entity.User;
import org.nosulkora.fileloader.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserControllerTest")
public class UserControllerTest {

    private static final Integer USER_ID = 1;
    private static final String USER_NAME = "Ivan Ivanov";
    private static final String NEW_USER_NAME = "Roman Romanov";

    private UserController userController;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userController = new UserController(userRepository);
    }

    @Test
    @DisplayName("Create User")
    void createUserTest() {
        User expectedUser = new User(USER_ID, USER_NAME, new ArrayList<>());
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        User actualUser = userController.createUser(USER_NAME);
        assertAll(
                () -> assertEquals(expectedUser.getId(), actualUser.getId(), "userId"),
                () -> assertEquals(expectedUser.getName(), actualUser.getName(), "userName"),
                () -> assertEquals(expectedUser.getEvents(), actualUser.getEvents(), "eventsUser")
        );
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Update User")
    void updateUser() {

        User existingdUser = new User(USER_ID, USER_NAME, new ArrayList<>());
        User updatedUser = new User(USER_ID, NEW_USER_NAME, new ArrayList<>());
        when(userRepository.getById(USER_ID)).thenReturn(existingdUser);
        when(userRepository.update(any(User.class))).thenReturn(updatedUser);
        User actualUser = userController.updateUser(USER_ID, NEW_USER_NAME);
        assertAll(
                () -> assertEquals(updatedUser.getId(), actualUser.getId(), "userId"),
                () -> assertEquals(updatedUser.getName(), actualUser.getName(), "userName"),
                () -> assertEquals(updatedUser.getEvents(), actualUser.getEvents(), "EventUser")
        );
        verify(userRepository, times(1)).getById(USER_ID);
        verify(userRepository, times(1)).update(any(User.class));
    }

    @Test
    @DisplayName("Get user by ID")
    void getUserByIdTest() {
        User expectedUser = new User(USER_ID, USER_NAME, new ArrayList<>());
        when(userRepository.getById(USER_ID)).thenReturn(expectedUser);
        User actualUser = userController.getUserById(USER_ID);
        assertAll(
                () -> assertEquals(expectedUser.getId(), actualUser.getId(), "userId"),
                () -> assertEquals(expectedUser.getName(), actualUser.getName(), "userName"),
                () -> assertEquals(expectedUser.getEvents(), actualUser.getEvents(), "eventsUser")
        );
        verify(userRepository, times(1)).getById(USER_ID);
    }

    @Test
    @DisplayName("Get all users")
    void getAllUsersTest() {
        User firstUser = new User(USER_ID, USER_NAME, new ArrayList<>());
        User secondUser = new User(2, NEW_USER_NAME, new ArrayList<>());
        List<User> expectedUsers = List.of(firstUser, secondUser);
        when(userRepository.getAll()).thenReturn(expectedUsers);
        List<User> actualUsers = userController.getAllUsers();
        assertThat(expectedUsers)
                .describedAs("Check all users")
                .hasSameSizeAs(actualUsers)
                .containsExactlyInAnyOrderElementsOf(actualUsers);
        verify(userRepository, times(1)).getAll();
    }

    @Test
    @DisplayName("Delete user by ID")
    void deleteUserById() {
        when(userRepository.deleteById(USER_ID)).thenReturn(true);
        boolean result = userController.deleteUserById(USER_ID);
        assertTrue(result);
        verify(userRepository, times(1)).deleteById(USER_ID);
    }

    @Test
    @DisplayName("Update user but user is not found - return null")
    void updateUserNotFoundTest() {
        when(userRepository.getById(USER_ID)).thenReturn(null);
        User actualUser = userController.updateUser(USER_ID, NEW_USER_NAME);
        assertNull(actualUser);
        verify(userRepository, times(1)).getById(USER_ID);
        verify(userRepository, never()).update(any(User.class));
    }
}
