package ffam.task.domain;

import ffam.agent.data.Agent;
import ffam.task.api.TaskRequest;
import ffam.task.api.TaskRequestBusinessErrorResponse;
import ffam.task.api.TaskRequestServerErrorResponse;
import ffam.task.data.TaskAllocationDetailRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AddTaskWhenAgentBusyUseCase {

    private final AddTaskUseCase addTaskUseCase;
    private final TaskAllocationDetailRepository taskAllocationDetailRepository;

    @Autowired
    public AddTaskWhenAgentBusyUseCase(AddTaskUseCase addTaskUseCase, TaskAllocationDetailRepository taskAllocationDetailRepository) {
        this.addTaskUseCase = addTaskUseCase;
        this.taskAllocationDetailRepository = taskAllocationDetailRepository;
    }

    public ResponseEntity<?> addTask(List<Agent> eligibleAgents, TaskRequest taskRequest) {
        // All Agents are currently working here
        // Get the task allocation detail list by Date Created DSC
        val taskAllocationList = taskAllocationDetailRepository.findAll();
        if (taskAllocationList == null || taskAllocationList.isEmpty()) {
            // This can't happen. Something is really screwed up if that happened
            log.error("Agents didn't have tasks allocated on TASK_ALLOCATION table but has a value in TASK table. Sending an internal error. Need to Fix this logic/scenario");
            return ResponseEntity.unprocessableEntity().body(new TaskRequestServerErrorResponse("V007", "Internal Server Error"));
        }

        for (TaskAllocationDetail taskAllocationDetail : taskAllocationList) {
            // If the task is of Higher Priority
            // Check an Agent with Required Skills and Working on a Low Priority Task
            // If not Found, Return a response that no agent is available to pick up the task
            // If Found and Single Agent, Assign that agent to the task. Older Task is Left in progress state as is
            // If Found and Multiple Agents, Assign that agent with newer Task Allocation Created Time
            if (taskAllocationDetail.getTaskPriority() == TaskPriority.LOW
                    && eligibleAgents.stream().anyMatch(e -> StringUtils.equalsIgnoreCase(e.getAgentId(), taskAllocationDetail.getAgentId()))) {
                return addTaskUseCase.addTask(taskAllocationDetail.getAgentId(), taskRequest);
            }
        }

        // No Agent with required skills is available to take the lower priority task
        return ResponseEntity.unprocessableEntity().body(new TaskRequestBusinessErrorResponse("V006", "All Agents are busy at this time"));
    }
}
