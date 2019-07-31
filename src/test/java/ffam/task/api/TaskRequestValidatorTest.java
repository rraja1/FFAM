package ffam.task.api;

import ffam.task.domain.TaskPriority;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TaskRequestValidatorTest {
    private TaskRequestValidator subject;

    @Before
    public void setUp() {
        subject = new TaskRequestValidator();
    }

    @Test
    public void testWhenRequestIsNull() {
        assertEquals(subject.isValid(null),
                Optional.of(new TaskRequestValidationErrorResponse("V001", "Invalid Request")));
    }

    @Test
    public void testWhenRequestDoesNotContainPriority() {
        assertEquals(subject.isValid(new TaskRequest(null, false, false, false)),
                Optional.of(new TaskRequestValidationErrorResponse("V002", "Task Priority is Mandatory")));
    }

    @Test
    public void testWhenRequestDoesNotContainSkill() {
        assertEquals(subject.isValid(new TaskRequest(TaskPriority.LOW, false, false, false)),
                Optional.of(new TaskRequestValidationErrorResponse("V003", "At least One Skill is required")));
    }

    @Test
    public void testWhenRequestIsGood() {
        assertEquals(subject.isValid(new TaskRequest(TaskPriority.LOW, true, false, false)),
                Optional.empty());
    }
}