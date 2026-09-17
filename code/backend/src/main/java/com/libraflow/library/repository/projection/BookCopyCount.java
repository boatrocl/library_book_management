package com.libraflow.library.repository.projection;

/**
 * ผลรวมจำนวนตัวเล่มของหนังสือหนึ่งเล่ม ใช้เติม availableCopies / totalCopies ใน BookResponse
 *
 * เหตุผลที่ต้องมี projection นี้แทนการนับจาก book.getCopies():
 * ถ้าอ่านจาก collection ของ entity จะเกิด N+1 (หนังสือ 10 เล่ม = 10 query นับตัวเล่ม)
 * แต่ถ้า JOIN FETCH collection มาพร้อม Page ก็ไม่ได้อีก เพราะ Hibernate จะเลิกแบ่งหน้า
 * ที่ระดับฐานข้อมูลแล้วดึงทุกแถวมาแบ่งหน้าในหน่วยความจำแทน (คำเตือน HHH000104)
 *
 * ทางออกคือยิง query รวมทีเดียวสำหรับหนังสือทุกเล่มในหน้านั้น แล้วค่อยจับคู่กันในโค้ด
 */
public record BookCopyCount(Long bookId, Long totalCopies, Long availableCopies) {
}
