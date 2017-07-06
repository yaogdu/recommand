package com.demai.common;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 
 * @author lishiwei
 * 
 */
@Component
public class Properties {

  @Value("${alter}")
  public String alter;

  @PostConstruct
  public void init() {
    System.out.println(this.alter);
  }

  public void setAlter(String alter) {
    this.alter = alter;
  }

}
