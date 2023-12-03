/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.integradorintermediov2;

import static com.mycompany.integradorintermediov2.Cliente.cargarClienteDesdeConsola;
import com.mycompany.integradorintermediov2.ConsultasServicio.DatosIncidente;
import static com.mycompany.integradorintermediov2.Tecnico.asociarEspecialidadATecnicoDesdeConsola;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author neo_0
 */
public class InterfazCarga {

    private static final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    private static List<Tecnico> tecnicos;
    private static List<DatosIncidente> incidentes; // Agrega esta línea

    public static void cargarDatosDesdeConsola(Connection connection) throws SQLException {
        tecnicos = ConsultasServicio.obtenerListaDeTecnicosDesdeBD(connection);
        incidentes = ConsultasServicio.obtenerListaDeIncidentesDesdeBD(connection); // Agrega esta línea

        boolean salir = false;

        while (!salir) {
            System.out.println("Elija una opción:");
            System.out.println("1. Cargar técnico");
            System.out.println("2. Cargar cliente");
            System.out.println("3. Cargar incidente");
            System.out.println("4. Cargar especialidad");
            System.out.println("5. Asociar especialidad a técnico");
            System.out.println("6. Obtener técnico con más incidentes resueltos");
            System.out.println("7. Generar reporte diario");
            System.out.println("8. Salir");

            int opcion = scanner.nextInt();

            try {
                switch (opcion) {
                    case 1 -> {
                        Tecnico.cargarTecnicoConEspecialidadDesdeConsola(connection);
                    }
                    case 2 ->
                        cargarClienteDesdeConsola(connection);
                    case 3 -> {
                        Incidente incidente = new Incidente();
                        incidente.cargarIncidenteDesdeConsola(connection);
                        incidentes = ConsultasServicio.obtenerListaDeIncidentesDesdeBD(connection); // Actualiza la lista de incidentes
                    }
                    case 4 -> {
                        Especialidad.cargarEspecialidadDesdeConsola(connection);
                        preguntarSalir();
                    }
                    case 5 -> {
                        asociarEspecialidadATecnicoDesdeConsola(connection);
                        preguntarSalir();
                    }
                    case 6 -> {
                        Map<Integer, Integer> cantidadIncidentesPorTecnico = ConsultasServicio.obtenerCantidadIncidentesResueltosPorTecnico(incidentes);
                        ConsultasServicio.obtenerInformacionFinal(cantidadIncidentesPorTecnico, tecnicos);
                        preguntarSalir();
                    }
                    case 7 -> {
                        System.out.println("Ingrese la fecha (YYYY-MM-DD): ");
                        String fechaString = scanner.next();
                        LocalDate fechaConsulta = LocalDate.parse(fechaString);
                        ConsultasServicio.contarIncidentesTotalesResueltosEnFecha(incidentes, fechaConsulta);
                        preguntarSalir();
                    }

                    case 8 ->
                        salir = true;
                    default ->
                        System.out.println("Opción inválida");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error al realizar la operación: " + e.getMessage());
            }
        }
    }

    public static void crearTablas(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Crear tabla Tecnico si no existe
            statement.execute("CREATE TABLE IF NOT EXISTS tecnico ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "nombre VARCHAR(255) NOT NULL,"
                    + "apellido VARCHAR(255) NOT NULL)");

            // Crear tabla Cliente si no existe
            statement.execute("CREATE TABLE IF NOT EXISTS cliente ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "razon_social VARCHAR(255) NOT NULL,"
                    + "cuit VARCHAR(20) NOT NULL)");

            // Crear tabla Especialidad si no existe
            statement.execute("CREATE TABLE IF NOT EXISTS especialidad ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "tipo_especialidad VARCHAR(255) NOT NULL)");

            // Crear tabla Incidente si no existe
            statement.execute("CREATE TABLE IF NOT EXISTS incidente ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "descripcion VARCHAR(255) NOT NULL,"
                    + "tipo_problema VARCHAR(255) NOT NULL,"
                    + "estado VARCHAR(50),"
                    + "tiempo_estimado INT,"
                    + "fecha_resolucion TIMESTAMP,"
                    + "cliente_id BIGINT,"
                    + "tecnico_asignado_id BIGINT,"
                    + "FOREIGN KEY (cliente_id) REFERENCES cliente(id),"
                    + "FOREIGN KEY (tecnico_asignado_id) REFERENCES tecnico(id))");

            // Crear tabla Contrato si no existe
            statement.execute("CREATE TABLE IF NOT EXISTS contrato ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "servicio VARCHAR(255) NOT NULL,"
                    + "cliente_id BIGINT,"
                    + "FOREIGN KEY (cliente_id) REFERENCES cliente(id))");

            // Crear tabla Tecnico_Especialidad si no existe (tabla de relación muchos a muchos)
            statement.execute("CREATE TABLE IF NOT EXISTS tecnico_especialidad ("
                    + "tecnico_id BIGINT,"
                    + "especialidad_id BIGINT,"
                    + "PRIMARY KEY (tecnico_id, especialidad_id),"
                    + "FOREIGN KEY (tecnico_id) REFERENCES tecnico(id),"
                    + "FOREIGN KEY (especialidad_id) REFERENCES especialidad(id))");

            // Crear tabla tipo_problema si no existe
            statement.execute("CREATE TABLE IF NOT EXISTS tipo_problema ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "tipo_problema VARCHAR(255) NOT NULL,"
                    + "tiempo_resolucion INT NOT NULL)");

            TiempoEstimadoProblemas.cargarTiposProblemaEnBaseDeDatos(connection);

            System.out.println("Tablas creadas correctamente.");
        }
    }

    private static void preguntarSalir() {
        System.out.println("¿Desea salir del sistema? (S/N)");
        char respuesta = scanner.next().charAt(0);
        if (respuesta == 'S' || respuesta == 's') {
            System.out.println("Saliendo del sistema...");
            System.exit(0);
        }
    }
}
