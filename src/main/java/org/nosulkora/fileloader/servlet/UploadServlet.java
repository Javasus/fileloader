package org.nosulkora.fileloader.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.nosulkora.fileloader.controller.EventController;
import org.nosulkora.fileloader.controller.FileController;
import org.nosulkora.fileloader.controller.UserController;
import org.nosulkora.fileloader.entity.Event;
import org.nosulkora.fileloader.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

@WebServlet("/api/files/*")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,   // 1MB
        maxFileSize = 1024 * 1024 * 10,    // 10MB
        maxRequestSize = 1024 * 1024 * 50  // 50MB
)
public class UploadServlet extends HttpServlet {

    private static final ServletUtils SERVLET_UTILS = new ServletUtils();
    private static final Logger logger = LoggerFactory.getLogger(UploadServlet.class);
    private static final String UPLOAD_DIR = "C:/javaStudy/fileloader/src/main/resources/uploads";

    private final EventController eventController = new EventController();
    private final FileController fileController = new FileController();
    private final UserController userController = new UserController();
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void init() throws ServletException {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            logger.debug("Папка создана: " + created + " | Путь: " + UPLOAD_DIR);
        }
        logger.info("Папка для загрузок: " + UPLOAD_DIR);
    }

    //upload File -> post api/files
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        setUTF8Encoding(req, resp);
        resp.setContentType("application/json");

        try {
            if (!req.getContentType().startsWith("multipart/form-data")) {
                returnError(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "{\"error\": \"Требуется multipart/form-data запрос\"}"
                );
                return;
            }

            String userIdStr = req.getParameter("userId");
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                returnError(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "{\"error\": \"Файл не предоставлен\"}"
                );
                return;
            }

            Integer userId;
            User user = null;
            if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                try {
                    userId = Integer.parseInt(userIdStr);
                    user = userController.getUserById(userId);
                    if (Objects.isNull(user)) {
                        returnError(
                                resp,
                                HttpServletResponse.SC_BAD_REQUEST,
                                "{\"error\": \"Пользователь не найден\"}"
                        );
                        return;
                    }

                } catch (NumberFormatException e) {
                    returnError(
                            resp,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "{\"error\": \"Некорректный ID пользователя\"}"
                    );
                    return;
                }
            }

            String originalFileName = SERVLET_UTILS.getFilePart(filePart);
            // Проверяем что название файла не содержит вредоносных инъекций.
            if (originalFileName.contains("..") || originalFileName.contains("/")
                || originalFileName.contains("\\")) {
                returnError(resp, 400, "{\"error\": \"Некорректное имя файла\"}");
                return;
            }
            String fileExtension = SERVLET_UTILS.getFileExtension(originalFileName);
            String uniqueFileName = SERVLET_UTILS.generateUniqueFileName(fileExtension);
            Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);
            Files.copy(filePart.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Файл сохранён - " + filePath);

            org.nosulkora.fileloader.entity.File savedFile =
                    fileController.createFile(originalFileName, filePath.toString());

            if (Objects.isNull(savedFile)) {
                returnError(
                        resp,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "{\"error\": \"Не удалось сохранить информацию о файле в БД\"}"
                );
                return;
            }
            if (Objects.nonNull(user)) {
                Event returnEvent = eventController.createEvent(user, savedFile);
                if (Objects.isNull(returnEvent)) {
                    returnError(
                            resp,
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "{\"error\": \"Не удалось сохранить event в БД\"}"
                    );
                    return;
                }
            }

            UploadResponse response = new UploadResponse(
                    savedFile.getId(),
                    originalFileName,
                    uniqueFileName,
                    savedFile.getFilePath(),
                    filePart.getSize(),
                    "Файл успешно загружен"
            );

            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), response);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(
                    resp.getWriter(),
                    "{\"error\": \"Ошибка при загрузке файла: " + e.getMessage() + "\"}"
            );
        }
    }

    // update file -> put api/files/1 , где 1 - это fileId.
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setUTF8Encoding(req, resp);
        resp.setContentType("application/json");

        try {
            if (!req.getContentType().startsWith("multipart/form-data")) {
                returnError(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "{\"error\": \"Требуется multipart/form-data запрос\"}"
                );
                return;
            }

            String pathInfo = req.getPathInfo();

            Part filePart = req.getPart("file");

            if (filePart == null || filePart.getSize() == 0) {
                returnError(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "{\"error\": \"Файл не предоставлен\"}"
                );
                return;
            }

            Integer fileId = null;
            org.nosulkora.fileloader.entity.File file = null;
            if (pathInfo != null && !pathInfo.trim().isEmpty()) {
                try {
                    fileId = SERVLET_UTILS.extractId(pathInfo);
                    file = fileController.getFileById(fileId);
                    if (Objects.isNull(file)) {
                        returnError(
                                resp,
                                HttpServletResponse.SC_BAD_REQUEST,
                                "{\"error\": \"Файл не найден\"}"
                        );
                        return;
                    }

                } catch (NumberFormatException e) {
                    returnError(
                            resp,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "{\"error\": \"Некорректный ID файла\"}"
                    );
                    return;
                }
            }

            String originalFileName = SERVLET_UTILS.getFilePart(filePart);
            // Проверяем что название файла не содержит вредоносных инъекций.
            if (originalFileName.contains("..") || originalFileName.contains("/")
                || originalFileName.contains("\\")) {
                returnError(resp, 400, "{\"error\": \"Некорректное имя файла\"}");
                return;
            }
            String fileExtension = SERVLET_UTILS.getFileExtension(originalFileName);
            String uniqueFileName = SERVLET_UTILS.generateUniqueFileName(fileExtension);
            Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);
            // сохраняем старый файл
            String oldFilePath = file.getFilePath();
            File oldFile = new File(oldFilePath);
            // сохраняем новый файл
            Files.copy(filePart.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            // удаляем старый файл
            if (oldFile.exists() && !oldFile.getPath().equals(filePath.toString())) {
                boolean deleted = oldFile.delete();
                if (!deleted) {
                    logger.warn("Не удалось удалить старый файл: {}", oldFilePath);
                }
            }
            logger.info("Файл сохранён - " + filePath);

            org.nosulkora.fileloader.entity.File updatedFile =
                    fileController.updateFile(fileId, originalFileName, filePath.toString());

            if (Objects.isNull(updatedFile)) {
                returnError(
                        resp,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "{\"error\": \"Не удалось обновить файл в БД\"}"
                );
                return;
            }

            Event latestEvent = eventController.findLatestByFileId(fileId);
            if (Objects.nonNull(latestEvent)) {
                User user = latestEvent.getUser();
                Event newEvent = eventController.createEvent(user, updatedFile);
                if (Objects.isNull(newEvent)) {
                    returnError(
                            resp,
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "{\"error\": \"Не удалось сохранить event в БД\"}"
                    );
                    return;
                }
            }

            UploadResponse response = new UploadResponse(
                    updatedFile.getId(),
                    originalFileName,
                    uniqueFileName,
                    updatedFile.getFilePath(),
                    filePart.getSize(),
                    "Файл успешно обновлен"
            );

            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), response);
        } catch (NumberFormatException e) {
            returnError(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "{\"error\": \"Некорректный ID файла\"}"
            );
        } catch (Exception e) {
            returnError(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"error\": \"Ошибка при обновлении файла: " + e.getMessage() + "\"}"
            );
        }
    }

    // get all files OR download File -> GET api/files OR api/files/1
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setUTF8Encoding(req, resp);

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            List<org.nosulkora.fileloader.entity.File> files = fileController.getAllFiles();
            objectMapper.writeValue(resp.getWriter(), files);
        } else {
            try {
                Integer fileId = SERVLET_UTILS.extractId(pathInfo);
                org.nosulkora.fileloader.entity.File fileEntity = fileController.getFileById(fileId);

                if (Objects.isNull(fileEntity)) {
                    returnError(
                            resp,
                            HttpServletResponse.SC_NOT_FOUND,
                            "{\"error\": \"Файл не найден\"}"
                    );
                    return;
                }

                File physicalFile = new File(fileEntity.getFilePath());
                if (!SERVLET_UTILS.isSafePath(UPLOAD_DIR, physicalFile.getPath())) {
                    returnError(resp, 403, "{\"error\": \"Отказано в доступе\"}");
                    return;
                }
                if (!physicalFile.exists()) {
                    returnError(
                            resp,
                            HttpServletResponse.SC_NOT_FOUND,
                            "{\"error\": \"Физический файл не найден\"}"
                    );
                    return;
                }

                resp.setContentType(getServletContext().getMimeType(fileEntity.getName()));
                resp.setHeader(
                        "Content-Disposition",
                        "attachment; filename=\"" + fileEntity.getName() + "\""
                );
                resp.setContentLength((int) physicalFile.length());

                // TODO Если не будет работать вывод вернуть старый обратно.
//                Files.copy(physicalFile.toPath(), resp.getOutputStream());
                try (InputStream in = Files.newInputStream(physicalFile.toPath());
                     OutputStream out = resp.getOutputStream()) {
                    in.transferTo(out);
                } catch (IOException e) {
                    logger.error("Ошибка отправки файла", e);
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(
                        resp.getWriter(),
                        "{\"error\": \"Некорректный ID файла\"}"
                );
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(
                        resp.getWriter(),
                        "{\"error\": \"Ошибка при скачивани файла: " + e.getMessage() + "\"}"
                );
            }
        }
    }

    // Delete File -> DELETE api/files/1
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setUTF8Encoding(req, resp);
        resp.setContentType("application/json");
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                returnError(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "{\"error\": \"ID файла отсутствует\"}"
                );
                return;
            }

            int fileId = SERVLET_UTILS.extractId(pathInfo);

            org.nosulkora.fileloader.entity.File file = fileController.getFileById(fileId);
            if (Objects.isNull(file)) {
                returnError(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "{\"error\": \"Файл по ID = %s не найден\"}".formatted(fileId)
                );
                return;
            }

            Event latestEvent = eventController.findLatestByFileId(fileId);
            if (Objects.nonNull(latestEvent)) {
                User user = latestEvent.getUser();
                Event newEvent = eventController.createEvent(user, file);
                if (Objects.isNull(newEvent)) {
                    returnError(
                            resp,
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "{\"error\": \"Не удалось сохранить event в БД\"}"
                    );
                    return;
                }
            }

            boolean resultDelete = fileController.deleteFileById(fileId);
            if (resultDelete) {
                File physicalFile = new File(file.getFilePath());
                if (physicalFile.exists()) {
                    boolean deleted = physicalFile.delete();
                    if (!deleted) {
                        logger.warn("Не удалось удалить физический файл: {}", file.getFilePath());
                    }
                }
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Файл не найден\"}");
            }
        } catch (NumberFormatException e) {
            returnError(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "{\"error\": \"Некорректный ID файла\"}"
            );
        } catch (Exception e) {
            returnError(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"error\": \"Ошибка при удалении файла: " + e.getMessage() + "\"}"
            );
        }
    }

    /**
     * Возвращает ответ с ошибкой.
     */
    private void returnError(
            HttpServletResponse resp,
            int scBadRequest,
            String answer
    ) throws IOException {
        resp.setStatus(scBadRequest);
        objectMapper.writeValue(resp.getWriter(), answer);
    }

    /**
     * Меняет кодировку на UTF-8.
     */
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
