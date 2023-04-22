package com.game.domain.p;

import com.game.pb.DataPb;

import java.util.Iterator;
import java.util.LinkedList;

// pvp 队伍
public class PvpBattle {
    private int placeId;
    private LinkedList<PvpHero> attackTeam = new LinkedList<PvpHero>();
    private LinkedList<PvpHero> defenceTeam = new LinkedList<PvpHero>();
    private int lastHeroCountry = 0;  // 需要存盘
    private long startedTime;     // 战斗开始时间
    private boolean startBattle;  // 是否开始战斗
    private boolean flag ; //是否是无兵力占领


    public LinkedList<PvpHero> getAttackTeam() {
        return attackTeam;
    }

    public void setAttackTeam(LinkedList<PvpHero> attackTeam) {
        this.attackTeam = attackTeam;
    }

    public LinkedList<PvpHero> getDefenceTeam() {
        return defenceTeam;
    }

    public void setDefenceTeam(LinkedList<PvpHero> defenceTeam) {
        this.defenceTeam = defenceTeam;
    }

    public boolean isHeroInBattle(int paramHeroId, long paramLordId) {
        boolean isInAttackTeam = isHeroInTeam(attackTeam, paramHeroId, paramLordId);
        boolean isInDefenceTeam = isHeroInTeam(defenceTeam, paramHeroId, paramLordId);
        return isInAttackTeam && isInDefenceTeam;
    }

    public boolean isHeroInTeam(LinkedList<PvpHero> team, int paramHeroId, long paramLordId) {
        for (PvpHero pvpHero : team) {
            if (pvpHero == null) {
                continue;
            }

            if (pvpHero.isHeroInBattle(paramHeroId, paramLordId)) {
                return true;
            }
        }

        return false;
    }

    public int getPlaceId() {
        return placeId;
    }

    public void setPlaceId(int placeId) {
        this.placeId = placeId;
    }

    public boolean isEmpty() {
        return defenceTeam.isEmpty() && attackTeam.isEmpty();
    }

    public void addAttacker(PvpHero pvpHero) {
        attackTeam.add(pvpHero);
    }

    public void addDefencer(PvpHero pvpHero) {
        defenceTeam.add(pvpHero);
    }

    public long getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(long startedTime) {
        this.startedTime = startedTime;
    }

    // 战斗三秒倒计时
    public boolean isBattleTick() {
        if (this.startedTime <= 0) {
            return false;
        }

        if (attackTeam.isEmpty()) {
            return false;
        }

        if (defenceTeam.isEmpty()) {
            return false;
        }

        return this.startedTime > System.currentTimeMillis();
    }

    public boolean hasAttacker() {
        return !attackTeam.isEmpty();
    }


    public void removeHero(PvpHero pvpHero) {
        Iterator<PvpHero> defenceIt = defenceTeam.iterator();
        while (defenceIt.hasNext()) {
            PvpHero hero = defenceIt.next();
            if (hero == null) {
                continue;
            }

            if (hero.isEqual(pvpHero)) {
                defenceIt.remove();
                setLastHeroCountry(pvpHero.getCountry());
                //System.out.println("+=======removeHero==========");
                return;
            }


        }

        Iterator<PvpHero> attackerIt = attackTeam.iterator();
        while (attackerIt.hasNext()) {
            PvpHero hero = attackerIt.next();
            if (hero == null) {
                continue;
            }

            if (hero.isEqual(pvpHero)) {
                attackerIt.remove();
                return;
            }
        }
    }

    public void addHero(PvpHero pvpHero) {
        if (!isEmpty()) {
            LinkedList<PvpHero> defenceTeam = getDefenceTeam();
            if (!defenceTeam.isEmpty()) {
                PvpHero first = defenceTeam.getFirst();
                int teamCountry = first.getCountry();
                if (teamCountry == pvpHero.getCountry()) {
                    addDefencer(pvpHero);    // add defencer
                } else {
                    addAttacker(pvpHero);    // add attacker
                }
            } else {
                addDefencer(pvpHero);        // add defencer
            }
        } else {
            addDefencer(pvpHero);            // add defencer
        }
    }

    public boolean isInBattleState() {
        if (this.startedTime <= 0) {
            return false;
        }

        if (attackTeam.isEmpty()) {
            return false;
        }

        if (defenceTeam.isEmpty()) {
            return false;
        }

        return this.startedTime <= System.currentTimeMillis();
    }

    public void insertHero(PvpHero pvpHero) {
        if (!isEmpty()) {
            LinkedList<PvpHero> defenceTeam = getDefenceTeam();
            if (!defenceTeam.isEmpty()) {
                PvpHero first = defenceTeam.getFirst();
                int teamCountry = first.getCountry();
                if (teamCountry == pvpHero.getCountry()) {
                    insertDefencer(pvpHero);    // add defencer
                } else {
                    insertAttacker(pvpHero);    // add attacker
                }
            } else {
                insertDefencer(pvpHero);        // add defencer
            }
        } else {
            insertDefencer(pvpHero);            // add defencer
        }
    }

    public void insertAttacker(PvpHero pvpHero) {
        if (attackTeam.isEmpty()) {
            attackTeam.add(pvpHero);
        } else {
            attackTeam.add(1, pvpHero);
        }
    }

    public void insertDefencer(PvpHero pvpHero) {
        if (defenceTeam.isEmpty()) {
            defenceTeam.add(pvpHero);
        } else {
            defenceTeam.add(1, pvpHero);
        }
    }


    public int getPvpState(PvpHero pvpHero) {
        int pvpState;
        if (isEmpty()) {
            pvpState = 2;
            return pvpState;
        }

        LinkedList<PvpHero> defenceTeam = getDefenceTeam();
        if (defenceTeam.isEmpty()) {
            pvpState = 2;
            return pvpState;
        }

        PvpHero first = defenceTeam.getFirst();
        int teamCountry = first.getCountry();
        if (teamCountry == pvpHero.getCountry()) {
            pvpState = 2;
        } else {
            pvpState = 1;
        }

        return pvpState;
    }

    public LinkedList<PvpHero> getOppositeTeam(int pvpState) {
        if (pvpState == 1) {
            return defenceTeam;
        } else {
            return attackTeam;
        }
    }

    // check attacker and defencer
    public boolean hasTwoSides() {
        return !attackTeam.isEmpty() && !defenceTeam.isEmpty();
    }

    public boolean isStartBattle() {
        return startBattle;
    }

    public void setStartBattle(boolean startBattle) {
        this.startBattle = startBattle;
    }

    public PvpHero getAttacker() {
        if (!attackTeam.isEmpty()) {
            return attackTeam.getFirst();
        }
        return null;
    }

    public PvpHero getDefencer() {
        if (!defenceTeam.isEmpty()) {
            return defenceTeam.getFirst();
        }

        return null;
    }

    public boolean hasDefencer() {
        return !defenceTeam.isEmpty();
    }

    public void clearAttacker() {
        attackTeam.clear();
    }

    public DataPb.PvpBattle.Builder writeData() {
        DataPb.PvpBattle.Builder builder = DataPb.PvpBattle.newBuilder();
        builder.setPlaceId(placeId);
        for (PvpHero pvpHero : attackTeam) {
            builder.addAttackTeam(pvpHero.writeData());
        }

        for (PvpHero pvpHero : defenceTeam) {
            builder.addDefenceTeam(pvpHero.writeData());
        }

        builder.setStartedTime(startedTime);
        builder.setStartBattle(startBattle);
        builder.setLastHeroCountry(lastHeroCountry);

        return builder;
    }

    public void readData(DataPb.PvpBattle data) {
        placeId = data.getPlaceId();
        for (DataPb.PvpHeroData heroData : data.getAttackTeamList()) {
            PvpHero pvpHero = new PvpHero();
            pvpHero.readData(heroData);
            attackTeam.add(pvpHero);
        }

        for (DataPb.PvpHeroData heroData : data.getDefenceTeamList()) {
            PvpHero pvpHero = new PvpHero();
            pvpHero.readData(heroData);
            defenceTeam.add(pvpHero);
        }

        startedTime = data.getStartedTime();
        startBattle = data.getStartBattle();
        lastHeroCountry = data.getLastHeroCountry();
    }

    public void clear() {
        attackTeam.clear();
        defenceTeam.clear();
        startedTime = 0L;
        startBattle = false;
        lastHeroCountry = 0;
    }

    public int getLastHeroCountry() {
        return lastHeroCountry;
    }

    public void setLastHeroCountry(int lastHeroCountry) {
       // System.out.println("=================setLastHeroCountry============="+lastHeroCountry);
        this.lastHeroCountry = lastHeroCountry;
    }

    public boolean hasPvpHero(PvpHero pvpHero) {
        Iterator<PvpHero> defenceIt = defenceTeam.iterator();
        while (defenceIt.hasNext()) {
            PvpHero hero = defenceIt.next();
            if (hero == null) {
                continue;
            }

            if (hero.isEqual(pvpHero)) {
                return true;
            }
        }

        Iterator<PvpHero> attackerIt = attackTeam.iterator();
        while (attackerIt.hasNext()) {
            PvpHero hero = attackerIt.next();
            if (hero == null) {
                continue;
            }

            if (hero.isEqual(pvpHero)) {
                return true;
            }
        }

        return false;
    }

    public int getDefenceNum() {
        return defenceTeam.size();
    }

    public int getAttackerNum() {
        return attackTeam.size();
    }

    public int getHeroIndex(PvpHero pvpHero) {
        for (int i = 0; i < defenceTeam.size(); i++) {
            PvpHero hero = defenceTeam.get(i);
            if (hero == null) {
                continue;
            }
            if (hero.isEqual(pvpHero)) {
                return i+1;
            }
        }

        for (int i = 0; i < attackTeam.size(); i++) {
            PvpHero hero = attackTeam.get(i);
            if (hero == null) {
                continue;
            }
            if (hero.isEqual(pvpHero)) {
                return i+1;
            }
        }

        return 0;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
