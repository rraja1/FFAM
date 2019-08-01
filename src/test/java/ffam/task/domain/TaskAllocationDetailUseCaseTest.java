package ffam.task.domain;

import ffam.task.data.TaskAllocationDetailRepository;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class TaskAllocationDetailUseCaseTest {
    private TaskAllocationDetailRepository taskAllocationDetailRepository;
    private TaskAllocationDetailUseCase subject;

    @Before
    public void setUp(){
        taskAllocationDetailRepository = mock(TaskAllocationDetailRepository.class, RETURNS_SMART_NULLS);
        subject = new TaskAllocationDetailUseCase(taskAllocationDetailRepository);
    }

    //region createOrUpdate
    @Test
    public void test_createOrUpdate_callsCreate_whenNoPreviousTasksForAgentPresent(){
        when(taskAllocationDetailRepository.findByAgentId("agentId1")).thenReturn(Optional.empty());
        when(taskAllocationDetailRepository.create("agentId1", "taskId1", TaskPriority.HIGH)).thenReturn(true);
        assertTrue(subject.createOrUpdate("agentId1", "taskId1", TaskPriority.HIGH));

        verify(taskAllocationDetailRepository, times(1)).findByAgentId("agentId1");
        verify(taskAllocationDetailRepository, times(1)).create("agentId1", "taskId1", TaskPriority.HIGH);
        verifyNoMoreInteractions(taskAllocationDetailRepository);

    }

    @Test
    public void test_createOrUpdate_callsUpdate_whenPreviousTasksForAgentPresent(){
        List<String> taskIdList = new ArrayList<>();
        taskIdList.add("taskId1");
        taskIdList.add("taskId2");

        val taskAllocationDetail = new TaskAllocationDetail("agentId1", taskIdList, TaskPriority.LOW, ZonedDateTime.now().minusDays(3));
        when(taskAllocationDetailRepository.findByAgentId("agentId1")).thenReturn(Optional.of(taskAllocationDetail));
        when(taskAllocationDetailRepository.update("agentId1", TaskPriority.HIGH, Arrays.asList("taskId1", "taskId2", "taskId3"), Optional.empty())).thenReturn(true);
        assertTrue(subject.createOrUpdate("agentId1", "taskId3", TaskPriority.HIGH));

        verify(taskAllocationDetailRepository, times(1)).findByAgentId("agentId1");
        verify(taskAllocationDetailRepository, times(1)).update("agentId1", TaskPriority.HIGH, Arrays.asList("taskId1", "taskId2", "taskId3"), Optional.empty());
        verifyNoMoreInteractions(taskAllocationDetailRepository);
    }
    //endregion

    //region deleteTask
    @Test
    public void test_deleteTask_returnsFalse_whenNoPreviousTasksForAgentPresent(){
        when(taskAllocationDetailRepository.findByAgentId("agentId1")).thenReturn(Optional.empty());
        assertFalse(subject.deleteTask("agentId1", "taskId3", TaskPriority.HIGH));

        verify(taskAllocationDetailRepository, times(1)).findByAgentId("agentId1");
        verifyNoMoreInteractions(taskAllocationDetailRepository);
    }

    @Test
    public void test_deleteTask_returnsFalse_whenPreviousTasksForAgentPresentButTaskIdDoesNotExist(){
        List<String> taskIdList = new ArrayList<>();
        taskIdList.add("taskId1");
        taskIdList.add("taskId2");

        val taskAllocationDetail = new TaskAllocationDetail("agentId1", taskIdList, TaskPriority.LOW, ZonedDateTime.now().minusDays(3));
        when(taskAllocationDetailRepository.findByAgentId("agentId1")).thenReturn(Optional.of(taskAllocationDetail));
        assertFalse(subject.deleteTask("agentId1", "taskId3", TaskPriority.HIGH));

        verify(taskAllocationDetailRepository, times(1)).findByAgentId("agentId1");
        verifyNoMoreInteractions(taskAllocationDetailRepository);
    }

    @Test
    public void test_deleteTask_callsDelete_whenPreviousTasksAndTaskIdPresentAndIsOnlyTask(){
        List<String> taskIdList = new ArrayList<>();
        taskIdList.add("taskId1");

        val taskAllocationDetail = new TaskAllocationDetail("agentId1", taskIdList, TaskPriority.LOW, ZonedDateTime.now().minusDays(3));
        when(taskAllocationDetailRepository.findByAgentId("agentId1")).thenReturn(Optional.of(taskAllocationDetail));
        when(taskAllocationDetailRepository.delete("agentId1")).thenReturn(true);
        assertTrue(subject.deleteTask("agentId1", "taskId1", TaskPriority.HIGH));

        verify(taskAllocationDetailRepository, times(1)).findByAgentId("agentId1");
        verify(taskAllocationDetailRepository, times(1)).delete("agentId1");
        verifyNoMoreInteractions(taskAllocationDetailRepository);
    }

    @Test
    public void test_deleteTask_callsUpdate_whenPreviousTasksAndTaskIdPresentAndMultipleTasks(){
        List<String> taskIdList = new ArrayList<>();
        taskIdList.add("taskId1");
        taskIdList.add("taskId2");

        val taskAllocationDetail = new TaskAllocationDetail("agentId1", taskIdList, TaskPriority.LOW, ZonedDateTime.now().minusDays(3));
        when(taskAllocationDetailRepository.findByAgentId("agentId1")).thenReturn(Optional.of(taskAllocationDetail));
        when(taskAllocationDetailRepository.update("agentId1", TaskPriority.LOW, Arrays.asList("taskId2"), Optional.of(taskAllocationDetail.getDateCreated()))).thenReturn(true);
        assertTrue(subject.deleteTask("agentId1", "taskId1", TaskPriority.HIGH));

        verify(taskAllocationDetailRepository, times(1)).findByAgentId("agentId1");
        verify(taskAllocationDetailRepository, times(1)).update("agentId1", TaskPriority.LOW, Arrays.asList("taskId2"), Optional.of(taskAllocationDetail.getDateCreated()));
        verifyNoMoreInteractions(taskAllocationDetailRepository);
    }

    @Test
    public void test_deleteTask_callsUpdate_whenPreviousTasksHIGHAndTaskIdPresentLOWAndMultipleTasks(){
        List<String> taskIdList = new ArrayList<>();
        taskIdList.add("taskId1");
        taskIdList.add("taskId2");

        val taskAllocationDetail = new TaskAllocationDetail("agentId1", taskIdList, TaskPriority.HIGH, ZonedDateTime.now().minusDays(3));
        when(taskAllocationDetailRepository.findByAgentId("agentId1")).thenReturn(Optional.of(taskAllocationDetail));
        when(taskAllocationDetailRepository.update("agentId1", TaskPriority.HIGH, Arrays.asList("taskId2"), Optional.of(taskAllocationDetail.getDateCreated()))).thenReturn(true);
        assertTrue(subject.deleteTask("agentId1", "taskId1", TaskPriority.LOW));

        verify(taskAllocationDetailRepository, times(1)).findByAgentId("agentId1");
        verify(taskAllocationDetailRepository, times(1)).update("agentId1", TaskPriority.HIGH, Arrays.asList("taskId2"), Optional.of(taskAllocationDetail.getDateCreated()));
        verifyNoMoreInteractions(taskAllocationDetailRepository);
    }

    @Test
    public void test_deleteTask_callsUpdate_whenPreviousTasksLOWAndTaskIdPresentLOWAndMultipleTasks(){
        List<String> taskIdList = new ArrayList<>();
        taskIdList.add("taskId1");
        taskIdList.add("taskId2");

        val taskAllocationDetail = new TaskAllocationDetail("agentId1", taskIdList, TaskPriority.LOW, ZonedDateTime.now().minusDays(3));
        when(taskAllocationDetailRepository.findByAgentId("agentId1")).thenReturn(Optional.of(taskAllocationDetail));
        when(taskAllocationDetailRepository.update("agentId1", TaskPriority.LOW, Arrays.asList("taskId2"), Optional.of(taskAllocationDetail.getDateCreated()))).thenReturn(true);
        assertTrue(subject.deleteTask("agentId1", "taskId1", TaskPriority.LOW));

        verify(taskAllocationDetailRepository, times(1)).findByAgentId("agentId1");
        verify(taskAllocationDetailRepository, times(1)).update("agentId1", TaskPriority.LOW, Arrays.asList("taskId2"), Optional.of(taskAllocationDetail.getDateCreated()));
        verifyNoMoreInteractions(taskAllocationDetailRepository);
    }

    //endregion
}