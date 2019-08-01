package ffam.task.domain;

import ffam.task.data.TaskAllocationDetailRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static ffam.task.domain.TaskPriority.LOW;

@Slf4j
@Component
public class TaskAllocationDetailUseCase {
    private final TaskAllocationDetailRepository taskAllocationDetailRepository;

    @Autowired
    public TaskAllocationDetailUseCase(TaskAllocationDetailRepository taskAllocationDetailRepository) {
        this.taskAllocationDetailRepository = taskAllocationDetailRepository;
    }

    public boolean createOrUpdate(String agentId,
                                  String taskId,
                                  TaskPriority taskPriority) {
        //Check id the row exists per agent
        val taskAllocationDetailOptional = taskAllocationDetailRepository.findByAgentId(agentId);
        if (!taskAllocationDetailOptional.isPresent()) {
            return taskAllocationDetailRepository.create(agentId, taskId, taskPriority);
        }

        List<String> taskIdList = taskAllocationDetailOptional.get().getTaskId();
        taskIdList.add(taskId);
        return taskAllocationDetailRepository.update(agentId, taskPriority, taskIdList, Optional.empty());
    }

    public boolean deleteTask(String agentId,
                              String taskId,
                              TaskPriority taskPriority) {
        //Check id the row exists per agent
        val taskAllocationDetailOptional = taskAllocationDetailRepository.findByAgentId(agentId);
        if (!taskAllocationDetailOptional.isPresent()) {
            //Not Present
            return false;
        }
        val taskAllocationDetail = taskAllocationDetailOptional.get();
        if (!taskAllocationDetail.getTaskId().contains(taskId)) {
            //Task Id is not present in the task allocation list
            log.error("Task Id : [{}] is present on Task and not on TaskAllocationDetail for agent : [{}]", taskId, agentId);
            return false;
        }
        //Update - Contains Task Id - Check if that's the only one or multiple
        List<String> taskIdList = taskAllocationDetailOptional.get().getTaskId();
        taskIdList.remove(taskId);

        if (taskIdList.isEmpty()) {
            // No more tasks left for the agent. Clean the record up
            return taskAllocationDetailRepository.delete(agentId);
        } else {
            // More tasks left for the agent. Keep the record here. But just remove single task
            // If removing a HIGH Priority task, overall priority now becomes LOW. Otherwise, it stays at previous
            // Preserve the original latest time that the agent started working on the previous task. Task might be complete/in progress ?
            return taskAllocationDetailRepository.update(agentId,
                    taskPriority == TaskPriority.HIGH ? LOW : taskAllocationDetail.getTaskPriority(),
                    taskIdList,
                    Optional.of(taskAllocationDetailOptional.get().getDateCreated()));
        }
    }
}
