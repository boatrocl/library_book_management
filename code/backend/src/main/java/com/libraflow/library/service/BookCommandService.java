package com.libraflow.library.service;

import com.libraflow.library.dto.request.CreateBookCopyRequest;
import com.libraflow.library.dto.request.CreateBookRequest;
import com.libraflow.library.dto.request.UpdateBookRequest;
import com.libraflow.library.dto.response.BookCopyResponse;
import com.libraflow.library.dto.response.BookResponse;

/**
 * งานฝั่ง "เขียน" ของหนังสือทั้งหมด
 *
 * SOLID - I: แยกจาก BookQueryService เพื่อให้ผู้เรียกขึ้นกับเฉพาะสิ่งที่ตัวเองใช้จริง
 * คลาสที่ต้องการแค่อ่านจะไม่รู้จักเมธอดเหล่านี้เลย
 */
public interface BookCommandService {

    /** เพิ่มหนังสือใหม่ — ISBN ซ้ำจะโยน BusinessException(ISBN_ALREADY_EXISTS) */
    BookResponse create(CreateBookRequest request);

    BookResponse update(Long id, UpdateBookRequest request);

    /** ลบหนังสือ — ติด BR-11 จะโยน BusinessException(BOOK_IN_USE) */
    void delete(Long id);

    /** เพิ่มตัวเล่มให้หนังสือเล่มหนึ่ง */
    BookCopyResponse addCopy(Long bookId, CreateBookCopyRequest request);
}
