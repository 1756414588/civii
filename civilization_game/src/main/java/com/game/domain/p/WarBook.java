package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.util.LogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CaoBing
 * @date 2020/12/3 17:06
 */
public class WarBook implements Cloneable {

	private int keyId;                    //兵书唯一Id(因为兵书有技能，所以需要区分)
	private int bookId;                  //兵书配置ID
	private ArrayList<Integer> baseProperty;    //兵书基础属性

	private ArrayList<Integer> allSkill; //兵书配置的总技能
	private ArrayList<Integer> currentSkill; //当前已经解锁的技能
	private int isLock;//是否加锁
	private int soldierType;//兵种技能对应的兵种类型
	private int basePropertyLv;//兵书基础技能的等级(对应兵书的等级)

	public WarBook() {
		baseProperty = new ArrayList<Integer>();
		allSkill = new ArrayList<Integer>();
		currentSkill = new ArrayList<Integer>();
	}

	public WarBook(int keyId, int bookId, int soldierType, int basePropertyLv, ArrayList<Integer> baseProperty, ArrayList<Integer> allSkill, ArrayList<Integer> currentSkill) {
		this.keyId = keyId;
		this.bookId = bookId;
		this.soldierType = soldierType;
		this.basePropertyLv = basePropertyLv;
		this.copyBookPropAndSkill(baseProperty, allSkill, currentSkill);
	}

	private void copyBookPropAndSkill(ArrayList<Integer> baseProperty, ArrayList<Integer> allSkill, ArrayList<Integer> currentSkill) {
		if (this.baseProperty == null) {
			LogHelper.CONFIG_LOGGER.trace("book: baseProperty nu");
			return;
		}
		this.baseProperty = new ArrayList<Integer>();
		this.baseProperty.addAll(baseProperty);

		if (this.allSkill == null) {
			LogHelper.CONFIG_LOGGER.trace("book: allSkill nu");
			return;
		}
		this.allSkill = new ArrayList<Integer>();
		this.allSkill.addAll(allSkill);

		if (this.currentSkill == null) {
			LogHelper.CONFIG_LOGGER.trace("book: currentSkill nu");
			return;
		}
		this.currentSkill = new ArrayList<Integer>();
		this.currentSkill.addAll(currentSkill);
	}

	public WarBook cloneInfo() {
		WarBook book = new WarBook();
		book.keyId = keyId;
		book.bookId = bookId;
		book.isLock = isLock;
		book.soldierType = soldierType;
		book.basePropertyLv = basePropertyLv;
		book.copyBookPropAndSkill(baseProperty, allSkill, currentSkill);
		return book;
	}

	public CommonPb.WarBook.Builder wrapPb() {
		CommonPb.WarBook.Builder builder = CommonPb.WarBook.newBuilder();
		builder.setKeyId(keyId);
		builder.setBookId(bookId);
		builder.setIsLock(isLock);
		builder.setSoldierType(soldierType);
		builder.setBasePropertyLv(basePropertyLv);
		//兵书基础属性
		builder.addAllBaseProperty(baseProperty);
		builder.addAllCurrentSkill(currentSkill);
		builder.addAllAllSkill(allSkill);
        /*for (int i = 0; i < baseProperty.size(); i++) {
            Integer basePropertyId = baseProperty.get(i);
            if (basePropertyId == null) {
                continue;
            }
            builder.addBaseProperty(basePropertyId);
        }

        for (int i = 0; i < totalSkill.size(); i++) {
            Integer totalSkillId = totalSkill.get(i);
            if (totalSkillId == null) {
                continue;
            }
            builder.addTotalSkill(totalSkillId);
        }

        for (int i = 0; i < currentSkill.size(); i++) {
            Integer currentSkillId = currentSkill.get(i);
            if (currentSkillId == null) {
                continue;
            }
            builder.addCurrentSkill(currentSkillId);
        }*/

		return builder;
	}

	public void unwrapPb(CommonPb.WarBook build) {
		keyId = build.getKeyId();
		bookId = build.getBookId();
		isLock = build.getIsLock();
		soldierType = build.getSoldierType();
		basePropertyLv = build.getBasePropertyLv();

		//基础属性
		baseProperty.clear();
		List<Integer> basePropertyDatas = build.getBasePropertyList();
		for (int i = 0; i < basePropertyDatas.size(); i++) {
			Integer basePropertyId = basePropertyDatas.get(i);
			if (basePropertyId == null) {
				continue;
			}

			baseProperty.add(basePropertyId);
		}

		//配置总技能
		allSkill.clear();
		List<Integer> allSkillDatas = build.getAllSkillList();
		for (int i = 0; i < allSkillDatas.size(); i++) {
			Integer allSkillId = allSkillDatas.get(i);
			if (allSkillId == null) {
				continue;
			}

			allSkill.add(allSkillId);
		}

		//当前技能
		currentSkill.clear();
		List<Integer> currentSkillDatas = build.getCurrentSkillList();
		for (int i = 0; i < currentSkillDatas.size(); i++) {
			Integer currentSkillId = currentSkillDatas.get(i);
			if (currentSkillId == null) {
				continue;
			}

			currentSkill.add(currentSkillId);
		}
	}


	public void copyData(WarBook book) {
		keyId = book.getKeyId();
		bookId = book.getBookId();
		isLock = book.getIsLock();
		soldierType = book.getSoldierType();
		basePropertyLv = book.getBasePropertyLv();
		copyBookPropAndSkill(book.getBaseProperty(), book.getAllSkill(), book.getCurrentSkill());
	}


	public DataPb.WarBookData.Builder writeData() {
		DataPb.WarBookData.Builder builder = DataPb.WarBookData.newBuilder();
		builder.setKeyId(keyId);
		builder.setBookId(bookId);
		builder.setIsLock(isLock);
		builder.setSoldierType(soldierType);
		builder.setBasePropertyLv(basePropertyLv);
		//基础属性
		builder.addAllBaseProperty(baseProperty);
		builder.addAllCurrentSkill(currentSkill);
		builder.addAllAllSkill(allSkill);
        /*for (int i = 0; i < baseProperty.size(); i++) {
            Integer basePropertyId = baseProperty.get(i);
            if (basePropertyId == null) {
                continue;
            }
            builder.addBaseProperty(basePropertyId);
        }

        //总技能
        for (int i = 0; i < totalSkill.size(); i++) {
            Integer totalSkillId = totalSkill.get(i);
            if (totalSkillId == null) {
                continue;
            }
            builder.addTotalSkill(totalSkillId);
        }

        //当前技能
        for (int i = 0; i < currentSkill.size(); i++) {
            Integer currentSkillId = currentSkill.get(i);
            if (currentSkillId == null) {
                continue;
            }
            builder.addCurrentSkill(currentSkillId);
        }*/
		return builder;
	}

	public void readData(DataPb.WarBookData build) {
		keyId = build.getKeyId();
		bookId = build.getBookId();
		isLock = build.getIsLock();
		soldierType = build.getSoldierType();
		basePropertyLv = build.getBasePropertyLv();

		//基础属性技能
		baseProperty.clear();
		for (Integer basePropertyId : build.getBasePropertyList()) {
			if (basePropertyId == null) {
				continue;
			}
			baseProperty.add(basePropertyId);
		}

		allSkill.clear();
		for (Integer allSkillId : build.getAllSkillList()) {
			if (allSkillId == null) {
				continue;
			}
			allSkill.add(allSkillId);
		}

		currentSkill.clear();
		for (Integer currentSkillId : build.getCurrentSkillList()) {
			if (currentSkillId == null) {
				continue;
			}
			currentSkill.add(currentSkillId);
		}

	}

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public ArrayList<Integer> getBaseProperty() {
		return baseProperty;
	}

	public void setBaseProperty(ArrayList<Integer> baseProperty) {
		this.baseProperty = baseProperty;
	}

	public ArrayList<Integer> getCurrentSkill() {
		return currentSkill;
	}

	public void setCurrentSkill(ArrayList<Integer> currentSkill) {
		this.currentSkill = currentSkill;
	}

	public int getIsLock() {
		return isLock;
	}

	public void setIsLock(int isLock) {
		this.isLock = isLock;
	}

	public int getSoldierType() {
		return soldierType;
	}

	public void setSoldierType(int soldierType) {
		this.soldierType = soldierType;
	}

	public int getBasePropertyLv() {
		return basePropertyLv;
	}

	public void setBasePropertyLv(int basePropertyLv) {
		this.basePropertyLv = basePropertyLv;
	}

	public ArrayList<Integer> getAllSkill() {
		return allSkill;
	}

	public void setAllSkill(ArrayList<Integer> allSkill) {
		this.allSkill = allSkill;
	}

	@Override
	public String toString() {
		return "WarBook{" +
			"keyId=" + keyId +
			", bookId=" + bookId +
			", baseProperty=" + baseProperty +
			", allSkill=" + allSkill +
			", currentSkill=" + currentSkill +
			", isLock=" + isLock +
			", soldierType=" + soldierType +
			", basePropertyLv=" + basePropertyLv +
			'}';
	}

	@Override
	public WarBook clone() {
		WarBook warBook = null;
		try {
			warBook = (WarBook) super.clone();
			ArrayList<Integer> list1 = new ArrayList<>();
			this.baseProperty.forEach(integer -> {
				list1.add(integer);
			});
			warBook.setBaseProperty(list1);

			ArrayList<Integer> list2 = new ArrayList<>();
			this.allSkill.forEach(integer -> {
				list2.add(integer);
			});
			warBook.setAllSkill(list2);

			ArrayList<Integer> list3 = new ArrayList<>();
			this.currentSkill.forEach(integer -> {
				list3.add(integer);
			});
			warBook.setCurrentSkill(list3);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return warBook;
	}
}
