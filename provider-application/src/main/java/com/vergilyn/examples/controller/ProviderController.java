package com.vergilyn.examples.controller;

import com.google.common.base.Strings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author VergiLyn
 * @date 2019-12-12
 */
@RestController
@RequestMapping("/provider")
@Slf4j
public class ProviderController {

    @RequestMapping("/hello")
    public String hello(@RequestParam(defaultValue = "0") Long ms){
        long begin = System.currentTimeMillis();
        if (ms > 0){
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }

        String placeholder = Strings.repeat("0", 1_0000);

        return String.format("%s hello, exec: %s ms", placeholder, System.currentTimeMillis() - begin);
    }
}
