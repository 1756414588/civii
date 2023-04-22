package com.game.log.consumer.domin;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cpz
 * @date 2020/12/6 0:08
 * @description
 */
@Getter
@Setter
public class BaseProperties {
    Map<String, Object> properties = new HashMap<>();
    protected String eventName;
    protected String distinct_id;
    protected String account_id;
    protected Object event_id;
    
    public void register(String key, Object val) {
        this.properties.put(key, val);
    }
}
