package com.libraflow.library.common;

import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.MemberTier;
import com.libraflow.library.domain.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit Test สำหรับ LoanPolicyUtil
 * ทดสอบตรรกะการคำนวณวันกำหนดส่งคืน (Due Date), การต่ออายุ, โควต้า และการจับคู่ประเภทสมาชิก (BR-03, BR-05, BR-06)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanPolicyUtil — Loan Policy & Due Date Calculation Test")
class LoanPolicyUtilTest {

    @Mock
    private User mockUser;

    @Nested
    @DisplayName("resolveMemberTier(...) — การระบุประเภทสมาชิก")
    class ResolveMemberTierTests {

        @Test
        @DisplayName("ADMIN ต้องเป็น STAFF")
        void resolveMemberTier_admin_shouldReturnStaff() {
            when(mockUser.getRole()).thenReturn(UserRole.ADMIN);
            assertThat(LoanPolicyUtil.resolveMemberTier(mockUser)).isEqualTo(MemberTier.STAFF);
        }

        @Test
        @DisplayName("LIBRARIAN ต้องเป็น STAFF")
        void resolveMemberTier_librarian_shouldReturnStaff() {
            when(mockUser.getRole()).thenReturn(UserRole.LIBRARIAN);
            assertThat(LoanPolicyUtil.resolveMemberTier(mockUser)).isEqualTo(MemberTier.STAFF);
        }

        @Test
        @DisplayName("MEMBER ใช้ member_tier ที่บันทึกไว้")
        void resolveMemberTier_member_shouldReturnStudent() {
            when(mockUser.getRole()).thenReturn(UserRole.MEMBER);
            when(mockUser.getMemberTier()).thenReturn("STUDENT");
            assertThat(LoanPolicyUtil.resolveMemberTier(mockUser)).isEqualTo(MemberTier.STUDENT);
        }

        @Test
        @DisplayName("MEMBER ที่เป็น EXTERNAL ต้องใช้ข้อมูล member_tier")
        void resolveMemberTier_externalMember_shouldReturnExternal() {
            when(mockUser.getRole()).thenReturn(UserRole.MEMBER);
            when(mockUser.getMemberTier()).thenReturn("EXTERNAL");
            assertThat(LoanPolicyUtil.resolveMemberTier(mockUser)).isEqualTo(MemberTier.EXTERNAL);
        }

        @Test
        @DisplayName("member_tier ที่ไม่ถูกต้องต้อง fallback เป็น STUDENT")
        void resolveMemberTier_invalidTier_shouldReturnStudent() {
            when(mockUser.getRole()).thenReturn(UserRole.MEMBER);
            when(mockUser.getMemberTier()).thenReturn("UNKNOWN");
            assertThat(LoanPolicyUtil.resolveMemberTier(mockUser)).isEqualTo(MemberTier.STUDENT);
        }

        @Test
        @DisplayName("User เป็น null ต้อง fallback เป็น STUDENT")
        void resolveMemberTier_null_shouldReturnStudent() {
            assertThat(LoanPolicyUtil.resolveMemberTier(null)).isEqualTo(MemberTier.STUDENT);
        }
    }

    @Nested
    @DisplayName("calculateDueDate(...) — การคำนวณวันกำหนดส่งคืน (BR-05)")
    class CalculateDueDateTests {

        private final LocalDate baseDate = LocalDate.of(2026, 10, 1);

        @Test
        @DisplayName("STUDENT ยืมได้ 7 วัน")
        void calculateDueDate_student_shouldAdd7Days() {
            LocalDate dueDate = LoanPolicyUtil.calculateDueDate(MemberTier.STUDENT, baseDate);
            assertThat(dueDate).isEqualTo(LocalDate.of(2026, 10, 8));
        }

        @Test
        @DisplayName("STAFF ยืมได้ 14 วัน")
        void calculateDueDate_staff_shouldAdd14Days() {
            LocalDate dueDate = LoanPolicyUtil.calculateDueDate(MemberTier.STAFF, baseDate);
            assertThat(dueDate).isEqualTo(LocalDate.of(2026, 10, 15));
        }

        @Test
        @DisplayName("EXTERNAL ยืมได้ 3 วัน")
        void calculateDueDate_external_shouldAdd3Days() {
            LocalDate dueDate = LoanPolicyUtil.calculateDueDate(MemberTier.EXTERNAL, baseDate);
            assertThat(dueDate).isEqualTo(LocalDate.of(2026, 10, 4));
        }

        @Test
        @DisplayName("tier เป็น null ต้อง fallback เป็น STUDENT (7 วัน)")
        void calculateDueDate_nullTier_shouldFallbackToStudent() {
            LocalDate dueDate = LoanPolicyUtil.calculateDueDate(null, baseDate);
            assertThat(dueDate).isEqualTo(LocalDate.of(2026, 10, 8));
        }

        @Test
        @DisplayName("calculateDueDate(tier) โดยไม่ระบุ baseDate ต้องนับจาก LocalDate.now()")
        void calculateDueDate_defaultNow() {
            LocalDate expected = LocalDate.now().plusDays(MemberTier.STUDENT.getLoanDurationDays());
            assertThat(LoanPolicyUtil.calculateDueDate(MemberTier.STUDENT)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("calculateRenewDueDate(...) — การคำนวณวันต่ออายุ (BR-06)")
    class CalculateRenewDueDateTests {

        @Test
        @DisplayName("ต่ออายุขยายเพิ่ม 7 วันจากกำหนดคืนเดิม")
        void calculateRenewDueDate_shouldAdd7Days() {
            LocalDate currentDue = LocalDate.of(2026, 10, 10);
            LocalDate newDue = LoanPolicyUtil.calculateRenewDueDate(currentDue);
            assertThat(newDue).isEqualTo(LocalDate.of(2026, 10, 17));
        }
    }

    @Nested
    @DisplayName("getLoanDurationDays & getLoanQuota")
    class QuotaAndDurationTests {

        @Test
        @DisplayName("ระยะเวลายืมถูกต้องตาม Tier")
        void getLoanDurationDays_shouldMatchTier() {
            assertThat(LoanPolicyUtil.getLoanDurationDays(MemberTier.STUDENT)).isEqualTo(7);
            assertThat(LoanPolicyUtil.getLoanDurationDays(MemberTier.STAFF)).isEqualTo(14);
            assertThat(LoanPolicyUtil.getLoanDurationDays(MemberTier.EXTERNAL)).isEqualTo(3);
            assertThat(LoanPolicyUtil.getLoanDurationDays(null)).isEqualTo(7);
        }

        @Test
        @DisplayName("โควต้าการยืมถูกต้องตาม Tier")
        void getLoanQuota_shouldMatchTier() {
            assertThat(LoanPolicyUtil.getLoanQuota(MemberTier.STUDENT)).isEqualTo(5);
            assertThat(LoanPolicyUtil.getLoanQuota(MemberTier.STAFF)).isEqualTo(10);
            assertThat(LoanPolicyUtil.getLoanQuota(MemberTier.EXTERNAL)).isEqualTo(2);
            assertThat(LoanPolicyUtil.getLoanQuota(null)).isEqualTo(5);
        }
    }
}
