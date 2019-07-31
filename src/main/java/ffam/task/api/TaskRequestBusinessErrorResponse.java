package ffam.task.api;

import lombok.Value;

@Value
public class TaskRequestBusinessErrorResponse {
    String errorCode;
    String errorResponse;
}
