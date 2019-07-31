package ffam.task.domain;

import ffam.general.UuidGenerator;
import ffam.task.api.TaskRequest;
import ffam.task.api.TaskRequestBusinessErrorResponse;
import ffam.task.api.TaskResponse;
import ffam.task.data.TaskAllocationDetailRepository;
import ffam.task.data.TaskRepository;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class AddTaskUseCaseTest {
    private TaskRepository taskRepository;
    private TaskAllocationDetailRepository taskAllocationDetailRepository;
    private UuidGenerator uuidGenerator;
    private AddTaskUseCase subject;

    private UUID guid = UUID.randomUUID();

    @Before
    public void setUp() {
        taskRepository = mock(TaskRepository.class, RETURNS_SMART_NULLS);
        taskAllocationDetailRepository = mock(TaskAllocationDetailRepository.class, RETURNS_SMART_NULLS);
        uuidGenerator = mock(UuidGenerator.class, RETURNS_SMART_NULLS);
        subject = new AddTaskUseCase(taskRepository,
                taskAllocationDetailRepository, uuidGenerator);
        when(uuidGenerator.randomUUID()).thenReturn(guid);
    }

    //region addTask
    @Test
    public void test_addTask_returnsUnProcessableEntity_whenTaskAllocationDetailRepoCreateOrUpdateFails(){
        val taskRequest = new TaskRequest(TaskPriority.HIGH, true, false, false);
        val agentId = "agentId";

        when(taskAllocationDetailRepository.createOrUpdate(agentId,
                guid.toString(), taskRequest.getTaskPriority())).thenReturn(false);

        val taskResponse = subject.addTask(agentId, taskRequest);
        assertEquals(422, taskResponse.getStatusCode().value());
        assertEquals(new TaskRequestBusinessErrorResponse("V005", "Can't Create a Task At this time"), (TaskRequestBusinessErrorResponse)taskResponse.getBody());

        verify(taskAllocationDetailRepository, times(1)).createOrUpdate(agentId,
                guid.toString(), taskRequest.getTaskPriority());
        verifyNoMoreInteractions(taskAllocationDetailRepository);

        verify(uuidGenerator, times(1)).randomUUID();
        verifyNoMoreInteractions(uuidGenerator);

        verifyZeroInteractions(taskRepository);
    }

    @Test
    public void test_addTask_returnsUnProcessableEntity_whenTaskRepoCreateFails(){
        val taskRequest = new TaskRequest(TaskPriority.HIGH, true, false, false);
        val agentId = "agentId";

        when(taskAllocationDetailRepository.createOrUpdate(agentId,
                guid.toString(), taskRequest.getTaskPriority())).thenReturn(true);
        when(taskRepository.create(agentId, guid.toString(), taskRequest.getTaskPriority(),
                TaskStatus.IN_PROGRESS, taskRequest.isSkill1(), taskRequest.isSkill2(), taskRequest.isSkill3())).thenReturn(false);

        val taskResponse = subject.addTask(agentId, taskRequest);
        assertEquals(422, taskResponse.getStatusCode().value());
        assertEquals(new TaskRequestBusinessErrorResponse("V005", "Can't Create a Task At this time"), (TaskRequestBusinessErrorResponse)taskResponse.getBody());

        verify(taskAllocationDetailRepository, times(1)).createOrUpdate(agentId,
                guid.toString(), taskRequest.getTaskPriority());
        verifyNoMoreInteractions(taskAllocationDetailRepository);

        verify(uuidGenerator, times(1)).randomUUID();
        verifyNoMoreInteractions(uuidGenerator);

        verify(taskRepository, times(1)).create(agentId, guid.toString(), taskRequest.getTaskPriority(),
                TaskStatus.IN_PROGRESS, taskRequest.isSkill1(), taskRequest.isSkill2(), taskRequest.isSkill3());
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    public void test_addTask_returnsOk_whenTaskRepoAndTaskAllocationCreateSuccess(){
        val taskRequest = new TaskRequest(TaskPriority.HIGH, true, false, false);
        val agentId = "agentId";

        when(taskAllocationDetailRepository.createOrUpdate(agentId,
                guid.toString(), taskRequest.getTaskPriority())).thenReturn(true);
        when(taskRepository.create(agentId, guid.toString(), taskRequest.getTaskPriority(),
                TaskStatus.IN_PROGRESS, taskRequest.isSkill1(), taskRequest.isSkill2(), taskRequest.isSkill3())).thenReturn(true);

        val taskResponse = subject.addTask(agentId, taskRequest);
        assertEquals(200, taskResponse.getStatusCode().value());
        assertEquals(new TaskResponse(guid.toString(), agentId, TaskStatus.IN_PROGRESS), (TaskResponse) taskResponse.getBody());

        verify(taskAllocationDetailRepository, times(1)).createOrUpdate(agentId,
                guid.toString(), taskRequest.getTaskPriority());
        verifyNoMoreInteractions(taskAllocationDetailRepository);

        verify(uuidGenerator, times(1)).randomUUID();
        verifyNoMoreInteractions(uuidGenerator);

        verify(taskRepository, times(1)).create(agentId, guid.toString(), taskRequest.getTaskPriority(),
                TaskStatus.IN_PROGRESS, taskRequest.isSkill1(), taskRequest.isSkill2(), taskRequest.isSkill3());
        verifyNoMoreInteractions(taskRepository);

    }
    //endregion

}