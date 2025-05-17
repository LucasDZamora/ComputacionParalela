package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
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

    private String traducirCategoria(String categoriaEnIngles) {
        Map<String, String> traducciones = new HashMap<>();
        traducciones.put("men's clothing", "Ropa de Hombre");
        traducciones.put("women's clothing", "Ropa de Mujer");
        traducciones.put("jewelery", "Joyería");
        traducciones.put("electronics", "Electrónica");

        return traducciones.getOrDefault(categoriaEnIngles, categoriaEnIngles);
    }

    public void comprarProducto(Scanner scanner) throws RemoteException {
        if (usuarioLogueado == null) {
            System.out.println("Debe iniciar sesión primero.");
            return;
        }

        Object[] productos = server.getProductos();
        Map<String, ArrayList<Map<String, Object>>> productosPorCategoria = new HashMap<>();

        for (Object obj : productos) {
            @SuppressWarnings("unchecked")
            Map<String, Object> producto = (Map<String, Object>) obj;
            String categoria = (String) producto.get("category");
            productosPorCategoria.putIfAbsent(categoria, new ArrayList<>());
            productosPorCategoria.get(categoria).add(producto);
        }

        System.out.println("Categorías disponibles:");
        ArrayList<String> categorias = new ArrayList<>(productosPorCategoria.keySet());
        for (int i = 0; i < categorias.size(); i++) {
            System.out.println((i + 1) + ". " + traducirCategoria(categorias.get(i)));
        }

        int opcionCategoria = -1;
        while (true) {
            System.out.print("Seleccione una categoría (número): ");
            String entrada = scanner.nextLine();
            try {
                opcionCategoria = Integer.parseInt(entrada);
                if (opcionCategoria >= 1 && opcionCategoria <= categorias.size()) {
                    break;
                } else {
                    System.out.println("Opción fuera de rango. Intente nuevamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Debe ingresar un número.");
            }
        }

        String categoriaSeleccionada = categorias.get(opcionCategoria - 1);
        ArrayList<Map<String, Object>> productosFiltrados = productosPorCategoria.get(categoriaSeleccionada);

        System.out.println("Productos en la categoría: " + traducirCategoria(categoriaSeleccionada));
        for (Map<String, Object> producto : productosFiltrados) {
            System.out.println("ID: " + producto.get("id"));
            System.out.println("Nombre: " + producto.get("title"));
            System.out.println("Precio: $" + producto.get("price"));
            System.out.println("Rating: " + producto.get("rate") + " (" + producto.get("count") + " valoraciones)");
            System.out.println("Descripción: " + producto.get("description"));
            System.out.println("-------------------------");
        }

        int idProducto = -1;
        Map<String, Object> productoSeleccionado = null;

        while (productoSeleccionado == null) {
            System.out.print("Ingrese el ID del producto a comprar: ");
            try {
                idProducto = Integer.parseInt(scanner.nextLine());

                for (Map<String, Object> prod : productosFiltrados) {
                    if ((Integer) prod.get("id") == idProducto) {
                        productoSeleccionado = prod;
                        break;
                    }
                }

                if (productoSeleccionado == null) {
                    System.out.println("ID no válido. Por favor, seleccione un ID de los productos listados.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Debe ingresar un número.");
            }
        }

        server.agregarHistorial(usuarioLogueado.getId(), idProducto);
        System.out.println("¡Compra realizada!");

        Map<Integer, Map<String, Object>> productosMap = new HashMap<>();
        for (Object obj : productos) {
            @SuppressWarnings("unchecked")
            Map<String, Object> producto = (Map<String, Object>) obj;
            productosMap.put((Integer) producto.get("id"), producto);
        }

        ArrayList<Persona> personas = server.getPersonasQueCompraronProducto(idProducto);
        personas.removeIf(p -> p.getId() == usuarioLogueado.getId());

        Map<Integer, Map<String, Object>> recomendaciones = new HashMap<>();

        for (Persona p : personas) {
            ArrayList<Historial> historial = server.getHistorialPorUsuario(p.getId());
            for (int i = historial.size() - 1; i >= 0; i--) {
                int productoId = historial.get(i).getIdProducto();
                if (productoId != idProducto && productosMap.containsKey(productoId)) {
                    recomendaciones.put(productoId, productosMap.get(productoId));
                    break;
                }
            }
        }

        if (recomendaciones.isEmpty()) {
            System.out.println("Nadie más ha comprado este producto aún.");
            Map<String, Object> productoComprado = productosMap.get(idProducto);
            String categoria = (String) productoComprado.get("category");

            System.out.println("Recomendaciones basadas en categoría (" + traducirCategoria(categoria) + "):");

            for (Map<String, Object> producto : productosMap.values()) {
                int prodId = (Integer) producto.get("id");
                if (prodId != idProducto && categoria.equals(producto.get("category"))) {
                    System.out.println("ID: " + producto.get("id"));
                    System.out.println("Nombre: " + producto.get("title"));
                    System.out.println("Precio: $" + producto.get("price"));
                    System.out.println("-------------------------");
                }
            }
        } else {
            System.out.println("Personas que compraron este producto también compraron:");
            recomendaciones.values().forEach(producto -> {
                System.out.println("ID: " + producto.get("id"));
                System.out.println("Nombre: " + producto.get("title"));
                System.out.println("Precio: $" + producto.get("price"));
                System.out.println("-------------------------");
            });
        }

        System.out.println("¿Desea seguir comprando? (s/n)");
        if (scanner.nextLine().equalsIgnoreCase("s")) {
            comprarProducto(scanner);
        }
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
            System.out.println("ID: " + producto.get("id"));
            System.out.println("Nombre: " + producto.get("title"));
            System.out.println("Precio: $" + producto.get("price"));
            System.out.println("Categoría: " + traducirCategoria((String) producto.get("category")));
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
                    System.out.println("Categoría: " + traducirCategoria((String) producto.get("category")));
                    System.out.println("Rating: " + producto.get("rate") + " (" + producto.get("count") + " valoraciones)");
                    System.out.println("Descripción: " + producto.get("description"));
                    System.out.println("-------------------------");
                    break;
                }
            }
        }
    }
}
