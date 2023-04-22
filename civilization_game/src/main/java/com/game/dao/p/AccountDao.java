package com.game.dao.p;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import com.game.domain.p.Account;

public interface AccountDao {

	public Account selectAccount(@Param("accountKey") int accountKey, @Param("serverId") int serverId);

	public Account selectAccountByKeyId(int keyId);
	
	@MapKey("lordId")
	public Map<Long, Account> selectAccountMapByLords(List<Long> lordIds);
	
	public void updateCreateRole(Account account);
	
	public void insertAccount(Account account);

	public void recordLoginTime(Account account);

	public void updateIordId(Account account);

	public void updateFirstLoginDate(Account account);

	public void deleteIordId(Account account);
	
	public List<Account> load(@Param("curIndex")long curIndex, @Param("count")int count);
}
