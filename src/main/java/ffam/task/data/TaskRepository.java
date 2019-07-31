package ffam.task.data;

import ffam.task.domain.Task;
import ffam.task.domain.TaskPriority;
import ffam.task.domain.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class TaskRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Task> findByTaskId(String taskId) {
        val results = jdbcTemplate.query(
                "SELECT TASK_ID, PRIORITY, SKILL_1, SKILL_2, SKILL_3, STATUS, AGENT_ID " +
                        "FROM TASK " +
                        "WHERE TASK_ID = ? ",
                new Object[]{taskId},
                taskRowMapper);

        return results.isEmpty() ? Optional.empty() : Optional.of((Task) results.get(0));
    }

    public List<Task> findByAgentIdAndStatus(String agentId,
                                             TaskStatus taskStatus) {
        List<Task> results = jdbcTemplate.query(
                "SELECT TASK_ID, PRIORITY, SKILL_1, SKILL_2, SKILL_3, STATUS, AGENT_ID " +
                        "FROM TASK " +
                        "WHERE AGENT_ID = ? " +
                        "AND STATUS = ?",
                new Object[]{agentId, taskStatus.name()},
                taskRowMapper);

        return results;
    }

    public boolean create(String agentId,
                          String taskId,
                          TaskPriority taskPriority,
                          TaskStatus taskStatus,
                          boolean skill1,
                          boolean skill2,
                          boolean skill3) {
        int count = jdbcTemplate.update(
                "INSERT INTO TASK(TASK_ID, PRIORITY, SKILL_1, SKILL_2, SKILL_3, STATUS, AGENT_ID) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                new Object[]{
                        taskId,
                        taskPriority == TaskPriority.HIGH ? 1 : 0,
                        skill1,
                        skill2,
                        skill3,
                        taskStatus.name(),
                        agentId
                }
        );

        return count > 0;
    }

    public boolean updateTaskStatus(String taskId,
                                    TaskStatus taskStatus) {
        int count = jdbcTemplate.update(
                "UPDATE TASK " +
                        "SET STATUS = ? " +
                        "WHERE TASK_ID = ?",

                new Object[]{taskStatus.name(), taskId});

        return count > 0;
    }

    //region private
    private final RowMapper<Task> taskRowMapper =
            (ResultSet rs, int rowNum) -> {
                String taskId = rs.getString("TASK_ID");
                TaskPriority taskPriority = rs.getInt("PRIORITY") == 1 ? TaskPriority.HIGH : TaskPriority.LOW;
                TaskStatus taskStatus = Enum.valueOf(TaskStatus.class, rs.getString("STATUS"));
                boolean skill1 = rs.getBoolean("SKILL_1");
                boolean skill2 = rs.getBoolean("SKILL_2");
                boolean skill3 = rs.getBoolean("SKILL_3");
                String agentId = rs.getString("AGENT_ID");

                return new Task(
                        taskId,
                        taskPriority,
                        skill1,
                        skill2,
                        skill3,
                        taskStatus,
                        agentId
                );
            };
    //endregion
}
