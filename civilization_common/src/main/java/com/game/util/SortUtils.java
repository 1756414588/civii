package com.game.util;


import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.springframework.util.StringUtils;

/**
*2020年5月19日
*@CaoBing
*halo_uc
*SortUtils.java
**/
public class SortUtils {
	/**
	 * 字典排序url字段
	 * 
     * @param paraMap 参数
     * @param encode 编码
     * @param isLower 是否区分小写
     * @return
     */
    public static String formatUrlParam(Map<String, String> param, String encode, boolean isLower) {
        String params = "";
        Map<String, String> map = param;
        
        try {
            List<Map.Entry<String, String>> itmes = new ArrayList<Map.Entry<String, String>>(map.entrySet());
            
            //对所有传入的参数按照字段名从小到大排序
            //Collections.sort(items); 默认正序
            //可通过实现Comparator接口的compare方法来完成自定义排序
            Collections.sort(itmes, new Comparator<Map.Entry<String, String>>() {
                @Override
                public int compare(Entry<String, String> o1, Entry<String, String> o2) {
                    // TODO Auto-generated method stub
                    return (o1.getKey().toString().compareTo(o2.getKey()));
                }
            });
            //构造URL 键值对的形式
            StringBuffer sb = new StringBuffer();
            for (Map.Entry<String, String> item : itmes) {
                if (!StringUtils.isEmpty(item.getKey())) {
                    String key = item.getKey();
                    String val = item.getValue();
                    val = URLDecoder.decode(val, encode);
                    if (isLower) {
                        sb.append(key.toLowerCase() + "=" + val);
                    } else {
                        sb.append(key + "=" + val);
                    }
                    sb.append("&");
                }
            }
            
            params = sb.toString();
            if (!params.isEmpty()) {
                params = params.substring(0, params.length() - 1);
            }
        } catch (Exception e) {
            return "";
        }
        return params;
    }
    
    @Test
	public void aaa() {
		/*Map<String, String> map = new HashMap<String, String>();
		map.put("crTce", "200");
		map.put("bitTe", "测试标题");
		map.put("aonTent", "测试内容");
		map.put("drder_To", "1807160850122023");
		String url = SortUtils.formatUrlParam(map, "utf-8", false);
		System.out.println(url);*/
		
		String aaa = "appId=1855&body=60钻石[在线充值2020070920314764430456]&createTime=2020-07-09 20:31:47&extradata=202007092031451000002097969865&fee=0.01&orderId=202007092031451000002097969865&servername=预发布&status=succ&subject=60钻石&trade_sn=2020070920314764430456&userid=13301625&username=13301625alg46fsfmkfy8wg6pqxmbgmaxorz1etq";
		/*String newKySign = Md5Util.string2MD5(aaa);
		System.out.println(newKySign);*/
		
		String md5 = Md5Util.string2MD5(aaa);
		System.out.println(md5);
	}
}

