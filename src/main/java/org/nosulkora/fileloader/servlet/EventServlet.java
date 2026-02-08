package org.nosulkora.fileloader.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nosulkora.fileloader.controller.EventController;
import org.nosulkora.fileloader.entity.Event;
import org.nosulkora.fileloader.repository.impl.EventRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@WebServlet("/api/events/*")
public class EventServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(EventServlet.class);

    private final EventController eventController = new EventController(new EventRepositoryImpl());
    private final ServletUtils servletUtils = new ServletUtils();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // get all User OR get User by ID -> GET /api/events OR /api/events/1
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servletUtils.setUTF8Encoding(req, resp);
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // all events
            List<Event> events = eventController.getAllEvents();
            objectMapper.writeValue(resp.getWriter(), events);
        } else {
            // event by id
            try {
                Integer eventId = servletUtils.extractId(pathInfo);
                Event eventById = eventController.getEventById(eventId);
                if (Objects.isNull(eventById)) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Эвен не найден\"}");
                    logger.error("Эвент не найден id = {}", eventId);
                    return;
                } else {
                    objectMapper.writeValue(resp.getWriter(),eventById);
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный ID эвента\"}");
                logger.error("Неверный ID эвента", e);
                return;
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.error("При возврате эвента/эвентов произошла ошибка - ", e);
                return;
            }
        }
    }
}
