package com.libraflow.library.exception;

import com.libraflow.library.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * แปลง exception ทุกชนิดให้เป็น HTTP response รูปแบบเดียวกันทั้งระบบ
 *
 * SOLID - S (Single Responsibility): คลาสนี้มีเหตุผลเดียวที่จะถูกแก้ คือ
 * "รูปแบบ error response เปลี่ยน" ไม่มี business logic ปนอยู่เลย
 * และทำให้ Controller ไม่ต้องมี try-catch สักตัว
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** กฎทางธุรกิจไม่ผ่าน -> status ตามที่ผูกไว้กับ ErrorCode (ส่วนใหญ่คือ 409) */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = ex.getErrorCode().getStatus();
        return ResponseEntity.status(status).body(ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                request.getRequestURI()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ErrorCode.RESOURCE_NOT_FOUND.name(),
                ex.getMessage(),
                request.getRequestURI()));
    }

    /**
     * @Valid ไม่ผ่าน -> 400 พร้อมรายการ field ที่ผิดทีละตัว
     * frontend ของสมาชิกคนที่ 4 เอา fieldErrors ไปแสดงใต้ช่องกรอกได้ตรง ๆ
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ErrorCode.VALIDATION_FAILED.name(),
                ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
                request.getRequestURI(),
                fieldErrors));
    }

    /** เช่นส่ง /api/v1/books/abc ทั้งที่ id เป็นตัวเลข */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String message = "ค่าของพารามิเตอร์ '" + ex.getName() + "' ไม่ถูกต้อง";
        return ResponseEntity.badRequest().body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ErrorCode.VALIDATION_FAILED.name(),
                message,
                request.getRequestURI()));
    }

    /**
     * ตาข่ายรับสุดท้าย — ไม่ปล่อยให้ stack trace หลุดออกไปหา client
     * แต่เขียนลง log ให้ครบเพื่อให้ไล่หาสาเหตุได้
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ErrorCode.INTERNAL_ERROR.name(),
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                request.getRequestURI()));
    }
}
