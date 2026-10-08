package com.libraflow.library.repository;

import com.libraflow.library.domain.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    /** ค้นผู้แต่งแบบไม่สนตัวพิมพ์เล็กใหญ่ ใช้ index idx_authors_full_name */
    List<Author> findByFullNameContainingIgnoreCase(String keyword);

    /**
     * ใช้ตอนสร้างหนังสือ เพื่อดึงผู้แต่งทั้งหมดตาม id ที่ client ส่งมาในครั้งเดียว
     * แทนที่จะวน findById ทีละคน (N+1)
     */
    List<Author> findByIdIn(List<Long> ids);
}
