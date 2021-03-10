package com.vergilyn.examples.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author vergilyn
 * @date 2021-01-14
 */
@RestController
@RequestMapping("/cookie")
@Slf4j
public class CookieController {

	@GetMapping
	public void post(){

	}
}
