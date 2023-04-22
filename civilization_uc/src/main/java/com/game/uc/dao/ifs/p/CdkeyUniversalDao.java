package com.game.uc.dao.ifs.p;

import com.game.uc.CdkeyUniversal;
import com.game.uc.CdkeyUniversalKey;

import java.util.List;

public interface CdkeyUniversalDao {
    int deleteByPrimaryKey(CdkeyUniversalKey key);

    int insert(CdkeyUniversal record);

    int insertSelective(CdkeyUniversal record);

    CdkeyUniversal selectByPrimaryKey(CdkeyUniversalKey key);

    int updateByPrimaryKeySelective(CdkeyUniversal record);

    int updateByPrimaryKey(CdkeyUniversal record);

    List<CdkeyUniversal> selectByRoleId(long roleId);

    List<CdkeyUniversal> selectByCdk(String ckd);
}