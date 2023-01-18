package SQL;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
//Класс для чтения из config
public class ConnectionsH2 {

    public static final String DB_URL;
    public static final String DB_Driver;
    public static final String User_DB;
    public static final String Password_DB;
    public static final String create_Table_Clients;
    public static final String create_Table_Specialties;
    public static final String create_Table_Doctors;
    public static final String create_Table_Time_Doctors;
    public static final String create_Table_Clients_Reception;
    public static final String create_Table_Time_Reception;
    private static final Properties properties = new Properties();

    public static final String drop_Table_Time_Reception = "drop table Time_Reception";
    public static final String drop_Table_Clients_Reception = "drop table Clients_Reception";
    public static final String drop_Table_Time_Doctors = "drop table Time_Doctors";
    public static final String drop_Table_Doctors = "drop table Doctors";
    public static final String drop_Table_Specialties = "drop table Specialties";
    public static final String drop_Table_Clients = "drop table Clients";

    static {

        try (InputStream input = new FileInputStream(".\\src\\SQL\\config.properties")) {
            properties.load(input);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        DB_URL = properties.getProperty("DB_URL");
        DB_Driver = properties.getProperty("DB_Driver");
        User_DB = properties.getProperty("User_DB");
        Password_DB = properties.getProperty("Password_DB");
        create_Table_Clients = properties.getProperty("Create_Table_Clients");
        create_Table_Specialties = properties.getProperty("Create_Table_Specialties");
        create_Table_Doctors = properties.getProperty("Create_Table_Doctors");
        create_Table_Time_Doctors = properties.getProperty("Create_Table_Time_Doctors");
        create_Table_Clients_Reception = properties.getProperty("Create_Table_Clients_Reception");
        create_Table_Time_Reception = properties.getProperty("Create_Table_Time_Reception");
    }


}
