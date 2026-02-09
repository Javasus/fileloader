package org.nosulkora.fileloader.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ServletUtils {

    private static final Logger logger = LoggerFactory.getLogger(UploadServlet.class);

    /**
     * Возвращает ID из URL-строки.
     *
     * @param pathInfo URL-строка
     * @return ID
     */
    public Integer extractId(String pathInfo) throws NumberFormatException {
        if (pathInfo != null) {
            String idStr = pathInfo.replace("/", "");
            return Integer.parseInt(idStr);
        }
        throw new NumberFormatException();
    }

    /**
     * Возвращает названия файла вместе с расширением.
     *
     * @param filePart наименование файла
     * @return наименование файла в строковом представлении
     */
    public String getFilePart(Part filePart) {
        String contentDisposition = filePart.getHeader("content-disposition");
        if (contentDisposition == null) {
            return "unknown";
        }

        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1)
                        .trim().replace("\"", "");
            }
        }
        return "unknown";
    }

    /**
     * Возвращает расширение файла.
     *
     * @param fileName наименование файла
     * @return расширение
     */
    public String getFileExtension(String fileName) {
        int doIndex = fileName.lastIndexOf('.');
        return (doIndex == -1) ? "" : fileName.substring(doIndex);
    }

    /**
     * Возвращает уникальное название для сохраняемого файла.
     *
     * @param fileExtension расширение файла
     * @return наименование файла
     */
    public String generateUniqueFileName(String fileExtension) {
        return UUID.randomUUID() + fileExtension;
    }

    /**
     * Проверяет, что путь до файла соответствует установленному пути до папки с файлами.
     *
     * @param uploadDir
     * @param filepath
     * @return
     */
    public boolean isSafePath(String uploadDir, String filepath) {
        try {
            Path uploadedPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path requestedPath = Paths.get(filepath).toAbsolutePath().normalize();
            return requestedPath.startsWith(uploadedPath);
        } catch (InvalidPathException e) {
            logger.error("Указан некорректный путь", e);
            return false;
        }
    }

    public void setUTF8Encoding(HttpServletRequest req, HttpServletResponse resp) {
        try {
            req.setCharacterEncoding("UTF-8");
        } catch (Exception e) {
            logger.error("Не удалось поменять кодировку.", e);
        }
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
    }
}
