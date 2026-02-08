package org.nosulkora.fileloader.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nosulkora.fileloader.entity.File;
import org.nosulkora.fileloader.repository.FileRepository;
import org.nosulkora.fileloader.repository.impl.FileRepositoryImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("FileControllerTest")
public class FileControllerTest {

    private static final Integer FILE_ID = 1;
    private static final String FILE_NAME = "text.txt";
    private static final String FILE_PATH = "C:/fileloader/resources/uploadfifle/text.txt";

    private FileRepository fileRepository;
    private FileController fileController;

    @BeforeEach
    void setUp() {
        fileRepository = mock(FileRepositoryImpl.class);
        fileController = new FileController(fileRepository);
    }

    @Test
    @DisplayName("Create file")
    void createFileTest() {
        File expectedFile = new File(FILE_ID, FILE_NAME, FILE_PATH);
        when(fileRepository.save(any(File.class))).thenReturn(expectedFile);
        File actualFile = fileController.createFile(FILE_NAME, FILE_PATH);
        assertAll(
                () -> assertEquals(expectedFile.getId(), actualFile.getId(), "fileId"),
                () -> assertEquals(expectedFile.getName(), actualFile.getName(), "fileName"),
                () -> assertEquals(expectedFile.getFilePath(), actualFile.getFilePath(), "filePath")
        );
        verify(fileRepository, times(1)).save(any(File.class));
    }

    @Test
    @DisplayName("Update file")
    void updateFileTest() {
        File existingFile = new File(FILE_ID, FILE_NAME, FILE_PATH);
        File updatedFile = new File(
                FILE_ID,
                "update_text.txt",
                "C:/fileloader/resources/uploadfifle/update_text.txt");
        when(fileRepository.getById(FILE_ID)).thenReturn(existingFile);
        when(fileRepository.update(any(File.class))).thenReturn(updatedFile);
        File actualFile = fileController.updateFile(
                FILE_ID,
                "update_text.txt",
                "C:/fileloader/resources/uploadfifle/update_text.txt");
        assertAll(
                () -> assertEquals(updatedFile.getId(), actualFile.getId(), "fileId"),
                () -> assertEquals(updatedFile.getName(), actualFile.getName(), "fileName"),
                () -> assertEquals(updatedFile.getFilePath(), actualFile.getFilePath(), "filePath")
        );
        verify(fileRepository, times(1)).getById(FILE_ID);
        verify(fileRepository, times(1)).update(any(File.class));
    }

    @Test
    @DisplayName("Get file by id")
    void getFileByIdTest() {
        File expectedFile = new File(FILE_ID, FILE_PATH, FILE_PATH);
        when(fileRepository.getById(FILE_ID)).thenReturn(expectedFile);
        File actualFile = fileController.getFileById(FILE_ID);
        assertAll(
                () -> assertEquals(expectedFile.getId(), actualFile.getId(), "fileId"),
                () -> assertEquals(expectedFile.getName(), actualFile.getName(), "fileName"),
                () -> assertEquals(expectedFile.getFilePath(), actualFile.getFilePath(), "filePath")
        );
        verify(fileRepository, times(1)).getById(FILE_ID);
    }

    @Test
    @DisplayName("Get all files")
    void geyAllFilesTest() {
        File firstFile = new File(FILE_ID, FILE_NAME, FILE_PATH);
        File secondFile = new File(
                2,
                "update_text.txt",
                "C:/fileloader/resources/uploadfifle/update_text.txt");
        List<File> expectedFiles = List.of(firstFile, secondFile);
        when(fileRepository.getAll()).thenReturn(expectedFiles);
        List<File> actualFiles = fileController.getAllFiles();
        assertThat(expectedFiles)
                .describedAs("Check get all files")
                .containsExactlyInAnyOrderElementsOf(actualFiles);
        verify(fileRepository, times(1)).getAll();
    }

    @Test
    @DisplayName("Delete file by ID")
    void deleteFileByIdTest() {
        when(fileRepository.deleteById(FILE_ID)).thenReturn(true);
        boolean result = fileController.deleteFileById(FILE_ID);
        assertTrue(result);
        verify(fileRepository, times(1)).deleteById(FILE_ID);
    }

    @Test
    @DisplayName("Update file but file not found - return null")
    void updateFileNotFoundTest() {
        when(fileRepository.getById(FILE_ID)).thenReturn(null);
        File actualFile = fileController.updateFile(FILE_ID, FILE_NAME, FILE_PATH);
        assertNull(actualFile);
        verify(fileRepository, times(1)).getById(FILE_ID);
        verify(fileRepository, never()).update(any(File.class));
    }
}
