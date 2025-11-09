package com.inovamei.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // CERTIFIQUE-SE QUE ESTES VALORES ESTÃO CORRETOS
    // E adicione o parâmetro de timeout
    private static final String DB_URL = "jdbc:mysql://localhost:3306/aep4s?connectTimeout=3000";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Ou a sua senha, se existir

    public static Connection getConnection() throws SQLException {
        // A partir do Java 9, o Driver é carregado automaticamente.
        // O código pode travar nesta linha se a Base de Dados estiver inacessível.
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}