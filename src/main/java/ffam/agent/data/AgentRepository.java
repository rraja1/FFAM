package ffam.agent.data;

import ffam.general.Sql;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

import static ffam.general.StatementAttribute.Type.API;
import static ffam.general.StatementAttribute.Type.DATA;
import static ffam.general.StatementAttribute.attribute;
import static ffam.general.StatementInfo.withAttributes;

@Slf4j
@Repository
public class AgentRepository {
    private final Sql sql;

    @Autowired
    public AgentRepository(Sql sql) {
        this.sql = sql;
    }

    public List<Agent> findBySkillSet(boolean skill1, boolean skill2, boolean skill3) {
        List<Agent> results = sql.query(
                withAttributes(attribute(API, "AgentRepository.findBySkillSet"),
                        attribute(DATA, skill1),
                        attribute(DATA, skill2),
                        attribute(DATA, skill3)
                ),
                buildSql(skill1, skill2, skill3),
                new Object[]{},
                agentRowMapper);

        return results;
    }

    //region private
    private final RowMapper<Agent> agentRowMapper =
            (ResultSet rs, int rowNum) -> {
                String agentId = rs.getString("AGENT_ID");
                String agentName = rs.getString("AGENT_NAME");
                boolean skill1 = rs.getBoolean("SKILL_1");
                boolean skill2 = rs.getBoolean("SKILL_2");
                boolean skill3 = rs.getBoolean("SKILL_3");

                return new Agent(
                        agentId,
                        agentName,
                        skill1,
                        skill2,
                        skill3
                );
            };

    private String buildSql(boolean skill1, boolean skill2, boolean skill3) {
        StringBuilder stringBuilder = new StringBuilder().append("SELECT AGENT_ID, AGENT_NAME, SKILL_1, SKILL_2, SKILL_3 " +
                "FROM AGENT_LOOKUP " +
                "WHERE ");

        if (skill1) {
            stringBuilder.append(" SKILL_1 = 1 ");
        }
        if (skill2) {
            if (stringBuilder.toString().endsWith("1 ")) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(" SKILL_2 = 1 ");
        }
        if (skill3) {
            if (stringBuilder.toString().endsWith("1 ")) {
                stringBuilder.append(" AND ");
            }
            stringBuilder.append(" SKILL_3 = 1 ");
        }
        return stringBuilder.toString();
    }
    //endregion
}
