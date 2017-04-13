package com.daeliin.components.core.event;

import com.daeliin.components.core.resource.service.ResourceService;
import org.springframework.stereotype.Service;

@Service
public final class EventLogService extends ResourceService<EventLog, EventLogRepository> {
}
