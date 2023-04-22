package com.game.domain.p;

import com.game.domain.Player;
import com.game.pb.CommonPb;

import java.util.ArrayList;
import java.util.List;

// 战报信息
public class Report {
    private long keyId;
    private boolean result;
    private ReportHead leftHead = new ReportHead();
    private ReportHead rightHead = new ReportHead();
    private List<Attender> leftAttender = new ArrayList<Attender>();
    private List<Attender> rightAttender = new ArrayList<Attender>();

    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

    public ReportHead getLeftHead() {
        return leftHead;
    }

    public void setLeftHead(ReportHead leftHead) {
        this.leftHead = leftHead;
    }

    public List<Attender> getLeftAttender() {
        return leftAttender;
    }

    public void setLeftAttender(List<Attender> leftAttender) {
        this.leftAttender = leftAttender;
    }

    public List<Attender> getRightAttender() {
        return rightAttender;
    }

    public void setRightAttender(List<Attender> rightAttender) {
        this.rightAttender = rightAttender;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public ReportHead getRightHead() {
        return rightHead;
    }

    public void setRightHead(ReportHead rightHead) {
        this.rightHead = rightHead;
    }


    // report
    public CommonPb.Report.Builder wrapPb() {
        CommonPb.Report.Builder builder = CommonPb.Report.newBuilder();
        builder.setKeyId(keyId);
        builder.setResult(result);
        builder.setLeftHead(leftHead.wrapPb());
        builder.setRightHead(rightHead.wrapPb());
        for (Attender attender : leftAttender) {
            if (attender == null) {
                continue;
            }

            builder.addLeftAttender(attender.wrapPb());
        }
        for (Attender attender : rightAttender) {
            if (attender == null) {
                continue;
            }

            builder.addRightAttender(attender.wrapPb());
        }
        return builder;
    }


    public CommonPb.Report.Builder wrapPb(Player player) {
        CommonPb.Report.Builder builder = CommonPb.Report.newBuilder();
        boolean flag = true;
        if (!leftHead.getName().equals(player.getNick())) {
            flag = false;
        }
        builder.setKeyId(keyId);
        builder.setResult(flag ? result : !result);
        builder.setLeftHead(flag ? leftHead.wrapPb() : rightHead.wrapPb());
        builder.setRightHead(flag ? rightHead.wrapPb() : leftHead.wrapPb());
        for (Attender attender : flag ? leftAttender : rightAttender) {
            if (attender == null) {
                continue;
            }

            builder.addLeftAttender(attender.wrapPb());
        }
        for (Attender attender : flag ? rightAttender : leftAttender) {
            if (attender == null) {
                continue;
            }

            builder.addRightAttender(attender.wrapPb());
        }
        return builder;
    }

    // report
    public void unwrapPb(CommonPb.Report builder) {
        keyId = builder.getKeyId();
        result = builder.getResult();
        if (builder.hasLeftHead()) {
            leftHead.unwrapPb(builder.getLeftHead());
        }

        if (builder.hasRightHead()) {
            rightHead.unwrapPb(builder.getRightHead());
        }

        // 参与者
        for (CommonPb.Attender attenderPb : builder.getLeftAttenderList()) {
            Attender attender = new Attender();
            attender.unwrapPb(attenderPb);
            leftAttender.add(attender);
        }

        for (CommonPb.Attender attenderPb : builder.getRightAttenderList()) {
            Attender attender = new Attender();
            attender.unwrapPb(attenderPb);
            rightAttender.add(attender);
        }
    }

}
