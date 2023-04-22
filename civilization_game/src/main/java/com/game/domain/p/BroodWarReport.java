package com.game.domain.p;

import com.game.pb.DataPb;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 母巢战报
 *
 * @author zcp
 * @date 2021/7/23 20:37
 */
@Getter
@Setter
public class BroodWarReport {
    private long attacker;
    private long defencer;
    private Report report;
    private ReportMsg reportMsg;

    public BroodWarReport() {

    }

    @Builder
    public BroodWarReport(long attacker, long defencer, Report report, ReportMsg reportMsg) {
        this.attacker = attacker;
        this.defencer = defencer;
        this.report = report;
        this.reportMsg = reportMsg;
    }

    public DataPb.BroodWarReportData wrapPb() {
        DataPb.BroodWarReportData.Builder builder = DataPb.BroodWarReportData.newBuilder();
        builder.setAttacker(attacker);
        builder.setDefencer(defencer);
        builder.setReport(report.wrapPb());
        builder.setReportMsg(reportMsg.wrapPb());
        return builder.build();
    }

    public void loadData(DataPb.BroodWarReportData data) {
        this.attacker = data.getAttacker();
        this.defencer = data.getDefencer();
        Report report = new Report();
        report.unwrapPb(data.getReport());
        this.report = report;
        ReportMsg reportMsg = new ReportMsg();
        reportMsg.unwrapPb(data.getReportMsg());
        this.reportMsg = reportMsg;
    }

    public long getKeyId() {
        return this.report.getKeyId();
    }
}
