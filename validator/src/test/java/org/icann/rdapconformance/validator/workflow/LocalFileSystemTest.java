package org.icann.rdapconformance.validator.workflow;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LocalFileSystemTest {

    private LocalFileSystem fileSystem;
    private Path tempDir;
    private Path testFile;
    private Path testDir;

    @BeforeMethod
    public void setUp() throws IOException {
        fileSystem = new LocalFileSystem();
        tempDir = Files.createTempDirectory("localfilesystem-test");
        testFile = tempDir.resolve("test-file.txt");
        testDir = tempDir.resolve("test-dir");
    }

    @AfterMethod
    public void tearDown() throws IOException {
        // Clean up test files and directories recursively
        if (Files.exists(tempDir)) {
            deleteDirectoryRecursively(tempDir);
        }
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var stream = Files.list(path)) {
                stream.forEach(child -> {
                    try {
                        deleteDirectoryRecursively(child);
                    } catch (IOException e) {
                        // Log but don't fail the test cleanup
                        System.err.println("Failed to delete: " + child + " - " + e.getMessage());
                    }
                });
            }
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log but don't fail the test cleanup
            System.err.println("Failed to delete: " + path + " - " + e.getMessage());
        }
    }

    @Test
    public void testWrite_ValidFile_Success() throws IOException {
        String testData = "Hello, World!";
        String filePath = testFile.toString();

        fileSystem.write(filePath, testData);

        assertThat(Files.exists(testFile)).isTrue();
        String writtenContent = Files.readString(testFile);
        assertThat(writtenContent).isEqualTo(testData);
    }

    @Test
    public void testWrite_EmptyData_Success() throws IOException {
        String testData = "";
        String filePath = testFile.toString();

        fileSystem.write(filePath, testData);

        assertThat(Files.exists(testFile)).isTrue();
        String writtenContent = Files.readString(testFile);
        assertThat(writtenContent).isEqualTo(testData);
    }

    @Test
    public void testWrite_MultilineData_Success() throws IOException {
        String testData = "Line 1\nLine 2\nLine 3";
        String filePath = testFile.toString();

        fileSystem.write(filePath, testData);

        assertThat(Files.exists(testFile)).isTrue();
        String writtenContent = Files.readString(testFile);
        assertThat(writtenContent).isEqualTo(testData);
    }

    @Test
    public void testWrite_InvalidPath_ThrowsIOException() {
        String invalidPath = "/invalid/path/that/does/not/exist/file.txt";
        String testData = "Test data";

        assertThatThrownBy(() -> fileSystem.write(invalidPath, testData))
                .isInstanceOf(IOException.class);
    }

    @Test
    public void testWrite_NullData_ThrowsException() {
        String filePath = testFile.toString();

        assertThatThrownBy(() -> fileSystem.write(filePath, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testMkdir_NewDirectory_Success() throws IOException {
        String dirPath = testDir.toString();

        fileSystem.mkdir(dirPath);

        assertThat(Files.exists(testDir)).isTrue();
        assertThat(Files.isDirectory(testDir)).isTrue();
    }

    @Test
    public void testMkdir_DirectoryAlreadyExists_Success() throws IOException {
        Files.createDirectory(testDir);
        String dirPath = testDir.toString();

        fileSystem.mkdir(dirPath); // Should not throw exception

        assertThat(Files.exists(testDir)).isTrue();
        assertThat(Files.isDirectory(testDir)).isTrue();
    }

    @Test
    public void testMkdir_FileWithSameNameExists_ThrowsIOException() throws IOException {
        Files.createFile(testDir); // Create file with same name as directory
        String dirPath = testDir.toString();

        assertThatThrownBy(() -> fileSystem.mkdir(dirPath))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cannot create directory")
                .hasMessageContaining("a file with the same name already exists");
    }

    @Test
    public void testMkdir_InvalidPath_ThrowsIOException() {
        // Try to create directory in a location that doesn't exist
        String invalidPath = "/invalid/deeply/nested/path/that/cannot/be/created";

        assertThatThrownBy(() -> fileSystem.mkdir(invalidPath))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cannot create directory");
    }

    @Test
    public void testReadFile_ValidFile_Success() throws IOException {
        String testData = "Hello, World!\nSecond line";
        Files.writeString(testFile, testData);
        URI fileUri = testFile.toUri();

        String readContent = fileSystem.readFile(fileUri);

        assertThat(readContent).isEqualTo(testData);
    }

    @Test
    public void testReadFile_EmptyFile_Success() throws IOException {
        Files.createFile(testFile);
        URI fileUri = testFile.toUri();

        String readContent = fileSystem.readFile(fileUri);

        assertThat(readContent).isEmpty();
    }

    @Test
    public void testReadFile_NonExistentFile_ThrowsIOException() {
        URI nonExistentUri = testFile.toUri();

        assertThatThrownBy(() -> fileSystem.readFile(nonExistentUri))
                .isInstanceOf(IOException.class);
    }

    @Test
    public void testReadFile_Directory_ThrowsIOException() throws IOException {
        Files.createDirectory(testDir);
        URI dirUri = testDir.toUri();

        assertThatThrownBy(() -> fileSystem.readFile(dirUri))
                .isInstanceOf(IOException.class);
    }

    @Test
    public void testExists_FileExists_ReturnsTrue() throws IOException {
        Files.createFile(testFile);
        String filePath = testFile.toString();

        boolean exists = fileSystem.exists(filePath);

        assertThat(exists).isTrue();
    }

    @Test
    public void testExists_DirectoryExists_ReturnsTrue() throws IOException {
        Files.createDirectory(testDir);
        String dirPath = testDir.toString();

        boolean exists = fileSystem.exists(dirPath);

        assertThat(exists).isTrue();
    }

    @Test
    public void testExists_FileDoesNotExist_ReturnsFalse() {
        String filePath = testFile.toString();

        boolean exists = fileSystem.exists(filePath);

        assertThat(exists).isFalse();
    }

    @Test
    public void testExists_NullPath_ThrowsNullPointerException() {
        assertThatThrownBy(() -> fileSystem.exists(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testExists_EmptyPath_ReturnsFalse() {
        boolean exists = fileSystem.exists("");

        assertThat(exists).isFalse();
    }

    @Test
    public void testDownload_LocalFileURI_Success() throws IOException {
        // Create source file
        String sourceData = "Source file content";
        Path sourceFile = tempDir.resolve("source.txt");
        Files.writeString(sourceFile, sourceData);
        URI sourceUri = sourceFile.toUri();

        // Download to target file
        String targetPath = testFile.toString();
        fileSystem.download(sourceUri, targetPath);

        assertThat(Files.exists(testFile)).isTrue();
        String downloadedContent = Files.readString(testFile);
        assertThat(downloadedContent).isEqualTo(sourceData);
    }

    @Test
    public void testDownload_NonExistentURI_ThrowsIOException() {
        URI nonExistentUri = tempDir.resolve("non-existent.txt").toUri();
        String targetPath = testFile.toString();

        assertThatThrownBy(() -> fileSystem.download(nonExistentUri, targetPath))
                .isInstanceOf(IOException.class);
    }

    @Test
    public void testDownload_InvalidTargetPath_ThrowsIOException() throws IOException {
        // Create source file
        Path sourceFile = tempDir.resolve("source.txt");
        Files.writeString(sourceFile, "content");
        URI sourceUri = sourceFile.toUri();

        String invalidTargetPath = "/invalid/path/target.txt";

        assertThatThrownBy(() -> fileSystem.download(sourceUri, invalidTargetPath))
                .isInstanceOf(IOException.class);
    }

    @Test
    public void testUriToStream_AbsoluteFileURI_Success() throws IOException {
        String testData = "Stream test content";
        Files.writeString(testFile, testData);
        URI fileUri = testFile.toUri();

        try (InputStream inputStream = fileSystem.uriToStream(fileUri)) {
            String content = new String(inputStream.readAllBytes());
            assertThat(content).isEqualTo(testData);
        }
    }

    @Test
    public void testUriToStream_RelativeURI_Success() throws IOException {
        // Create file in current directory
        String testData = "Relative path test";
        Path currentDirFile = Paths.get("test-relative.txt");
        Files.writeString(currentDirFile, testData);

        try {
            URI relativeUri = URI.create("test-relative.txt");

            try (InputStream inputStream = fileSystem.uriToStream(relativeUri)) {
                String content = new String(inputStream.readAllBytes());
                assertThat(content).isEqualTo(testData);
            }
        } finally {
            // Clean up
            Files.deleteIfExists(currentDirFile);
        }
    }

    @Test
    public void testUriToStream_URIWithoutScheme_Success() throws IOException {
        String testData = "No scheme test";
        Files.writeString(testFile, testData);
        URI uriWithoutScheme = URI.create(testFile.toString());

        try (InputStream inputStream = fileSystem.uriToStream(uriWithoutScheme)) {
            String content = new String(inputStream.readAllBytes());
            assertThat(content).isEqualTo(testData);
        }
    }

    @Test
    public void testUriToStream_NonExistentFile_ThrowsIOException() {
        URI nonExistentUri = testFile.toUri();

        assertThatThrownBy(() -> fileSystem.uriToStream(nonExistentUri))
                .isInstanceOf(IOException.class);
    }

    @Test
    public void testWrite_OverwriteExistingFile_Success() throws IOException {
        String originalData = "Original content";
        String newData = "New content";
        String filePath = testFile.toString();

        // Write original content
        fileSystem.write(filePath, originalData);
        assertThat(Files.readString(testFile)).isEqualTo(originalData);

        // Overwrite with new content
        fileSystem.write(filePath, newData);
        assertThat(Files.readString(testFile)).isEqualTo(newData);
    }

    @Test
    public void testReadFile_LargeFile_Success() throws IOException {
        // Create a larger file
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeContent.append("Line ").append(i).append("\n");
        }
        String testData = largeContent.toString();
        Files.writeString(testFile, testData);
        URI fileUri = testFile.toUri();

        String readContent = fileSystem.readFile(fileUri);

        assertThat(readContent).isEqualTo(testData.trim()); // trim because of line separator handling
    }

    @Test
    public void testDownload_EmptyFile_Success() throws IOException {
        // Create empty source file
        Path sourceFile = tempDir.resolve("empty-source.txt");
        Files.createFile(sourceFile);
        URI sourceUri = sourceFile.toUri();

        // Download to target file
        String targetPath = testFile.toString();
        fileSystem.download(sourceUri, targetPath);

        assertThat(Files.exists(testFile)).isTrue();
        assertThat(Files.size(testFile)).isEqualTo(0);
    }
}