package ffam.features;

import com.fasterxml.jackson.databind.ObjectMapper;
import ffam.TestApplication;
import ffam.agent.data.AgentRepository;
import ffam.task.data.TaskAllocationDetailRepository;
import ffam.task.data.TaskRepository;
import ffam.task.domain.TaskPriority;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebAppConfiguration
@SpringBootTest(classes = TestApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TaskCreationTest {
    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    AgentRepository agentRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    TaskAllocationDetailRepository taskAllocationDetailRepository;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @After
    public void cleanUp() throws Exception {
        jdbcTemplate.execute("DELETE from TASK_ALLOCATION");
        jdbcTemplate.execute("DELETE from TASK");
    }

    /**
     * Simple test to create a task with low priority
     * @throws Exception
     */
    @Test
    public void test_createTask() throws Exception {
        val addTaskJson = stringify(map(
                "taskPriority", "LOW",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        val agent = agentRepository.findBySkillSet(true, true, true);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskAllocationDetail = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskAllocationDetail.isPresent());

        val taskOptional = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional.isPresent());
        assertTrue(taskOptional.get().isSkill1());
        assertTrue(taskOptional.get().isSkill2());
        assertTrue(taskOptional.get().isSkill3());
        assertEquals(taskOptional.get().getTaskPriority(), TaskPriority.LOW);
    }

    /**
     * Test to create a task with low priority with a skill set
     * Then try to add another task with Low Priority
     * Should not add the task and send an error
     * @throws Exception
     */
    @Test
    public void test_createTask_Low_Low() throws Exception {
        val addTaskJson = stringify(map(
                "taskPriority", "LOW",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        val agent = agentRepository.findBySkillSet(true, true, true);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskAllocationDetail = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskAllocationDetail.isPresent());

        val taskOptional = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional.isPresent());
        assertTrue(taskOptional.get().isSkill1());
        assertTrue(taskOptional.get().isSkill2());
        assertTrue(taskOptional.get().isSkill3());
        assertEquals(taskOptional.get().getTaskPriority(), TaskPriority.LOW);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("V006"))
                .andExpect(jsonPath("$.errorResponse").value("All Agents are busy at this time"));
    }

    /**
     * Test to create a task with low priority with a skill set
     * Then try to add another task with Low Priority with a different skill set
     * Should create the task
     * @throws Exception
     */
    @Test
    public void test_createTask_Low_LowAndDifferentSkills() throws Exception {
        val addTaskJson = stringify(map(
                "taskPriority", "LOW",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        val agent = agentRepository.findBySkillSet(true, true, true);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskAllocationDetail = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskAllocationDetail.isPresent());

        val taskOptional = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional.isPresent());
        assertTrue(taskOptional.get().isSkill1());
        assertTrue(taskOptional.get().isSkill2());
        assertTrue(taskOptional.get().isSkill3());
        assertEquals(taskOptional.get().getTaskPriority(), TaskPriority.LOW);

        val addTaskJson1 = stringify(map(
                "taskPriority", "LOW",
                "skill1", false,
                "skill2", true,
                "skill3", true));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").isString());
    }

    /**
     * Test to create a task with low priority with a skill set
     * Then try to add another task with High Priority with a same skill set
     * Should create the task
     * @throws Exception
     */
    @Test
    public void test_createTask_Low_High() throws Exception {
        val addTaskJson = stringify(map(
                "taskPriority", "LOW",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        val agent = agentRepository.findBySkillSet(true, true, true);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskAllocationDetail = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskAllocationDetail.isPresent());

        val taskOptional = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional.isPresent());
        assertTrue(taskOptional.get().isSkill1());
        assertTrue(taskOptional.get().isSkill2());
        assertTrue(taskOptional.get().isSkill3());
        assertEquals(taskOptional.get().getTaskPriority(), TaskPriority.LOW);

        val addTaskJson1 = stringify(map(
                "taskPriority", "HIGH",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").isString());
    }

    /**
     * Test to create a task with low priority with a skill set
     * Then try to add another task with High Priority with a same skill set
     * Should create the task
     * Now try to create another task with Low Priority
     * Should fail to create this task
     * @throws Exception
     */
    @Test
    public void test_createTask_Low_High_Low() throws Exception {
        val addTaskJson = stringify(map(
                "taskPriority", "LOW",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        val agent = agentRepository.findBySkillSet(true, true, true);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskAllocationDetail = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskAllocationDetail.isPresent());

        val taskOptional = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional.isPresent());
        assertTrue(taskOptional.get().isSkill1());
        assertTrue(taskOptional.get().isSkill2());
        assertTrue(taskOptional.get().isSkill3());
        assertEquals(taskOptional.get().getTaskPriority(), TaskPriority.LOW);

        val addTaskJson1 = stringify(map(
                "taskPriority", "HIGH",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").isString());

        val addTaskJson2 = stringify(map(
                "taskPriority", "LOW",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson2))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("V006"))
                .andExpect(jsonPath("$.errorResponse").value("All Agents are busy at this time"));
    }

    /**
     * Test to create a task with low priority with a skill set
     * Then try to add another task with High Priority with a same skill set
     * Should create the task
     * Now try to create another task with High Priority
     * Should fail to create this task
     * @throws Exception
     */
    @Test
    public void test_createTask_Low_High_High() throws Exception {
        val addTaskJson = stringify(map(
                "taskPriority", "LOW",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        val agent = agentRepository.findBySkillSet(true, true, true);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskAllocationDetail = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskAllocationDetail.isPresent());

        val taskOptional = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional.isPresent());
        assertTrue(taskOptional.get().isSkill1());
        assertTrue(taskOptional.get().isSkill2());
        assertTrue(taskOptional.get().isSkill3());
        assertEquals(taskOptional.get().getTaskPriority(), TaskPriority.LOW);

        val addTaskJson1 = stringify(map(
                "taskPriority", "HIGH",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").isString());

        val addTaskJson2 = stringify(map(
                "taskPriority", "HIGH",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson2))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("V006"))
                .andExpect(jsonPath("$.errorResponse").value("All Agents are busy at this time"));
    }

    /**
     * Test to create a task with low priority with a skill set
     * Then try to add another task with High Priority with a same skill set
     * Should create the task
     * Now try to complete the High priority task
     * Should complete the task
     * Add another task with High Priority
     * Should create this task
     * @throws Exception
     */
    @Test
    public void test_createTask_Low_High_Complete_High() throws  Exception {
        val addTaskJson = stringify(map(
                "taskPriority", "LOW",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        val agent = agentRepository.findBySkillSet(true, true, true);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskAllocationDetail = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskAllocationDetail.isPresent());

        val taskOptional = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional.isPresent());
        assertTrue(taskOptional.get().isSkill1());
        assertTrue(taskOptional.get().isSkill2());
        assertTrue(taskOptional.get().isSkill3());
        assertEquals(taskOptional.get().getTaskPriority(), TaskPriority.LOW);

        val addTaskJson1 = stringify(map(
                "taskPriority", "HIGH",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").isString());

        val taskId = jdbcTemplate.queryForObject("SELECT TASK_ID FROM TASK WHERE PRIORITY = '1'", String.class);

        mockMvc.perform(patch("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .param("taskId", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value("COMPLETE"));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").isString());
    }

    /**
     * Test to create a task with low priority with a skill set
     * Then try to add another task with High Priority with a same skill set
     * Should create the task
     * Now try to complete the High priority task
     * Should complete the task
     * Add another task with Low Priority
     * Should fail to create this task
     * @throws Exception
     */
    @Test
    public void test_createTask_Low_High_Complete_Low() throws  Exception {
        val addTaskJson = stringify(map(
                "taskPriority", "LOW",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        val agent = agentRepository.findBySkillSet(true, true, true);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskAllocationDetail = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskAllocationDetail.isPresent());

        val taskOptional = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional.isPresent());
        assertTrue(taskOptional.get().isSkill1());
        assertTrue(taskOptional.get().isSkill2());
        assertTrue(taskOptional.get().isSkill3());
        assertEquals(taskOptional.get().getTaskPriority(), TaskPriority.LOW);

        val addTaskJson1 = stringify(map(
                "taskPriority", "HIGH",
                "skill1", true,
                "skill2", true,
                "skill3", true));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").isString());

        val taskId = jdbcTemplate.queryForObject("SELECT TASK_ID FROM TASK WHERE PRIORITY = '1'", String.class);

        mockMvc.perform(patch("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .param("taskId", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value("COMPLETE"));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("V006"))
                .andExpect(jsonPath("$.errorResponse").value("All Agents are busy at this time"));
    }

    /**
     * Test to create a task with low priority with a skill set which 2 agents possess
     * Now try to add the another low priority tasks with a same skill set
     * Should create this task
     * Verify that tasks are assigned to two different agents
     * Try to add another low priority task with same skill set
     * Should fail to create a task
     * @throws Exception
     */
    @Test
    public void test_createTask_LowAgent1_LowAgent2_LowFail() throws Exception {
        val addTaskJson = stringify(map(
                "taskPriority", "LOW",
                "skill1", false,
                "skill2", true,
                "skill3", true));

        val agent = agentRepository.findBySkillSet(false, true, true);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskAllocationDetail = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskAllocationDetail.isPresent());

        val taskOptional = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional.isPresent());
        assertFalse(taskOptional.get().isSkill1());
        assertTrue(taskOptional.get().isSkill2());
        assertTrue(taskOptional.get().isSkill3());
        assertEquals(taskOptional.get().getTaskPriority(), TaskPriority.LOW);

        val addTaskJson1 = stringify(map(
                "taskPriority", "LOW",
                "skill1", false,
                "skill2", true,
                "skill3", true));
        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(1).getAgentId()));

        val addTaskJson4 = stringify(map(
                "taskPriority", "LOW",
                "skill1", false,
                "skill2", true,
                "skill3", true));
        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson4))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("V006"))
                .andExpect(jsonPath("$.errorResponse").value("All Agents are busy at this time"));
    }

    /**
     * Test to create a task with low priority with a skill set which 2 agents possess
     * Now try to add the another low priority tasks with a same skill set
     * Should create this task
     * Verify that tasks are assigned to two different agents
     * Try to add another High priority task with same skill set to each agent
     * Should create a task and assign it to newer agent first
     * Add a Low Priority task, Should fail
     * @throws Exception
     */
    @Test
    public void test_createTask_LowAgent1_LowAgent2_HighSuccess_HighSuccess_LowFail() throws Exception {
        val addTaskJson = stringify(map(
                "taskPriority", "LOW",
                "skill1", false,
                "skill2", true,
                "skill3", true));

        val agent = agentRepository.findBySkillSet(false, true, true);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskAllocationDetail = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskAllocationDetail.isPresent());

        val taskOptional = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional.isPresent());
        assertFalse(taskOptional.get().isSkill1());
        assertTrue(taskOptional.get().isSkill2());
        assertTrue(taskOptional.get().isSkill3());
        assertEquals(taskOptional.get().getTaskPriority(), TaskPriority.LOW);

        val addTaskJson1 = stringify(map(
                "taskPriority", "LOW",
                "skill1", false,
                "skill2", true,
                "skill3", true));
        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(1).getAgentId()));

        val addTaskJson2 = stringify(map(
                "taskPriority", "HIGH",
                "skill1", false,
                "skill2", true,
                "skill3", true));
        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(1).getAgentId()));

        val addTaskJson3 = stringify(map(
                "taskPriority", "HIGH",
                "skill1", false,
                "skill2", true,
                "skill3", true));
        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val addTaskJson4 = stringify(map(
                "taskPriority", "LOW",
                "skill1", false,
                "skill2", true,
                "skill3", true));
        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson4))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("V006"))
                .andExpect(jsonPath("$.errorResponse").value("All Agents are busy at this time"));
    }

    @Test
    public void test_createTask_LowAgent1_LowAgent2_HighSuccess_HighSuccess_HighComplete_HighSuccess() throws Exception{
        val addTaskJson = stringify(map(
                "taskPriority", "LOW",
                "skill1", false,
                "skill2", true,
                "skill3", true));

        val agent = agentRepository.findBySkillSet(false, true, true);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskAllocationDetail = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskAllocationDetail.isPresent());

        val taskOptional = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional.isPresent());
        assertFalse(taskOptional.get().isSkill1());
        assertTrue(taskOptional.get().isSkill2());
        assertTrue(taskOptional.get().isSkill3());
        assertEquals(taskOptional.get().getTaskPriority(), TaskPriority.LOW);

        val addTaskJson1 = stringify(map(
                "taskPriority", "LOW",
                "skill1", false,
                "skill2", true,
                "skill3", true));
        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(1).getAgentId()));

        val addTaskJson2 = stringify(map(
                "taskPriority", "HIGH",
                "skill1", false,
                "skill2", true,
                "skill3", true));
        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(1).getAgentId()));

        val addTaskJson3 = stringify(map(
                "taskPriority", "HIGH",
                "skill1", false,
                "skill2", true,
                "skill3", true));
        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));

        val taskId = jdbcTemplate.queryForObject("SELECT TASK_ID FROM TASK WHERE PRIORITY = '1' AND AGENT_ID = ?", String.class, agent.get(0).getAgentId());
        mockMvc.perform(patch("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .param("taskId", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value("COMPLETE"));

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTaskJson3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isString())
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.agentId").value(agent.get(0).getAgentId()));
    }

    //region private
    private static String stringify(Map<String, Object> json) throws IOException {
        return new ObjectMapper().writeValueAsString(json);
    }

    private static Map<String, Object> map(Object... objects) {
        val hashMap = new HashMap<String, Object>();

        for (int i = 0; i < objects.length; i += 2) {
            val key = (String) objects[i];
            val value = objects[i + 1];
            hashMap.put(key, value);
        }

        return Collections.unmodifiableMap(hashMap);
    }
    //endregion
}
