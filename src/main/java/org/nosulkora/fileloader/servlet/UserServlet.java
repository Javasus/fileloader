package org.nosulkora.fileloader.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nosulkora.fileloader.controller.UserController;
import org.nosulkora.fileloader.entity.User;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {

    private final UserController userController = new UserController();
    private final ObjectMapper mapper = new ObjectMapper();

    // create User -> POST api/users
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        setUTF8Encoding(req, resp);
        resp.setContentType("application/json");

        try {
            User user = mapper.readValue(req.getReader(), User.class);
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Name is required\"}");
                return;
            }

            User createdUser = userController.createUser(user.getName());
            if (createdUser != null) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                mapper.writeValue(resp.getWriter(), createdUser);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Failed to create user\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid JSON\"}");
        }
    }

    // update User -> PUT /api/users/1
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        setUTF8Encoding(req, resp);
        resp.setContentType("application/json");

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"User ID required\"}");
            }

            Integer id = extractId(pathInfo);
            User user = mapper.readValue(req.getReader(), User.class);
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Name is required\"}");
                return;
            }

            User updateUser = userController.updateUser(id, user.getName());
            if (updateUser != null) {
                mapper.writeValue(resp.getWriter(), updateUser);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"User not found\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid user ID\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid Json\"}");
        }
    }

    // get all User OR get User by ID -> GET /api/users OR /api/users/1
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        setUTF8Encoding(req, resp);
        resp.setContentType("application/json");

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // Все пользователи
            List<User> users = userController.getAllUsers();
            mapper.writeValue(resp.getWriter(), users);
        } else {
            // Конкретный пользователь
            try {
                Integer id = extractId(pathInfo);
                User user = userController.getUserById(id);
                if (user != null) {
                    mapper.writeValue(resp.getWriter(), user);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"User not found\"}");
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Invalid user ID\"}");
            }
        }
    }

    // Delete User -> DELETE /api/users/1
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        setUTF8Encoding(req, resp);
        resp.setContentType("application/json");

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"User ID required\"}");
                return;
            }

            Integer id = extractId(pathInfo);
            boolean resultDelete = userController.deleteUserById(id);
            if (resultDelete) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"User not found\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid user ID\"}");
        }
    }

    private Integer extractId(String pathInfo) throws NumberFormatException {
        if (pathInfo != null) {
            String idStr = pathInfo.replace("/", "");
            return Integer.parseInt(idStr);
        }
        throw new NumberFormatException();
    }

    private void setUTF8Encoding(HttpServletRequest req, HttpServletResponse resp) {
        try {
            req.setCharacterEncoding("UTF-8");
        } catch (Exception e) {
            // ignore
        }
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
    }
}
