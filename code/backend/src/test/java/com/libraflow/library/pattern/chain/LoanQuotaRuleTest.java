package com.libraflow.library.pattern.chain;

import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.MemberTier;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test สำหรับ LoanQuotaRule (BR-03)
 * ตรวจสอบโควต้าการยืมหนังสือตามระดับสมาชิก:
 * - STUDENT: ยืมได้สูงสุด 5 เล่ม
 * - STAFF: ยืมได้สูงสุด 10 เล่ม
 * - EXTERNAL: ยืมได้สูงสุด 2 เล่ม
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanQuotaRule (BR-03) — ตรวจสอบโควต้าการยืมตาม MemberTier")
class LoanQuotaRuleTest {

    private LoanQuotaRule rule;

    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        rule = new LoanQuotaRule();
    }

    @Test
    @DisplayName("order() ต้องคืนค่า 3 (เป็นกฎลำดับที่สามใน Chain)")
    void order_shouldReturn3() {
        assertThat(rule.order()).isEqualTo(3);
    }

    private List<BookCopy> createMockCopies(int count) {
        List<BookCopy> copies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            copies.add(org.mockito.Mockito.mock(BookCopy.class));
        }
        return copies;
    }

    @Nested
    @DisplayName("กรณี MemberTier = STUDENT (โควต้า 5 เล่ม)")
    class StudentTierTests {

        @Test
        @DisplayName("ยืมอยู่เดิม 2 เล่ม + ขอยืมใหม่ 3 เล่ม = รวม 5 เล่ม (พอดีโควต้า) ต้องผ่าน")
        void check_studentTier_withinQuota_shouldPass() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    createMockCopies(3),
                    2L,
                    BigDecimal.ZERO
            );

            assertDoesNotThrow(() -> rule.check(context));
        }

        @Test
        @DisplayName("ยังไม่เคยยืม (0 เล่ม) + ขอยืมใหม่ 5 เล่ม (พอดีโควต้า) ต้องผ่าน")
        void check_studentTier_atZero_shouldPass() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    createMockCopies(5),
                    0L,
                    BigDecimal.ZERO
            );

            assertDoesNotThrow(() -> rule.check(context));
        }

        @Test
        @DisplayName("ยืมอยู่เดิม 3 เล่ม + ขอยืมใหม่ 3 เล่ม = รวม 6 เล่ม (เกิน 5) ต้องโยน LOAN_QUOTA_EXCEEDED")
        void check_studentTier_exceedingQuota_shouldThrowException() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    createMockCopies(3),
                    3L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_QUOTA_EXCEEDED);
            assertThat(exception.getMessage()).contains("STUDENT").contains("5");
        }

        @Test
        @DisplayName("ยืมอยู่เดิม 5 เล่ม (เต็มโควต้า) + ขอยืมเพิ่มอีก 1 เล่ม ต้องโยน LOAN_QUOTA_EXCEEDED")
        void check_studentTier_alreadyAtQuota_shouldThrowException() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    createMockCopies(1),
                    5L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_QUOTA_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("กรณี MemberTier = STAFF (โควต้า 10 เล่ม)")
    class StaffTierTests {

        @Test
        @DisplayName("ยืมอยู่เดิม 5 เล่ม + ขอยืมใหม่ 5 เล่ม = รวม 10 เล่ม (พอดีโควต้า) ต้องผ่าน")
        void check_staffTier_withinQuota_shouldPass() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STAFF,
                    createMockCopies(5),
                    5L,
                    BigDecimal.ZERO
            );

            assertDoesNotThrow(() -> rule.check(context));
        }

        @Test
        @DisplayName("ยืมอยู่เดิม 8 เล่ม + ขอยืมใหม่ 3 เล่ม = รวม 11 เล่ม (เกิน 10) ต้องโยน LOAN_QUOTA_EXCEEDED")
        void check_staffTier_exceedingQuota_shouldThrowException() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STAFF,
                    createMockCopies(3),
                    8L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_QUOTA_EXCEEDED);
            assertThat(exception.getMessage()).contains("STAFF").contains("10");
        }
    }

    @Nested
    @DisplayName("กรณี MemberTier = EXTERNAL (โควต้า 2 เล่ม)")
    class ExternalTierTests {

        @Test
        @DisplayName("ยืมอยู่เดิม 1 เล่ม + ขอยืมใหม่ 1 เล่ม = รวม 2 เล่ม (พอดีโควต้า) ต้องผ่าน")
        void check_externalTier_withinQuota_shouldPass() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.EXTERNAL,
                    createMockCopies(1),
                    1L,
                    BigDecimal.ZERO
            );

            assertDoesNotThrow(() -> rule.check(context));
        }

        @Test
        @DisplayName("ยืมอยู่เดิม 1 เล่ม + ขอยืมใหม่ 2 เล่ม = รวม 3 เล่ม (เกิน 2) ต้องโยน LOAN_QUOTA_EXCEEDED")
        void check_externalTier_exceedingQuota_shouldThrowException() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.EXTERNAL,
                    createMockCopies(2),
                    1L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_QUOTA_EXCEEDED);
            assertThat(exception.getMessage()).contains("EXTERNAL").contains("2");
        }

        @Test
        @DisplayName("ยืมอยู่เดิม 0 เล่ม แต่ขอยืมพร้อมกัน 3 เล่ม (เกิน 2) ต้องโยน LOAN_QUOTA_EXCEEDED")
        void check_externalTier_borrowingThreeAtOnce_shouldThrowException() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.EXTERNAL,
                    createMockCopies(3),
                    0L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_QUOTA_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("กรณีไม่ระบุ MemberTier (null fallback to STUDENT)")
    class NullTierTests {

        @Test
        @DisplayName("ถ้า memberTier เป็น null ต้อง fallback เป็น STUDENT (โควต้า 5 เล่ม)")
        void check_defaultToStudentTier_whenTierIsNull() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    null,
                    createMockCopies(3),
                    3L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_QUOTA_EXCEEDED);
            assertThat(exception.getMessage()).contains("STUDENT").contains("5");
        }
    }
}
