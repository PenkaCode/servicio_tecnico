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
import java.util.Scanner;
import lombok.Data;

/**
 *
 * @author neo_0
 */
@Data

public class Cliente {

    private Long Id;
    private String razonSocial;
    private String cuit;
    private String NumeroTelefono;

    public static Cliente cargarClienteDesdeConsola(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\n");

        System.out.println("Ingrese el CUIT del cliente:");
        String cuit = scanner.next();

        // Obtener información del cliente desde la consola
        System.out.println("Ingrese la razón social del cliente:");
        String razonSocial = scanner.next();

        System.out.println("Ingrese el número de teléfono del cliente:");
        String numeroTelefono = scanner.next();

        // Crear instancia de PreparedStatement para ejecutar la consulta parametrizada
        String sql = "INSERT INTO cliente (razon_social, cuit, numero_telefono) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Establecer los parámetros en la consulta
            statement.setString(1, razonSocial);
            statement.setString(2, cuit);
            statement.setString(3, numeroTelefono);

            // Ejecutar la consulta
            statement.executeUpdate();

            System.out.println("Cliente ingresado correctamente.");

            // Obtener el ID generado para el cliente
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(generatedKeys.getLong(1));
                cliente.setRazonSocial(razonSocial);
                cliente.setCuit(cuit);
                cliente.setNumeroTelefono(numeroTelefono);
                return cliente;
            } else {
                throw new SQLException("Error al obtener el ID generado para el cliente.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al cargar el cliente en la base de datos.", e);
        }
    }
}
