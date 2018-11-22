package net.wifiprobe.wifiprobelog.business.base;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 配置数据源
 */
@Configuration
@MapperScan(basePackages = "net.wifiprobe.wifiprobelog.business.cctvdao", sqlSessionTemplateRef = "cctvSqlSessionTemplate")
public class DatabaseCCTVConfiguration {


    @Bean(name = "cctvDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.cctv")
    public DataSource setDataSource() {
        return new DruidDataSource();
    }

    @Bean(name = "cctvTransactionManager")
    public DataSourceTransactionManager setTransactionManager(@Qualifier("cctvDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "cctvSqlSessionFactory")
    public SqlSessionFactory setSqlSessionFactory(@Qualifier("cctvDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean(name = "cctvSqlSessionTemplate")
    public SqlSessionTemplate setSqlSessionTemplate(@Qualifier("cctvSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}