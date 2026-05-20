package com.ashi.orderservice.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseUrlConfig {

    @Bean
    @Primary
    @ConditionalOnExpression("'${DATABASE_URL:}' != ''")
    public DataSource dataSourceFromDatabaseUrl(@Value("${DATABASE_URL}") String databaseUrl) {
        try {
            URI uri = new URI(databaseUrl);
            String[] userInfoParts = uri.getUserInfo().split(":", 2);
            String username = userInfoParts[0];
            String password = userInfoParts.length > 1 ? userInfoParts[1] : "";

            String jdbcUrl = "jdbc:postgresql://" + uri.getHost();
            if (uri.getPort() != -1) {
                jdbcUrl += ":" + uri.getPort();
            }
            jdbcUrl += uri.getPath();
            if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                jdbcUrl += "?" + uri.getQuery();
            }

            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setJdbcUrl(jdbcUrl);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            return dataSource;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid DATABASE_URL format", e);
        }
    }
}

