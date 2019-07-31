package ffam.task.api;

import lombok.Value;

@Value
public class TaskRequestValidationErrorResponse {
    String errorCode;
    String errorResponse;
}
