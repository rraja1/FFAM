package ffam.task.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class TaskController {

    private final TaskRequestValidator taskRequestValidator;
    private final TaskControllerUseCase taskControllerUseCase;

    @Autowired
    public TaskController(TaskRequestValidator taskRequestValidator, TaskControllerUseCase taskControllerUseCase) {
        this.taskRequestValidator = taskRequestValidator;
        this.taskControllerUseCase = taskControllerUseCase;
    }

    @ApiOperation(
            value = "Add a Task which assigns an Agent",
            code = 200,
            response = TaskResponse.class,
            notes = "200 indicates that a task has been added successfully",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({
            // validation errors
            @ApiResponse(code = 400,
                    response = TaskRequestValidationErrorResponse.class,
                    message = "the given request is invalid"),
            // application errors
            @ApiResponse(code = 422,
                    response = TaskRequestBusinessErrorResponse.class,
                    message = "error trying to process the task"),
            // server errors
            @ApiResponse(code = 503,
                    response = TaskRequestServerErrorResponse.class,
                    message = "error communicating with the dependent services")
    })
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/task")
    public ResponseEntity<?> addTask(@RequestBody TaskRequest taskRequest) {

        val validationErrorOptional = taskRequestValidator.isValid(taskRequest);
        if (validationErrorOptional.isPresent()) {
            return ResponseEntity.badRequest().body(validationErrorOptional.get());
        }

        return taskControllerUseCase.createTask(taskRequest);
    }

    @ApiOperation(
            value = "Complete a Task",
            code = 200,
            response = TaskResponse.class,
            notes = "200 indicates that a task has been successfully finished",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses({
            // validation errors
            @ApiResponse(code = 400,
                    response = TaskRequestValidationErrorResponse.class,
                    message = "the given request is invalid"),
            // application errors
            @ApiResponse(code = 422,
                    response = TaskRequestBusinessErrorResponse.class,
                    message = "error trying to process the task"),
            // server errors
            @ApiResponse(code = 503,
                    response = TaskRequestServerErrorResponse.class,
                    message = "error communicating with the dependent services")
    })
    @RequestMapping(
            method = RequestMethod.PATCH,
            value = "/task",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> finishTask(@RequestParam String taskId) {

        if (StringUtils.isEmpty(taskId)) {
            return ResponseEntity.badRequest().body(new TaskRequestValidationErrorResponse("V103", "Given Request is Invalid"));
        }

        return taskControllerUseCase.finishTask(taskId);
    }
}
