package client;

import java.util.Scanner;

public class RunClient {
    public static void main(String[] args) {
        try {
            Client cliente = new Client();
            cliente.startCliente();
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("1. Iniciar sesión");
                System.out.println("2. Ver productos");
                System.out.println("3. Ver historial");
                System.out.println("4. Mostrar personas");
                System.out.println("5. Comprar producto");
                System.out.println("6. Salir");
                System.out.print("Seleccione una opción: ");
                
                int opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1: cliente.iniciarSesion(scanner); break;
                    case 2: cliente.mostrarProductos(scanner); break;
                    case 3: cliente.mostrarHistorialDeUsuario(); break;
                    case 4: cliente.mostrarPersonas(); break;
                    case 5: cliente.comprarProducto(scanner); break;
                    case 6: System.exit(0);
                    default: System.out.println("Opción inválida");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}