package com.game.uc;

public class CdkeyItem {
    private Integer id;

    private Integer giftbagid;

    private Integer itemid;

    private Integer itemtype;

    private Integer itemnum;

    private String itemname;

    private String itemdesc;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGiftbagid() {
        return giftbagid;
    }

    public void setGiftbagid(Integer giftbagid) {
        this.giftbagid = giftbagid;
    }

    public Integer getItemid() {
        return itemid;
    }

    public void setItemid(Integer itemid) {
        this.itemid = itemid;
    }

    public Integer getItemtype() {
        return itemtype;
    }

    public void setItemtype(Integer itemtype) {
        this.itemtype = itemtype;
    }

    public Integer getItemnum() {
        return itemnum;
    }

    public void setItemnum(Integer itemnum) {
        this.itemnum = itemnum;
    }

    public String getItemname() {
        return itemname;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname == null ? null : itemname.trim();
    }

    public String getItemdesc() {
        return itemdesc;
    }

    public void setItemdesc(String itemdesc) {
        this.itemdesc = itemdesc == null ? null : itemdesc.trim();
    }


    @Override
    public String toString() {
        return "CdkeyItem{" +
                "itemid=" + itemid +
                ", itemtype=" + itemtype +
                ", itemnum=" + itemnum +
                '}';
    }
}