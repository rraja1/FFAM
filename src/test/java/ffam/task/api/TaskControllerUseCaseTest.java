package ffam.task.api;

import ffam.agent.data.Agent;
import ffam.agent.data.AgentRepository;
import ffam.task.data.TaskAllocationDetailRepository;
import ffam.task.data.TaskRepository;
import ffam.task.domain.*;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class TaskControllerUseCaseTest {
    private AddTaskUseCase addTaskUseCase;
    private AddTaskWhenAgentBusyUseCase addTaskWhenAgentBusyUseCase;
    private AgentRepository agentRepository;
    private TaskRepository taskRepository;
    private TaskAllocationDetailUseCase taskAllocationDetailUseCase;

    private TaskControllerUseCase subject;

    @Before
    public void setUp() {
        addTaskUseCase = mock(AddTaskUseCase.class, RETURNS_SMART_NULLS);
        addTaskWhenAgentBusyUseCase = mock(AddTaskWhenAgentBusyUseCase.class, RETURNS_SMART_NULLS);
        agentRepository = mock(AgentRepository.class, RETURNS_SMART_NULLS);
        taskRepository = mock(TaskRepository.class, RETURNS_SMART_NULLS);
        taskAllocationDetailUseCase = mock(TaskAllocationDetailUseCase.class, RETURNS_SMART_NULLS);
        subject = new TaskControllerUseCase(agentRepository,
                taskRepository,
                addTaskUseCase,
                addTaskWhenAgentBusyUseCase,
                taskAllocationDetailUseCase);
    }

    //region createTask
    @Test
    public void test_createTask_returnsUnProcessableEntity_whenNoEligibleAgents(){
        val taskRequest = new TaskRequest(TaskPriority.HIGH, false, false, false);
        when(agentRepository.findBySkillSet(false, false, false)).thenReturn(Collections.emptyList());

        val taskResponse = subject.createTask(taskRequest);
        assertEquals(422, taskResponse.getStatusCode().value());
        assertEquals(new TaskRequestBusinessErrorResponse("V004", "No Agents Available with the SkillSet"), (TaskRequestBusinessErrorResponse)taskResponse.getBody());

        verify(agentRepository, times(1)).findBySkillSet(taskRequest.isSkill1(), taskRequest.isSkill2(), taskRequest.isSkill3());
        verifyNoMoreInteractions(agentRepository);

        verifyZeroInteractions(taskRepository);
        verifyZeroInteractions(addTaskUseCase);
        verifyZeroInteractions(addTaskWhenAgentBusyUseCase);
    }

    @Test
    public void test_createTask_returnsAddTaskUseCaseResponse_whenEligibleAgentsWithNoTaskList(){
        val taskRequest = new TaskRequest(TaskPriority.HIGH, true, false, false);
        val agent = new Agent("agentId", "agentName", true, false, false);

        when(agentRepository.findBySkillSet(true, false, false)).thenReturn(Arrays.asList(agent));
        when(taskRepository.findByAgentIdAndStatus("agentId", TaskStatus.IN_PROGRESS)).thenReturn(Collections.EMPTY_LIST);
        when(addTaskUseCase.addTask("agentId", taskRequest)).thenReturn(ResponseEntity.ok().build());

        val taskResponse = subject.createTask(taskRequest);
        assertEquals(200, taskResponse.getStatusCode().value());

        verify(agentRepository, times(1)).findBySkillSet(taskRequest.isSkill1(), taskRequest.isSkill2(), taskRequest.isSkill3());
        verifyNoMoreInteractions(agentRepository);

        verify(taskRepository, times(1)).findByAgentIdAndStatus("agentId", TaskStatus.IN_PROGRESS);
        verifyNoMoreInteractions(taskRepository);

        verify(addTaskUseCase, times(1)).addTask("agentId", taskRequest);
        verifyNoMoreInteractions(addTaskUseCase);

        verifyZeroInteractions(addTaskWhenAgentBusyUseCase);
    }

    @Test
    public void test_createTask_returnsUnProcessableEntity_whenEligibleAgentsHasTaskListButRequestIsLowPriority(){
        val taskRequest = new TaskRequest(TaskPriority.LOW, true, false, false);
        val agent = new Agent("agentId", "agentName", true, false, false);
        val existingTask = new Task("taskId", TaskPriority.LOW, true, false, false, TaskStatus.IN_PROGRESS, "agentId");

        when(agentRepository.findBySkillSet(true, false, false)).thenReturn(Arrays.asList(agent));
        when(taskRepository.findByAgentIdAndStatus("agentId", TaskStatus.IN_PROGRESS)).thenReturn(Arrays.asList(existingTask));

        val taskResponse = subject.createTask(taskRequest);
        assertEquals(422, taskResponse.getStatusCode().value());
        assertEquals(new TaskRequestBusinessErrorResponse("V006", "All Agents are busy at this time"), (TaskRequestBusinessErrorResponse)taskResponse.getBody());

        verify(agentRepository, times(1)).findBySkillSet(taskRequest.isSkill1(), taskRequest.isSkill2(), taskRequest.isSkill3());
        verifyNoMoreInteractions(agentRepository);

        verify(taskRepository, times(1)).findByAgentIdAndStatus("agentId", TaskStatus.IN_PROGRESS);
        verifyNoMoreInteractions(taskRepository);

        verifyZeroInteractions(addTaskUseCase);
        verifyZeroInteractions(addTaskWhenAgentBusyUseCase);
    }

    @Test
    public void test_createTask_returnsAddTaskWhenAgentBusyUseCase_whenEligibleAgentsHasTaskListAndRequestIsHighPriority(){
        val taskRequest = new TaskRequest(TaskPriority.HIGH, true, false, false);
        val agent = new Agent("agentId", "agentName", true, false, false);
        val existingTask = new Task("taskId", TaskPriority.LOW, true, false, false, TaskStatus.IN_PROGRESS, "agentId");

        when(agentRepository.findBySkillSet(true, false, false)).thenReturn(Arrays.asList(agent));
        when(taskRepository.findByAgentIdAndStatus("agentId", TaskStatus.IN_PROGRESS)).thenReturn(Arrays.asList(existingTask));
        when(addTaskWhenAgentBusyUseCase.addTask(Arrays.asList(agent), taskRequest)).thenReturn(ResponseEntity.ok().build());

        val taskResponse = subject.createTask(taskRequest);
        assertEquals(200, taskResponse.getStatusCode().value());

        verify(agentRepository, times(1)).findBySkillSet(taskRequest.isSkill1(), taskRequest.isSkill2(), taskRequest.isSkill3());
        verifyNoMoreInteractions(agentRepository);

        verify(taskRepository, times(1)).findByAgentIdAndStatus("agentId", TaskStatus.IN_PROGRESS);
        verifyNoMoreInteractions(taskRepository);

        verify(addTaskWhenAgentBusyUseCase, times(1)).addTask(Arrays.asList(agent), taskRequest);
        verifyNoMoreInteractions(addTaskWhenAgentBusyUseCase);

        verifyZeroInteractions(addTaskUseCase);
    }
    //endregion

    //region finishTask
    @Test
    public void test_finishTask_returnsUnProcessableEntity_whenTaskNotPresent(){
        when(taskRepository.findByTaskId("taskId")).thenReturn(Optional.empty());
        val taskResponse = subject.finishTask("taskId");
        assertEquals(422, taskResponse.getStatusCode().value());
        assertEquals(new TaskRequestBusinessErrorResponse("V102", "No such task present"), (TaskRequestBusinessErrorResponse)taskResponse.getBody());

        verify(taskRepository, times(1)).findByTaskId("taskId");
        verifyNoMoreInteractions(taskRepository);

        verifyZeroInteractions(agentRepository);
        verifyZeroInteractions(addTaskUseCase);
        verifyZeroInteractions(addTaskWhenAgentBusyUseCase);
    }

    @Test
    public void test_finishTask_returnsUnProcessableEntity_whenTaskAllocationDetailDeleteFails(){
        val task = new Task("taskId", TaskPriority.HIGH, true, false, false, TaskStatus.IN_PROGRESS, "agentId");
        when(taskRepository.findByTaskId("taskId")).thenReturn(Optional.of(task));
        when(taskAllocationDetailUseCase.deleteTask(task.getAgentId(),"taskId", task.getTaskPriority())).thenReturn(false);

        val taskResponse = subject.finishTask("taskId");
        assertEquals(422, taskResponse.getStatusCode().value());
        assertEquals(new TaskRequestBusinessErrorResponse("V101", "Unable to delete the task at this time"), (TaskRequestBusinessErrorResponse)taskResponse.getBody());

        verify(taskRepository, times(1)).findByTaskId("taskId");
        verifyNoMoreInteractions(taskRepository);
        verify(taskAllocationDetailUseCase, times(1)).deleteTask(task.getAgentId(),"taskId", task.getTaskPriority());
        verifyNoMoreInteractions(taskAllocationDetailUseCase);

        verifyZeroInteractions(agentRepository);
        verifyZeroInteractions(addTaskUseCase);
        verifyZeroInteractions(addTaskWhenAgentBusyUseCase);
    }

    @Test
    public void test_finishTask_returnsUnProcessableEntity_whenTaskUpdateFails(){
        val task = new Task("taskId", TaskPriority.HIGH, true, false, false, TaskStatus.IN_PROGRESS, "agentId");
        when(taskRepository.findByTaskId("taskId")).thenReturn(Optional.of(task));
        when(taskAllocationDetailUseCase.deleteTask(task.getAgentId(),"taskId", task.getTaskPriority())).thenReturn(true);
        when(taskRepository.updateTaskStatus("taskId", TaskStatus.COMPLETE)).thenReturn(false);

        val taskResponse = subject.finishTask("taskId");
        assertEquals(422, taskResponse.getStatusCode().value());
        assertEquals(new TaskRequestBusinessErrorResponse("V101", "Unable to delete the task at this time"), (TaskRequestBusinessErrorResponse)taskResponse.getBody());

        verify(taskRepository, times(1)).findByTaskId("taskId");
        verify(taskRepository, times(1)).updateTaskStatus("taskId", TaskStatus.COMPLETE);
        verifyNoMoreInteractions(taskRepository);
        verify(taskAllocationDetailUseCase, times(1)).deleteTask(task.getAgentId(),"taskId", task.getTaskPriority());
        verifyNoMoreInteractions(taskAllocationDetailUseCase);

        verifyZeroInteractions(agentRepository);
        verifyZeroInteractions(addTaskUseCase);
        verifyZeroInteractions(addTaskWhenAgentBusyUseCase);
    }

    @Test
    public void test_finishTask_returnsOk_whenTaskUpdateSuccessAndTaskAllocationDetailSuccess(){
        val task = new Task("taskId", TaskPriority.HIGH, true, false, false, TaskStatus.IN_PROGRESS, "agentId");
        when(taskRepository.findByTaskId("taskId")).thenReturn(Optional.of(task));
        when(taskAllocationDetailUseCase.deleteTask(task.getAgentId(),"taskId", task.getTaskPriority())).thenReturn(true);
        when(taskRepository.updateTaskStatus("taskId", TaskStatus.COMPLETE)).thenReturn(true);

        val taskResponse = subject.finishTask("taskId");
        assertEquals(200, taskResponse.getStatusCode().value());
        assertEquals(new TaskResponse(task.getTaskId(), task.getAgentId(), TaskStatus.COMPLETE), (TaskResponse) taskResponse.getBody());

        verify(taskRepository, times(1)).findByTaskId("taskId");
        verify(taskRepository, times(1)).updateTaskStatus("taskId", TaskStatus.COMPLETE);
        verifyNoMoreInteractions(taskRepository);
        verify(taskAllocationDetailUseCase, times(1)).deleteTask(task.getAgentId(),"taskId", task.getTaskPriority());
        verifyNoMoreInteractions(taskAllocationDetailUseCase);

        verifyZeroInteractions(agentRepository);
        verifyZeroInteractions(addTaskUseCase);
        verifyZeroInteractions(addTaskWhenAgentBusyUseCase);
    }
    //endregion
}