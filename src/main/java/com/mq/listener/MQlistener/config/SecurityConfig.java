//package com.mq.listener.MQlistener.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@EnableWebSecurity
//public class SecurityConfig {
//
//  @Bean
//  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    return http
//      .requiresChannel(channel -> 
//          channel.anyRequest().requiresSecure())
//      .authorizeHttpRequests(authorize ->
//          authorize.anyRequest().permitAll())
//      .build();
//    }
//
//}