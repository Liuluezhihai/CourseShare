package com.courseshare.resource.service;

import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.InvalidPathException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "md", "txt", "zip"
    );

    private final Path storageRoot;
    private final long maximumSize;

    public FileService(
            @Value("${storage.root}") String storageRoot,
            @Value("${upload.max-size:52428800}") long maximumSize) {
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
        this.maximumSize = maximumSize;
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > maximumSize) {
            throw new BusinessException(ErrorCode.FILE_REJECTED, "附件超过允许的大小");
        }
        String originalName = sanitizeName(file.getOriginalFilename());
        String extension = extensionOf(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_REJECTED, "不支持该附件类型");
        }

        String relativePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"))
                + "/" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = safeResolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "附件保存失败");
        }
        String mime = file.getContentType();
        if (mime == null || mime.isBlank()) {
            mime = "application/octet-stream";
        } else if (mime.length() > 100) {
            mime = mime.substring(0, 100);
        }
        return new StoredFile(
                originalName,
                relativePath.replace('\\', '/'),
                extension,
                mime,
                file.getSize()
        );
    }

    public DownloadFile resolveForDownload(String relativePath, String originalName, String mime) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "该资源没有附件");
        }
        Path target = safeResolve(relativePath);
        try {
            Path realRoot = Files.exists(storageRoot) ? storageRoot.toRealPath() : storageRoot;
            Path realTarget = target.toRealPath();
            if (!realTarget.startsWith(realRoot) || !Files.isRegularFile(realTarget)) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "附件不存在");
            }
            return new DownloadFile(realTarget, originalName, mime);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "附件不存在");
        }
    }

    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(safeResolve(relativePath));
        } catch (IOException ignored) {
            // A scheduled cleanup job can remove an orphan later.
        }
    }

    private Path safeResolve(String relativePath) {
        Path path = storageRoot.resolve(relativePath).normalize();
        if (!path.startsWith(storageRoot)) {
            throw new BusinessException(ErrorCode.FILE_REJECTED, "附件路径不合法");
        }
        return path;
    }

    private String sanitizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(ErrorCode.FILE_REJECTED, "附件名称不能为空");
        }
        String normalizedName = name.replace('\\', '/');
        String cleaned = StringUtils.cleanPath(normalizedName);
        if (cleaned.contains("..") || cleaned.indexOf('\0') >= 0) {
            throw new BusinessException(ErrorCode.FILE_REJECTED, "附件名称不合法");
        }
        String base;
        try {
            base = Paths.get(cleaned).getFileName().toString();
        } catch (InvalidPathException exception) {
            throw new BusinessException(ErrorCode.FILE_REJECTED, "附件名称不合法");
        }
        if (base.length() > 200) {
            String extension = extensionOf(base);
            base = base.substring(0, Math.min(180, base.length() - extension.length() - 1))
                    + "." + extension;
        }
        return base;
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public record StoredFile(
            String originalName,
            String relativePath,
            String extension,
            String mime,
            long size) {
    }

    public record DownloadFile(Path path, String originalName, String mime) {
    }
}

