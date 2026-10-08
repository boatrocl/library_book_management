package com.libraflow.library.pattern.state;

import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.enums.LoanStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory สำหรับเลือก LoanState ตามสถานะของใบยืม
 * อ้างอิง doc/design-patterns.md ข้อ 3.2 และ doc/diagrams/04-class-diagram.puml
 */
@Component
public class LoanStateFactory {

    private final Map<LoanStatus, LoanState> stateMap = new EnumMap<>(LoanStatus.class);

    public LoanStateFactory(List<LoanState> states) {
        for (LoanState state : states) {
            stateMap.put(state.status(), state);
        }
    }

    /**
     * ดึง LoanState ที่ตรงกับสถานะของ Loan
     * (ดู doc/diagrams/06-sequence-return.puml ขั้นที่ 35-38)
     */
    public LoanState stateOf(Loan loan) {
        if (loan == null || loan.getStatus() == null) {
            return stateMap.get(LoanStatus.ACTIVE);
        }
        return stateOf(loan.getStatus());
    }

    /**
     * ดึง LoanState ตาม enum LoanStatus
     */
    public LoanState stateOf(LoanStatus status) {
        LoanState state = stateMap.get(status);
        if (state == null) {
            throw new IllegalArgumentException("ไม่พบ State สำหรับสถานะ: " + status);
        }
        return state;
    }
}
