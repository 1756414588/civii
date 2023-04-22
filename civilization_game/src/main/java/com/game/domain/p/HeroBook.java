package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author CaoBing
 * @date 2020/12/3 19:42
 */
public class HeroBook implements Cloneable {
    private int pos;                    //兵书槽位,TO DELETE
    private WarBook book;                //兵书,类型决定位置
    /**
     * // 兵书部分技能存档数据(沿用buff类的数据结构,字段)
     * private int buffId;    // buffId, 1. 兵书的技能ID
     * private long period;   // buff时长
     * private long endTime;  // buff结束时间
     * private int value;     // buff效果 / 1000
     */
    private HashMap<Integer, Buff> buffMap = new HashMap<Integer, Buff>();

    public HeroBook() {
        book = new WarBook();
    }

    public HeroBook(int pos, boolean hasBook) {
        this.setPos(pos);
        this.setBook(book);
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public WarBook getBook() {
        return book;
    }

    public void setBook(WarBook book) {
        this.book = book;
    }

    public HeroBook cloneInfo() {
        HeroBook heroBook = new HeroBook();
        heroBook.pos = pos;
        heroBook.book = book.cloneInfo();
        heroBook.buffMap = buffMap;
        return heroBook;
    }

    public CommonPb.HeroBook.Builder wrapPb() {
        CommonPb.HeroBook.Builder builder = CommonPb.HeroBook.newBuilder();
        builder.setPos(pos);
        if (book != null) {
            builder.setBook(book.wrapPb());
        }
        return builder;
    }

    public void unwrapPb(CommonPb.HeroBook build) {
        pos = build.getPos();
        if (build.hasBook()) {
            book.unwrapPb(build.getBook());
        }
    }

    public void cloneBook(WarBook book) {
        this.book = book.cloneInfo();
    }

    public WarBook getEquipClone() {
        return this.book.cloneInfo();
    }

    public ArrayList<Integer> getBaseProperty() {
        return book.getBaseProperty();
    }

    public ArrayList<Integer> getAllSkill() {
        return book.getAllSkill();
    }

    public ArrayList<Integer> getCurrentSkill() {
        return book.getCurrentSkill();
    }

    public HashMap<Integer, Buff> getBuffMap() {
        return buffMap;
    }

    public void setBuffMap(HashMap<Integer, Buff> buffMap) {
        this.buffMap = buffMap;
    }

    public DataPb.HeroBookData.Builder writeData() {
        DataPb.HeroBookData.Builder builder = DataPb.HeroBookData.newBuilder();
        builder.setPos(pos);
        if (book != null) {
            builder.setBook(book.writeData());
        }
        for (Buff buff : buffMap.values()) {
            builder.addBuffData(buff.writeData());
        }
        return builder;
    }

    public void readData(DataPb.HeroBookData build) {
        pos = build.getPos();
        if (build.hasBook()) {
            book.readData(build.getBook());
        }

        List<DataPb.BuffData> buffDatas = build.getBuffDataList();
        for (DataPb.BuffData buffData : buffDatas) {
            Buff buff = new Buff();
            buff.readData(buffData);
            buffMap.put(buff.getBuffId(), buff);
        }
    }

    @Override
    public HeroBook clone() {
        HeroBook heroBook = null;
        try {
            heroBook = (HeroBook) super.clone();
            heroBook.setBook(this.book.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return heroBook;

    }
}
