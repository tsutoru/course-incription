package school.hei.app.endpoint.event;

import static java.lang.System.getenv;

import lombok.Getter;
import school.hei.app.PojaGenerated;

@PojaGenerated
public enum EventStack {
  EVENT_STACK_1(getenv("AWS_EVENT_STACK_1_SQS_QUEUE_URL"));

  @Getter private final String sqsQueueUrl;

  EventStack(String sqsQueueUrl) {
    this.sqsQueueUrl = sqsQueueUrl;
  }
}
