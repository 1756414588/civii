package com.game.domain.p;

import com.game.pb.CommonPb.ManoeuverApply;
import com.game.pb.CommonPb.ManoeuverReport;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;

public class Mail {

    private int keyId;
    private long lordId;
    private int mailId;// s_mail表的mailId
    private int state; // 0未读1.已读2.锁定
    private int awardGot;
    private int[] title;
    private String[] param;
    private List<CommonPb.Award> showAward = new ArrayList<CommonPb.Award>();// 叛军展示的奖励
    private List<CommonPb.Award> award = new ArrayList<CommonPb.Award>();
    private CommonPb.Report report;
    private CommonPb.ReportMsg reportMsg;
    private long createTime;
    private List<CommonPb.HeroScot> heroScots = new ArrayList<CommonPb.HeroScot>();
    private int portrait;
    private int replyId;
    private long replyLordId;
    private long mailKey;    //mail的key删除时调用
    private LinkedList<CommonPb.MailReply> mailReply = new LinkedList<CommonPb.MailReply>();
    private CommonPb.MailCollectRes mailCollectRes;
    private List<CommonPb.SoldierRec> soldierRecs = new ArrayList<CommonPb.SoldierRec>();
    private List<CommonPb.RiotAssist> riotAssist = new ArrayList<CommonPb.RiotAssist>();
	private ManoeuverReport manoeuverReport;
	private List<ManoeuverApply> manoeuverApply = new ArrayList<>();


    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getMailId() {
        return mailId;
    }

    public void setMailId(int mailId) {
        this.mailId = mailId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getAwardGot() {
        return awardGot;
    }

    public void setAwardGot(int awardGot) {
        this.awardGot = awardGot;
    }

    public int[] getTitle() {
        return title;
    }

    public void setTitle(int[] title) {
        this.title = title;
    }

    public String[] getParam() {
        return param;
    }

    public void setParam(String[] param) {
        this.param = param;
    }

    public CommonPb.Report getReport() {
        return report;
    }

    public void setReport(CommonPb.Report report) {
        this.report = report;
    }

    public List<CommonPb.Award> getAward() {
        return award;
    }

    public void setAward(List<CommonPb.Award> award) {
        this.award = award;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getPortrait() {
        return portrait;
    }

    public void setPortrait(int portrait) {
        this.portrait = portrait;
    }

    public int getReplyId() {
        return replyId;
    }

    public void setReplyId(int replyId) {
        this.replyId = replyId;
    }

    public long getReplyLordId() {
        return replyLordId;
    }

    public void setReplyLordId(long replyLordId) {
        this.replyLordId = replyLordId;
    }

    public LinkedList<CommonPb.MailReply> getMailReply() {
        return mailReply;
    }

    public void setMailReply(LinkedList<CommonPb.MailReply> mailReply) {
        this.mailReply = mailReply;
    }

    public List<CommonPb.Award> getShowAward() {
        return showAward;
    }

    public void setShowAward(List<CommonPb.Award> showAward) {
        this.showAward = showAward;
    }

	public ManoeuverReport getManoeuverReport() {
		return manoeuverReport;
	}

	public void setManoeuverReport(ManoeuverReport manoeuverReport) {
		this.manoeuverReport = manoeuverReport;
	}

	public List<ManoeuverApply> getManoeuverApply() {
		return manoeuverApply;
	}

	public void setManoeuverApply(List<ManoeuverApply> manoeuverApply) {
		this.manoeuverApply = manoeuverApply;
	}

    public Mail() {
    }

    public Mail(DataPb.MailData mailData) {
        this.keyId = mailData.getKeyId();
        this.lordId = mailData.getLordId();
        this.mailId = mailData.getMailId();
        this.state = mailData.getState();
        this.awardGot = mailData.getAwardGot();
        this.createTime = mailData.getCreateTime();
        this.portrait = mailData.getPortrait();
        this.replyId = mailData.getReplyId();
        this.replyLordId = mailData.getReplyLordId();

        if (mailData.getMailReplyCount() > 0) {
            for (CommonPb.MailReply mailReply : mailData.getMailReplyList()) {
                this.mailReply.add(mailReply);
            }
        }

        if (mailData.getTitleParamCount() > 0) {
            int c = 0;
            this.title = new int[mailData.getTitleParamList().size()];
            for (Integer p : mailData.getTitleParamList()) {
                this.title[c++] = p;
            }
        }
        if (mailData.getParamCount() > 0) {
            int c = 0;
            this.param = new String[mailData.getParamCount()];
            for (String p : mailData.getParamList()) {
                this.param[c++] = p;
            }
        }
        if (mailData.getAwardCount() > 0) {
            this.award.addAll(mailData.getAwardList());
        }

        if (mailData.getReport() != null) {
            this.setReport(mailData.getReport());
        }

        if (mailData.getReportMsg() != null) {
            this.setReportMsg(mailData.getReportMsg());
        }

        if (mailData.getScoutDataCount() > 0) {
            this.heroScots.addAll(mailData.getScoutDataList());
        }

        if (mailData.getMailCollectRes() != null) {
            this.setMailCollectRes(mailData.getMailCollectRes());
        }

        if (mailData.getShowAwardCount() > 0) {
            this.showAward.addAll(mailData.getShowAwardList());
        }

        if (mailData.getSoldierRecCount() > 0) {
            this.getSoldierRecs().addAll(mailData.getSoldierRecList());
        }

        if (mailData.getRiotAssitCount() > 0) {
            riotAssist.addAll(mailData.getRiotAssitList());
		}

		if (mailData.getManoeuverApplyCount() > 0) {
			manoeuverApply.addAll(mailData.getManoeuverApplyList());
		}

		if (mailData.getManoeuverReport() != null) {
			this.manoeuverReport = mailData.getManoeuverReport();
        }

        if (mailData.getMailKey() > 0) {
            this.mailKey = mailData.getMailKey();
        }
    }

    public Mail(int keyId, long lordId, int type, int mailId, int state, long time) {
        this.keyId = keyId;
        this.lordId = lordId;
        this.mailId = mailId;
        this.state = state;
        this.createTime = time;
    }

    public DataPb.MailData serMailData() {
        DataPb.MailData.Builder builder = DataPb.MailData.newBuilder();
        builder.setKeyId(keyId);
        builder.setMailId(mailId);
        builder.setState(state);
        builder.setLordId(lordId);
        builder.setAwardGot(awardGot);
        builder.setCreateTime(createTime);
        builder.setPortrait(portrait);
        builder.setReplyId(replyId);
        builder.setReplyLordId(replyLordId);
        if (!mailReply.isEmpty()) {
            builder.addAllMailReply(mailReply);
        }

        if (title != null && title.length > 0) {
            for (Integer pa : title) {
                builder.addTitleParam(pa);
            }
        }
        if (param != null && param.length > 0) {
            for (String pa : param) {
                if (pa == null) {
                    continue;
                }
                builder.addParam(pa);
            }
        }
        if (!award.isEmpty()) {
            builder.addAllAward(award);
        }
        if (report != null) {
            builder.setReport(report);
        }
        if (reportMsg != null) {
            builder.setReportMsg(reportMsg);
        }

        if (!heroScots.isEmpty()) {
            builder.addAllScoutData(heroScots);
        }

        if (getMailCollectRes() != null) {
            builder.setMailCollectRes(getMailCollectRes());
        }

        if (!showAward.isEmpty()) {
            builder.addAllShowAward(showAward);
        }

        if (!getSoldierRecs().isEmpty()) {
            builder.addAllSoldierRec(getSoldierRecs());
        }

        if (!riotAssist.isEmpty()) {
            builder.addAllRiotAssit(riotAssist);
        }
		if (manoeuverReport != null) {
			builder.setManoeuverReport(manoeuverReport);
		}
		if (!manoeuverApply.isEmpty()) {
			builder.addAllManoeuverApply(manoeuverApply);
		}
        builder.setMailKey(mailKey);
        return builder.build();
    }

    public CommonPb.Mail serShow() {
        CommonPb.Mail.Builder builder = CommonPb.Mail.newBuilder();
        builder.setKeyId(keyId);
        builder.setMailId(mailId);
        builder.setState(state);
        builder.setLordId(lordId);
        builder.setAwardGot(awardGot);
        builder.setCreateTime(createTime);
        builder.setReplyId(replyId);
        builder.setReplyLordId(replyLordId);
        if (!mailReply.isEmpty()) {
            builder.addAllMailReply(mailReply);
        }
        builder.setPortrait(portrait);
        if (title != null && title.length > 0 && title.length <= param.length) {
            for (Integer index : title) {
                builder.addTitleParam(param[index]);
            }
        }
        if (mailCollectRes != null && mailCollectRes.hasPos()) {
            builder.setMailCollectRes(mailCollectRes);
        }
        if (param != null && param.length > 0) {
            for (String pa : param) {
                builder.addParam(pa);
            }
        }
        if (!showAward.isEmpty()) {
            builder.addAllShowAward(showAward);
        }

        if (!getSoldierRecs().isEmpty()) {
            builder.addAllSoldierRec(getSoldierRecs());
        }

        if (!riotAssist.isEmpty()) {
            builder.addAllRiotAssit(riotAssist);
        }

        if (!award.isEmpty()) {
            builder.addAllAward(award);
        }

		if (manoeuverReport != null) {
			builder.setManoeuverReport(manoeuverReport);
		}

		if(!manoeuverApply.isEmpty()){
			builder.addAllManoeuverApply(manoeuverApply);
		}

        return builder.build();
    }

    /**
     * 给出参与双方武将信息
     *
     * @return
     */
    public CommonPb.Mail serDefault() {
        CommonPb.Mail.Builder builder = CommonPb.Mail.newBuilder();
        builder.setKeyId(keyId);
        builder.setMailId(mailId);
        builder.setLordId(lordId);
        builder.setState(state);
        builder.setAwardGot(awardGot);
        builder.setCreateTime(createTime);
        builder.setPortrait(portrait);
        builder.setReplyId(replyId);
        builder.setReplyLordId(replyLordId);
        if (!mailReply.isEmpty()) {
            builder.addAllMailReply(mailReply);
        }
        if (title != null && title.length > 0 && title.length <= param.length) {
            for (Integer index : title) {
                builder.addTitleParam(param[index]);
            }
        }
        if (param != null && param.length > 0) {
            for (String pa : param) {
                builder.addParam(pa);
            }
        }
        if (!award.isEmpty()) {
            builder.addAllAward(award);
        }
        if (report != null) {
            builder.setReport(report);
        }

        if (!heroScots.isEmpty()) {
            builder.addAllHeroScot(heroScots);
        }

        if (getMailCollectRes() != null) {
            builder.setMailCollectRes(getMailCollectRes());
        }

        if (!showAward.isEmpty()) {
            builder.addAllShowAward(showAward);
        }

        if (!getSoldierRecs().isEmpty()) {
            builder.addAllSoldierRec(getSoldierRecs());
        }

        if (!riotAssist.isEmpty()) {
            builder.addAllRiotAssit(riotAssist);
        }

		if (manoeuverReport != null) {
			builder.setManoeuverReport(manoeuverReport);
		}

		if(!manoeuverApply.isEmpty()){
			builder.addAllManoeuverApply(manoeuverApply);
		}

        return builder.build();
    }

    public CommonPb.ReportMsg getReportMsg() {
        return reportMsg;
    }

    public void setReportMsg(CommonPb.ReportMsg reportMsg) {
        this.reportMsg = reportMsg;
    }

    public List<CommonPb.HeroScot> getHeroScots() {
        return heroScots;
    }

    public void setHeroScots(List<CommonPb.HeroScot> heroScots) {
        this.heroScots = heroScots;
    }

    public CommonPb.MailCollectRes getMailCollectRes() {
        return mailCollectRes;
    }

    public void setMailCollectRes(CommonPb.MailCollectRes mailCollectRes) {
        this.mailCollectRes = mailCollectRes;
    }

    public List<CommonPb.SoldierRec> getSoldierRecs() {
        return soldierRecs;
    }

    public void setSoldierRecs(List<CommonPb.SoldierRec> soldierRecs) {
        this.soldierRecs = soldierRecs;
    }

    public List<CommonPb.RiotAssist> getRiotAssist() {
        return riotAssist;
    }

    public void setRiotAssist(List<CommonPb.RiotAssist> riotAssist) {
        this.riotAssist = riotAssist;
    }

    public long getMailKey() {
        return mailKey;
    }

    public void setMailKey(long mailKey) {
        this.mailKey = mailKey;
    }
}
