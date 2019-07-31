package ffam.task.domain;

import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;

@Value
public class TaskAllocationDetail {
    private String agentId;
    private List<String> taskId;
    private TaskPriority taskPriority;
    private ZonedDateTime dateCreated;
}
