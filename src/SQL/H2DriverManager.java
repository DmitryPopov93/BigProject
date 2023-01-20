package SQL;

import Polyclinic.Specialties;
import org.h2.jdbc.JdbcSQLSyntaxErrorException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

//Класс для работы без пула соединений JDBC
public class H2DriverManager extends ConnectionsH2 {

    private static Connection getDriverManagerConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, User_DB, Password_DB);
    }


    //Вставляем данные в таблицу
    private static void insertDataClients(String fullName, String problemInfo) throws SQLException {

        final String customerEntryQuery = "Insert into " +
                "CLIENTS (FULL_NAME , PROBLEM_INFO) " +
                "values (?, ?)";
        try (PreparedStatement preparedStatement = getDriverManagerConnection().prepareStatement(customerEntryQuery)) {
            preparedStatement.setString(1, fullName);
            preparedStatement.setString(2, problemInfo);
            preparedStatement.execute();
            System.out.println("Add client: result " + preparedStatement.getUpdateCount());

            //getUpdateCount() - Извлекает текущий результат как количество обновлений; если результатом является
            // ResultSet объект или результатов больше нет, возвращается -1.

            //getMoreResults() - Переходит к Statement следующему результату этого объекта, возвращает значение,
            // true если это ResultSet объект, и неявно закрывает все текущие ResultSet объекты, полученные методом getResultSet.

            //getResultSet() - Извлекает текущий результат как ResultSet объект.
        }
    }

    private static void insertSpecialties(String specialties) throws SQLException {

        final String customerEntryQuery = "Insert into SPECIALTIES " +
                "(SPECIALITY)" +
                "values" +
                "(?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnection().prepareStatement(customerEntryQuery)) {
            preparedStatement.setString(1, specialties);
            System.out.println("Add Specialties: " + preparedStatement.executeUpdate());
        }
    }

    // Метод добавляющий несколько специальностей разом, через Batching запросов
    private static void insertBatchSpecialties(Specialties... specialties) throws SQLException {

        final String customerEntryQuery = "Insert into SPECIALTIES " +
                "(SPECIALITY)" +
                "values" +
                "(?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnection().prepareStatement(customerEntryQuery)) {
            for (int i = 0; i < specialties.length; i++) {
                preparedStatement.setString(1, specialties[i].toString());
                preparedStatement.addBatch();
                // Запрос не выполняется, а укладывается в буфер,
                //  который потом выполняется сразу для всех команд
            }
            // Выполняем все запросы разом
            int[] results = preparedStatement.executeBatch();

            for (int i = 0; i < results.length; i++) {
                System.out.println("Add Specialties: " + results[i]);
            }
        }
    }

    private static void insertDoctors(String doctor, int speciality) throws SQLException {

        final String customerEntryQuery = "Insert into doctors" +
                "(FULL_NAME , SPECIALITY )" +
                "values" +
                "(?, ?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnection().prepareStatement(customerEntryQuery)) {
            preparedStatement.setString(1, doctor);
            preparedStatement.setInt(2, speciality);
            System.out.println("Add Doctor: " + preparedStatement.executeUpdate());
        }
    }


    // Этот метод сделан через RowSet, метод сначала делает Select и добавляет затем добавляет строку,
    // будем использовать этот метод для перезаписи
    private static void insertDoctorsRowSet(int iD, String doctor, int speciality) throws SQLException {
        // Точка с запятой ; в селекте приведет к ошибке.
        final String customerEntryQuery = "SELECT DOCTORS.ID, DOCTORS.FULL_NAME, DOCTORS.SPECIALITY FROM DOCTORS";

    //        JdbcRowSet rowSet = new JdbcRowSetImpl(connection); Один из способов, с готовым соединением,
    //        не смог найти библиотеку

        try ( CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet()) {

            rowSet.setUrl(DB_URL);
            rowSet.setUsername(User_DB);
            rowSet.setPassword(Password_DB);
            rowSet.setCommand(customerEntryQuery);
            rowSet.execute();

            // Этот тип операции может изменить только автономный RowSet
            // Сначала перемещаем указатель на пустую (новую) строку, текущая позиция запоминается
            rowSet.moveToInsertRow();
            rowSet.updateInt(1, iD);
            rowSet.updateString(2, doctor);
            rowSet.updateInt(3, speciality);
            rowSet.insertRow();  // Добавляем текущую (новую) строку к остальные строкам
            rowSet.moveToCurrentRow(); // Возвращаем указатель на ту строку, где он был до вставки

            rowSet.beforeFirst(); //Перемещает курсор к началу этого ResultSet объекта, непосредственно перед первой строкой.
            while(rowSet.next()) {
                System.out.println(rowSet.getInt(1) + " ," + rowSet.getString(2) + " ," + rowSet.getInt(3));
            }

            // А теперь мы можем все наши изменения залить в базу
            Connection con = getDriverManagerConnection();
            con.setAutoCommit(false); // Нужно для синхронизации
            rowSet.acceptChanges(con);// Синхронизировать данные в базу данных
        }
    }

    private static void insertTimeDoctor(int doctor, int dayReceipt, LocalTime startReception, LocalTime endReception) throws SQLException {

        final String customerEntryQuery = "Insert into Time_Doctors " +
                "values" +
                "(?,?, ?,?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnection().prepareStatement(customerEntryQuery)) {
            preparedStatement.setInt(1, doctor);
            preparedStatement.setInt(2, dayReceipt);
            preparedStatement.setTime(3, Time.valueOf(startReception));
            preparedStatement.setTime(4, Time.valueOf(endReception));
            preparedStatement.execute();
            System.out.println("Add TimeDoctor: " + preparedStatement.getUpdateCount());
        }
    }

    private static void insertClientReception(int doctor, LocalDate dateReception, LocalTime timeReception, int client) throws SQLException {

        final String customerEntryQuery = "Insert into CLIENTS_RECEPTION " +
                "values" +
                "(?,?,?,?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnection().prepareStatement(customerEntryQuery)) {
            preparedStatement.setInt(1, doctor);
            preparedStatement.setDate(2, Date.valueOf(dateReception));
            preparedStatement.setTime(3, Time.valueOf(timeReception));
            preparedStatement.setInt(4, client);
            System.out.println("Add ClientReception: " + preparedStatement.executeUpdate());
        }
    }

    private static void insertTimeReception(int doctor, LocalDate dateReception, LocalTime timeReception, int client) throws SQLException {

        final String customerEntryQuery = "Insert into TIME_RECEPTION" +
                "(DOCTOR, DATE_RECEPTION, \"" + timeReception + "\")" +
                "values" +
                "(?,?,?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnection().prepareStatement(customerEntryQuery)) {
            preparedStatement.setInt(1, doctor);
            preparedStatement.setDate(2, Date.valueOf(dateReception));
            preparedStatement.setInt(3, client);
            System.out.println("Add TimeReception: " + preparedStatement.executeUpdate());
        }
    }

    //TODO Написать метод, на вход приходит стринга с ФИО клиента, нужно получить на выходе результсет(нераспакованный)
    //TODO Содержащий ФИО, Описание проблемы, ФИО врача, Специальность врача, дату, время.

    /**
     * Примеры работы с RowSet
     **/

    private static void selectDoctorStatistics(Integer doctor) throws SQLException {

        final String customerEntryQuery = "SELECT DOCTORS.FULL_NAME, SPECIALTIES.SPECIALITY, CLIENTS_RECEPTION.DATE_RECEPTION, CLIENTS.FULL_NAME, CLIENTS.PROBLEM_INFO  \n" +
                "FROM\n" +
                "DOCTORS, SPECIALTIES, CLIENTS_RECEPTION, CLIENTS\n" +
                "WHERE\n" +
                "DOCTORS.SPECIALITY = SPECIALTIES.ID\n" +
                "AND CLIENTS_RECEPTION.DOCTOR = DOCTORS.ID \n" +
                "AND CLIENTS.ID = CLIENTS_RECEPTION.CLIENT \n" +
                "AND DOCTORS.ID = ?;";

        RowSetFactory factory = RowSetProvider.newFactory();
        //Например, CachedRowSet. Он является "отключённым"
        // (то есть не использует постоянное подключение к БД) и требует явного выполнения синхронизации с БД:
        CachedRowSet cachedRowSet = factory.createCachedRowSet();

        try (PreparedStatement preparedStatement = getDriverManagerConnection().prepareStatement(customerEntryQuery)) {
            preparedStatement.setInt(1, doctor);
            ResultSet resultSet = preparedStatement.executeQuery();

            cachedRowSet.populate(resultSet);
            // Закрываем соединение
        }
        // Данные из кэша все еще доступны
        while (cachedRowSet.next()) {
            System.out.println(cachedRowSet.getString(1) + "\t" + cachedRowSet.getString(2) + "\t" +
                    cachedRowSet.getDate(3).toLocalDate() + "\t" + cachedRowSet.getString(4) + "\t" +
                    cachedRowSet.getString(5));
        }
    }

    // Update TIME_RECEPTION
    private static void updateTimeReception(int doctor, LocalDate dateReception, LocalTime timeReception, int client) throws SQLException {
        final String customerEntryQuery = "UPDATE TIME_RECEPTION \n" +
                "SET\n" +
                "\"" + timeReception + "\" = ?\n" +
                "WHERE TIME_RECEPTION.DATE_RECEPTION = ? AND TIME_RECEPTION.DOCTOR =?;";

        try (PreparedStatement preparedStatement = getDriverManagerConnection().prepareStatement(customerEntryQuery)) {
            preparedStatement.setInt(1, client);
            preparedStatement.setDate(2, Date.valueOf(dateReception));
            preparedStatement.setInt(3, doctor);
            System.out.println("Update TIME_RECEPTION: " + preparedStatement.executeUpdate());
        }
    }

    // Запрос к рассписанию доктора по специальности c именем доктора
    private static void selectTimeDoctor(Specialties specialties) throws SQLException {

        final String customerEntryQuery = "SELECT DOCTORS.FULL_NAME, SPECIALTIES.SPECIALITY,  TIME_DOCTORS.DAY_RECEIPT,\n" +
                "TIME_DOCTORS.START_RECEPTION, TIME_DOCTORS.END_RECEPTION\n" +
                "FROM DOCTORS join TIME_DOCTORS on DOCTORS.ID=TIME_DOCTORS.DOCTOR\n" +
                "join SPECIALTIES on DOCTORS.SPECIALITY=SPECIALTIES.ID \n" +
                "where\n" +
                "SPECIALTIES.SPECIALITY  =?;";
        try (PreparedStatement preparedStatement = getDriverManagerConnection().prepareStatement(customerEntryQuery)) {
            preparedStatement.setString(1, specialties.toString());
            if (preparedStatement.execute()) {
                ResultSet resultSet = preparedStatement.getResultSet();
// Узнаем о нашей таблице с помощью ResultSetMetaData
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int column = 1; column <= columnCount; column++) {
                    String name = metaData.getColumnName(column);
                    String className = metaData.getColumnClassName(column);
                    String typeName = metaData.getColumnTypeName(column);
                    int type = metaData.getColumnType(column);

                    System.out.println(name + "\t" + className + "\t" + typeName + "\t" + type);
                }
// Обработаем наш ResultSet

                // Получает, находится ли курсор перед первой строкой
                System.out.println("\n" + resultSet.isBeforeFirst());

                while (resultSet.next()) {
                    String nameDoctor = resultSet.getString(1);
                    String speciality = resultSet.getString(2);
                    Integer dayReceipt = resultSet.getInt("DAY_RECEIPT");
                    Time startReception = resultSet.getTime(4);
                    Time endReception = resultSet.getTime(5);
                    System.out.println("\n" + resultSet.getRow() + ". " + nameDoctor + "\t" + speciality + "\t\t" + dayReceipt
                            + "\t" + startReception + "\t" + endReception);
                }
            }
        }
    }

    // Запрос к времени приема доктора по дате и специальности.
    private static void selectTimeReception(Specialties specialties, LocalDate dateReception) throws SQLException {

        final String customerEntryQuery = "SELECT SPECIALTIES.SPECIALITY, DOCTORS.FULL_NAME, TIME_RECEPTION.*   \n" +
                "FROM SPECIALTIES JOIN DOCTORS ON SPECIALTIES.ID = DOCTORS.SPECIALITY \n" +
                "JOIN TIME_RECEPTION ON TIME_RECEPTION.DOCTOR = SPECIALTIES.ID\n" +
                "WHERE\n" +
                "SPECIALTIES.SPECIALITY  =? AND\n" +
                "TIME_RECEPTION.DATE_RECEPTION = ?;";

        try (PreparedStatement preparedStatement = getDriverManagerConnection().prepareStatement(customerEntryQuery)) {
            preparedStatement.setString(1, specialties.toString());
            preparedStatement.setDate(2, Date.valueOf(dateReception));
            if (preparedStatement.execute()) {
                ResultSet resultSet = preparedStatement.getResultSet();

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int column = 1; column <= columnCount; column++) {
                    String name = metaData.getColumnName(column);
                    System.out.print(name + "\t");
                }

                while (resultSet.next()) {
                    String speciality = resultSet.getString(1);
                    String nameDoctor = resultSet.getString(2);
                    Integer doctor = resultSet.getInt(3);
                    Date tableDateReception = resultSet.getDate(4);
                    Integer time_9_00 = resultSet.getInt(5);
                    Integer time_9_30 = resultSet.getInt(6);
                    Integer time_10_00 = resultSet.getInt(7);
                    Integer time_10_30 = resultSet.getInt(8);
                    Integer time_11_00 = resultSet.getInt(9);
                    Integer time_11_30 = resultSet.getInt(10);
                    Integer time_12_00 = resultSet.getInt(11);
                    Integer time_12_30 = resultSet.getInt(12);
                    Integer time_14_00 = resultSet.getInt(13);
                    Integer time_14_30 = resultSet.getInt(14);
                    Integer time_15_00 = resultSet.getInt(15);
                    Integer time_15_30 = resultSet.getInt(16);
                    Integer time_16_00 = resultSet.getInt(17);
                    Integer time_16_30 = resultSet.getInt(18);
                    Integer time_17_00 = resultSet.getInt(19);
                    Integer time_17_30 = resultSet.getInt(20);

                    System.out.println("\n" + speciality + "\t" + nameDoctor + "\t" + doctor + "\t" + tableDateReception + "\t" + "\t" + time_9_00 + "\t" + time_9_30 + "\t" + time_10_00 + "\t"
                            + time_10_30 + "\t" + time_11_00 + "\t" + time_11_30 + "\t" + time_12_00 + "\t" + time_12_30 + "\t" + time_14_00 + "\t"
                            + time_14_30 + "\t" + time_15_00 + "\t" + time_15_30 + "\t" + time_16_00 + "\t" + time_16_30 + "\t" + time_17_00
                            + "\t" + time_17_30);
                }
            }
        }
    }

    // Запись клиента к доктору
    private static int makeAppointment(int doctor, LocalDate dateReception, LocalTime timeReception, int client) throws SQLException {

        final String customerEntryQuery1 = "SELECT TIME_DOCTORS.DAY_RECEIPT, TIME_DOCTORS.START_RECEPTION, TIME_DOCTORS.END_RECEPTION  FROM TIME_DOCTORS\n" +
                "WHERE TIME_DOCTORS.DOCTOR=?;";

        final String customerEntryQuery2 = "SELECT \"" + timeReception + "\" FROM TIME_RECEPTION\n" +
                "WHERE TIME_RECEPTION.DATE_RECEPTION=? AND TIME_RECEPTION.DOCTOR=?;";


        try (Connection connection = getDriverManagerConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(customerEntryQuery1);
            preparedStatement.setInt(1, doctor);
            ResultSet resultSet = preparedStatement.executeQuery();

            boolean trueTimeReception = false;

            while (resultSet.next()) {
                if (dateReception.getDayOfWeek().getValue() == resultSet.getInt(1) &&
                        !timeReception.isAfter(resultSet.getTime(3).toLocalTime().minusMinutes(1)) &&
                        !timeReception.isBefore(resultSet.getTime(2).toLocalTime())) {
                    trueTimeReception = true;
                }
            }

            if (trueTimeReception) {
                preparedStatement = connection.prepareStatement(customerEntryQuery2);
                preparedStatement.setDate(1, Date.valueOf(dateReception));
                preparedStatement.setInt(2, doctor);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.isBeforeFirst()) {
                    trueTimeReception = false;
                    resultSet.next();
                } else trueTimeReception = true;

                if (trueTimeReception) {
                    insertTimeReception(doctor, dateReception, timeReception, client);
                    insertClientReception(doctor, dateReception, timeReception, client);
                    preparedStatement.close();
                    System.out.println("Мы у доктрора в этот день первые");
                    return 1;
                } else if (resultSet.getInt(1) == 0) {
                    updateTimeReception(doctor, dateReception, timeReception, client);
                    insertClientReception(doctor, dateReception, timeReception, client);
                    preparedStatement.close();
                    System.out.println("В этот день были и другие на прием");
                    return 2;
                } else {
                    preparedStatement.close();
                    System.out.println("Это время уже занято");
                    return 3;
                }
            }

            preparedStatement.close();
        }
        System.out.println("Доктор в это время не работает");
        return 0;
    }


    //Метод создания таблиц
    private static void createAllTables(boolean sw) throws SQLException {
        if (sw) {
            try (Statement statement = getDriverManagerConnection().createStatement()) {
                //true, если инструкция возвращает результирующий набор. false, если инструкция возвращает число обновлений или не возвращает результата.
                try {
                    System.out.println("Create Clients result: " + statement.execute(create_Table_Clients) + " " + statement.getUpdateCount());
                } catch (JdbcSQLSyntaxErrorException e) {
                    System.out.println("Do not create Clients");
                } finally {
                    try {
                        System.out.println("Create Specialties result: " + statement.execute(create_Table_Specialties) + " " + statement.getUpdateCount());
                    } catch (JdbcSQLSyntaxErrorException e) {
                        System.out.println("Do not create Specialties");
                    } finally {
                        try {
                            System.out.println("Create Doctors result: " + statement.execute(create_Table_Doctors) + " " + statement.getUpdateCount());
                        } catch (JdbcSQLSyntaxErrorException e) {
                            System.out.println("Do not create Doctors");
                        } finally {
                            try {
                                System.out.println("Create Time_Doctors result: " + statement.execute(create_Table_Time_Doctors) + " " + statement.getUpdateCount());
                            } catch (JdbcSQLSyntaxErrorException e) {
                                System.out.println("Do not create Time_Doctors");
                            } finally {
                                try {
                                    //Значение типа int, указывающее либо число обработанных строк, либо значение 0 для инструкций языка DDL. Update/Insert/Delete
                                    //executeQuery() – для чтения данных из СУБД. При использовании ошибка
                                    System.out.println("Create Clients_Reception result: " + statement.executeUpdate(create_Table_Clients_Reception));
                                } catch (JdbcSQLSyntaxErrorException e) {
                                    System.out.println("Do not create Clients_Reception");
                                } finally {
                                    try {
                                        System.out.println("Create Time_Reception result: " + statement.executeUpdate(create_Table_Time_Reception));
                                    } catch (JdbcSQLSyntaxErrorException e) {
                                        System.out.println("Do not create Time_Reception");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void dropAllTables(boolean sw) throws SQLException {
        if (sw) {
            try (Statement statement = getDriverManagerConnection().createStatement()) {
                try {
                    System.out.println("Drop Time_Reception result: " + statement.execute(drop_Table_Time_Reception) + " " + statement.getUpdateCount());
                } catch (JdbcSQLSyntaxErrorException e) {
                    System.out.println("Do not drop Time_Reception");
                } finally {
                    try {
                        System.out.println("Drop Clients_Reception result: " + statement.execute(drop_Table_Clients_Reception));
                    } catch (JdbcSQLSyntaxErrorException e) {
                        System.out.println("Do not drop Clients_Reception");
                    } finally {
                        try {
                            System.out.println("Drop Time_Doctors result: " + statement.execute(drop_Table_Time_Doctors));
                        } catch (JdbcSQLSyntaxErrorException e) {
                            System.out.println("Do not drop Time_Doctors");
                        } finally {
                            try {
                                System.out.println("Drop Table_Doctors result: " + statement.execute(drop_Table_Doctors));
                            } catch (JdbcSQLSyntaxErrorException e) {
                                System.out.println("Do not drop Doctors");
                            } finally {
                                try {
                                    System.out.println("Drop Table_Specialties result: " + statement.executeUpdate(drop_Table_Specialties));
                                } catch (JdbcSQLSyntaxErrorException e) {
                                    System.out.println("Do not drop Specialties");
                                } finally {
                                    try {
                                        System.out.println("Drop Table_Clients result: " + statement.executeUpdate(drop_Table_Clients));
                                    } catch (JdbcSQLSyntaxErrorException e) {
                                        System.out.println("Do not drop Clients");
                                    }
                                }
                            }
                        }
                    }
                }


            }
        }
    }

    private static void insertData(boolean sw) throws SQLException {
        if(sw) {
            insertBatchSpecialties(Specialties.OTOLARYNGOLOGIST, Specialties.TRAUMATOLOGIST, Specialties.OCULIST, Specialties.SURGEON);
            insertDataClients("Petrov Nikolay Vasilyevich", "Hand x-ray");
            insertSpecialties("Roentgenologist");
            insertDoctorsRowSet(5,"Petrov Semyonovich Semenov", 4);
            insertTimeDoctor(1, 1, LocalTime.of(9, 0), LocalTime.of(18, 0));
            insertClientReception(1,LocalDate.of(2023,12,1),LocalTime.of(9,0), 1);
            insertTimeReception(1, LocalDate.of(2023,12,1),LocalTime.of(10,0),1);
        }

    }

    public static void main(String[] args) throws SQLException {
//        selectDoctorStatistics(1);
        createAllTables(false);
        dropAllTables(false);
        insertData(true);
//        System.out.println(makeAppointment(1,LocalDate.of(2023,12,1),LocalTime.of(10, 30),33));
        updateTimeReception(1, LocalDate.of(2023,12,1), LocalTime.of(10, 30),1);

    }
}
