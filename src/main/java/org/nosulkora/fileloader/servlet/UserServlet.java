package org.nosulkora.fileloader.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nosulkora.fileloader.controller.UserController;
import org.nosulkora.fileloader.entity.User;
import org.nosulkora.fileloader.repository.impl.UserRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(UploadServlet.class);

    private final ServletUtils servletUtils = new ServletUtils();
    private final UserController userController = new UserController(new UserRepositoryImpl());
    private final ObjectMapper objectMapper = new ObjectMapper();

    //create User -> POST api/users
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        servletUtils.setUTF8Encoding(req, resp);

        try {
            User user = objectMapper.readValue(req.getReader(), User.class);
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"\"Имя не задано\"}");
                return;
            }

            User createdUser = userController.createUser(user.getName());
            if (createdUser != null) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                objectMapper.writeValue(resp.getWriter(), createdUser);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при создании пользователя\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неккоректный JSON\"}");
            logger.error("Ошибка при создании user", e);
        }
    }

    // update User -> PUT /api/users/1
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        servletUtils.setUTF8Encoding(req, resp);

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"ID пользователя отсутствует\"}");
                return;
            }

            Integer id = servletUtils.extractId(pathInfo);
            User user = objectMapper.readValue(req.getReader(), User.class);
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Имя не задано\"}");
                return;
            }

            User updateUser = userController.updateUser(id, user.getName());
            if (updateUser != null) {
                objectMapper.writeValue(resp.getWriter(), updateUser);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Пользователь не найден\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный ID пользователя\"}");
            logger.error("Неверный userId, при обновлении пользователя");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неккоректный Json\"}");
            logger.error("Ошибка при обновлении пользователя", e);
        }
    }

    // get all User OR get User by ID -> GET /api/users OR /api/users/1
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        servletUtils.setUTF8Encoding(req, resp);
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // Все пользователи
            List<User> users = userController.getAllUsers();
            objectMapper.writeValue(resp.getWriter(), users);
        } else {
            // Конкретный пользователь
            try {
                Integer id = servletUtils.extractId(pathInfo);
                User user = userController.getUserById(id);
                if (user != null) {
                    objectMapper.writeValue(resp.getWriter(), user);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Пользователь не найден\"}");
                    return;
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный ID пользователя\"}");
                logger.error("Ошибка при возврате пользователя", e);
            }
        }
    }

    // Delete User -> DELETE /api/users/1
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        servletUtils.setUTF8Encoding(req, resp);

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"ID пользователя отсутствует\"}");
                return;
            }

            Integer id = servletUtils.extractId(pathInfo);
            boolean resultDelete = userController.deleteUserById(id);
            if (resultDelete) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Пользователь не найден\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный ID пользователя\"}");
            logger.error("ошибка при удалении пользователя", e);
        }
    }


}
