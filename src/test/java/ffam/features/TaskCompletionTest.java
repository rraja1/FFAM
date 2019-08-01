package ffam.features;

import com.fasterxml.jackson.databind.ObjectMapper;
import ffam.TestApplication;
import ffam.agent.data.AgentRepository;
import ffam.task.data.TaskAllocationDetailRepository;
import ffam.task.data.TaskRepository;
import ffam.task.domain.TaskPriority;
import ffam.task.domain.TaskStatus;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@SpringBootTest(classes = TestApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TaskCompletionTest {
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
     * Simple test to create a task and complete it
     */
    @Test
    public void test_completeTask() throws Exception {
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

        val taskId = jdbcTemplate.queryForObject("SELECT TASK_ID FROM TASK WHERE PRIORITY = '0'", String.class);

        mockMvc.perform(patch("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .param("taskId", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value("COMPLETE"));

        assertFalse(taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId()).isPresent());

        val taskOptional1 = taskRepository.findByTaskId(taskAllocationDetail.get().getTaskId().get(0));
        assertTrue(taskOptional1.isPresent());
        assertTrue(taskOptional1.get().isSkill1());
        assertTrue(taskOptional1.get().isSkill2());
        assertTrue(taskOptional1.get().isSkill3());
        assertEquals(taskOptional1.get().getTaskPriority(), TaskPriority.LOW);
        assertEquals(taskOptional1.get().getTaskStatus(), TaskStatus.COMPLETE);
    }

    /**
     * Add two tasks for a same agent
     * Complete HIGH Task
     * Check Task Allocation Exists, Overall Priority and Date
     */
    @Test
    public void test_completeTask_LOW_HIGH_completeHIGH() throws Exception {
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

        val taskDetailOptional = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskDetailOptional.isPresent());
        assertEquals(taskDetailOptional.get().getTaskPriority(), TaskPriority.LOW);

        val taskOptional1 = taskRepository.findByTaskId(taskId);
        assertTrue(taskOptional1.isPresent());
        assertTrue(taskOptional1.get().isSkill1());
        assertTrue(taskOptional1.get().isSkill2());
        assertTrue(taskOptional1.get().isSkill3());
        assertEquals(taskOptional1.get().getTaskPriority(), TaskPriority.HIGH);
        assertEquals(taskOptional1.get().getTaskStatus(), TaskStatus.COMPLETE);
    }

    /**
     * Add two tasks for a same agent
     * Complete LOW Task
     * Check Task Allocation Exists, Overall Priority and Date
     */
    @Test
    public void test_completeTask_LOW_HIGH_completeLOW() throws Exception {
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

        val taskId = jdbcTemplate.queryForObject("SELECT TASK_ID FROM TASK WHERE PRIORITY = '0'", String.class);

        mockMvc.perform(patch("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .param("taskId", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value("COMPLETE"));

        val taskDetailOptional = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskDetailOptional.isPresent());
        assertEquals(taskDetailOptional.get().getTaskPriority(), TaskPriority.HIGH);

        val taskOptional1 = taskRepository.findByTaskId(taskId);
        assertTrue(taskOptional1.isPresent());
        assertTrue(taskOptional1.get().isSkill1());
        assertTrue(taskOptional1.get().isSkill2());
        assertTrue(taskOptional1.get().isSkill3());
        assertEquals(taskOptional1.get().getTaskPriority(), TaskPriority.LOW);
        assertEquals(taskOptional1.get().getTaskStatus(), TaskStatus.COMPLETE);
    }

    /**
     * Add two tasks for a same agent
     * Complete both Tasks
     * Check Task Allocation does not exist
     */
    @Test
    public void test_completeTask_LOW_HIGH_completeLOW_completeHIGH() throws Exception {
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

        val taskId = jdbcTemplate.queryForObject("SELECT TASK_ID FROM TASK WHERE PRIORITY = '0'", String.class);

        mockMvc.perform(patch("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .param("taskId", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value("COMPLETE"));

        val taskDetailOptional = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertTrue(taskDetailOptional.isPresent());
        assertEquals(taskDetailOptional.get().getTaskPriority(), TaskPriority.HIGH);

        val taskOptional1 = taskRepository.findByTaskId(taskId);
        assertTrue(taskOptional1.isPresent());
        assertTrue(taskOptional1.get().isSkill1());
        assertTrue(taskOptional1.get().isSkill2());
        assertTrue(taskOptional1.get().isSkill3());
        assertEquals(taskOptional1.get().getTaskPriority(), TaskPriority.LOW);
        assertEquals(taskOptional1.get().getTaskStatus(), TaskStatus.COMPLETE);

        val taskId1 = jdbcTemplate.queryForObject("SELECT TASK_ID FROM TASK WHERE PRIORITY = '1'", String.class);
        mockMvc.perform(patch("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .param("taskId", taskId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value("COMPLETE"));

        val taskDetailOptional1 = taskAllocationDetailRepository.findByAgentId(agent.get(0).getAgentId());
        assertFalse(taskDetailOptional1.isPresent());

        val taskOptional2 = taskRepository.findByTaskId(taskId1);
        assertTrue(taskOptional2.isPresent());
        assertTrue(taskOptional2.get().isSkill1());
        assertTrue(taskOptional2.get().isSkill2());
        assertTrue(taskOptional2.get().isSkill3());
        assertEquals(taskOptional2.get().getTaskPriority(), TaskPriority.HIGH);
        assertEquals(taskOptional2.get().getTaskStatus(), TaskStatus.COMPLETE);
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
