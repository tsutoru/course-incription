package school.hei.app.model;

import java.time.Instant;

public record SubscriptionRecord(String id, Long userId, Long courseId, Instant subscribedAt) {}
