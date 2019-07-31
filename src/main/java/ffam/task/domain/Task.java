package ffam.task.domain;

import lombok.Value;

@Value
public class Task {
    private String taskId;
    private TaskPriority taskPriority;
    private boolean skill1;
    private boolean skill2;
    private boolean skill3;
    private TaskStatus taskStatus;
    private String agentId;
}
