package SQL;

import Polyclinic.Specialties;
import org.h2.jdbc.JdbcSQLSyntaxErrorException;
import org.postgresql.util.PSQLException;

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

//����� ��� ������ ��� ���� ���������� JDBC
public class H2DriverManager extends ConnectionsH2 {

    private static Connection getDriverManagerConnectionPostgres() throws SQLException, ClassNotFoundException {
        return DriverManager.getConnection(DB_URL, User_DB, Password_DB);
    }


    //��������� ������ � �������


    private static void insertDataClients(String fullName, String problemInfo) throws SQLException, ClassNotFoundException {

        Connection connection = null;

        final String customerEntryQuery = "Insert into " +
                "CLIENTS (FULL_NAME , PROBLEM_INFO) " +
                "values ('Dima', 'healh');";
        try  {
            connection = getDriverManagerConnectionPostgres();
            connection.setAutoCommit(false);
//            connection.beginRequest();
            try (Statement preparedStatement = connection.createStatement()) {
                preparedStatement.execute(customerEntryQuery);
                connection.commit();
                connection.setAutoCommit(true);
                //getUpdateCount() - ��������� ������� ��������� ��� ���������� ����������; ���� ����������� ��������
                // ResultSet ������ ��� ����������� ������ ���, ������������ -1.

                //getMoreResults() - ��������� � Statement ���������� ���������� ����� �������, ���������� ��������,
                // true ���� ��� ResultSet ������, � ������ ��������� ��� ������� ResultSet �������, ���������� ������� getResultSet.

                //getResultSet() - ��������� ������� ��������� ��� ResultSet ������.
            }
            final String customerEntryQueryError = "Insert into " +
                    "CLIENTS (FULL_NAME , PROBLEM_INFO) " +
                    "values ('Dima', 'healh');";
            try (Statement preparedStatement = connection.createStatement()) {
                preparedStatement.execute(customerEntryQueryError);
                System.out.println("Add client: result " + preparedStatement.getUpdateCount());

                //getUpdateCount() - ��������� ������� ��������� ��� ���������� ����������; ���� ����������� ��������
                // ResultSet ������ ��� ����������� ������ ���, ������������ -1.

                //getMoreResults() - ��������� � Statement ���������� ���������� ����� �������, ���������� ��������,
                // true ���� ��� ResultSet ������, � ������ ��������� ��� ������� ResultSet �������, ���������� ������� getResultSet.

                //getResultSet() - ��������� ������� ��������� ��� ResultSet ������.

            }
            connection.rollback();
            connection.close();
        } catch (Throwable e) {
            assert connection != null;
            connection.rollback();
            connection.close();

        }

    }

    private static void insertSpecialties(String specialties) throws SQLException, ClassNotFoundException {

        final String customerEntryQuery = "Insert into SPECIALTIES " +
                "(SPECIALITY)" +
                "values" +
                "(?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(customerEntryQuery)) {
            preparedStatement.setString(1, specialties);
            System.out.println("Add Specialties: " + preparedStatement.executeUpdate());
        }
    }

    // ����� ����������� ��������� �������������� �����, ����� Batching ��������
    private static void insertBatchSpecialties(Specialties... specialties) throws SQLException, ClassNotFoundException {

        final String customerEntryQuery = "Insert into SPECIALTIES " +
                "(SPECIALITY)" +
                "values" +
                "(?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(customerEntryQuery)) {
            for (int i = 0; i < specialties.length; i++) {
                preparedStatement.setString(1, specialties[i].toString());
                preparedStatement.addBatch();
                // ������ �� �����������, � ������������ � �����,
                //  ������� ����� ����������� ����� ��� ���� ������
            }
            // ��������� ��� ������� �����
            int[] results = preparedStatement.executeBatch();

            for (int i = 0; i < results.length; i++) {
                System.out.println("Add Specialties: " + results[i]);
            }
        }
    }

    private static void insertDoctors(String doctor, int speciality) throws SQLException, ClassNotFoundException {

        final String customerEntryQuery = "Insert into doctors" +
                "(FULL_NAME , SPECIALITY )" +
                "values" +
                "(?, ?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(customerEntryQuery)) {
            preparedStatement.setString(1, doctor);
            preparedStatement.setInt(2, speciality);
            System.out.println("Add Doctor: " + preparedStatement.executeUpdate());
        }
    }


    // ���� ����� ������� ����� RowSet, ����� ������� ������ Select � ��������� ����� ��������� ������,
    // ����� ������������� ���� ����� ��� ����������
    private static void insertDoctorsRowSet(int iD, String doctor, int speciality) throws SQLException, ClassNotFoundException {
        // ����� � ������� ; � ������� �������� � ������.
        final String customerEntryQuery = "SELECT DOCTORS.ID, DOCTORS.FULL_NAME, DOCTORS.SPECIALITY FROM DOCTORS";

        //        JdbcRowSet rowSet = new JdbcRowSetImpl(connection); ���� �� ��������, � ������� �����������,
        //        �� ���� ����� ����������

        try ( CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet()) {

            rowSet.setUrl(DB_URL);
            rowSet.setUsername(User_DB);
            rowSet.setPassword(Password_DB);
            rowSet.setCommand(customerEntryQuery);
            rowSet.execute();

            while(rowSet.next()) {
                System.out.println(rowSet.getInt(1) + " ," + rowSet.getString(2) + " ," + rowSet.getInt(3));
            }
            rowSet.beforeFirst();
            System.out.println("-----------------------------------");

            // ���� ��� �������� ����� �������� ������ ���������� RowSet
            // ������� ���������� ��������� �� ������ (�����) ������, ������� ������� ������������
            rowSet.absolute(3); //���������� �� 3� ������
            rowSet.moveToInsertRow(); // ���������� ������ �� ����������� ������. ������, ���� �� ���� ����� 1� �������,
            // �� �� ������� ����������� ������ ����� 1�, � ��� ������ ���������� ����� ����� ��� ������ ������ 1�,
            // �� ��� ������ ������ � �����, ���� �� �����, � ����� ���������� �������� � ����� �� �����, �� �����
            // ������������ �������� �� ����, ��������� � ����� ������������ ���, ��� ��� ����
            rowSet.moveToInsertRow();
            rowSet.updateInt(1, iD);
            rowSet.updateString(2, doctor);
            rowSet.updateInt(3, speciality);
            rowSet.insertRow();  // ��������� ������� (�����) ������ � ��������� �������
            rowSet.moveToCurrentRow(); // ���������� ��������� �� �� ������, ��� �� ��� �� �������

            rowSet.beforeFirst(); //���������� ������ � ������ ����� ResultSet �������, ��������������� ����� ������ �������.
            while(rowSet.next()) {
                System.out.println(rowSet.getInt(1) + " ," + rowSet.getString(2) + " ," + rowSet.getInt(3));
            }

            // � ������ �� ����� ��� ���� ��������� ������ � ����
            Connection con = getDriverManagerConnectionPostgres();
            con.setAutoCommit(false); // ����� ��� �������������
            rowSet.acceptChanges(con);// ���������������� ������ � ���� ������
        }
    }

    private static void insertTimeDoctor(int doctor, int dayReceipt, LocalTime startReception, LocalTime endReception) throws SQLException, ClassNotFoundException {

        final String customerEntryQuery = "Insert into Time_Doctors " +
                "values" +
                "(?,?, ?,?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(customerEntryQuery)) {
            preparedStatement.setInt(1, doctor);
            preparedStatement.setInt(2, dayReceipt);
            preparedStatement.setTime(3, Time.valueOf(startReception));
            preparedStatement.setTime(4, Time.valueOf(endReception));
            preparedStatement.execute();
            System.out.println("Add TimeDoctor: " + preparedStatement.getUpdateCount());
        }
    }

    private static void insertClientReception(int doctor, LocalDate dateReception, LocalTime timeReception, int client) throws SQLException, ClassNotFoundException {

        final String customerEntryQuery = "Insert into CLIENTS_RECEPTION " +
                "values" +
                "(?,?,?,?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(customerEntryQuery)) {
            preparedStatement.setInt(1, doctor);
            preparedStatement.setDate(2, Date.valueOf(dateReception));
            preparedStatement.setTime(3, Time.valueOf(timeReception));
            preparedStatement.setInt(4, client);
            System.out.println("Add ClientReception: " + preparedStatement.executeUpdate());
        }
    }

    private static void insertTimeReception(int doctor, LocalDate dateReception, LocalTime timeReception, int client) throws SQLException, ClassNotFoundException {

        final String customerEntryQuery = "Insert into TIME_RECEPTION" +
                "(DOCTOR, DATE_RECEPTION, \"" + timeReception + "\")" +
                "values" +
                "(?,?,?);";
        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(customerEntryQuery)) {
            preparedStatement.setInt(1, doctor);
            preparedStatement.setDate(2, Date.valueOf(dateReception));
            preparedStatement.setInt(3, client);
            System.out.println("Add TimeReception: " + preparedStatement.executeUpdate());
        }
    }

    //����� ��������� �� �� �������� �������
    private static void insertDataClientsUseProcedure(String nameClient, String problemInfo) throws SQLException, ClassNotFoundException {
        try (CallableStatement callableStatement = getDriverManagerConnectionPostgres().prepareCall("CALL public.addclient (?,?,?)")) {
            callableStatement.setString(1, nameClient);
            callableStatement.setString(2, problemInfo);
            callableStatement.registerOutParameter(3, Types.INTEGER);
            callableStatement.execute();
            System.out.println("result insertDataClientsUseProcedure : " + callableStatement.getInt(3));

        }
    }


    //����� ������� �� �� ������� ���������� �������� � ��������
    private static void selectCountDoctorsFunction() throws SQLException, ClassNotFoundException {
        try (CallableStatement callableStatement = getDriverManagerConnectionPostgres().prepareCall("{CALL public.countDoctors (?)}")) {
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.execute();
            System.out.println("result selectCountDoctorsFunction : " + callableStatement.getInt(1));
        }
    }


    //TODO �������� �����, �� ���� �������� ������� � ��� �������, ����� �������� �� ������ ����������(���������������)
    //TODO ���������� ���, �������� ��������, ��� �����, ������������� �����, ����, �����.

    /**
     * ������� ������ � RowSet
     **/

    private static void selectDoctorStatistics(Integer doctor) throws SQLException, ClassNotFoundException {

        final String customerEntryQuery = "SELECT DOCTORS.FULL_NAME, SPECIALTIES.SPECIALITY, CLIENTS_RECEPTION.DATE_RECEPTION, CLIENTS.FULL_NAME, CLIENTS.PROBLEM_INFO  \n" +
                "FROM\n" +
                "DOCTORS, SPECIALTIES, CLIENTS_RECEPTION, CLIENTS\n" +
                "WHERE\n" +
                "DOCTORS.SPECIALITY = SPECIALTIES.ID\n" +
                "AND CLIENTS_RECEPTION.DOCTOR = DOCTORS.ID \n" +
                "AND CLIENTS.ID = CLIENTS_RECEPTION.CLIENT \n" +
                "AND DOCTORS.ID = ?;";

        RowSetFactory factory = RowSetProvider.newFactory();
        //��������, CachedRowSet. �� �������� "�����������"
        // (�� ���� �� ���������� ���������� ����������� � ��) � ������� ������ ���������� ������������� � ��:
        CachedRowSet cachedRowSet = factory.createCachedRowSet();

        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(customerEntryQuery)) {
            preparedStatement.setInt(1, doctor);
            ResultSet resultSet = preparedStatement.executeQuery();

            cachedRowSet.populate(resultSet);
            // ��������� ����������
        }
        // ������ �� ���� ��� ��� ��������
        while (cachedRowSet.next()) {
            System.out.println(cachedRowSet.getString(1) + "\t" + cachedRowSet.getString(2) + "\t" +
                    cachedRowSet.getDate(3).toLocalDate() + "\t" + cachedRowSet.getString(4) + "\t" +
                    cachedRowSet.getString(5));
        }
    }

    // Update TIME_RECEPTION
    private static void updateTimeReception(int doctor, LocalDate dateReception, LocalTime timeReception, int client) throws SQLException, ClassNotFoundException {
        final String customerEntryQuery = "UPDATE TIME_RECEPTION \n" +
                "SET\n" +
                "\"" + timeReception + "\" = ?\n" +
                "WHERE TIME_RECEPTION.DATE_RECEPTION = ? AND TIME_RECEPTION.DOCTOR =?;";

        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(customerEntryQuery)) {
            preparedStatement.setInt(1, client);
            preparedStatement.setDate(2, Date.valueOf(dateReception));
            preparedStatement.setInt(3, doctor);
            System.out.println("Update TIME_RECEPTION: " + preparedStatement.executeUpdate());
        }
    }

    // ������ � ����������� ������� �� ������������� c ������ �������
    private static void selectTimeDoctor(Specialties specialties) throws SQLException, ClassNotFoundException {

        final String customerEntryQuery = "SELECT DOCTORS.FULL_NAME, SPECIALTIES.SPECIALITY,  TIME_DOCTORS.DAY_RECEIPT,\n" +
                "TIME_DOCTORS.START_RECEPTION, TIME_DOCTORS.END_RECEPTION\n" +
                "FROM DOCTORS join TIME_DOCTORS on DOCTORS.ID=TIME_DOCTORS.DOCTOR\n" +
                "join SPECIALTIES on DOCTORS.SPECIALITY=SPECIALTIES.ID \n" +
                "where\n" +
                "SPECIALTIES.SPECIALITY  =?;";
        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(customerEntryQuery)) {
            preparedStatement.setString(1, specialties.toString());
            if (preparedStatement.execute()) {
                ResultSet resultSet = preparedStatement.getResultSet();
// ������ � ����� ������� � ������� ResultSetMetaData
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int column = 1; column <= columnCount; column++) {
                    String name = metaData.getColumnName(column);
                    String className = metaData.getColumnClassName(column);
                    String typeName = metaData.getColumnTypeName(column);
                    int type = metaData.getColumnType(column);

                    System.out.println(name + "\t" + className + "\t" + typeName + "\t" + type);
                }
                // ���������� ��� ResultSet

                // ��������, ��������� �� ������ ����� ������ �������
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

    // ������ � ������� ������ ������� �� ���� � �������������.
    private static void selectTimeReception(Specialties specialties, LocalDate dateReception) throws SQLException, ClassNotFoundException {

        final String customerEntryQuery = "SELECT SPECIALTIES.SPECIALITY, DOCTORS.FULL_NAME, TIME_RECEPTION.*   \n" +
                "FROM SPECIALTIES JOIN DOCTORS ON SPECIALTIES.ID = DOCTORS.SPECIALITY \n" +
                "JOIN TIME_RECEPTION ON TIME_RECEPTION.DOCTOR = SPECIALTIES.ID\n" +
                "WHERE\n" +
                "SPECIALTIES.SPECIALITY  =? AND\n" +
                "TIME_RECEPTION.DATE_RECEPTION = ?;";

        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(customerEntryQuery)) {
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

    // ������ ������� � �������
    private static int makeAppointment(int doctor, LocalDate dateReception, LocalTime timeReception, int client) throws SQLException, ClassNotFoundException {

        final String customerEntryQuery1 = "SELECT TIME_DOCTORS.DAY_RECEIPT, TIME_DOCTORS.START_RECEPTION, TIME_DOCTORS.END_RECEPTION  FROM TIME_DOCTORS\n" +
                "WHERE TIME_DOCTORS.DOCTOR=?;";

        final String customerEntryQuery2 = "SELECT \"" + timeReception + "\" FROM TIME_RECEPTION\n" +
                "WHERE TIME_RECEPTION.DATE_RECEPTION=? AND TIME_RECEPTION.DOCTOR=?;";


        try (Connection connection = getDriverManagerConnectionPostgres()) {
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
                    System.out.println("�� � �������� � ���� ���� ������");
                    return 1;
                } else if (resultSet.getInt(1) == 0) {
                    updateTimeReception(doctor, dateReception, timeReception, client);
                    insertClientReception(doctor, dateReception, timeReception, client);
                    preparedStatement.close();
                    System.out.println("� ���� ���� ���� � ������ �� �����");
                    return 2;
                } else {
                    preparedStatement.close();
                    System.out.println("��� ����� ��� ������");
                    return 3;
                }
            }

            preparedStatement.close();
        }
        System.out.println("������ � ��� ����� �� ��������");
        return 0;
    }

    private static void insertData(boolean sw) throws SQLException, ClassNotFoundException {
        if (sw) {
            insertDataClients("Petrov Nikolay Vasilyevich", "Hand x-ray");
            insertSpecialties("Roentgenologist");
     //       insertDoctorsRowSet(5, "Petrov Semyonovich Semenov", 4);
            insertTimeDoctor(1, 1, LocalTime.of(9, 0), LocalTime.of(18, 0));
            insertClientReception(1, LocalDate.of(2023, 12, 1), LocalTime.of(9, 0), 1);
            insertTimeReception(1, LocalDate.of(2023, 12, 1), LocalTime.of(10, 0), 1);
            insertBatchSpecialties(Specialties.OTOLARYNGOLOGIST, Specialties.TRAUMATOLOGIST, Specialties.OCULIST, Specialties.SURGEON);
            insertDataClientsUseProcedure("Lgunov Andrey Vasilyevich", "Headache");
        }
    }

    //����� �������� ������
    private static void createAllTables(boolean sw) throws SQLException, ClassNotFoundException {
        if (sw) {
            try (Statement statement = getDriverManagerConnectionPostgres().createStatement()) {
                //true, ���� ���������� ���������� �������������� �����. false, ���� ���������� ���������� ����� ���������� ��� �� ���������� ����������.
                try {
                    System.out.println("Create Clients result: " + statement.execute(create_Table_Clients) + " " + statement.getUpdateCount());
                } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                    System.out.println("Do not create Clients");
                } finally {
                    try {
                        System.out.println("Create Specialties result: " + statement.execute(create_Table_Specialties) + " " + statement.getUpdateCount());
                    } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                        System.out.println("Do not create Specialties");
                    } finally {
                        try {
                            System.out.println("Create Doctors result: " + statement.execute(create_Table_Doctors) + " " + statement.getUpdateCount());
                        } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                            System.out.println("Do not create Doctors");
                        } finally {
                            try {
                                System.out.println("Create Time_Doctors result: " + statement.execute(create_Table_Time_Doctors) + " " + statement.getUpdateCount());
                            } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                                System.out.println("Do not create Time_Doctors");
                            } finally {
                                try {
                                    //�������� ���� int, ����������� ���� ����� ������������ �����, ���� �������� 0 ��� ���������� ����� DDL. Update/Insert/Delete
                                    //executeQuery() � ��� ������ ������ �� ����. ��� ������������� ������
                                    System.out.println("Create Clients_Reception result: " + statement.executeUpdate(create_Table_Clients_Reception));
                                } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                                    System.out.println("Do not create Clients_Reception");
                                } finally {
                                    try {
                                        System.out.println("Create Time_Reception result: " + statement.executeUpdate(create_Table_Time_Reception));
                                    } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
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

    private static void dropAllTables(boolean sw) throws SQLException, ClassNotFoundException {
        if (sw) {
            try (Statement statement = getDriverManagerConnectionPostgres().createStatement()) {
                try {
                    System.out.println("Drop Time_Reception result: " + statement.execute(drop_Table_Time_Reception) + " " + statement.getUpdateCount());
                } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                    System.out.println("Do not drop Time_Reception");
                } finally {
                    try {
                        System.out.println("Drop Clients_Reception result: " + statement.execute(drop_Table_Clients_Reception));
                    } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                        System.out.println("Do not drop Clients_Reception");
                    } finally {
                        try {
                            System.out.println("Drop Time_Doctors result: " + statement.execute(drop_Table_Time_Doctors));
                        } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                            System.out.println("Do not drop Time_Doctors");
                        } finally {
                            try {
                                System.out.println("Drop Table_Doctors result: " + statement.execute(drop_Table_Doctors));
                            } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                                System.out.println("Do not drop Doctors");
                            } finally {
                                try {
                                    System.out.println("Drop Table_Specialties result: " + statement.executeUpdate(drop_Table_Specialties));
                                } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                                    System.out.println("Do not drop Specialties");
                                } finally {
                                    try {
                                        System.out.println("Drop Table_Clients result: " + statement.executeUpdate(drop_Table_Clients));
                                    } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
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

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // isAfter ���������, �������� �� ��� ����� ����� ���������� �������.
        // isBefore ���������, �� ������������ �� ��� ����� ���������� �������.

//        System.out.println(Time.valueOf(LocalTime.of(15,00)).toString().substring(0,5));
//        System.out.println(LocalTime.of(9,00));
//        System.out.println(LocalDate.of(2023,01,1).getDayOfWeek());
//        System.out.println(DayOfWeek.SUNDAY);
//        System.out.println(LocalDate.of(2023,01,1).getDayOfWeek().equals(DayOfWeek.SUNDAY));
//        System.out.println(LocalTime.of(18,01).isAfter(LocalTime.of(18,00)));
//        System.out.println(!LocalTime.of(10,00).isBefore(LocalTime.of(9,00)));
//        System.out.println(LocalTime.of(8,59).minusMinutes(1));

//        selectTimeDoctor(Specialties.ROENTGENOLOGIST);
//        selectTimeReception(Specialties.ROENTGENOLOGIST,LocalDate.of(2023,12,1));
//        makeAppointment(1,LocalDate.of(2023,12,1), LocalTime.of(9, 0),1);

//        selectDoctorStatistics(1);
        createAllTables(false);
        dropAllTables(false);
        insertData(false);
//        selectCountDoctorsFunction();
        insertDataClients("Petrov Nikolay Vasilyevich", "Hand x-ray");
//        System.out.println(makeAppointment(1,LocalDate.of(2023,12,1),LocalTime.of(10, 30),33));
//        updateTimeReception(1, LocalDate.of(2023,12,1), LocalTime.of(10, 30),1);
//        insertBatchSpecialties(Specialties.OTOLARYNGOLOGIST, Specialties.TRAUMATOLOGIST, Specialties.OCULIST, Specialties.SURGEON);
//        insertDoctorsRowSet(5,"Petrov Semyonovich Semenov", 4);

//        insertDataClients("Petrov Nikolay Vasilyevich", "Hand x-ray");
//        insertSpecialties("Roentgenologist");
//        insertDoctors("Doctor",1);
//        insertTimeDoctor(1, 1, LocalTime.of(9, 0), LocalTime.of(18, 0));
//        insertClientReception(1,LocalDate.of(2023,12,1),LocalTime.of(9,0), 1);
//        insertTimeReception(1, LocalDate.of(2023,12,1),LocalTime.of(10,0),1);
    }
}
