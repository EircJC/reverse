package com.yulink.texas.core.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

class SqlSessionFactoryUtil {

    static SqlSessionFactory buildSqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setDefaultFetchSize(100);
        configuration.setDefaultStatementTimeout(10000);
        sessionFactory.setConfiguration(configuration);

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("classpath:mybatis/*.xml"));
        sessionFactory.setTypeAliasesPackage("com.yulink.texas.core.domain");
        return sessionFactory.getObject();
    }
}
