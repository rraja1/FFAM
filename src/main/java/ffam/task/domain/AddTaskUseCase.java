package ffam.task.domain;

import ffam.task.api.TaskRequest;
import ffam.task.api.TaskRequestBusinessErrorResponse;
import ffam.task.api.TaskResponse;
import ffam.task.data.TaskAllocationDetailRepository;
import ffam.task.data.TaskRepository;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AddTaskUseCase {
    private final TaskRepository taskRepository;
    private final TaskAllocationDetailRepository taskAllocationDetailRepository;

    @Autowired
    public AddTaskUseCase(TaskRepository taskRepository, TaskAllocationDetailRepository taskAllocationDetailRepository) {
        this.taskRepository = taskRepository;
        this.taskAllocationDetailRepository = taskAllocationDetailRepository;
    }

    public ResponseEntity<?> addTask(String agentId, TaskRequest taskRequest) {
        val taskId = UUID.randomUUID().toString();
        // Add a record to the task allocation table
        if (!taskAllocationDetailRepository.createOrUpdate(agentId,
                taskId,
                taskRequest.getTaskPriority())) {
            return ResponseEntity.unprocessableEntity().body(new TaskRequestBusinessErrorResponse("V005", "Can't Create a Task At this time"));
        }
        // Add a record to the task table
        if (!taskRepository.create(agentId,
                taskId,
                taskRequest.getTaskPriority(),
                TaskStatus.IN_PROGRESS,
                taskRequest.isSkill1(),
                taskRequest.isSkill2(),
                taskRequest.isSkill3())) {
            return ResponseEntity.unprocessableEntity().body(new TaskRequestBusinessErrorResponse("V005", "Can't Create a Task At this time"));
        }

        // Return the response as OK Created
        return ResponseEntity.ok().body(new TaskResponse(taskId,
                agentId,
                TaskStatus.IN_PROGRESS));
    }
}
