package com.game.pay.domain;

/**
 * 快游红包SDK角色等级校验接口参数
 * @author caobing
 *
 */
public class KyValidateParams {
	private int result;

	private String error;

	private KyData data;
	
	public KyValidateParams(int result, String error) {
		super();
		this.result = result;
		this.error = error;
	}

	public KyValidateParams(int result, String error, KyData data) {
		super();
		this.result = result;
		this.error = error;
		this.data = data;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public KyData getData() {
		return data;
	}

	public void setData(KyData data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "KyValidateParams [result=" + result + ", error=" + error + ", data=" + data + "]";
	} 
}
