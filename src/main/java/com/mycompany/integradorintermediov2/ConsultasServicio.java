/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.integradorintermediov2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 *
 * @author neo_0
 */
@Data
public class ConsultasServicio {

    private List<Tecnico> tecnicos;

    public static class DatosIncidente {

        private int tecnicoAsignadoId;
        private String estado;
        private LocalDateTime fechaResolucion;

        public int getTecnicoAsignadoId() {
            return tecnicoAsignadoId;
        }

        public void setTecnicoAsignadoId(int tecnicoAsignadoId) {
            this.tecnicoAsignadoId = tecnicoAsignadoId;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public LocalDateTime getFechaResolucion() {
            return fechaResolucion;
        }

        public void setFechaResolucion(LocalDateTime fechaResolucion) {
            this.fechaResolucion = fechaResolucion;
        }

        public DatosIncidente(int tecnicoAsignadoId, String estado, LocalDateTime fechaResolucion) {
            this.tecnicoAsignadoId = tecnicoAsignadoId;
            this.estado = estado;
            this.fechaResolucion = fechaResolucion;
        }

        @Override
        public String toString() {
            return "DatosIncidente{"
                    + "tecnicoAsignadoId=" + tecnicoAsignadoId
                    + ", estado='" + estado + '\''
                    + ", fechaResolucion=" + fechaResolucion
                    + '}';
        }
    }

    public static List<Tecnico> obtenerListaDeTecnicosDesdeBD(Connection connection) throws SQLException {
        List<Tecnico> tecnicos = new ArrayList<>();
        String query = "SELECT id, nombre, apellido FROM tecnico";

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                long tecnicoId = resultSet.getLong("id");
                String nombre = resultSet.getString("nombre");
                String apellido = resultSet.getString("apellido");

                Tecnico tecnico = new Tecnico(tecnicoId, nombre, apellido);
                tecnicos.add(tecnico);
            }
        }

        return tecnicos;
    }

    public static List<DatosIncidente> obtenerListaDeIncidentesDesdeBD(Connection connection) throws SQLException {
        List<DatosIncidente> incidentes = new ArrayList<>();
        String query = "SELECT tecnico_asignado_id, estado, fecha_resolucion FROM incidente";

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                int tecnicoAsignadoId = resultSet.getInt("tecnico_asignado_id");
                String estado = resultSet.getString("estado");
                LocalDateTime fechaResolucion = resultSet.getTimestamp("fecha_resolucion").toLocalDateTime();

                DatosIncidente incidente = new DatosIncidente(tecnicoAsignadoId, estado, fechaResolucion);
                incidentes.add(incidente);
            }
        }

        return incidentes;
    }

    public static Map<Integer, Integer> obtenerCantidadIncidentesResueltosPorTecnico(List<DatosIncidente> incidentes) {
        Map<Integer, Integer> cantidadIncidentesPorTecnico = new HashMap<>();

        for (DatosIncidente incidente : incidentes) {
            if ("Resuelto".equals(incidente.getEstado())) {
                cantidadIncidentesPorTecnico.merge(incidente.getTecnicoAsignadoId(), 1, Integer::sum);
            }
        }
        return cantidadIncidentesPorTecnico;
    }

    public static void obtenerInformacionFinal(Map<Integer, Integer> cantidadIncidentesPorTecnico, List<Tecnico> tecnicos) {
        if (!cantidadIncidentesPorTecnico.isEmpty()) {

            // Obtener el máximo solo si el mapa no está vacío
            Map.Entry<Integer, Integer> maxEntry = Collections.max(cantidadIncidentesPorTecnico.entrySet(), Map.Entry.comparingByValue());
            int tecnicoIdConMasIncidentes = maxEntry.getKey();
            int incidentesResueltos = maxEntry.getValue();

            // Obtener el nombre y apellido del técnico con más incidentes resueltos
            String nombreApellidoTecnico = obtenerNombreApellidoTecnico(tecnicos, tecnicoIdConMasIncidentes);

            // Imprimir resultados
            System.out.println("Técnico con más incidentes resueltos: " + nombreApellidoTecnico + ", incidentes resueltos: " + incidentesResueltos);
        } else {
            System.out.println("No hay datos disponibles para obtener el máximo.");
        }
    }

    private static String obtenerNombreApellidoTecnico(List<Tecnico> tecnicos, int tecnicoId) {
        for (Tecnico tecnico : tecnicos) {
            Long id = tecnico.getId();
            if (id != null && id == tecnicoId) {
                return tecnico.getNombre() + " " + tecnico.getApellido();
            }
        }
        System.out.println("Técnico no encontrado para el ID: " + tecnicoId);
        System.out.println("Lista de técnicos: " + tecnicos);
        return ""; // Manejo de error si no se encuentra el técnico o el ID es null
    }

    public static int contarIncidentesTotalesResueltosEnFecha(List<DatosIncidente> incidentes, LocalDate fecha) {
        int incidentesTotales = 0;

        for (DatosIncidente incidente : incidentes) {
            if ("Resuelto".equals(incidente.getEstado()) && incidente.getFechaResolucion().toLocalDate().isEqual(fecha)) {
                incidentesTotales++;
            }
        }

        //Imprimir información de la cantidad de incidentes totales por fecha
        System.out.println("Cantidad de incidentes totales resueltos en la fecha " + fecha + ": " + incidentesTotales);

        return incidentesTotales;
    }

}
