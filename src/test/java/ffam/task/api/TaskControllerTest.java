package ffam.task.api;

import ffam.task.domain.TaskPriority;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class TaskControllerTest {

    private TaskController subject;
    private TaskRequestValidator taskRequestValidator;
    private TaskControllerUseCase taskControllerUseCase;

    @Before
    public void setUp(){
        taskRequestValidator = mock(TaskRequestValidator.class, RETURNS_SMART_NULLS);
        taskControllerUseCase = mock(TaskControllerUseCase.class, RETURNS_SMART_NULLS);
        subject = new TaskController(taskRequestValidator, taskControllerUseCase);
    }

    //region addTask
    @Test
    public void testWhenRequestInvalid(){
        val taskRequest = new TaskRequest(TaskPriority.HIGH, false, false, false);
        when(taskRequestValidator.isValid(taskRequest)).thenReturn(Optional.of(new TaskRequestValidationErrorResponse("V001", "Error")));

        val taskResponse = subject.addTask(taskRequest);
        assertEquals(400, taskResponse.getStatusCode().value());
        assertEquals(new TaskRequestValidationErrorResponse("V001", "Error"), (TaskRequestValidationErrorResponse)taskResponse.getBody());

        verify(taskRequestValidator, times(1)).isValid(taskRequest);
        verifyNoMoreInteractions(taskRequestValidator);

        verifyZeroInteractions(taskControllerUseCase);
    }

    @Test
    public void testWhenRequestValidReturnsControllerUseCaseResponse(){
        val taskRequest = new TaskRequest(TaskPriority.HIGH, true, false, false);
        when(taskRequestValidator.isValid(taskRequest)).thenReturn(Optional.empty());
        when(taskControllerUseCase.createTask(taskRequest)).thenReturn(ResponseEntity.ok().build());
        val taskResponse = subject.addTask(taskRequest);
        assertEquals(200, taskResponse.getStatusCode().value());

        verify(taskRequestValidator, times(1)).isValid(taskRequest);
        verifyNoMoreInteractions(taskRequestValidator);

        verify(taskControllerUseCase, times(1)).createTask(taskRequest);
        verifyNoMoreInteractions(taskControllerUseCase);
    }
    //endregion

    //region finishTask

    //endregion

}