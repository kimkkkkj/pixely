package com.pixely.global.storage;

import com.pixely.global.config.PixelyProperties;
import com.pixely.global.exception.BusinessException;
import com.pixely.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
public class LocalStorageService implements StorageService {

    private static final String URL_PREFIX = "/files/";
    private final Path root;

    public LocalStorageService(PixelyProperties properties) {
        this.root = Path.of(properties.storage().uploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 폴더를 만들 수 없습니다: " + root, e);
        }
        log.info("Local storage root: {}", root);
    }

    @Override
    public String store(String directory, byte[] bytes, String extension) {
        LocalDate today = LocalDate.now();
        String key = "%s/%d/%02d/%s.%s".formatted(directory, today.getYear(), today.getMonthValue(),
                UUID.randomUUID().toString().replace("-", ""), extension);
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
            return key;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.STORAGE_ERROR);
        }
    }

    @Override
    public byte[] load(String key) {
        try {
            return Files.readAllBytes(resolve(key));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "이미지 파일을 찾을 수 없습니다.");
        }
    }

    @Override
    public void delete(String key) {
        if (key == null) return;
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            log.warn("Failed to delete file {}", key, e);
        }
    }

    @Override
    public String toUrl(String key) {
        return URL_PREFIX + key;
    }

    @Override
    public String toKey(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) return null;
        return url.substring(URL_PREFIX.length());
    }

    private Path resolve(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "잘못된 파일 경로입니다.");
        }
        return path;
    }
}
