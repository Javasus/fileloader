package org.nosulkora.fileloader.controller;

import org.nosulkora.fileloader.entity.File;
import org.nosulkora.fileloader.repository.FileRepository;
import org.nosulkora.fileloader.repository.impl.FileRepositoryImpl;

import java.util.List;
import java.util.Objects;

public class FileController {

    private final FileRepository fileRepository;

    public FileController(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public File createFile(String name, String filePath) {
        File file = new File(name, filePath);
        return fileRepository.save(file);
    }

    public File updateFile(Integer id, String name, String filePath) {
        File fileById = fileRepository.getById(id);
        if (Objects.nonNull(fileById)) {
            fileById.setName(name);
            fileById.setFilePath(filePath);
            return fileRepository.update(fileById);
        }
        return null;
    }

    public File getFileById(Integer id) {
        return fileRepository.getById(id);
    }

    public List<File> getAllFiles() {
        return fileRepository.getAll();
    }

    public boolean deleteFileById(Integer id) {
        return fileRepository.deleteById(id);
    }
}
