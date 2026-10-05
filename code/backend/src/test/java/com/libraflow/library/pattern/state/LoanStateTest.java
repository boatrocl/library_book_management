package com.libraflow.library.pattern.state;

import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.LoanStatus;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test สำหรับ State Pattern ในระบบการยืม-คืน (Circulation)
 * ทดสอบการเปลี่ยนสถานะทุกเส้นตาม State Diagram (doc/diagrams/09-state-loan.puml)
 * และยืนยันความถูกต้องตามหลัก Liskov Substitution Principle (LSP):
 * การพยายามทำ action ที่ไม่อนุญาตในสถานะใด ๆ ต้องโยน BusinessException ไม่ใช่ UnsupportedOperationException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanState — State Pattern & Transitions Test")
class LoanStateTest {

    private ActiveState activeState;
    private OverdueState overdueState;
    private ReturnedState returnedState;
    private LostState lostState;
    private LoanStateFactory stateFactory;

    @Mock
    private User mockUser;

    @Mock
    private BookCopy mockBookCopy1;

    @Mock
    private BookCopy mockBookCopy2;

    @BeforeEach
    void setUp() {
        activeState = new ActiveState();
        overdueState = new OverdueState();
        returnedState = new ReturnedState();
        lostState = new LostState();

        stateFactory = new LoanStateFactory(List.of(
                activeState,
                overdueState,
                returnedState,
                lostState
        ));
    }

    private Loan createLoanWithTwoItems(LoanStatus status) {
        Loan loan = new Loan("LN-20261005-0001", mockUser, null, LocalDateTime.now(), status);
        LoanItem item1 = new LoanItem(loan, mockBookCopy1, LocalDate.now().plusDays(7));
        LoanItem item2 = new LoanItem(loan, mockBookCopy2, LocalDate.now().plusDays(7));
        loan.addItem(item1);
        loan.addItem(item2);
        return loan;
    }

    @Nested
    @DisplayName("1. ActiveState (สถานะปกติ)")
    class ActiveStateTests {

        @Test
        @DisplayName("status() ต้องเป็น ACTIVE")
        void status_shouldBeActive() {
            assertThat(activeState.status()).isEqualTo(LoanStatus.ACTIVE);
        }

        @Test
        @DisplayName("คืนบางเล่ม: สถานะใบยืมยังคงเป็น ACTIVE และบันทึก returnedAt ของเล่มนั้น")
        void onReturn_singleItem_loanRemainsActive() {
            Loan loan = createLoanWithTwoItems(LoanStatus.ACTIVE);
            LoanItem item1 = loan.getItems().get(0);

            activeState.onReturn(loan, item1);

            assertThat(item1.isReturned()).isTrue();
            assertThat(item1.getReturnedAt()).isEqualTo(LocalDate.now());
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        }

        @Test
        @DisplayName("คืนครบทุกเล่ม: สถานะใบยืมเปลี่ยนเป็น RETURNED (ACTIVE -> RETURNED)")
        void onReturn_allReturned_loanStatusChangesToReturned() {
            Loan loan = createLoanWithTwoItems(LoanStatus.ACTIVE);
            LoanItem item1 = loan.getItems().get(0);
            LoanItem item2 = loan.getItems().get(1);

            activeState.onReturn(loan, item1);
            activeState.onReturn(loan, item2);

            assertThat(item1.isReturned()).isTrue();
            assertThat(item2.isReturned()).isTrue();
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        }

        @Test
        @DisplayName("คืนเล่มเดิมซ้ำใน ActiveState ต้องโยน BusinessException(VALIDATION_FAILED)")
        void onReturn_alreadyReturnedItem_shouldThrowBusinessException() {
            Loan loan = createLoanWithTwoItems(LoanStatus.ACTIVE);
            LoanItem item1 = loan.getItems().get(0);

            activeState.onReturn(loan, item1);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> activeState.onReturn(loan, item1)
            );
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
            assertThat(ex.getMessage()).contains("หนังสือเล่มนี้ถูกบันทึกการคืนไปแล้ว");
        }

        @Test
        @DisplayName("ต่ออายุระดับใบยืม: ขยาย dueDate ไปอีก 7 วัน และเพิ่ม renewCount (ACTIVE -> ACTIVE)")
        void onRenew_loanLevel_shouldExtendDueDate() {
            Loan loan = createLoanWithTwoItems(LoanStatus.ACTIVE);
            LocalDate originalDue = loan.getItems().get(0).getDueDate();

            activeState.onRenew(loan);

            for (LoanItem item : loan.getItems()) {
                assertThat(item.getRenewCount()).isEqualTo((short) 1);
                assertThat(item.getDueDate()).isEqualTo(originalDue.plusDays(7));
            }
        }

        @Test
        @DisplayName("ต่ออายุเกิน 2 ครั้งตาม BR-06 ต้องโยน BusinessException(RENEW_LIMIT_REACHED)")
        void onRenew_exceedMaxRenewCount_shouldThrowBusinessException() {
            Loan loan = createLoanWithTwoItems(LoanStatus.ACTIVE);

            activeState.onRenew(loan); // ครั้งที่ 1
            activeState.onRenew(loan); // ครั้งที่ 2

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> activeState.onRenew(loan) // ครั้งที่ 3 -> Exception
            );
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RENEW_LIMIT_REACHED);
            assertThat(ex.getMessage()).contains("ไม่เกิน 2 ครั้ง");
        }

        @Test
        @DisplayName("ต่ออายุหนังสือที่คืนหมดแล้วใน ActiveState ต้องโยน BusinessException(VALIDATION_FAILED)")
        void onRenew_whenAllItemsAlreadyReturned_shouldThrowBusinessException() {
            Loan loan = createLoanWithTwoItems(LoanStatus.ACTIVE);
            activeState.onReturn(loan, loan.getItems().get(0));
            activeState.onReturn(loan, loan.getItems().get(1));

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> activeState.onRenew(loan)
            );
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("ต่ออายุระดับเล่มเดี่ยว: ขยายเวลาสำเร็จ")
        void onRenew_singleItem_shouldSucceed() {
            Loan loan = createLoanWithTwoItems(LoanStatus.ACTIVE);
            LoanItem item1 = loan.getItems().get(0);
            LocalDate originalDue = item1.getDueDate();

            activeState.onRenew(loan, item1);

            assertThat(item1.getRenewCount()).isEqualTo((short) 1);
            assertThat(item1.getDueDate()).isEqualTo(originalDue.plusDays(7));
        }

        @Test
        @DisplayName("ต่ออายุเล่มที่คืนไปแล้วต้องโยน BusinessException")
        void onRenew_singleItem_alreadyReturned_shouldThrowBusinessException() {
            Loan loan = createLoanWithTwoItems(LoanStatus.ACTIVE);
            LoanItem item1 = loan.getItems().get(0);
            activeState.onReturn(loan, item1);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> activeState.onRenew(loan, item1)
            );
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }

    @Nested
    @DisplayName("2. OverdueState (สถานะเกินกำหนด)")
    class OverdueStateTests {

        @Test
        @DisplayName("status() ต้องเป็น OVERDUE")
        void status_shouldBeOverdue() {
            assertThat(overdueState.status()).isEqualTo(LoanStatus.OVERDUE);
        }

        @Test
        @DisplayName("คืนหนังสือที่ Overdue: คืนครบทุกเล่มเปลี่ยนเป็น RETURNED (OVERDUE -> RETURNED)")
        void onReturn_allReturned_shouldTransitionToReturned() {
            Loan loan = createLoanWithTwoItems(LoanStatus.OVERDUE);
            LoanItem item1 = loan.getItems().get(0);
            LoanItem item2 = loan.getItems().get(1);

            overdueState.onReturn(loan, item1);
            overdueState.onReturn(loan, item2);

            assertThat(item1.isReturned()).isTrue();
            assertThat(item2.isReturned()).isTrue();
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        }

        @Test
        @DisplayName("ห้ามต่ออายุในสถานะ OVERDUE: ต้องโยน BusinessException (รักษา LSP ไม่โยน UnsupportedOperationException)")
        void onRenew_shouldThrowBusinessException_lspCompliance() {
            Loan loan = createLoanWithTwoItems(LoanStatus.OVERDUE);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> overdueState.onRenew(loan)
            );
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
            assertThat(ex.getMessage()).contains("เกินกำหนดส่งคืนแล้ว");
        }

        @Test
        @DisplayName("ห้ามต่ออายุระดับเล่มในสถานะ OVERDUE: ต้องโยน BusinessException")
        void onRenew_singleItem_shouldThrowBusinessException() {
            Loan loan = createLoanWithTwoItems(LoanStatus.OVERDUE);
            LoanItem item1 = loan.getItems().get(0);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> overdueState.onRenew(loan, item1)
            );
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }

    @Nested
    @DisplayName("3. ReturnedState (สถานะคืนแล้ว — ยืนยันหลัก LSP)")
    class ReturnedStateTests {

        @Test
        @DisplayName("status() ต้องเป็น RETURNED")
        void status_shouldBeReturned() {
            assertThat(returnedState.status()).isEqualTo(LoanStatus.RETURNED);
        }

        @Test
        @DisplayName("พยายามคืนซ้ำใน ReturnedState: ต้องโยน BusinessException ไม่ใช่ UnsupportedOperationException (LSP)")
        void onReturn_repeatedReturn_shouldThrowBusinessException_lspCompliance() {
            Loan loan = createLoanWithTwoItems(LoanStatus.RETURNED);
            LoanItem item1 = loan.getItems().get(0);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> returnedState.onReturn(loan, item1)
            );
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
            assertThat(ex.getMessage()).contains("ไม่สามารถบันทึกการคืนซ้ำได้");
        }

        @Test
        @DisplayName("พยายามต่ออายุใน ReturnedState: ต้องโยน BusinessException (LSP)")
        void onRenew_shouldThrowBusinessException_lspCompliance() {
            Loan loan = createLoanWithTwoItems(LoanStatus.RETURNED);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> returnedState.onRenew(loan)
            );
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
            assertThat(ex.getMessage()).contains("คืนหนังสือแล้ว ไม่สามารถต่ออายุได้");
        }

        @Test
        @DisplayName("พยายามต่ออายุระดับเล่มใน ReturnedState: ต้องโยน BusinessException")
        void onRenew_singleItem_shouldThrowBusinessException() {
            Loan loan = createLoanWithTwoItems(LoanStatus.RETURNED);
            LoanItem item1 = loan.getItems().get(0);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> returnedState.onRenew(loan, item1)
            );
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }

    @Nested
    @DisplayName("4. LostState (สถานะสูญหาย — BR-08)")
    class LostStateTests {

        @Test
        @DisplayName("status() ต้องเป็น LOST")
        void status_shouldBeLost() {
            assertThat(lostState.status()).isEqualTo(LoanStatus.LOST);
        }

        @Test
        @DisplayName("ชดใช้ค่าหนังสือและคืนครบทุกเล่ม: เปลี่ยนสถานะเป็น RETURNED (LOST -> RETURNED)")
        void onReturn_whenCompensated_transitionsToReturned() {
            Loan loan = createLoanWithTwoItems(LoanStatus.LOST);
            LoanItem item1 = loan.getItems().get(0);
            LoanItem item2 = loan.getItems().get(1);

            lostState.onReturn(loan, item1);
            lostState.onReturn(loan, item2);

            assertThat(item1.isReturned()).isTrue();
            assertThat(item2.isReturned()).isTrue();
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        }

        @Test
        @DisplayName("พยายามต่ออายุใน LostState: ต้องโยน BusinessException (BR-08 / LSP)")
        void onRenew_shouldThrowBusinessException_lspCompliance() {
            Loan loan = createLoanWithTwoItems(LoanStatus.LOST);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> lostState.onRenew(loan)
            );
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
            assertThat(ex.getMessage()).contains("สูญหาย");
        }

        @Test
        @DisplayName("พยายามต่ออายุระดับเล่มใน LostState: ต้องโยน BusinessException")
        void onRenew_singleItem_shouldThrowBusinessException() {
            Loan loan = createLoanWithTwoItems(LoanStatus.LOST);
            LoanItem item1 = loan.getItems().get(0);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> lostState.onRenew(loan, item1)
            );
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED);
        }
    }

    @Nested
    @DisplayName("5. LoanStateFactory")
    class LoanStateFactoryTests {

        @Test
        @DisplayName("ดึง State ตาม LoanStatus ได้ถูกต้องทุกสถานะ")
        void stateOf_byStatus_shouldReturnCorrectStateInstance() {
            assertThat(stateFactory.stateOf(LoanStatus.ACTIVE)).isInstanceOf(ActiveState.class);
            assertThat(stateFactory.stateOf(LoanStatus.OVERDUE)).isInstanceOf(OverdueState.class);
            assertThat(stateFactory.stateOf(LoanStatus.RETURNED)).isInstanceOf(ReturnedState.class);
            assertThat(stateFactory.stateOf(LoanStatus.LOST)).isInstanceOf(LostState.class);
        }

        @Test
        @DisplayName("ดึง State จาก Loan object ได้ถูกต้อง")
        void stateOf_byLoan_shouldReturnMatchingState() {
            Loan activeLoan = createLoanWithTwoItems(LoanStatus.ACTIVE);
            Loan overdueLoan = createLoanWithTwoItems(LoanStatus.OVERDUE);
            Loan returnedLoan = createLoanWithTwoItems(LoanStatus.RETURNED);
            Loan lostLoan = createLoanWithTwoItems(LoanStatus.LOST);

            assertThat(stateFactory.stateOf(activeLoan)).isInstanceOf(ActiveState.class);
            assertThat(stateFactory.stateOf(overdueLoan)).isInstanceOf(OverdueState.class);
            assertThat(stateFactory.stateOf(returnedLoan)).isInstanceOf(ReturnedState.class);
            assertThat(stateFactory.stateOf(lostLoan)).isInstanceOf(LostState.class);
        }

        @Test
        @DisplayName("กรณี Loan เป็น null หรือ status เป็น null ต้อง fallback เป็น ActiveState")
        void stateOf_nullLoan_shouldFallbackToActiveState() {
            assertThat(stateFactory.stateOf((Loan) null)).isInstanceOf(ActiveState.class);

            Loan loanWithNullStatus = new Loan("CODE", mockUser, null, LocalDateTime.now(), null);
            assertThat(stateFactory.stateOf(loanWithNullStatus)).isInstanceOf(ActiveState.class);
        }
    }
}
