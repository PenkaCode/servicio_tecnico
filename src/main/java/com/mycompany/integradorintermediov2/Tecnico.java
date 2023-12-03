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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class Tecnico {

    private Long id;
    private String nombre;
    private String apellido;
    private List<Especialidad> especialidades;
    private List<Incidente> incidentesAsignados;

    // Constructor para inicializar las propiedades id, nombre y apellido
    public Tecnico(Long id, String nombre, String apellido) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.especialidades = new ArrayList<>();
        this.incidentesAsignados = new ArrayList<>();
    }

    public void agregarEspecialidad(Especialidad especialidad) {
        especialidades.add(especialidad);
        especialidad.getTecnicos().add(this);
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return "Tecnico{"
                + "id=" + id
                + ", nombre='" + nombre + '\''
                + ", apellido='" + apellido + '\''
                + ", especialidades=" + especialidades.stream().map(Especialidad::getTipoProblema).collect(Collectors.toList())
                + ", incidentesAsignados=" + incidentesAsignados
                + '}';
    }

    public static void cargarTecnicoConEspecialidadDesdeConsola(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\n");

        // Obtener información del técnico desde la consola
        System.out.println("Ingrese el nombre del técnico:");
        String nombre = scanner.next();

        System.out.println("Ingrese el apellido del técnico:");
        String apellido = scanner.next();

        // Crear instancia de PreparedStatement para ejecutar la consulta parametrizada
        String sqlTecnico = "INSERT INTO tecnico (nombre, apellido) VALUES (?, ?)";
        try (PreparedStatement statementTecnico = connection.prepareStatement(sqlTecnico, Statement.RETURN_GENERATED_KEYS)) {
            // Establecer los parámetros en la consulta
            statementTecnico.setString(1, nombre);
            statementTecnico.setString(2, apellido);

            // Ejecutar la consulta
            statementTecnico.executeUpdate();

            // Obtener el ID generado para el técnico
            long idTecnico;
            try (var generatedKeys = statementTecnico.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idTecnico = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Error al obtener el ID generado para el técnico.");
                }
            }

            // Llamar al método para cargar las especialidades del técnico
            cargarEspecialidadesDelTecnico(connection, idTecnico);

            System.out.println("Técnico ingresado correctamente.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al cargar el técnico en la base de datos.", e);
        }
    }

    private static void cargarEspecialidadesDelTecnico(Connection connection, long idTecnico) throws SQLException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\n");

        System.out.println("Ingrese la cantidad de especialidades del técnico:");
        int cantidadEspecialidades = scanner.nextInt();

        for (int i = 1; i <= cantidadEspecialidades; i++) {
            System.out.println("Ingrese el tipo de especialidad " + i + ":");
            String tipoEspecialidad = scanner.next();

            // Consultar si la especialidad ya existe en la base de datos
            long idEspecialidad = obtenerIdEspecialidad(connection, tipoEspecialidad);

            if (idEspecialidad == -1) {
                // Si la especialidad no existe, la insertamos en la base de datos
                idEspecialidad = insertarEspecialidad(connection, tipoEspecialidad);
            }

            // Asociar la especialidad al técnico en la tabla de relación muchos a muchos
            asociarEspecialidadATecnico(connection, idTecnico, idEspecialidad);
        }
    }

    private static long obtenerIdEspecialidad(Connection connection, String tipoEspecialidad) throws SQLException {
        String sql = "SELECT id FROM especialidad WHERE tipo_problema = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tipoEspecialidad);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? resultSet.getLong("id") : -1;
        }
    }

    public static long insertarEspecialidad(Connection connection, String tipoEspecialidad) throws SQLException {
        String sql = "INSERT INTO especialidad (tipo_problema) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, tipoEspecialidad);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Error al obtener el ID generado para la especialidad.");
                }
            }
        }
    }

    public static void asociarEspecialidadATecnico(Connection connection, long idTecnico, long idEspecialidad) throws SQLException {
        // Verificar si la asociación ya existe
        if (!existeAsociacion(connection, idTecnico, idEspecialidad)) {
            String sql = "INSERT INTO tecnico_especialidad (tecnico_id, especialidad_id) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, idTecnico);
                statement.setLong(2, idEspecialidad);
                statement.executeUpdate();
            }
        } else {
            System.out.println("La asociación entre el técnico y la especialidad ya existe.");
        }
    }

    private static boolean existeAsociacion(Connection connection, long idTecnico, long idEspecialidad) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM tecnico_especialidad WHERE tecnico_id = ? AND especialidad_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idTecnico);
            statement.setLong(2, idEspecialidad);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt("count") > 0;
        }
    }

    public static List<Tecnico> obtenerTecnicosConEspecialidadYProblema(Connection connection, long tipoEspecialidad) {
        String sql = "SELECT t.id, t.nombre, t.apellido FROM tecnico t "
                + "JOIN tecnico_especialidad te ON t.id = te.tecnico_id "
                + "JOIN especialidad e ON te.especialidad_id = e.id "
                + "WHERE e.id = ?";

        List<Tecnico> tecnicos = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, tipoEspecialidad);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Obtener los datos del técnico y crear una instancia de Tecnico
                    Long id = resultSet.getLong("id");
                    String nombre = resultSet.getString("nombre");
                    String apellido = resultSet.getString("apellido");
                    Tecnico tecnico = new Tecnico(id, nombre, apellido);

                    // Agregar el técnico a la lista
                    tecnicos.add(tecnico);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo adecuado de la excepción en tu aplicación real
        }

        return tecnicos;
    }

// Agrega este método en la clase Tecnico para verificar si tiene una especialidad específica
    public boolean tieneEspecialidad(String tipoEspecialidad) {
        return this.especialidades.stream().anyMatch(especialidad -> especialidad.getTipoProblema().equals(tipoEspecialidad));
    }

    public static void mostrarListadoEspecialidades(Connection connection) throws SQLException {
        String sql = "SELECT id, tipo_problema FROM especialidad";

        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Listado de especialidades:");
            while (resultSet.next()) {
                long idEspecialidad = resultSet.getLong("id");
                String tipoProblema = resultSet.getString("tipo_problema");
                System.out.println(idEspecialidad + ": " + tipoProblema);
            }
        }
    }

    public static void asociarEspecialidadATecnicoDesdeConsola(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\n");

        // Mostrar listado de técnicos
        System.out.println("Listado de Técnicos:");
        mostrarListadoTecnicos(connection);

        // Solicitar al usuario que ingrese el ID del técnico
        System.out.println("Ingrese el ID del técnico:");
        long idTecnico = scanner.nextLong();

        // Mostrar listado de especialidades
        System.out.println("Listado de Especialidades:");
        mostrarListadoEspecialidades(connection);

        // Solicitar al usuario que ingrese el ID de la especialidad
        System.out.println("Ingrese el ID de la especialidad:");
        long idEspecialidad = scanner.nextLong();

        // Llamar al método para asociar la especialidad al técnico en la base de datos
        Tecnico.asociarEspecialidadATecnico(connection, idTecnico, idEspecialidad);

        System.out.println("Especialidad asociada al técnico correctamente.");
    }

    private static void mostrarListadoTecnicos(Connection connection) {
        String sql = "SELECT id, nombre, apellido FROM tecnico";
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String nombre = resultSet.getString("nombre");
                String apellido = resultSet.getString("apellido");
                System.out.println("ID: " + id + ", Técnico: " + nombre + " " + apellido);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo adecuado de la excepción en tu aplicación real
        }
    }
}
