package com.datastax.demo.controller;

import com.datastax.demo.dao.DseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

/**
 * Home page
 *
 * <p>List all ticker meta data from table 'ticker_info'. This table has been loaded by producer at
 * startup with lines of the CSV.
 */
@Controller
public class HomeController {

  @Autowired private DseDao dseDao;

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute(
        "tickerInfoList",
        new ReactiveDataDriverContextVariable(dseDao.findStockInfosByExchange("NYSE"), 1));
    return "home";
  }
}
