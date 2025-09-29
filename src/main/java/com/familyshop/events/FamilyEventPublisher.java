package com.familyshop.events;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FamilyEventPublisher {
    private final SimpMessagingTemplate template;

    public void send(Long familyId, EventType type, Object payload) {
        FamilyEvent event = new FamilyEvent(type, familyId, payload);
        // фронт подписан на /topic/family/{id}
        template.convertAndSend("/topic/family/" + familyId, event);
    }
}