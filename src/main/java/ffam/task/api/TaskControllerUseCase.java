package ffam.task.api;

import ffam.agent.data.Agent;
import ffam.agent.data.AgentRepository;
import ffam.task.data.TaskAllocationDetailRepository;
import ffam.task.data.TaskRepository;
import ffam.task.domain.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TaskControllerUseCase {

    private final AddTaskUseCase addTaskUseCase;
    private final AddTaskWhenAgentBusyUseCase addTaskWhenAgentBusyUseCase;
    private final AgentRepository agentRepository;
    private final TaskRepository taskRepository;
    private final TaskAllocationDetailUseCase taskAllocationDetailUseCase;

    @Autowired
    public TaskControllerUseCase(AgentRepository agentRepository, TaskRepository taskRepository, AddTaskUseCase addTaskUseCase, AddTaskWhenAgentBusyUseCase addTaskWhenAgentBusyUseCase, TaskAllocationDetailUseCase taskAllocationDetailUseCase) {
        this.agentRepository = agentRepository;
        this.taskRepository = taskRepository;
        this.addTaskUseCase = addTaskUseCase;
        this.addTaskWhenAgentBusyUseCase = addTaskWhenAgentBusyUseCase;
        this.taskAllocationDetailUseCase = taskAllocationDetailUseCase;
    }

    // An agent cannot be assigned a task if they’re already working on a task of equal or higher priority.
    // The system will always prefer an agent that is not assigned any task to an agent already assigned to a task.
    // If all agents are currently working on a lower priority task, the system will pick the agent that started working on his/her current task the most recently.
    // If no agent is able to take the task, the service should return an error.
    public ResponseEntity<?> createTask(TaskRequest taskRequest) {

        // The agent must possess all the skills required by the task. Look Up agents who can perform the task
        List<Agent> eligibleAgents = agentRepository.findBySkillSet(taskRequest.isSkill1(),
                taskRequest.isSkill2(), taskRequest.isSkill3());

        if (eligibleAgents == null || eligibleAgents.isEmpty()) {
            // No Agents with that Skill Set are Available
            return ResponseEntity.unprocessableEntity().body(new TaskRequestBusinessErrorResponse("V004", "No Agents Available with the SkillSet"));
        }

        for (Agent agent : eligibleAgents) {
            val taskList = taskRepository.findByAgentIdAndStatus(agent.getAgentId(), TaskStatus.IN_PROGRESS);
            if (taskList == null || taskList.isEmpty()) {
                // No tasks assigned to the agent
                // Agent is eligible to pick up the job
                // Assign Agent to the Job
                return addTaskUseCase.addTask(agent.getAgentId(), taskRequest);
            }
        }

        // Done with all the agents - No agent's Task List is not Empty
        if (taskRequest.getTaskPriority() == TaskPriority.LOW) {
            // If the task is a low priority task and all agents are busy
            // Send an Error Response that no agent would be able to pick up the Job
            return ResponseEntity.unprocessableEntity().body(new TaskRequestBusinessErrorResponse("V006", "All Agents are busy at this time"));
        }

        // All Agents are currently working here
        // Get the task allocation detail list by Date Created DSC
        return addTaskWhenAgentBusyUseCase.addTask(eligibleAgents, taskRequest);
    }

    public ResponseEntity<?> finishTask(String taskId) {
        val taskOptional = taskRepository.findByTaskId(taskId);

        if (!taskOptional.isPresent()) {
            return ResponseEntity.unprocessableEntity().body(new TaskRequestBusinessErrorResponse("V102", "No such task present"));
        }

        // Update Task Allocation Table
        if (!taskAllocationDetailUseCase.deleteTask(taskOptional.get().getAgentId(),
                taskId, taskOptional.get().getTaskPriority())) {
            return ResponseEntity.unprocessableEntity().body(new TaskRequestBusinessErrorResponse("V101", "Unable to delete the task at this time"));
        }

        // Mark the Status of the Task as Complete on the task table
        if (!taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETE)) {
            return ResponseEntity.unprocessableEntity().body(new TaskRequestBusinessErrorResponse("V101", "Unable to delete the task at this time"));
        }
        return ResponseEntity.ok().body(new TaskResponse(taskId,
                taskOptional.get().getAgentId(),
                TaskStatus.COMPLETE));
    }
}
