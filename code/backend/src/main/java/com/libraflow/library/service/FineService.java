package com.libraflow.library.service;

import com.libraflow.library.domain.entity.Fine;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.enums.MemberTier;

public interface FineService {

    /**
     * คำนวณและสร้างค่าปรับสำหรับรายการที่ยืมเกินกำหนด
     * @param loanItem รายการการยืม
     * @param overdueDays จำนวนวันที่เกินกำหนด
     * @param userRole ประเภทของผู้ยืม (เพื่อเลือก Strategy ที่เหมาะสม)
     * @return Fine 객체
     */
    Fine generateFine(LoanItem loanItem, int overdueDays, MemberTier memberTier);

    Fine generateLostBookFine(LoanItem loanItem, int overdueDays);

    /**
     * ชำระค่าปรับ
     * @param fineId รหัสค่าปรับ
     * @return ค่าปรับที่อัปเดตสถานะแล้ว
     */
    Fine payFine(Long fineId);

    /**
     * ดึงรายการค่าปรับทั้งหมดของสมาชิก
     * @param memberId รหัสสมาชิก
     * @return รายการค่าปรับ (Response DTO)
     */
    java.util.List<com.libraflow.library.dto.response.FineResponse> getFinesByMemberId(Long memberId);
}
