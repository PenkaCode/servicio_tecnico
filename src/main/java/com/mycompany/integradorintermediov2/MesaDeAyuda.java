/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.integradorintermediov2;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author neo_0
 */
public class MesaDeAyuda {

    public static void agregarColchonHorasEstimadas(Incidente incidente, int horasColchon) {
        // Lógica para agregar "colchón" de horas estimadas al incidente
        incidente.setTiempoEstimado(incidente.getTiempoEstimado() + horasColchon);
        System.out.println("Se agregó un colchón de " + horasColchon + " horas al incidente.");
    }

    public static void guardarNotificacionEnArchivo(Tecnico tecnico, String razonSocial, int tiempoEstimado, String descripcionProblema) {
        String mensaje = "Estimado cliente, el técnico " + tecnico.getNombre() + " se encargará de resolver el incidente. "
                + "Tiempo estimado de resolución: " + tiempoEstimado + " horas. Descripción del problema: " + descripcionProblema;

        // Lógica para guardar notificación en un archivo de texto
        try (PrintWriter writer = new PrintWriter(new FileWriter("notificacion.txt", true))) {
            writer.println(mensaje);
            System.out.println("Notificación guardada en el archivo 'notificacion.txt'");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al guardar la notificación en el archivo.");
        }
    }
}
