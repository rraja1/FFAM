package ffam.general;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class ZonedDateTimeProvider {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ZonedDateTimeProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ZonedDateTime now() {
        val now = jdbcTemplate.query(
                "SELECT CURRENT_TIMESTAMP AS now FROM DUAL",
                (rs, index) -> rs.getTimestamp("now")).get(0);

        return ZonedDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
    }
}
