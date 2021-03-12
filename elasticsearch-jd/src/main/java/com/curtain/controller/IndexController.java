package com.curtain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author ：Curtain
 * @date ：Created in 2021/3/11 15:19
 * @description：TODO
 */
@Controller
public class IndexController {
    
    @GetMapping({"/","/index"})
    public String index(){
        return "index";
    }
}
