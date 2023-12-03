/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.integradorintermediov2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import lombok.Data;

/**
 *
 * @author neo_0
 */
@Data
public class Especialidad {

    private Long id;
    private String tipoProblema;
    private List<Tecnico> tecnicos;

    // Constructor para inicializar la lista
    public Especialidad() {
        this.tecnicos = new ArrayList<>();
    }

    public void agregarTecnico(Tecnico tecnico) {
        tecnicos.add(tecnico);
        tecnico.getEspecialidades().add(this);
    }

    public static void cargarEspecialidad(Connection connection) throws SQLException {
        // Cargar datos en la tabla especialidad
        cargarDatosEspecialidad(connection);

        // Cargar datos en la tabla tecnico_especialidad
        cargarDatosTecnicoEspecialidad(connection);
    }

    private static void cargarDatosEspecialidad(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // No especifiques valores para la columna id, deja que la base de datos los genere automáticamente
            statement.executeUpdate("INSERT INTO especialidad () VALUES (), (), ()");
        }
    }

    private static void cargarDatosTecnicoEspecialidad(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Supongamos que quieres asociar el técnico con ID 1 a las especialidades con ID 1 y 2
            statement.executeUpdate("INSERT INTO tecnico_especialidad (tecnico_id, especialidad_id) VALUES (1, 1), (1, 2)");

            // Supongamos que quieres asociar el técnico con ID 2 a la especialidad con ID 3
            statement.executeUpdate("INSERT INTO tecnico_especialidad (tecnico_id, especialidad_id) VALUES (2, 3)");
        }
    }

    public static void cargarEspecialidadDesdeConsola(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\n");
        System.out.println("Ingrese el tipo de especialidad:");
        String tipoEspecialidad = scanner.next();

        // Llama al método para insertar la especialidad en la base de datos
        long idEspecialidad = Tecnico.insertarEspecialidad(connection, tipoEspecialidad);

        System.out.println("Especialidad ingresada correctamente. ID de especialidad: " + idEspecialidad);
    }
}
