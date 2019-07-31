package ffam.task.api;

import lombok.Value;

@Value
public class TaskRequestServerErrorResponse {
    String errorCode;
    String errorResponse;
}
