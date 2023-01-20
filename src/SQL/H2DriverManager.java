package SQL;

import Polyclinic.Specialties;
import org.h2.jdbc.JdbcSQLSyntaxErrorException;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

//����� ��� ������ ��� ���� ���������� JDBC
public class H2DriverManager extends ConnectionsH2 {

    private static Connection getDriverManagerConnectionPostgres() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(DB_URL, User_DB, Password_DB);
    }


    //��������� ������ � �������
    private static void insertDataClients(String fullName, String problemInfo) throws SQLException, ClassNotFoundException {

        final String customerEntryQuery = "Insert into " +
                "CLIENTS (FULL_NAME , PROBLEM_INFO) " +
                "values (?, ?)";
        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(customerEntryQuery)) {
            preparedStatement.setString(1, fullName);
            preparedStatement.setString(2, problemInfo);
            preparedStatement.execute();
            System.out.println("Add client: result " + preparedStatement.getUpdateCount());

            //getUpdateCount() - ��������� ������� ��������� ��� ���������� ���������� = executeUpdate();
            // ���� ����������� �������� ResultSet ������ ��� ����������� ������ ���, ������������ -1.

            //getMoreResults() - ��������� � Statement ���������� ���������� ����� �������, ���������� ��������,
            // true ���� ��� ResultSet ������, � ������ ��������� ��� ������� ResultSet �������, ���������� ������� getResultSet.

            //getResultSet() - ��������� ������� ��������� ��� ResultSet ������.
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

    //TODO ��������� � ������� �������� ������. �������� ��������� ������ resultSet ����� � ����
    private static void selectInformationWithReception(String nameClient) throws SQLException, ClassNotFoundException {
        final String selectInformationWithReceptionQuery = "SELECT CLIENTS.FULL_NAME, CLIENTS.PROBLEM_INFO, DOCTORS.FULL_NAME, " +
                "SPECIALTIES.SPECIALITY, TIME_RECEPTION.DATE_RECEPTION \" +\n" +
                "\"FROM CLIENTS, DOCTORS, SPECIALTIES, TIME_RECEPTION where CLIENTS.FULL_NAME=?;";

        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(selectInformationWithReceptionQuery)) {
            preparedStatement.setString(1, nameClient);
            System.out.println(preparedStatement.execute());
        }
    }


    //TODO �������� �� ��������, ���������� �� ������� � ���������
    //����� ������� informationWithReception � ��, ���������� ��� ����� selectInformationWithReception
    private static void selectInformationWithReceptionUseFunction() throws SQLException, ClassNotFoundException {
        try (CallableStatement callableStatement = getDriverManagerConnectionPostgres().prepareCall("{? = CALL public.countDoctors ()}")) {
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.execute();
            System.out.println(callableStatement.getInt(1));
        }


    }
    //����� ���������, �������� �������
    private static void insertDataClientsUseProcedure() throws SQLException, ClassNotFoundException {
        try (CallableStatement callableStatement = getDriverManagerConnectionPostgres().prepareCall("CALL public.addClient(?,?,?)")) {
            callableStatement.setString(1, "Razumov Sergey Igorevich");
            callableStatement.setString(2, "Teeth hurt");
            callableStatement.registerOutParameter(3, java.sql.Types.INTEGER);
            callableStatement.execute();
            System.out.println(callableStatement.getInt(3));
        }
    }


    // ������ � ���������� ������� �� ������������� c ������ �������
    private static void selectTimeDoctor(Specialties specialties) throws SQLException, ClassNotFoundException {

        final String selectTimeDoctorQuery = "SELECT DOCTORS.FULL_NAME, TIME_DOCTORS.DAY_RECEIPT, " +
                "TIME_DOCTORS.START_RECEPTION, TIME_DOCTORS.END_RECEPTION FROM DOCTORS, TIME_DOCTORS\n" +
                "where DOCTORS.ID=TIME_DOCTORS.DOCTOR\n" +
                "and exists(\n" +
                "SELECT * FROM DOCTORS\n" +
                "where  ID=TIME_DOCTORS.DOCTOR and exists(\n" +
                "SELECT * FROM SPECIALTIES\n" +
                "where\n" +
                "ID=DOCTORS.SPECIALITY and\n" +
                "SPECIALITY=?)\n" +
                ");";
        try (PreparedStatement preparedStatement = getDriverManagerConnectionPostgres().prepareStatement(selectTimeDoctorQuery)) {
            preparedStatement.setString(1, specialties.toString());
            System.out.println(preparedStatement.execute());

        }
    }

    // ������ ������� � �������
    private static int makeAppointment(int doctor, LocalDate dateReception, LocalTime timeReception, int client) throws SQLException {
        if (dateReception.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            System.out.println("����������� ��������");
            return -1;
        }
        return 1;
    }


    //����� �������� ������
    private static void createAllTables(boolean sw) throws SQLException, ClassNotFoundException {
        if (sw) {
            try (Statement statement = getDriverManagerConnectionPostgres().createStatement()) {
                //true, ���� ���������� ���������� �������������� �����. false, ���� ���������� ���������� ����� ���������� ��� �� ���������� ����������.
                try {
                    System.out.println("Create Clients result: " + statement.execute(create_Table_Clients));
                } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                    System.out.println("Do not create Clients");
                } finally {
                    try {
                        System.out.println("Create Specialties result: " + statement.execute(create_Table_Specialties));
                    } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                        System.out.println("Do not create Specialties");
                    } finally {
                        try {
                            System.out.println("Create Doctors result: " + statement.execute(create_Table_Doctors));
                        } catch (JdbcSQLSyntaxErrorException | PSQLException e) {
                            System.out.println("Do not create Doctors");
                        } finally {
                            try {
                                System.out.println("Create Time_Doctors result: " + statement.execute(create_Table_Time_Doctors));
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
                    System.out.println("Drop Time_Reception result: " + statement.execute(drop_Table_Time_Reception));
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
//        selectTimeDoctor(Specialties.Roentgenologist);
//        selectInformationWithReceptionUseProcedure("Ivanov Ivan Andreevich");
        selectInformationWithReception("Ivanov Ivan Andreevich");
        createAllTables(false);
        dropAllTables(false);
        insertDataClientsUseProcedure();
        selectInformationWithReceptionUseFunction();
//        selectTimeDoctor(Specialties.Roentgenologist);
//        insertDataClients("Ivanov Ivan Andreevich", "Chest x-ray");
//        insertSpecialties("Roentgenologist");
//        insertDoctors("Vasiliev Andrey Alexandrovich", 1);
//        insertTimeDoctor(1, 1, LocalTime.of(9, 0), LocalTime.of(18, 0));
//        insertClientReception(1,LocalDate.of(2023,12,1),LocalTime.of(9,0), 1);
//        insertTimeReception(1, LocalDate.of(2023,12,1),LocalTime.of(10,0),1);
    }
}
