package ffam.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.sql.Connection;
import java.sql.SQLException;

@Configuration
@EnableAutoConfiguration
@Slf4j
public class DatabaseConfiguration {

    @Bean
    @Primary
    public org.springframework.jdbc.core.JdbcTemplate jdbcTemplate(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.db-connection-timeout-seconds}") int connectionTimeout,
            @Value("${spring.datasource.initial_pool_size}") int initialPoolSize,
            @Value("${spring.datasource.maxIdle}") int maxIdle,
            @Value("${spring.datasource.maxActive}") int maxActive
    ) throws SQLException, InterruptedException {
        return new org.springframework.jdbc.core.JdbcTemplate(dataSource(url, connectionTimeout, initialPoolSize, maxIdle, maxActive));
    }

    @Bean
    @Primary
    public javax.sql.DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.db-connection-timeout-seconds}") int connectionTimeout,
            @Value("${spring.datasource.initial_pool_size}") int initialPoolSize,
            @Value("${spring.datasource.maxIdle}") int maxIdle,
            @Value("${spring.datasource.maxActive}") int maxActive
    ) throws SQLException, InterruptedException {

        DataSource dataSource = new DataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setInitialSize(initialPoolSize);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setMaxActive(maxActive);
        dataSource.setSuspectTimeout(40);
        dataSource.setTimeBetweenEvictionRunsMillis(30000);
        dataSource.setMinEvictableIdleTimeMillis(60000);
        dataSource.setInitSQL("SELECT 1 FROM DUAL");
        dataSource.setTestOnConnect(true);
        dataSource.setValidationQuery("SELECT 1 FROM DUAL");
        dataSource.setTestOnBorrow(true);
        dataSource.setLogAbandoned(true);
        dataSource.setLogValidationErrors(true);

        log.info(" InitSQL :"+dataSource.getInitSQL());
        log.info(" ValidationSQL :"+dataSource.getValidationQuery());

        final Integer MAX_ATTEMPTS = connectionTimeout;
        Integer attemptedConnections = 0;

        while (true) {
            try {
                attemptedConnections += 1;
                Connection connection = dataSource.getConnection();
                connection.close();

                log.info("Connection established to oracle after {} attempts\n", attemptedConnections);

                break;
            }
            catch (SQLException e) {
                if (attemptedConnections < MAX_ATTEMPTS) {
                    log.info("Still attempting to connect, try {}...", attemptedConnections);
                    Thread.sleep(1000);
                }
                else {
                    throw e;
                }
            }
        }

        return dataSource;
    }

}
