package com.yulink.texas.core.config;

import com.yulink.texas.common.admin.constant.DataSourceConstants;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfiguration {

    @Primary
    @ConfigurationProperties("app.datasource")
    @Bean(DataSourceConstants.APP_DATASOURCE_PROPERTIES)
    public DataSourceProperties appDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource appDataSource(DataSourceProperties appDataSourceProperties) {
        return ((DataSourceBuilder) appDataSourceProperties.initializeDataSourceBuilder()).build();
    }

    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(DataSource appDataSource) throws Exception {
        return SqlSessionFactoryUtil.buildSqlSessionFactory(appDataSource);
    }

}
