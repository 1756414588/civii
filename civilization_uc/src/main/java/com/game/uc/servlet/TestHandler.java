package com.game.uc.servlet;

import com.game.spring.SpringUtil;
import com.game.uc.service.RedisService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class TestHandler {


    @ResponseBody
    @RequestMapping(value = "/aa", method = RequestMethod.GET)
    public String get(){
        RedisService bean = SpringUtil.getBean(RedisService.class);
        bean.setAaccout();
        return "aaa";
    }
}
