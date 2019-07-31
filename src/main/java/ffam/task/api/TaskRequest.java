package ffam.task.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import ffam.task.domain.TaskPriority;
import lombok.Value;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
public class TaskRequest {
    private TaskPriority taskPriority;
    private boolean skill1;
    private boolean skill2;
    private boolean skill3;
}
