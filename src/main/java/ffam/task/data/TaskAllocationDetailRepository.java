package ffam.task.data;

import ffam.general.Sql;
import ffam.task.domain.TaskAllocationDetail;
import ffam.task.domain.TaskPriority;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static ffam.general.StatementAttribute.Type.API;
import static ffam.general.StatementAttribute.Type.DATA;
import static ffam.general.StatementAttribute.attribute;
import static ffam.general.StatementInfo.withAttributes;

@Slf4j
@Repository
public class TaskAllocationDetailRepository {
    private final Sql sql;

    @Autowired
    public TaskAllocationDetailRepository(Sql sql) {
        this.sql = sql;
    }

    public List<TaskAllocationDetail> findAll() {
        List<TaskAllocationDetail> results = sql.query(
                withAttributes(attribute(API, "TaskAllocationDetailRepository.findAll")
                ),
                "SELECT AGENT_ID, TASK_ID, PRIORITY, DATE_CREATED " +
                        "FROM TASK_ALLOCATION " +
                        "ORDER BY DATE_CREATED DSC ",
                new Object[]{},
                taskAllocationDetailRowMapper);

        return results;
    }

    public Optional<TaskAllocationDetail> findByAgentId(String agentId) {
        List<TaskAllocationDetail> results = sql.query(
                withAttributes(attribute(API, "TaskAllocationDetailRepository.findByAgentId"),
                        attribute(DATA, agentId)
                ),
                "SELECT AGENT_ID, TASK_ID, PRIORITY, DATE_CREATED " +
                        "FROM TASK_ALLOCATION WHERE AGENT_ID = ? ",
                new Object[]{agentId},
                taskAllocationDetailRowMapper);

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public boolean createOrUpdate(String agentId,
                                  String taskId,
                                  TaskPriority taskPriority) {
        //Check id the row exists per agent
        val taskAllocationDetailOptional = findByAgentId(agentId);

        if (!taskAllocationDetailOptional.isPresent()) {
            int count = sql.update(
                    withAttributes(
                            attribute(API, "TaskAllocationDetailRepository.createOrUpdate"),
                            attribute(DATA, agentId),
                            attribute(DATA, taskId)
                    ),
                    "INSERT INTO TASK_ALLOCATION(TASK_ID, AGENT_ID, TASK_PRIORITY) VALUES (?, ?, ?)",
                    new Object[]{taskId, agentId, taskPriority.name()}
            );

            return count > 0;
        } else {
            List<String> taskIdList = taskAllocationDetailOptional.get().getTaskId();
            taskIdList.add(taskId);

            int count = sql.update(
                    withAttributes(
                            attribute(API, "TaskAllocationDetailRepository.createOrUpdate"),
                            attribute(DATA, agentId),
                            attribute(DATA, taskId)
                    ),
                    "UPDATE TASK_ALLOCATION " +
                            "SET TASK_ID = ? ," +
                            "TASK_PRIORITY = ? , " +
                            "DATE_CREATED = ? , " +
                            "WHERE AGENT_ID = ? ",
                    new Object[]{String.join(",", taskIdList),
                            taskPriority.name(), // If already present and updating, take newer state
                            agentId}
            );

            return count > 0;
        }
    }

    public boolean delete(String taskId,
                          String agentId) {

        val taskAllocationDetailOptional = findByAgentId(agentId);
        if (!taskAllocationDetailOptional.isPresent()) {
            //Not Present
            return false;
        }
        val taskAllocationDetail = taskAllocationDetailOptional.get();
        if (taskAllocationDetail.getTaskId().contains(taskId)) {
            //Update
            List<String> taskIdList = taskAllocationDetailOptional.get().getTaskId();
            taskIdList.remove(taskId);

            int count = sql.update(
                    withAttributes(
                            attribute(API, "TaskAllocationDetailRepository.createOrUpdate"),
                            attribute(DATA, agentId),
                            attribute(DATA, taskId)
                    ),
                    "UPDATE TASK_ALLOCATION " +
                            "SET TASK_ID = ? ," +
                            "TASK_PRIORITY = ? , " +
                            "DATE_CREATED = ? , " +
                            "WHERE AGENT_ID = ? ",
                    new Object[]{String.join(",", taskIdList),
                            taskAllocationDetail.getTaskPriority().name(), // If already present and updating, take newer state
                            agentId}
            );

            return count > 0;
        } else {
            int count = sql.update(
                    withAttributes(
                            attribute(API, "TaskAllocationDetailRepository.delete"),
                            attribute(DATA, taskId)
                    ),
                    "DELETE FROM TASK_ALLOCATION WHERE TASK_ID = ? ",
                    new Object[]{taskId}
            );
            return count > 0;
        }

    }

    //region private
    private final RowMapper<TaskAllocationDetail> taskAllocationDetailRowMapper =
            (ResultSet rs, int rowNum) -> {
                List<String> taskId = new ArrayList<>(Arrays.asList(rs.getString("TASK_ID").split(",")));
                TaskPriority taskPriority = Enum.valueOf(TaskPriority.class, rs.getString("PRIORITY"));
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
