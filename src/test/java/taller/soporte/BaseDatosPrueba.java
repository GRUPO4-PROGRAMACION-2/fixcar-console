package taller.soporte;

import java.util.Properties;
import java.util.UUID;

import org.apache.tomcat.jdbc.pool.DataSource;

import taller.config.DataSourceConfig;

public final class BaseDatosPrueba {
    private BaseDatosPrueba() {
    }

    public static DataSource crear() {
        Properties p = new Properties();
        p.setProperty("db.url", "jdbc:h2:mem:test" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        p.setProperty("pool.initialSize", "1");
        p.setProperty("pool.minIdle", "1");
        p.setProperty("pool.maxActive", "10");
        DataSource dataSource = DataSourceConfig.crear(p);
        DataSourceConfig.inicializarBaseDeDatos(dataSource);
        return dataSource;
    }
}
