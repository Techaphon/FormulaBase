package com.formulabase.config;
@org.springframework.context.annotation.Configuration
public class PrimaryAdminConfig {
 @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
 public void protect(org.springframework.boot.context.event.ApplicationReadyEvent event) {
  event.getApplicationContext().getBean(com.formulabase.service.UserService.class).ensurePrimaryAdministrator();
 }
}
