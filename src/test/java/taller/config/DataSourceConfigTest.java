package taller.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.jupiter.api.Test;

import taller.soporte.BaseDatosPrueba;

class DataSourceConfigTest {
    @Test
    void db_propertiesCargaTodosLosParametrosDelPoolConSusValores() {
        DataSource ds = DataSourceConfig.crear(DataSourceConfig.cargarPropiedades());

        assertEquals("SELECT 1", ds.getValidationQuery());
        assertEquals(2, ds.getInitialSize());
        assertEquals(2, ds.getMinIdle());
        assertEquals(5, ds.getMaxIdle());
        assertEquals(8, ds.getMaxActive());
        assertEquals(5000, ds.getMaxWait());
        assertTrue(ds.isTestOnBorrow());
        assertFalse(ds.isTestOnReturn());
        assertTrue(ds.isTestWhileIdle());
        assertTrue(ds.isTestOnConnect());
        assertEquals(30000, ds.getTimeBetweenEvictionRunsMillis());
        assertEquals(60000, ds.getMinEvictableIdleTimeMillis());
        assertTrue(ds.isRemoveAbandoned());
        assertEquals(60, ds.getRemoveAbandonedTimeout());
        assertEquals(30000, ds.getValidationInterval());
    }

    @Test
    void sinPropiedadesUsaLosValoresPorDefecto() {
        DataSource ds = DataSourceConfig.crear(new Properties());

        assertEquals(8, ds.getMaxActive());
        assertEquals("SELECT 1", ds.getValidationQuery());
        assertTrue(ds.isTestOnBorrow());
    }

    @Test
    void lasPropiedadesDelUsuarioSobreescribenLosDefectos() {
        Properties p = new Properties();
        p.setProperty("pool.maxActive", "3");
        p.setProperty("pool.testOnBorrow", "false");

        DataSource ds = DataSourceConfig.crear(p);

        assertEquals(3, ds.getMaxActive());
        assertFalse(ds.isTestOnBorrow());
    }

    @Test
    void inicializarLaBaseDeDatosEsIdempotenteYNoDuplicaMecanicos() throws Exception {
        DataSource ds = BaseDatosPrueba.crear();
        try {
            DataSourceConfig.inicializarBaseDeDatos(ds);

            try (Connection con = ds.getConnection();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM trabajador")) {
                rs.next();
                assertEquals(2, rs.getInt(1));
            }
        } finally {
            ds.close();
        }
    }

    @Test
    void separaElScriptEnSentenciasIgnorandoComentarios() {
        String script = "-- comentario\nCREATE TABLE a (x INT);\n\n-- otro\nINSERT INTO a VALUES (1);\n";

        List<String> sentencias = DataSourceConfig.sentencias(script);

        assertEquals(2, sentencias.size());
        assertEquals("CREATE TABLE a (x INT)", sentencias.get(0));
        assertEquals("INSERT INTO a VALUES (1)", sentencias.get(1));
    }
}
