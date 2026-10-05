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
import static org.mockito.Mockito.when;

/**
 * Unit Test สำหรับ MemberStatusRule (BR-01)
 * ตรวจสอบว่าสมาชิกที่มีสถานะ Active ยืมหนังสือได้ และสมาชิกที่ Suspended ยืมไม่ได้
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberStatusRule (BR-01) — ตรวจสอบสถานะสมาชิก")
class MemberStatusRuleTest {

    private MemberStatusRule rule;

    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        rule = new MemberStatusRule();
    }

    @Test
    @DisplayName("order() ต้องคืนค่า 1 (เป็นกฎลำดับแรกสุดใน Chain)")
    void order_shouldReturn1() {
        assertThat(rule.order()).isEqualTo(1);
    }

    @Nested
    @DisplayName("กรณีสมาชิกมีสถานะปกติ (Active)")
    class ActiveMemberTests {

        @Test
        @DisplayName("สมาชิก active = true ต้องผ่านการตรวจสอบโดยไม่ throw exception")
        void check_shouldPass_whenMemberIsActive() {
            when(mockUser.isActive()).thenReturn(true);

            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    Collections.emptyList(),
                    0L,
                    BigDecimal.ZERO
            );

            assertDoesNotThrow(() -> rule.check(context));
        }
    }

    @Nested
    @DisplayName("กรณีสมาชิกถูกระงับสิทธิ์ (Suspended)")
    class SuspendedMemberTests {

        @Test
        @DisplayName("สมาชิก active = false ต้องโยน BusinessException(ErrorCode.MEMBER_SUSPENDED)")
        void check_shouldThrowException_whenMemberIsSuspended() {
            when(mockUser.isActive()).thenReturn(false);

            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    Collections.emptyList(),
                    0L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_SUSPENDED);
            assertThat(exception.getMessage()).contains("บัญชีสมาชิกถูกระงับ");
        }

        @Test
        @DisplayName("หากไม่มีข้อมูลสมาชิก (member == null) ต้องโยน BusinessException(ErrorCode.MEMBER_SUSPENDED)")
        void check_shouldThrowException_whenMemberIsNull() {
            BorrowContext context = new BorrowContext(
                    null,
                    MemberTier.STUDENT,
                    Collections.emptyList(),
                    0L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_SUSPENDED);
        }
    }
}
