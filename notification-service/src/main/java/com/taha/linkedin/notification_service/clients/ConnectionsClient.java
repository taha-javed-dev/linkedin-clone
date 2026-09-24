package com.taha.linkedin.notification_service.clients;


import com.taha.linkedin.notification_service.dto.PersonDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "connections-service", path = "/connections")
public interface ConnectionsClient {

    @GetMapping("/core/{userId}/first-degree")
    public List<PersonDto> getFirstConnections(Long userId);
}
