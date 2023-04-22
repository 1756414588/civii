package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.p.ConfigException;
import com.game.domain.s.StaticSensitiveWord;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "屏蔽字")
public class StaticSensitiveWordMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao staticDataDao;
    private Map<Integer, StaticSensitiveWord> privateChatFilter;
    private Map<Integer, StaticSensitiveWord> countryChatFilter;
    private Map<Integer, StaticSensitiveWord> areaChatFilter;
    private Map<Integer, StaticSensitiveWord> countryMailFilter;


    @Override
    public void load() throws Exception {
        this.privateChatFilter = this.staticDataDao.loadSensitiveWord("privateChatSwitch");
        this.countryChatFilter = this.staticDataDao.loadSensitiveWord("countryChatSwitch");
        this.areaChatFilter = this.staticDataDao.loadSensitiveWord("areaChatSwitch");
        this.countryMailFilter = this.staticDataDao.loadSensitiveWord("countryMailSwitch");
        this.check();
    }

    public void init() throws Exception {

    }

    public boolean containSensitiveWord(String str, String scene) {
//        long startTime = System.currentTimeMillis();
        String REGEX = "[^0-9a-zA-Z\\u4E00-\\u9FA5]";
        Pattern pattern = Pattern.compile(REGEX);
        Matcher m = pattern.matcher(str);
        str = m.replaceAll("").trim().toLowerCase();
        boolean flag = false;
        Map<Integer, StaticSensitiveWord> SensitiveWords = this.getSensitiveWordList(scene);

        // 最高效率遍历Map中的所有value
        for (StaticSensitiveWord SensitiveWord : SensitiveWords.values()) {
            if (str.contains(SensitiveWord.getSensitiveWord())) {
                flag = true;
                break;
            }
        }

//        long endTime = System.currentTimeMillis();
//        System.out.println("判断是否包含敏感词 执行耗时：" + (endTime - startTime) + "ms");
        return flag;
    }

    public String replaceSensitiveWord(String str, String scene) {
//        long startTime = System.currentTimeMillis();
        String raw = str;
//        System.out.println(str);
        String REGEX = "[^0-9a-zA-Z\\u4E00-\\u9FA5]";
        Pattern pattern = Pattern.compile(REGEX);
        Matcher m = pattern.matcher(str);
        str = m.replaceAll("").trim().toLowerCase();
        String result = str;
        Map<Integer, StaticSensitiveWord> SensitiveWords = this.getSensitiveWordList(scene);

        // 最高效率遍历Map中的所有value
        for (StaticSensitiveWord SensitiveWord : SensitiveWords.values()) {
            String Word = SensitiveWord.getSensitiveWord();
            String replaceStr = "";

            for(int i = 0; i < Word.length(); ++i) {
                replaceStr = replaceStr + "*";
            }

            //替换敏感词
            str = str.replaceAll(Word, replaceStr);
        }

        if (!str.equals(result)) {
            raw = str;
        }

//        long endTime = System.currentTimeMillis();
//        System.out.println("替换敏感词耗时：" + (endTime - startTime) + "ms");
        return raw;
    }

    public Map<Integer, StaticSensitiveWord> getSensitiveWordList(String scene) {
        switch(scene) {
            case "privateChatFilter":
                return this.privateChatFilter;
            case "countryChatFilter":
                return this.countryChatFilter;
            case "areaChatFilter":
                return this.areaChatFilter;
            case "countryMailFilter":
                return this.countryMailFilter;
            default:
                return null;
        }
    }

    public void check() throws ConfigException {
        if (privateChatFilter.isEmpty() || countryChatFilter.isEmpty() || areaChatFilter.isEmpty() || countryMailFilter.isEmpty()) {
            throw new ConfigException("敏感词相关配置为空,数据异常");
        }
    }
    public void initSenstiveWord() {
        this.privateChatFilter.clear();
        this.countryChatFilter.clear();
        this.areaChatFilter.clear();
        this.countryMailFilter.clear();
        this.privateChatFilter = this.staticDataDao.loadSensitiveWord("privateChatSwitch");
        this.countryChatFilter = this.staticDataDao.loadSensitiveWord("countryChatSwitch");
        this.areaChatFilter = this.staticDataDao.loadSensitiveWord("areaChatSwitch");
        this.countryMailFilter = this.staticDataDao.loadSensitiveWord("countryMailSwitch");
    }
}
