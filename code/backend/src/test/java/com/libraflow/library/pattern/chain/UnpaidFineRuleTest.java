package com.libraflow.library.pattern.chain;

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
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test สำหรับ UnpaidFineRule (BR-02)
 * ตรวจสอบว่าสมาชิกที่มีค่าปรับค้างชำระไม่เกิน 100 บาท ยืมได้
 * แต่ถ้าเกิน 100 บาท ต้องโยน BusinessException(ErrorCode.UNPAID_FINE_EXCEEDED)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UnpaidFineRule (BR-02) — ตรวจสอบค่าปรับค้างชำระ")
class UnpaidFineRuleTest {

    private UnpaidFineRule rule;

    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        rule = new UnpaidFineRule();
    }

    @Test
    @DisplayName("order() ต้องคืนค่า 2 (เป็นกฎลำดับที่สองใน Chain)")
    void order_shouldReturn2() {
        assertThat(rule.order()).isEqualTo(2);
    }

    @Nested
    @DisplayName("กรณีค่าปรับค้างชำระอยู่ในเกณฑ์ (<= 100 บาท)")
    class FineWithinLimitTests {

        @Test
        @DisplayName("ไม่มีค่าปรับค้างชำระ (0 บาท) ต้องผ่านการตรวจสอบ")
        void check_shouldPass_whenFineIsZero() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    Collections.emptyList(),
                    0L,
                    BigDecimal.ZERO
            );

            assertDoesNotThrow(() -> rule.check(context));
        }

        @Test
        @DisplayName("ค่าปรับค้างชำระ 50 บาท (น้อยกว่า 100) ต้องผ่านการตรวจสอบ")
        void check_shouldPass_whenFineIsLessThan100() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    Collections.emptyList(),
                    0L,
                    new BigDecimal("50.00")
            );

            assertDoesNotThrow(() -> rule.check(context));
        }

        @Test
        @DisplayName("ค่าปรับค้างชำระพอดี 100.00 บาท (Boundary Case) ต้องผ่านการตรวจสอบ")
        void check_shouldPass_whenFineIsExactly100() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    Collections.emptyList(),
                    0L,
                    new BigDecimal("100.00")
            );

            assertDoesNotThrow(() -> rule.check(context));
        }

        @Test
        @DisplayName("กรณีค่าปรับเป็น null ต้องถือเป็น 0 บาท และผ่านการตรวจสอบ")
        void check_shouldPass_whenFineIsNull() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    Collections.emptyList(),
                    0L,
                    null
            );

            assertDoesNotThrow(() -> rule.check(context));
        }
    }

    @Nested
    @DisplayName("กรณีค่าปรับค้างชำระเกินเกณฑ์ (> 100 บาท)")
    class FineExceededLimitTests {

        @Test
        @DisplayName("ค่าปรับค้างชำระ 100.01 บาท (Boundary Case) ต้องโยน BusinessException(ErrorCode.UNPAID_FINE_EXCEEDED)")
        void check_shouldThrowException_whenFineIsSlightlyOver100() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    Collections.emptyList(),
                    0L,
                    new BigDecimal("100.01")
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNPAID_FINE_EXCEEDED);
            assertThat(exception.getMessage()).contains("เกินเกณฑ์ 100 บาท");
        }

        @Test
        @DisplayName("ค่าปรับค้างชำระ 250.00 บาท ต้องโยน BusinessException(ErrorCode.UNPAID_FINE_EXCEEDED)")
        void check_shouldThrowException_whenFineIsSignificantlyOver100() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    Collections.emptyList(),
                    0L,
                    new BigDecimal("250.00")
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNPAID_FINE_EXCEEDED);
        }
    }
}
