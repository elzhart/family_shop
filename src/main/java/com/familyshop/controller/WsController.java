package com.familyshop.controller;

import com.familyshop.events.FamilyEvent;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WsController {

    @MessageMapping("/ping")              // клиент шлёт на /app/ping
    @SendTo("/topic/broadcast")           // все подписчики получат ответ
    public FamilyEvent ping(FamilyEvent in) {
        return in; // эхо
    }
}