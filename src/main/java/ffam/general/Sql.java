package ffam.general;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
public class Sql {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public Sql(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int update(StatementInfo info, String sql, Object[] args) {
        return run(info, () -> jdbcTemplate.update(sql, args));
    }

    public int update(StatementInfo info, String sql, Object[] args, int[] types) {
        return run(info, () -> jdbcTemplate.update(sql, args, types));
    }

    public <T> List<T> query(StatementInfo info, String sql, RowMapper<T> rowMapper) {
        return query(info, sql, new Object[]{}, rowMapper);
    }

    public <T> List<T> query(StatementInfo info, String sql, Object[] args, RowMapper<T> rowMapper) {
        return run(info, () -> jdbcTemplate.query(sql, rowMapper, args));
    }

    public <T> List<T> query(StatementInfo info, String sql, MapSqlParameterSource args, RowMapper<T> rowMapper) {
        return run(info, () -> new NamedParameterJdbcTemplate(jdbcTemplate).query(sql, args, rowMapper));
    }

    public <T> T queryForObject(StatementInfo info, String sql, Class<T> clazz, Object... args) {
        return run(info, () -> jdbcTemplate.queryForObject(sql, clazz, args));
    }

    //region private

    private <T> T run(StatementInfo info, Supplier<T> dbFunc) {
        val logBuilder = logBuilderForInfo(info);
        try {
            val returnValue = dbFunc.get();
            log.debug(logBuilder.success(true).build().log());
            return returnValue;
        }
        catch (Throwable e) {
            log.error(logBuilder.message(e.getMessage()).success(false).build().log());
            throw e;
        }
    }

    private LogBuilder.LogBuilderBuilder logBuilderForInfo(StatementInfo info) {
        val logBuilder = LogBuilder.builder().eventType("SQL_Query");

        info.getAttributes().forEach(attribute -> {
            switch (attribute.getType()) {
                case API:
                    logBuilder.api(attribute.getValue().toString());
                    break;
                case DATA:
                    logBuilder.api(attribute.getValue().toString());
                    break;
            }
        });

        return logBuilder;
    }
    //endRegion


}
