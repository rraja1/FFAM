package ffam.task.domain;

import ffam.agent.data.Agent;
import ffam.task.api.TaskRequest;
import ffam.task.api.TaskRequestBusinessErrorResponse;
import ffam.task.api.TaskRequestServerErrorResponse;
import ffam.task.data.TaskAllocationDetailRepository;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class AddTaskWhenAgentBusyUseCaseTest {
    private AddTaskUseCase addTaskUseCase;
    private TaskAllocationDetailRepository taskAllocationDetailRepository;
    private AddTaskWhenAgentBusyUseCase subject;

    @Before
    public void setUp(){
        addTaskUseCase = mock(AddTaskUseCase.class, RETURNS_SMART_NULLS);
        taskAllocationDetailRepository = mock(TaskAllocationDetailRepository.class, RETURNS_SMART_NULLS);
        subject = new AddTaskWhenAgentBusyUseCase(addTaskUseCase, taskAllocationDetailRepository);
    }

    //region addTask
    @Test
    public void test_addTask_returnUnProcessableEntity_whenTaskAllocationEmpty(){
        val taskRequest = new TaskRequest(TaskPriority.LOW, true, false, false);
        val agent = new Agent("agentId", "agentName", true, false, false);

        when(taskAllocationDetailRepository.findAll()).thenReturn(Collections.emptyList());

        val taskResponse = subject.addTask(Arrays.asList(agent), taskRequest);
        assertEquals(422, taskResponse.getStatusCode().value());
        assertEquals(new TaskRequestServerErrorResponse("V007", "Internal Server Error"), (TaskRequestServerErrorResponse)taskResponse.getBody());

        verify(taskAllocationDetailRepository, times(1)).findAll();
        verifyNoMoreInteractions(taskAllocationDetailRepository);

        verifyZeroInteractions(addTaskUseCase);
    }

    @Test
    public void test_addTask_returnUnProcessableEntity_whenTaskAllocationListPresentButEverythingIsHighPriority(){
        List<TaskAllocationDetail> taskAllocationDetailList = new ArrayList<TaskAllocationDetail>();
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId1", Arrays.asList("taskId1", "taskId2"), TaskPriority.HIGH, ZonedDateTime.now().minusDays(3)));
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId2", Arrays.asList("taskId3", "taskId4"), TaskPriority.LOW, ZonedDateTime.now().minusDays(3)));
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId3", Arrays.asList("taskId5", "taskId6", "taskId7"), TaskPriority.LOW, ZonedDateTime.now().minusDays(5)));

        val taskRequest = new TaskRequest(TaskPriority.LOW, true, false, false);
        val agent = new Agent("agentId1", "agentName", true, false, false);

        when(taskAllocationDetailRepository.findAll()).thenReturn(taskAllocationDetailList);

        val taskResponse = subject.addTask(Arrays.asList(agent), taskRequest);
        assertEquals(422, taskResponse.getStatusCode().value());
        assertEquals(new TaskRequestBusinessErrorResponse("V006", "All Agents are busy at this time"), (TaskRequestBusinessErrorResponse)taskResponse.getBody());

        verify(taskAllocationDetailRepository, times(1)).findAll();
        verifyNoMoreInteractions(taskAllocationDetailRepository);

        verifyZeroInteractions(addTaskUseCase);
    }

    @Test
    public void test_addTask_returnUnProcessableEntity_whenTaskAllocationListPresentButEverythingAndLowPriorityButAgentInEligible(){
        List<TaskAllocationDetail> taskAllocationDetailList = new ArrayList<TaskAllocationDetail>();
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId1", Arrays.asList("taskId1", "taskId2"), TaskPriority.LOW, ZonedDateTime.now().minusDays(3)));
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId2", Arrays.asList("taskId3", "taskId4"), TaskPriority.LOW, ZonedDateTime.now().minusDays(3)));
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId3", Arrays.asList("taskId5", "taskId6", "taskId7"), TaskPriority.LOW, ZonedDateTime.now().minusDays(5)));

        val taskRequest = new TaskRequest(TaskPriority.LOW, true, false, false);
        val agent1 = new Agent("agentId4", "agentName4", true, false, false);
        val agent2 = new Agent("agentId5", "agentName5", true, false, false);

        when(taskAllocationDetailRepository.findAll()).thenReturn(taskAllocationDetailList);

        val taskResponse = subject.addTask(Arrays.asList(agent1, agent2), taskRequest);
        assertEquals(422, taskResponse.getStatusCode().value());
        assertEquals(new TaskRequestBusinessErrorResponse("V006", "All Agents are busy at this time"), (TaskRequestBusinessErrorResponse)taskResponse.getBody());

        verify(taskAllocationDetailRepository, times(1)).findAll();
        verifyNoMoreInteractions(taskAllocationDetailRepository);

        verifyZeroInteractions(addTaskUseCase);
    }

    @Test
    public void test_addTask_returnAddTaskResponse_whenTaskAllocationListPresentAndAgentEligible_OnlyOneOption(){
        List<TaskAllocationDetail> taskAllocationDetailList = new ArrayList<TaskAllocationDetail>();
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId1", Arrays.asList("taskId1", "taskId2"), TaskPriority.LOW, ZonedDateTime.now().minusDays(3)));
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId2", Arrays.asList("taskId3", "taskId4"), TaskPriority.LOW, ZonedDateTime.now().minusDays(3)));
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId3", Arrays.asList("taskId5", "taskId6", "taskId7"), TaskPriority.LOW, ZonedDateTime.now().minusDays(5)));

        val taskRequest = new TaskRequest(TaskPriority.LOW, true, false, false);
        val agent1 = new Agent("agentId1", "agentName1", true, false, false);
        val agent2 = new Agent("agentId5", "agentName5", true, false, false);

        when(taskAllocationDetailRepository.findAll()).thenReturn(taskAllocationDetailList);
        when(addTaskUseCase.addTask("agentId1", taskRequest)).thenReturn(ResponseEntity.ok().build());

        val taskResponse = subject.addTask(Arrays.asList(agent1, agent2), taskRequest);
        assertEquals(200, taskResponse.getStatusCode().value());

        verify(taskAllocationDetailRepository, times(1)).findAll();
        verifyNoMoreInteractions(taskAllocationDetailRepository);

        verify(addTaskUseCase, times(1)).addTask("agentId1", taskRequest);
        verifyNoMoreInteractions(addTaskUseCase);
    }

    @Test
    public void test_addTask_returnAddTaskResponse_whenTaskAllocationListPresentAndAgentEligible_OnlyMultipleOptions(){
        List<TaskAllocationDetail> taskAllocationDetailList = new ArrayList<TaskAllocationDetail>();
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId1", Arrays.asList("taskId1", "taskId2"), TaskPriority.LOW, ZonedDateTime.now().minusDays(3)));
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId2", Arrays.asList("taskId3", "taskId4"), TaskPriority.LOW, ZonedDateTime.now().minusDays(3)));
        taskAllocationDetailList.add(new TaskAllocationDetail("agentId3", Arrays.asList("taskId5", "taskId6", "taskId7"), TaskPriority.LOW, ZonedDateTime.now().minusDays(5)));

        val taskRequest = new TaskRequest(TaskPriority.LOW, true, false, false);
        val agent1 = new Agent("agentId1", "agentName1", true, false, false);
        val agent2 = new Agent("agentId3", "agentName3", true, false, false);

        when(taskAllocationDetailRepository.findAll()).thenReturn(taskAllocationDetailList);
        when(addTaskUseCase.addTask("agentId1", taskRequest)).thenReturn(ResponseEntity.ok().build());

        val taskResponse = subject.addTask(Arrays.asList(agent1, agent2), taskRequest);
        assertEquals(200, taskResponse.getStatusCode().value());

        verify(taskAllocationDetailRepository, times(1)).findAll();
        verifyNoMoreInteractions(taskAllocationDetailRepository);

        verify(addTaskUseCase, times(1)).addTask("agentId1", taskRequest);
        verifyNoMoreInteractions(addTaskUseCase);

    }
    //endregion
}