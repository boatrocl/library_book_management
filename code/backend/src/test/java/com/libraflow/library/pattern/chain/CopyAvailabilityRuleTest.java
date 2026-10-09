package com.libraflow.library.pattern.chain;

import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.BookCopyStatus;
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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Unit Test สำหรับ CopyAvailabilityRule (BR-04)
 * ตรวจสอบความพร้อมของตัวเล่มหนังสือ:
 * - ตัวเล่มที่มีสถานะ AVAILABLE สามารถยืมได้
 * - ตัวเล่มที่มีสถานะ ON_LOAN, DAMAGED, LOST หรือ RESERVED ของผู้อื่นยืมไม่ได้
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CopyAvailabilityRule (BR-04) — ตรวจสอบสถานะความพร้อมของตัวเล่ม")
class CopyAvailabilityRuleTest {

    private CopyAvailabilityRule rule;

    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        rule = new CopyAvailabilityRule();
    }

    @Test
    @DisplayName("order() ต้องคืนค่า 4 (เป็นกฎลำดับที่สี่ใน Chain)")
    void order_shouldReturn4() {
        assertThat(rule.order()).isEqualTo(4);
    }

    private BookCopy createMockCopy(String barcode, BookCopyStatus status) {
        BookCopy copy = mock(BookCopy.class);
        lenient().when(copy.getBarcode()).thenReturn(barcode);
        lenient().when(copy.getStatus()).thenReturn(status);
        lenient().when(copy.isAvailable()).thenReturn(status == BookCopyStatus.AVAILABLE);
        return copy;
    }

    @Nested
    @DisplayName("กรณีตัวเล่มพร้อมให้ยืม (AVAILABLE)")
    class AvailableCopiesTests {

        @Test
        @DisplayName("ตัวเล่มทุกเล่มมีสถานะ AVAILABLE ต้องผ่านการตรวจสอบ")
        void check_allCopiesAvailable_shouldPass() {
            BookCopy copy1 = createMockCopy("LIB-001", BookCopyStatus.AVAILABLE);
            BookCopy copy2 = createMockCopy("LIB-002", BookCopyStatus.AVAILABLE);

            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    List.of(copy1, copy2),
                    0L,
                    BigDecimal.ZERO
            );

            assertDoesNotThrow(() -> rule.check(context));
        }

        @Test
        @DisplayName("กรณีไม่มีตัวเล่มในคำขอ (empty list) ต้องผ่านการตรวจสอบ")
        void check_emptyCopiesList_shouldPass() {
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
        @DisplayName("กรณี copies เป็น null ต้องผ่านการตรวจสอบโดยไม่ throw NullPointerException")
        void check_nullCopiesList_shouldPass() {
            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    null,
                    0L,
                    BigDecimal.ZERO
            );

            assertDoesNotThrow(() -> rule.check(context));
        }
    }

    @Nested
    @DisplayName("กรณีมีตัวเล่มที่ไม่พร้อมให้ยืม หรือ RESERVED ของผู้อื่น")
    class UnavailableCopiesTests {

        @Test
        @DisplayName("มีเล่มที่มีสถานะ ON_LOAN ต้องโยน BusinessException(ErrorCode.COPY_NOT_AVAILABLE)")
        void check_singleCopyOnLoan_shouldThrowException() {
            BookCopy copy = createMockCopy("LIB-003", BookCopyStatus.ON_LOAN);

            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    List.of(copy),
                    0L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COPY_NOT_AVAILABLE);
            assertThat(exception.getMessage())
                    .contains("LIB-003")
                    .contains("ON_LOAN");
        }

        @Test
        @DisplayName("ยืม 2 เล่ม โดยมีเล่มหนึ่ง AVAILABLE แต่อีกเล่ม ON_LOAN ต้องโยน COPY_NOT_AVAILABLE")
        void check_mixedAvailableAndOnLoan_shouldThrowException() {
            BookCopy copy1 = createMockCopy("LIB-001", BookCopyStatus.AVAILABLE);
            BookCopy copy2 = createMockCopy("LIB-002", BookCopyStatus.ON_LOAN);

            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    List.of(copy1, copy2),
                    0L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COPY_NOT_AVAILABLE);
            assertThat(exception.getMessage())
                    .contains("LIB-002")
                    .contains("ON_LOAN");
        }

        @Test
        @DisplayName("ตัวเล่มมีสถานะ RESERVED (ถูกจองไว้) ต้องโยน COPY_NOT_AVAILABLE")
        void check_copyReserved_shouldThrowException() {
            BookCopy copy = createMockCopy("LIB-RES-01", BookCopyStatus.RESERVED);

            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    List.of(copy),
                    0L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COPY_NOT_AVAILABLE);
            assertThat(exception.getMessage()).contains("RESERVED");
        }

        @Test
        @DisplayName("ตัวเล่ม RESERVED ผ่านได้เฉพาะเจ้าของคิว READY ที่ยังไม่หมดอายุ")
        void check_copyReservedForMember_shouldPass() {
            BookCopy copy = createMockCopy("LIB-RES-02", BookCopyStatus.RESERVED);
            lenient().when(copy.getId()).thenReturn(42L);
            BorrowContext context = new BorrowContext(
                    mockUser, MemberTier.STUDENT, List.of(copy), 0L, BigDecimal.ZERO, Set.of(42L));

            assertDoesNotThrow(() -> rule.check(context));
        }

        @Test
        @DisplayName("ตัวเล่มมีสถานะ DAMAGED (ชำรุด) ต้องโยน COPY_NOT_AVAILABLE")
        void check_copyDamaged_shouldThrowException() {
            BookCopy copy = createMockCopy("LIB-DMG-01", BookCopyStatus.DAMAGED);

            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    List.of(copy),
                    0L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COPY_NOT_AVAILABLE);
            assertThat(exception.getMessage()).contains("DAMAGED");
        }

        @Test
        @DisplayName("ตัวเล่มมีสถานะ LOST (สูญหาย) ต้องโยน COPY_NOT_AVAILABLE")
        void check_copyLost_shouldThrowException() {
            BookCopy copy = createMockCopy("LIB-LOST-01", BookCopyStatus.LOST);

            BorrowContext context = new BorrowContext(
                    mockUser,
                    MemberTier.STUDENT,
                    List.of(copy),
                    0L,
                    BigDecimal.ZERO
            );

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> rule.check(context)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COPY_NOT_AVAILABLE);
            assertThat(exception.getMessage()).contains("LOST");
        }
    }
}
