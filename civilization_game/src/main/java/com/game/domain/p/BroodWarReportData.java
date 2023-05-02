package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

/**
 * 母巢战报存档
 *
 *
 * @date 2021/7/23 20:37
 */
@Getter
@Setter
public class BroodWarReportData {
    private byte[] report;

    public void wrap(BroodWarReport report) {
        this.report = report.wrapPb().toByteArray();
    }
}
