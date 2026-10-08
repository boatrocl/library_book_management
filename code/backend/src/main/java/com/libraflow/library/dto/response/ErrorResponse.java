package com.libraflow.library.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;

/**
 * รูปแบบ error response มาตรฐานของทั้งระบบ ตาม doc/api-spec.md ข้อ 3
 *
 * ใช้ record เพราะเป็นข้อมูลอ่านอย่างเดียว ไม่ต้องมี setter และได้
 * constructor / equals / hashCode / toString มาให้ฟรี
 */
public record ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        String path,
        List<FieldError> fieldErrors
) {

    /** รายละเอียดราย field กรณี Bean Validation ไม่ผ่าน */
    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(int status, String error, String errorCode,
                                   String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, errorCode, message, path, List.of());
    }

    public static ErrorResponse of(int status, String error, String errorCode,
                                   String message, String path, List<FieldError> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, errorCode, message, path, fieldErrors);
    }
}
