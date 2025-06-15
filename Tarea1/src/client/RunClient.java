package client;

import java.util.Scanner;

public class RunClient {
    public static void main(String[] args) {
        try {
            Client cliente = new Client();
            Scanner scanner = new Scanner(System.in);

            // Mensaje de bienvenida
            System.out.println("=======================================");
            System.out.println("  ¡Bienvenido a la Tienda de Productos!");
            System.out.println("     Electrónica, Ropa, Joyería y más");
            System.out.println("=======================================\n");

            while (true) {
                System.out.println("Menú principal:");
                System.out.println("1. Iniciar sesión");
                System.out.println("2. Ver productos");
                System.out.println("3. Ver historial");
                System.out.println("4. Mostrar personas");
                System.out.println("5. Comprar producto");
                System.out.println("6. Modificar Datos");
                System.out.println("7. Borrar cuenta");
                System.out.println("8. Registrarse");
                System.out.println("9. Ver mis datos personales");
                System.out.println("10. Salir");
                System.out.print("Seleccione una opción: ");
                
                int opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1: cliente.iniciarSesion(scanner); break;
                    case 2: cliente.mostrarProductos(scanner); break;
                    case 3: cliente.mostrarHistorialDeUsuario(); break;
                    case 4: cliente.mostrarPersonas(); break;
                    case 5: cliente.comprarProducto(scanner); break;
                    case 6: cliente.modificarDatos(scanner); break;
                    case 7: cliente.borrarCuenta(scanner); break;
                    case 8: cliente.registrarse(scanner); break; 
                    case 9: cliente.verMisDatos(scanner); break; 
                    case 10: 
                        System.out.println("Gracias por visitar la tienda. ¡Hasta pronto!");
                        System.exit(0);
                    default: System.out.println("Opción inválida");
                }

                System.out.println(); 
            }
        } catch (Exception e) {
            System.out.println("Error al iniciar el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
