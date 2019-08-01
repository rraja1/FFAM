package ffam.task.data;

import ffam.general.ZonedDateTimeProvider;
import ffam.task.domain.TaskAllocationDetail;
import ffam.task.domain.TaskPriority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class TaskAllocationDetailRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ZonedDateTimeProvider zonedDateTimeProvider;

    @Autowired
    public TaskAllocationDetailRepository(JdbcTemplate jdbcTemplate, ZonedDateTimeProvider zonedDateTimeProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.zonedDateTimeProvider = zonedDateTimeProvider;
    }

    public List<TaskAllocationDetail> findAll() {
        List<TaskAllocationDetail> results = jdbcTemplate.query(
                "SELECT AGENT_ID, TASK_ID, TASK_PRIORITY, DATE_CREATED " +
                        "FROM TASK_ALLOCATION " +
                        "ORDER BY DATE_CREATED DESC ",
                new Object[]{},
                taskAllocationDetailRowMapper);

        return results;
    }

    public Optional<TaskAllocationDetail> findByAgentId(String agentId) {
        List<TaskAllocationDetail> results = jdbcTemplate.query(
                "SELECT AGENT_ID, TASK_ID, TASK_PRIORITY, DATE_CREATED " +
                        "FROM TASK_ALLOCATION WHERE AGENT_ID = ? ",
                new Object[]{agentId},
                taskAllocationDetailRowMapper);

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public boolean create(String agentId,
                          String taskId,
                          TaskPriority taskPriority) {
        int count = jdbcTemplate.update(
                "INSERT INTO TASK_ALLOCATION (TASK_ID, AGENT_ID, TASK_PRIORITY) VALUES (?, ?, ?)",
                new Object[]{taskId, agentId, taskPriority == TaskPriority.HIGH ? 1 : 0}
        );

        return count > 0;
    }

    public boolean update(String agentId,
                          TaskPriority taskPriority,
                          List<String> taskIdList,
                          Optional<ZonedDateTime> zonedDateTimeOptional) {

        int count = jdbcTemplate.update(
                "UPDATE TASK_ALLOCATION " +
                        "SET TASK_ID = ? ," +
                        "TASK_PRIORITY = ? , " +
                        "DATE_CREATED = ?  " +
                        "WHERE AGENT_ID = ? ",
                new Object[]{String.join(",", taskIdList),
                        taskPriority == TaskPriority.HIGH ? 1 : 0, // If already present and updating, take newer state
                        zonedDateTimeOptional.map(zonedDateTime -> Date.from(zonedDateTime.toInstant())).orElseGet(() -> Date.from(zonedDateTimeProvider.now().toInstant())),
                        agentId});

        return count > 0;
    }

    public boolean delete(String agentId) {
        int count = jdbcTemplate.update(
                "DELETE FROM TASK_ALLOCATION WHERE AGENT_ID = ? ",
                new Object[]{agentId}
        );
        return count > 0;
    }

    //region private
    private final RowMapper<TaskAllocationDetail> taskAllocationDetailRowMapper =
            (ResultSet rs, int rowNum) -> {
                List<String> taskId = new ArrayList<>(Arrays.asList(rs.getString("TASK_ID").split(",")));
                TaskPriority taskPriority = rs.getInt("TASK_PRIORITY") == 1 ? TaskPriority.HIGH : TaskPriority.LOW;
                String agentId = rs.getString("AGENT_ID");
                ZonedDateTime createdDate = ZonedDateTime.ofInstant(rs.getTimestamp("DATE_CREATED").toInstant(), ZoneId.systemDefault());
                return new TaskAllocationDetail(
                        agentId,
                        taskId,
                        taskPriority,
                        createdDate
                );
            };

    //endregion
}
