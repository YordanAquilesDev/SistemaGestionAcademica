package org.mackas.util.singlenton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSqLite implements ConexionDataBase {
  private Connection connection;
 private static ConexionSqLite instance;
 private ConexionSqLite(){
     connection = crearConexion();
 }

 public static ConexionSqLite getInstance(){
 if(instance==null){
    instance= new ConexionSqLite();
 }
 return instance;
 }


@Override
public synchronized Connection getConexion() {
    try {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
    } catch (SQLException e) {
        // Recreate a closed SQLite connection when it is requested again.
    }
    connection = crearConexion();
    return connection;
}

private Connection crearConexion() {
    // El archivo se creará automáticamente como 'sistema_universidad.db' en la raíz de tu proyecto
    String url = "jdbc:sqlite:sistema_universidad.db";
    
    // Si prefieres guardarlo en una ruta específica dentro de tu proyecto:
    // String url = "jdbc:sqlite:resources/database/sistema_universidad.db";

    try {
        // SQLite no requiere usuario ni contraseña por defecto
     return DriverManager.getConnection(url);
    } catch (SQLException e) {
        System.out.println("Error al conectar a SQLite: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}
    
}
