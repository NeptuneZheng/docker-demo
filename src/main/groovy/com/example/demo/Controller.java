package com.example.demo;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ZHENGNE on 11/20/2018.
 */
@RestController
@EnableAutoConfiguration
public class Controller {

    @RequestMapping(value = "/",method = RequestMethod.GET)
    @ResponseBody
    public String login(){
        return "2333";
    }

    @RequestMapping(value = "/hello",method = RequestMethod.GET)
    @ResponseBody
    public String sayHello(){
        return "this is a new face";
    }
}
