package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import common.Historial;
import common.InterfazDeServer;
import common.Persona;

public class Client {
	
	
    private InterfazDeServer server;
    private Persona usuarioLogueado = null;

    public Client() {}

    public void startCliente() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", 1009);
        server = (InterfazDeServer) registry.lookup("server");
    }

    public void mostrarPersonas() throws RemoteException {
        ArrayList<Persona> personas = server.getPersonas();
        int cuentaPersonas = 0;
        for (Persona persona : personas) {
            System.out.println("ID: " + persona.getId());
            System.out.println("Nombre: " + persona.getNombre());
            System.out.println("Apellido: " + persona.getApellido());
            System.out.println("Correo: " + persona.getCorreo());
            System.out.println("Contraseña: " + persona.getContraseña());
            System.out.println("---------------");
            cuentaPersonas++;
        }
        System.out.println("Cantidad de personas registradas: " + cuentaPersonas);
    }

    public void mostrarProductos(Scanner scanner) throws RemoteException {
    	Object[] productos = server.getProductos();

    	for (Object obj : productos) {
    	    @SuppressWarnings("unchecked")
			Map<String, Object> producto = (Map<String, Object>) obj;
    	    System.out.println("Nombre: " + producto.get("title"));
    	    System.out.println("Precio: $" + producto.get("price"));
    	    System.out.println("Categoría: " + producto.get("category"));
    	    System.out.println("Rating: " + producto.get("rate") + " (" + producto.get("count") + " valoraciones)");
    	    System.out.println("Descripción: " + producto.get("description"));
    	    System.out.println("-------------------------");
    	}
    }
    
    
    

    public void iniciarSesion(Scanner scanner) throws RemoteException {
    	
    	if (usuarioLogueado != null) {
            System.out.println("Ya has iniciado sesión como " + usuarioLogueado.getNombre());
            return;
        }
        System.out.print("Correo: ");
        String correo = scanner.nextLine();

        System.out.print("Contraseña: ");
        String contraseña = scanner.nextLine();

        Persona persona = server.login(correo, contraseña);

        if (persona != null) {
            usuarioLogueado = persona;
            System.out.println("¡Inicio de sesión exitoso! Bienvenido, " + persona.getNombre());
        } else {
            System.out.println("Correo o contraseña incorrectos.");
        }
    }
    
    public void mostrarHistorialDeUsuario() throws RemoteException {
        if (usuarioLogueado == null) {
            System.out.println("Debe iniciar sesión primero.");
            return;
        }

        int idUsuario = usuarioLogueado.getId();
        ArrayList<Historial> historial = server.getHistorialPorUsuario(idUsuario);
        Object[] productos = server.getProductos();

        for (Historial h : historial) {
            int idProducto = h.getIdProducto();
            for (Object obj : productos) {
                @SuppressWarnings("unchecked")
                Map<String, Object> producto = (Map<String, Object>) obj;
                if ((int) producto.get("id") == idProducto) {
                    System.out.println("Nombre: " + producto.get("title"));
                    System.out.println("Precio: $" + producto.get("price"));
                    System.out.println("Categoría: " + producto.get("category"));
                    System.out.println("Rating: " + producto.get("rate") + " (" + producto.get("count") + " valoraciones)");
                    System.out.println("Descripción: " + producto.get("description"));
                    System.out.println("-------------------------");
                    break;
                }
            }
        }
    }




}
