package ffam.agent.api;

import ffam.task.domain.Task;
import lombok.Value;

import java.util.List;

@Value
public class AgentsResponse {
    String agentId;
    List<Task> taskList;
}
