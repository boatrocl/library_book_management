package com.libraflow.library.pattern.template;

import com.libraflow.library.dto.request.ReportRequest;

public interface ReportFileGenerator {

    byte[] generate(ReportRequest request);
}
