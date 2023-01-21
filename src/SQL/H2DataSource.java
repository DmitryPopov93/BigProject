package SQL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;


import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

//����� ��� ������ � ����� ���������� JDBC
public class H2DataSource extends ConnectionsH2 {
private static DataSource getDataSourceConnectionWithJNDI() throws NamingException, ClassNotFoundException {
        final String dataSourceName = "myDataSource2";
        String sp = "com.sun.jndi.fscontext.RefFSContextFactory";
        DataSource dataSource = getDataSourceConnection();
        Hashtable env = new Hashtable();
        env.put (Context.INITIAL_CONTEXT_FACTORY, sp);
        Context context = new InitialContext(env);
        context.rebind(dataSourceName , dataSource);
        context.unbind("myDataSource");
        return (DataSource) context.lookup(dataSourceName);
    }

    private static DataSource getDataSourceConnection() throws ClassNotFoundException, NamingException {

        // ����� ��� �������� � ������ JDBC, ��� Pool �� lib commons-dbcp-1.4.jar
        DriverAdapterCPDS driver = new DriverAdapterCPDS();
        // ��������� ��� ��������
        driver.setDriver(DB_Driver);
        driver.setUrl(DB_URL);
        driver.setUser(User_DB);
        driver.setPassword(Password_DB);

        // ������� ������ ����������� Pool ��� ����������
        SharedPoolDataSource sharedPoolDS = new SharedPoolDataSource();
        // ������ ������� � Pool
        sharedPoolDS.setConnectionPoolDataSource(driver);
        // ��������� Pool
        sharedPoolDS.setMaxActive(10);
        sharedPoolDS.setMaxWait(10);
        sharedPoolDS.setTestOnBorrow(true);
        // ���������� 1 � ������ ������
        sharedPoolDS.setValidationQuery("SELECT 1");
        // ���� ��� �������� � ���� ��� ��������� �������
        sharedPoolDS.setTestWhileIdle(true);

        return sharedPoolDS;
    }

    @Test
    //���� �������� ����������
    public void test() throws SQLException, ClassNotFoundException, NamingException {
        try(Connection connection=getDataSourceConnectionWithJNDI().getConnection()) {
            //� ������ ������ �������� 1
            assertTrue(connection.isValid(1),"True");
            //�������� �� �������� ����������
            assertFalse(connection.isClosed(),"False");
        }
    }
}
