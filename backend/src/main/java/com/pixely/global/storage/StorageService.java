package com.pixely.global.storage;

/**
 * 파일 저장소 추상화. 현재는 로컬 디스크(LocalStorageService) 구현만 있고,
 * S3 등으로 바꿀 때는 이 인터페이스의 구현체만 교체하면 된다.
 * key 예: "library/2026/11/uuid.png"
 */
public interface StorageService {

    /** 바이트를 저장하고 key 를 반환 */
    String store(String directory, byte[] bytes, String extension);

    /** key 로 바이트를 읽음 */
    byte[] load(String key);

    void delete(String key);

    /** 클라이언트가 접근할 수 있는 URL (로컬은 /files/{key}) */
    String toUrl(String key);

    /** toUrl 로 만든 URL 을 다시 key 로 변환 (해당 저장소 URL 이 아니면 null) */
    String toKey(String url);
}
