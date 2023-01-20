package SQL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;


import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

//Класс для работы с пулом соединений JDBC
public class H2DataSource extends ConnectionsH2 {
    private static DataSource getDataSourceConnection() throws ClassNotFoundException, NamingException {
        //https://javarush.com/groups/posts/2650-ispoljhzovanie-jndi-v-java
        //https://se.ifmo.ru/~ad/Education_Information/Comp_Based_Inf_Systems/Practic_5/DataSource.html
        //https://coderlessons.com/articles/java/sozdanie-resursov-jndi-dlia-testirovaniia-junit-s-ispolzovaniem-spring
        //получить соединение через jndi
        //Context context = new InitialContext();
        //DataSource dataSource = (DataSource) context.lookup(DB_URL);

        //TODO: почему именно такой класс драйвера?

        // Драйвер от Н2 к нашему JDBC, для Pool из lib commons-dbcp-1.4.jar
        DriverAdapterCPDS driver = new DriverAdapterCPDS();
        // Настройки для драйвера
        driver.setDriver(DB_Driver);
        driver.setUrl(DB_URL);
        driver.setUser(User_DB);
        driver.setPassword(Password_DB);

        // Создаем объект управляющий Pool для соединений
        SharedPoolDataSource sharedPoolDS = new SharedPoolDataSource();
        // Дружим драйвер с Pool
        sharedPoolDS.setConnectionPoolDataSource(driver);
        // Настройка Pool
        sharedPoolDS.setMaxActive(10);
        sharedPoolDS.setMaxWait(10);
        sharedPoolDS.setTestOnBorrow(true);
        // Возвращает 1 в случае успеха
        sharedPoolDS.setValidationQuery("SELECT 1");
        // Пока нет запросов к базе она проверяет коннект
        sharedPoolDS.setTestWhileIdle(true);

        return sharedPoolDS;
    }

    @Test
    //Тест проверки соединения
    public void test() throws SQLException, ClassNotFoundException, NamingException {
        try(Connection connection=getDataSourceConnection().getConnection()) {
            //В случае успеха приходит 1
            assertTrue(connection.isValid(1),"True");
            //Проверка на закрытое соединение
            assertFalse(connection.isClosed(),"False");
        }
    }
}
