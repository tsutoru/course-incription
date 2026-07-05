package school.hei.app.endpoint.event.consumer.model;

import school.hei.app.PojaGenerated;
import school.hei.app.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
