package taller.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolProperties;

import taller.exception.PersistenciaException;

public final class DataSourceConfig {

    private DataSourceConfig() {
    }

    public static Properties cargarPropiedades() {
        Properties propiedades = new Properties();
        try (InputStream in = DataSourceConfig.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró db.properties");
            }
            propiedades.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer db.properties", e);
        }
        return propiedades;
    }

    public static org.apache.tomcat.jdbc.pool.DataSource crear() {
        return crear(cargarPropiedades());
    }

    public static org.apache.tomcat.jdbc.pool.DataSource crear(Properties p) {
        PoolProperties pool = new PoolProperties();

        pool.setUrl(p.getProperty("db.url", "jdbc:h2:file:./data/taller"));
        pool.setDriverClassName("org.h2.Driver");
        pool.setUsername(p.getProperty("db.username", "sa"));
        pool.setPassword(p.getProperty("db.password", ""));

        pool.setInitialSize(entero(p, "pool.initialSize", 2));
        pool.setMinIdle(entero(p, "pool.minIdle", 2));
        pool.setMaxIdle(entero(p, "pool.maxIdle", 5));
        pool.setMaxActive(entero(p, "pool.maxActive", 8));
        pool.setMaxWait(entero(p, "pool.maxWait", 5000));

        pool.setValidationQuery(p.getProperty("pool.validationQuery", "SELECT 1"));
        pool.setTestOnBorrow(booleano(p, "pool.testOnBorrow", true));
        pool.setTestOnReturn(booleano(p, "pool.testOnReturn", false));
        pool.setTestWhileIdle(booleano(p, "pool.testWhileIdle", true));
        pool.setTestOnConnect(booleano(p, "pool.testOnConnect", true));
        pool.setValidationInterval(entero(p, "pool.validationInterval", 30000));

        pool.setTimeBetweenEvictionRunsMillis(entero(p, "pool.timeBetweenEvictionRunsMillis", 30000));
        pool.setMinEvictableIdleTimeMillis(entero(p, "pool.minEvictableIdleTimeMillis", 60000));
        pool.setRemoveAbandoned(booleano(p, "pool.removeAbandoned", true));
        pool.setRemoveAbandonedTimeout(entero(p, "pool.removeAbandonedTimeout", 60));
        pool.setLogAbandoned(true);

        pool.setJmxEnabled(false);

        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setPoolProperties(pool);
        return dataSource;
    }

    public static void inicializarBaseDeDatos(DataSource dataSource) {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            for (String archivo : new String[] {"/database/schema.sql", "/database/data.sql"}) {
                for (String sentencia : sentencias(leerRecurso(archivo))) {
                    st.execute(sentencia);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo inicializar la base de datos", e);
        }
    }

    private static String leerRecurso(String ruta) {
        try (InputStream in = DataSourceConfig.class.getResourceAsStream(ruta)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + ruta);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + ruta, e);
        }
    }

    static List<String> sentencias(String script) {
        StringBuilder limpio = new StringBuilder();
        for (String linea : script.split("\\R")) {
            if (!linea.trim().startsWith("--")) {
                limpio.append(linea).append('\n');
            }
        }
        List<String> resultado = new ArrayList<>();
        for (String sentencia : limpio.toString().split(";")) {
            if (!sentencia.isBlank()) {
                resultado.add(sentencia.trim());
            }
        }
        return resultado;
    }

    private static int entero(Properties p, String clave, int porDefecto) {
        return Integer.parseInt(p.getProperty(clave, String.valueOf(porDefecto)).trim());
    }

    private static boolean booleano(Properties p, String clave, boolean porDefecto) {
        return Boolean.parseBoolean(p.getProperty(clave, String.valueOf(porDefecto)).trim());
    }
}
