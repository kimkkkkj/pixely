package com.pixely.domain.library;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibraryImageRepository extends JpaRepository<LibraryImage, Long> {

    /** 공용 샘플 + (로그인 시) 내가 올린 이미지. q 는 이미 소문자 %패턴% 형태 */
    @Query("""
            select i from LibraryImage i
            where i.type = :type
              and (i.ownerId is null or i.ownerId = :ownerId)
              and (:q is null or lower(i.title) like :q or lower(coalesce(i.tags, '')) like :q)
            """)
    Page<LibraryImage> searchVisible(@Param("type") ImageType type, @Param("ownerId") Long ownerId,
                                     @Param("q") String q, Pageable pageable);

    @Query("""
            select i from LibraryImage i
            where i.ownerId is null
              and (:type is null or i.type = :type)
              and (:q is null or lower(i.title) like :q or lower(coalesce(i.tags, '')) like :q)
            """)
    Page<LibraryImage> searchPublic(@Param("type") ImageType type, @Param("q") String q, Pageable pageable);

    long countByOwnerIdIsNullAndType(ImageType type);
}
