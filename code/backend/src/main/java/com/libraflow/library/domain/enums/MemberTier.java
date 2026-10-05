package com.libraflow.library.domain.enums;

/**
 * ประเภทสมาชิกของระบบห้องสมุด — มีผลต่อโควต้าการยืม ระยะเวลาการยืม และอัตราค่าปรับ
 * อ้างอิง doc/project-overview.md ข้อ 3 (BR-03, BR-05, BR-07)
 */
public enum MemberTier {

    /** นักศึกษา: ยืมได้สูงสุด 5 เล่ม, ยืมได้ 7 วัน, ค่าปรับ 3 บาท/วัน */
    STUDENT(5, 7),

    /** บุคลากร/อาจารย์: ยืมได้สูงสุด 10 เล่ม, ยืมได้ 14 วัน, ค่าปรับ 5 บาท/วัน (เพดาน 300 บาท) */
    STAFF(10, 14),

    /** บุคคลภายนอก: ยืมได้สูงสุด 2 เล่ม, ยืมได้ 3 วัน, ค่าปรับ 10 บาท/วัน */
    EXTERNAL(2, 3);

    private final int loanQuota;
    private final int loanDurationDays;

    MemberTier(int loanQuota, int loanDurationDays) {
        this.loanQuota = loanQuota;
        this.loanDurationDays = loanDurationDays;
    }

    public int getLoanQuota() {
        return loanQuota;
    }

    public int getLoanDurationDays() {
        return loanDurationDays;
    }
}
