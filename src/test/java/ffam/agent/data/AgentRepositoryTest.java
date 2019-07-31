package ffam.agent.data;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class AgentRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private AgentRepository subject;

    @Before
    public void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class, RETURNS_SMART_NULLS);
        subject = new AgentRepository(jdbcTemplate);
    }

    //region buildSql
    @Test
    public void test_buildSql_whenAllSkillsTrue() {
        val expected = "SELECT AGENT_ID, AGENT_NAME, SKILL_1, SKILL_2, SKILL_3 " +
                "FROM AGENT_LOOKUP WHERE  SKILL_1 = 1  AND  SKILL_2 = 1  AND  SKILL_3 = 1 ";

        assertEquals(expected, subject.buildSql(true, true, true));
    }

    @Test
    public void test_buildSql_whenFirstTwoSkillsTrue() {
        val expected = "SELECT AGENT_ID, AGENT_NAME, SKILL_1, SKILL_2, SKILL_3 " +
                "FROM AGENT_LOOKUP WHERE  SKILL_1 = 1  AND  SKILL_2 = 1 ";

        assertEquals(expected, subject.buildSql(true, true, false));
    }

    @Test
    public void test_buildSql_whenLastTwoSkillsTrue() {
        val expected = "SELECT AGENT_ID, AGENT_NAME, SKILL_1, SKILL_2, SKILL_3 " +
                "FROM AGENT_LOOKUP WHERE  SKILL_2 = 1  AND  SKILL_3 = 1 ";

        assertEquals(expected, subject.buildSql(false, true, true));
    }

    @Test
    public void test_buildSql_whenFirstAndLastSkillsTrue() {
        val expected = "SELECT AGENT_ID, AGENT_NAME, SKILL_1, SKILL_2, SKILL_3 " +
                "FROM AGENT_LOOKUP WHERE  SKILL_1 = 1  AND  SKILL_3 = 1 ";

        assertEquals(expected, subject.buildSql(true, false, true));
    }

    @Test
    public void test_buildSql_whenOnlyFirstSkillTrue() {
        val expected = "SELECT AGENT_ID, AGENT_NAME, SKILL_1, SKILL_2, SKILL_3 " +
                "FROM AGENT_LOOKUP WHERE  SKILL_1 = 1 ";

        assertEquals(expected, subject.buildSql(true, false, false));
    }

    @Test
    public void test_buildSql_whenOnlySecondSkillTrue() {
        val expected = "SELECT AGENT_ID, AGENT_NAME, SKILL_1, SKILL_2, SKILL_3 " +
                "FROM AGENT_LOOKUP WHERE  SKILL_2 = 1 ";

        assertEquals(expected, subject.buildSql(false, true, false));
    }

    @Test
    public void test_buildSql_whenOnlyThirdSkillTrue() {
        val expected = "SELECT AGENT_ID, AGENT_NAME, SKILL_1, SKILL_2, SKILL_3 " +
                "FROM AGENT_LOOKUP WHERE  SKILL_3 = 1 ";

        assertEquals(expected, subject.buildSql(false, false, true));
    }
    //endregion

}