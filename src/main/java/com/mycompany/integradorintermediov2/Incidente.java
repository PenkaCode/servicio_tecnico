/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.integradorintermediov2;

import static com.mycompany.integradorintermediov2.MesaDeAyuda.guardarNotificacionEnArchivo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author neo_0
 */
@Data
@NoArgsConstructor
public class Incidente {

    private Long id;
    private String descripcion;
    private String tipoProblema;
    private String estado;
    private int tiempoEstimado;
    private LocalDateTime fechaResolucion;
    private Cliente cliente;
    private Tecnico tecnicoAsignado;
    private List<Tecnico> tecnicos;


    public void cargarIncidenteDesdeConsola(Connection connection) {
        
        Scanner scanner = new Scanner(System.in).useDelimiter("\n");

        // Pedir el número de CUIT
        System.out.println("Ingrese el número de CUIT del cliente:");
        String cuit = scanner.nextLine();

        // Obtener el cliente desde la base de datos usando el CUIT
        Cliente cliente = obtenerClientePorCuit(connection, cuit);

        // Verificar si se encontró el cliente
        if (cliente != null) {
            // Obtener la razón social del cliente desde la base de datos
            String razonSocial = obtenerRazonSocialPorCuit(connection, cuit);

            // Imprimir la razón social obtenida
            System.out.println("Razón Social del Cliente: " + razonSocial);

            // Pedir el Sistema Operativo o Programa con problema
            System.out.println("Seleccione el ID del tipo de problema (Especialidad):");
            mostrarListadoEspecialidades(connection); // Mostrar listado de especialidades con IDs

            // Obtener el ID del tipo de problema seleccionado por el usuario
            long idTipoProblema = scanner.nextLong();
            scanner.nextLine(); // Consumir la nueva línea después de nextLong

            // Obtener la lista de técnicos con la especialidad relacionada al problema
            List<Tecnico> tecnicosDisponibles = Tecnico.obtenerTecnicosConEspecialidadYProblema(connection, idTipoProblema);

            // Imprimir la lista de técnicos y dejar que el usuario elija uno
            System.out.println("Técnicos disponibles para resolver el problema:");
            for (int i = 0; i < tecnicosDisponibles.size(); i++) {
                System.out.println((i + 1) + ". " + tecnicosDisponibles.get(i).getNombreCompleto());
            }

            // Pedir al usuario que elija un técnico
            System.out.println("Seleccione el número del técnico más apto:");
            int opcionTecnico = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea después de nextInt

            // Obtener el técnico seleccionado
            Tecnico tecnicoSeleccionado = tecnicosDisponibles.get(opcionTecnico - 1);

            try {
                // Llamada al método para cargar los tiempos desde la base de datos
                int tiempoEstimado = TiempoEstimadoProblemas.cargarTiemposDesdeBaseDeDatos(connection, String.valueOf(idTipoProblema));

                // Informar al operador sobre el técnico y tiempo estimado
                System.out.println("El técnico asignado es: " + tecnicoSeleccionado.getNombreCompleto());
                System.out.println("El tiempo estimado de resolución es: " + tiempoEstimado + " horas.");

                // Llamada al método para cargar el incidente en la base de datos
                try {
                    cargarIncidenteEnBaseDeDatos(connection, cliente, tecnicoSeleccionado, idTipoProblema, tiempoEstimado);
                    System.out.println("Incidente cargado en la base de datos.");
                } catch (SQLException e) {
                    System.err.println("Error al cargar el incidente en la base de datos:");
                    e.printStackTrace();
                    // Manejo del error si es necesario
                }

                // Preguntar si desea enviar el mensaje por WhatsApp
                System.out.println("¿Desea enviar un mensaje por WhatsApp al cliente? (Sí/No):");
                String respuesta = scanner.next().toLowerCase();

                if ("si".equals(respuesta) || "sí".equals(respuesta)) {
                    // Simular el envío por WhatsApp escribiendo en un archivo de texto
                    guardarNotificacionEnArchivo(tecnicoSeleccionado, razonSocial, tiempoEstimado, String.valueOf(idTipoProblema));
                } else {
                    System.out.println("Opción inválida. No se ha ingresado el incidente.");
                }
            } catch (SQLException e) {
                System.err.println("Error al cargar los tiempos desde la base de datos:");
                e.printStackTrace();
            }
        } else {
            System.out.println("Cliente no encontrado. No se ha ingresado el incidente.");
        }
    }

    public static void cargarIncidenteEnBaseDeDatos(Connection connection, Cliente cliente, Tecnico tecnicoAsignado, long tipoProblema, int tiempoEstimado) throws SQLException {
        String sql = "INSERT INTO incidente (tipo_problema, estado, tiempo_estimado, fecha_resolucion, cliente_id, tecnico_asignado_id) VALUES (?, ?, ?, ?, ?, ?)";

        long idIncidente = -1; // Valor predeterminado

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Establecer los parámetros en la consulta
            statement.setLong(1, tipoProblema);
            statement.setString(2, "Resuelto"); // Cambiar el estado a "Resuelto"
            statement.setInt(3, tiempoEstimado);
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now())); // Establecer la fecha de resolución en el momento actual
            statement.setLong(5, cliente.getId());
            statement.setLong(6, tecnicoAsignado.getId());

            // Ejecutar la consulta
            statement.executeUpdate();

            // Obtener el ID generado para el incidente
            // (Este paso es opcional, pero puede ser útil obtener el ID del incidente recién creado)
            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idIncidente = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Error al obtener el ID generado para el incidente.");
                }
            }

            System.out.println("Incidente ingresado correctamente. ID del incidente: " + idIncidente);
        } catch (SQLException e) {
            System.err.println("Error al ejecutar la consulta SQL en cargarIncidenteEnBaseDeDatos:");
            e.printStackTrace();
        }
    }

    private String obtenerRazonSocialPorCuit(Connection connection, String cuit) {
        Cliente cliente = obtenerClientePorCuit(connection, cuit);
        return (cliente != null) ? cliente.getRazonSocial() : null;
    }

    private Cliente obtenerClientePorCuit(Connection connection, String cuit) {
        String sql = "SELECT id, razon_social FROM cliente WHERE cuit = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cuit);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setId(resultSet.getLong("id"));
                    cliente.setRazonSocial(resultSet.getString("razon_social"));
                    return cliente;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente por CUIT:");
            e.printStackTrace();
        }

        return null;
    }

    private static void mostrarListadoEspecialidades(Connection connection) {
        String sql = "SELECT id, tipo_problema FROM especialidad";
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String tipoProblema = resultSet.getString("tipo_problema");
                System.out.println("ID: " + id + ", Especialidad: " + tipoProblema);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo adecuado de la excepción en tu aplicación real
        }
    }
}
