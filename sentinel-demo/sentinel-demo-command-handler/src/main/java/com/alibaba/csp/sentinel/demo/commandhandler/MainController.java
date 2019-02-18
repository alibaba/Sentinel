package com.alibaba.csp.sentinel.demo.commandhandler;

import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @GetMapping("/init")
    public @ResponseBody
    String checkPreload() {
        String retVal;
        String resource = "init";
        try{
            SphU.entry(resource);
            retVal = "pass";
        }catch (BlockException e){
            retVal = "block";
        }
        return retVal;
    }

}
