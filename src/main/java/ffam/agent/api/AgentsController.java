package ffam.agent.api;

import ffam.task.api.TaskRequestBusinessErrorResponse;
import ffam.task.api.TaskRequestServerErrorResponse;
import ffam.task.api.TaskRequestValidationErrorResponse;
import ffam.task.api.TaskResponse;
import ffam.task.data.TaskAllocationDetailRepository;
import ffam.task.data.TaskRepository;
import ffam.task.domain.Task;
import ffam.task.domain.TaskAllocationDetail;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
public class AgentsController {

    private final TaskAllocationDetailRepository taskAllocationDetailRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public AgentsController(TaskAllocationDetailRepository taskAllocationDetailRepository, TaskRepository taskRepository) {
        this.taskAllocationDetailRepository = taskAllocationDetailRepository;
        this.taskRepository = taskRepository;
    }

    @ApiOperation(
            value = "Get List of Agents and Tasks ",
            code = 200,
            response = TaskResponse.class,
            responseContainer = "List",
            notes = "200 list has been successfully returned",
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
            method = RequestMethod.GET,
            value = "/agent",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAgentList(
            @RequestParam(required = false) String agentId) {

        if (StringUtils.isNotEmpty(agentId)) {
            //If Agent Id is Present
            //Send the List with Single Agent Details
            val taskAllocationDetailOptional = taskAllocationDetailRepository.findByAgentId(agentId);
            if (taskAllocationDetailOptional.isEmpty()) {
                return ResponseEntity.unprocessableEntity().body(new TaskRequestBusinessErrorResponse("V201", "No Tasks Available for this agent"));
            }
            if (taskAllocationDetailOptional.get().getTaskId() == null
                    || taskAllocationDetailOptional.get().getTaskId().isEmpty()) {
                return ResponseEntity.unprocessableEntity().body(new TaskRequestBusinessErrorResponse("V201", "No Tasks Available for this agent"));
            }
            val taskAllocationDetail = taskAllocationDetailOptional.get();
            List<Task> taskList = new ArrayList<>();
            for (String taskId : taskAllocationDetail.getTaskId()) {
                val task = taskRepository.findByTaskId(taskId);
                task.ifPresent(taskList::add);
            }
            return ResponseEntity.ok().body(Arrays.asList(new AgentsResponse(agentId, taskList)));
        }

        //If not Present
        //Send the List with all Agent Details
        val taskAllocationDetailList = taskAllocationDetailRepository.findAll();
        if (taskAllocationDetailList == null || taskAllocationDetailList.isEmpty()) {
            return ResponseEntity.unprocessableEntity().body(new TaskRequestBusinessErrorResponse("V202", "No Tasks Available for any agent"));
        }

        List<AgentsResponse> agentsResponseList = new ArrayList<>();
        for (TaskAllocationDetail taskAllocationDetail : taskAllocationDetailList) {
            if (taskAllocationDetail.getTaskId() != null
                    && !taskAllocationDetail.getTaskId().isEmpty()) {
                List<Task> taskList = new ArrayList<>();
                for (String taskId : taskAllocationDetail.getTaskId()) {
                    val task = taskRepository.findByTaskId(taskId);
                    task.ifPresent(taskList::add);
                }
                agentsResponseList.add(new AgentsResponse(taskAllocationDetail.getAgentId(),
                        taskList));
            }
        }
        return ResponseEntity.ok().body(agentsResponseList);
    }
}
