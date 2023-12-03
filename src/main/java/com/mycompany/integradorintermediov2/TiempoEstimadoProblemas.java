/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.integradorintermediov2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author neo_0
 */
public class TiempoEstimadoProblemas {

    private static final Map<String, Integer> mapaTiemposEstimados = new HashMap<>();

    public static Map<String, Integer> getMapaTiemposEstimados() {
        return mapaTiemposEstimados;
    }

    public static int cargarTiemposDesdeBaseDeDatos(Connection connection, String tipoProblema) throws SQLException {
        cargarTiposProblemaDesdeBaseDeDatos(connection);
        return calcularTiempoEstimado(tipoProblema);
    }

    public static void cargarTiposProblemaDesdeBaseDeDatos(Connection connection) throws SQLException {
        String sql = "SELECT tipo_problema, tiempo_resolucion FROM tipo_problema";

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String tipoProblema = resultSet.getString("tipo_problema");
                int tiempoResolucion = resultSet.getInt("tiempo_resolucion");

                mapaTiemposEstimados.put(tipoProblema, tiempoResolucion);
            }
        }
    }

    private static int calcularTiempoEstimado(String tipoProblema) {
        // Devuelve el tiempo estimado según el tipo de problema
        // Si el tipo de problema no está en el mapa, devuelve un valor por defecto
        return mapaTiemposEstimados.getOrDefault(tipoProblema, 0); // Valor por defecto: 0 horas
    }

    public static void cargarTiposProblemaEnBaseDeDatos(Connection connection) throws SQLException {
        
        connection.setAutoCommit(false);
        // Consulta SQL para obtener todos los tipos de problema existentes
        String selectExistingSql = "SELECT tipo_problema FROM tipo_problema";
        Set<String> existingProblems = new HashSet<>();

        try (Statement selectExistingStatement = connection.createStatement(); ResultSet resultSet = selectExistingStatement.executeQuery(selectExistingSql)) {
            while (resultSet.next()) {
                existingProblems.add(resultSet.getString("tipo_problema"));
            }
        }
        
        // Agrega tipos de problemas y tiempos de resolución al mapa
        mapaTiemposEstimados.put("Windows", 3);
        mapaTiemposEstimados.put("MAC OS", 4);
        mapaTiemposEstimados.put("Linux", 3);
        mapaTiemposEstimados.put("Ubuntu", 2);
        mapaTiemposEstimados.put("SAP", 5);
        mapaTiemposEstimados.put("Tango", 7);
        mapaTiemposEstimados.put("BD MYSQL", 2);
        mapaTiemposEstimados.put("Server", 1);

        // Consulta SQL para insertar nuevos tipos de problemas
        String insertSql = "INSERT INTO tipo_problema (tipo_problema, tiempo_resolucion) VALUES (?, ?)";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            // Itera sobre el mapa y realiza las inserciones solo si el problema no existe en la base de datos
            for (Map.Entry<String, Integer> entry : mapaTiemposEstimados.entrySet()) {
                String tipoProblema = entry.getKey();
                if (!existingProblems.contains(tipoProblema)) {
                    insertStatement.setString(1, tipoProblema);
                    insertStatement.setInt(2, entry.getValue());
                    insertStatement.executeUpdate();
                }
            }
        }

        connection.setAutoCommit(true);
    }
}
