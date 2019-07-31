package ffam.task.api;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TaskRequestValidator {

    public Optional<TaskRequestValidationErrorResponse> isValid(TaskRequest taskRequest) {
        if (taskRequest == null) {
            return Optional.of(new TaskRequestValidationErrorResponse("V001", "Invalid Request"));
        } else if (taskRequest.getTaskPriority() == null) {
            return Optional.of(new TaskRequestValidationErrorResponse("V002", "Task Priority is Mandatory"));
        } else if (!(taskRequest.isSkill1() || taskRequest.isSkill2() || taskRequest.isSkill3())) {
            return Optional.of(new TaskRequestValidationErrorResponse("V003", "At least One Skill is required"));
        }
        return Optional.empty();
    }
}
