//package yaycrawler.master;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.sql.DataSource;
//
//
//@Configuration
//public class DataSourceConfig {
//
//    @Value("${spring.datasource.driverClassName}")
//    private String driver;
//    @Value("${spring.datasource.url}")
//    private String url;
//    @Value("${spring.datasource.username}")
//    private String username;
//    @Value("${spring.datasource.password}")
//    private String password;
//    @Value("${spring.jpa.properties.hibernate.dialect}")
//    private String dialect;
//    @Value("${spring.jpa.hibernate.ddl-auto}")
//    private String hbm2ddlAuto;
//    @Value("${spring.jpa.show-sql}")
//    private Boolean showSql;
////
////    @Bean(name ="datasource")
////    public DataSource configureDataSource() {
////        HikariConfig config = new HikariConfig();
////        config.setDriverClassName(driver);
////        config.setJdbcUrl(url);
////        config.setUsername(username);
////        config.setPassword(password);
////
////        config.addDataSourceProperty("useUnicode", "true");
////        config.addDataSourceProperty("characterEncoding", "utf8");
////        config.addDataSourceProperty("cachePrepStmts", "true");
////        config.addDataSourceProperty("prepStmtCacheSize", "250");
////        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
////        config.addDataSourceProperty("useServerPrepStmts", "true");
////
////        return new HikariDataSource(config);
////    }
//}
