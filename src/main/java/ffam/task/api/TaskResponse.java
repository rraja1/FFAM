package ffam.task.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import ffam.task.domain.TaskStatus;
import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse {
    private String taskId;
    private String agentId;
    private TaskStatus taskStatus;
}
