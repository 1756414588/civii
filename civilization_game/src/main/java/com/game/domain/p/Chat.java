package com.game.domain.p;

import java.util.ArrayList;
import java.util.List;

import com.game.pb.CommonPb;

/**
 * @author CaoBing
 * @date 2020/9/16 18:07
 * 聊天实体类
 */
public class Chat {
	public static final int WORLD = 1;
	public static final int COUNTRYS = 2;
	public static final int MAPCHAT = 3;
	public static final int VIPCHAT = 4;
	public static final int GAME_CHAT = 5;

    private int style;           // 0.国家聊天 1.玩家喇叭 2.公告滚屏
    private long lordId;            // 角色ID
    private int country;            // 国家
    private int title;            // 爵位
    private int level;            // 等级
    private String name;            // 昵称
    private int portrait;        // 头像
    private int x;                // 玩家坐标X
    private int y;                // 玩家坐标Y
    private String msg;            // 内容
    private long time;            // 发送时间
    private int chatId;            // s_chat表中的ID
    private String param;      // 参数
    private int mailKeyId;        // mailKeyId
    private int gm;                // 0,false玩家 1,true.GM
    private int guider;            // 0,false玩家 1,true新手指导员
    private int officerId;        // 官职
    private int type;        // 类型(1.world 2.countrys 3.mapChat 4.vipChat 5.gameChat)
    private int chatType;        //0.阵营聊天 1.区域聊天 2.私人聊天 3.全服聊天

    public Chat(CommonPb.Chat chat) {
        this.style = chat.getStyle();           // 0.国家聊天 1.玩家喇叭 2.公告滚屏
        this.lordId = chat.getLordId();            // 角色ID
        this.country = chat.getCountry();            // 国家
        this.title = chat.getTitle();            // 爵位
        this.level = chat.getLevel();            // 等级
        this.name = chat.getName();            // 昵称
        this.portrait = chat.getPortrait();        // 头像
        this.x = chat.getX();                // 玩家坐标X
        this.y = chat.getY();                // 玩家坐标Y
        this.msg = chat.getMsg();            // 内容
        this.time = chat.getTime();            // 发送时间
        this.chatId = chat.getChatId();            // s_chat表中的ID
        this.chatType = chat.getChatType();            //0.阵营聊天 1.区域聊天 2.私人聊天 3.全服聊天

        List<String> params = chat.getParamList();
        if (params.size() == 0) {
            this.param = "";    // 参数
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < params.size(); i++) {
                if (i == params.size() - 1) {
                    builder.append(params.get(i));
                } else {
                    builder.append(params.get(i));
                    builder.append(";");
                }
            }
            this.param = builder.toString();    // 参数
        }
        this.mailKeyId = chat.getMailKeyId();        // mailKeyId
        this.gm = chat.getGm() == false ? 0 : 1;       // 0,false玩家 1,true.GM
        this.guider = chat.getGuider() == false ? 0 : 1;            // 0,false玩家 1,true新手指导员
        this.officerId = chat.getOfficerId();        // 官职
    }

    public static CommonPb.Chat decChat(Chat chat){
        CommonPb.Chat.Builder chatBuild = CommonPb.Chat.newBuilder();

        chatBuild.setStyle(chat.getStyle());// 0.国家聊天 1.玩家喇叭 2.公告滚屏
        chatBuild.setLordId(chat.getLordId()); // 角色ID
        chatBuild.setCountry(chat.getCountry()); // 国家
        chatBuild.setTitle(chat.getTitle());// 爵位
        chatBuild.setLevel(chat.getLevel());// 等级
        chatBuild.setName(chat.getName()); // 昵称
        chatBuild.setPortrait(chat.getPortrait()); // 头像
        chatBuild.setX(chat.getX());// 玩家坐标X
        chatBuild.setY(chat.getY());// 玩家坐标Y
        chatBuild.setMsg(chat.getMsg());            // 内容
        chatBuild.setTime(chat.getTime());            // 发送时间
        chatBuild.setChatId(chat.getChatId());            // s_chat表中的ID
        chatBuild.setChatType(chat.getChatType());            //0.阵营聊天 1.区域聊天 2.私人聊天

        String param = chat.getParam();
        if(null != param && !param.equals("")) {
        	List<String> params = new ArrayList<String>();
        	String[] split = param.split(";");
        	for (String string : split) {
        		params.add(string);
            }
            chatBuild.addAllParam(params);
        }
        chatBuild.setMailKeyId(chat.getMailKeyId());        // mailKeyId
        chatBuild.setGm(chat.getGm() == 0?false : true);      // 0,false玩家 1,true.GM
        chatBuild.setGuider(chat.getGuider() == 0?false : true);            // 0,false玩家 1,true新手指导员
        chatBuild.setOfficerId(chat.getOfficerId());        // 官职
        return chatBuild.build();
    }

    public Chat() {
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPortrait() {
        return portrait;
    }

    public void setPortrait(int portrait) {
        this.portrait = portrait;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public int getMailKeyId() {
        return mailKeyId;
    }

    public void setMailKeyId(int mailKeyId) {
        this.mailKeyId = mailKeyId;
    }

    public int getGm() {
        return gm;
    }

    public void setGm(int gm) {
        this.gm = gm;
    }

    public int getGuider() {
        return guider;
    }

    public void setGuider(int guider) {
        this.guider = guider;
    }

    public int getOfficerId() {
        return officerId;
    }

    public void setOfficerId(int officerId) {
        this.officerId = officerId;
    }

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }


    @Override
    public String toString() {
        return "Chat{" +
                "style=" + style +
                ", lordId=" + lordId +
                ", country=" + country +
                ", title=" + title +
                ", level=" + level +
                ", name='" + name + '\'' +
                ", portrait=" + portrait +
                ", x=" + x +
                ", y=" + y +
                ", msg='" + msg + '\'' +
                ", time=" + time +
                ", chatId=" + chatId +
                ", param='" + param + '\'' +
                ", mailKeyId=" + mailKeyId +
                ", gm=" + gm +
                ", guider=" + guider +
                ", officerId=" + officerId +
                ", type=" + type +
                ", chatType=" + chatType +
                '}';
    }
}
