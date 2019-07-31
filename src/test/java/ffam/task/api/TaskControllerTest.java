package ffam.task.api;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class TaskControllerTest {

    private TaskController subject;
    private TaskRequestValidator taskRequestValidator;
    private TaskControllerUseCase taskControllerUseCase;

    @Before
    public void setUp(){
        taskRequestValidator = mock(TaskRequestValidator.class, RETURNS_SMART_NULLS);

    }
}