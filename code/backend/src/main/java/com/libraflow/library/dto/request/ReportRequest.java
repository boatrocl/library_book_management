package com.libraflow.library.dto.request;
import java.time.LocalDate;

public record ReportRequest(String type, LocalDate from, LocalDate to) {}