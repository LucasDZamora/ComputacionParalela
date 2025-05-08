package client;

import java.util.Scanner;

public class RunClient {
    public static void main(String[] args) {
        try {
            Client cliente = new Client();
            cliente.startCliente();
            System.out.println("Cliente conectado al servidor.");

            Scanner scanner = new Scanner(System.in);  

            while (true) {
                System.out.println("1. Iniciar sesión");
                System.out.println("2. Ver productos");
                System.out.println("3. Ver historial");
                System.out.println("4. Salir");
                System.out.print("Seleccione una opción: ");
                int opcion = scanner.nextInt();
                scanner.nextLine(); // Limpiar buffer

                switch (opcion) {
                    case 1:
                        cliente.iniciarSesion(scanner);
                        break;
                    case 2:
                        cliente.mostrarProductos(scanner);
                        break;
                    case 3:
                        cliente.mostrarHistorialDeUsuario();
                        break;
                    case 4:
                        cliente.mostrarPersonas();
                        break;
                    case 5:
                        System.out.println("Adiós!");
                        System.exit(0);
                    default:
                        System.out.println("Opción no válida.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error en el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
