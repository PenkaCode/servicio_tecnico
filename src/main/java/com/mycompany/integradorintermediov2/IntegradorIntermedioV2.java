/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.integradorintermediov2;

import static com.mycompany.integradorintermediov2.InterfazCarga.cargarDatosDesdeConsola;
import static com.mycompany.integradorintermediov2.InterfazCarga.crearTablas;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author neo_0
 */
public class IntegradorIntermedioV2 {

    public static void main(String[] args) {
        try {
            // Establecer la conexión con la base de datos
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/servicio_tecnico", "root", "root");

            // Crear tablas si no existen
            crearTablas(connection);

            // Realizar operaciones desde la consola
            cargarDatosDesdeConsola(connection);

            // Cerrar la conexión
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
