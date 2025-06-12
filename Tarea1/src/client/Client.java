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
    private final int primaryPort = 1009;
    private final int backupPort = 1010;
    private boolean connectedToPrimary = true;
    private boolean running = true;
    private final String host = "localhost"; // o IP del servidor

    public Client() throws RemoteException, NotBoundException {
        startCliente();
        startHeartbeat();
    }

    private void startHeartbeat() {
        new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    server.heartbeat();
                } catch (RemoteException e) {
                    if (connectedToPrimary) {
                        System.err.println("Heartbeat fallido, cambiando al servidor de respaldo...");
                        cambiarSvRespaldo();
                    } else {
                        System.err.println("Heartbeat fallido en el servidor de respaldo. Terminando ejecución...");
                        terminarEjecucion();
                    }
                } catch (InterruptedException e) {
                    System.err.println("Heartbeat interrumpido: " + e.getMessage());
                }
            }
        }).start();
    }

    private void startCliente() throws RemoteException, NotBoundException {
        server = establecerConexion(host, primaryPort, "server");

        if (server == null) {
            System.out.println("No se pudo conectar al servidor primario.");
            System.out.println("Intentando conectar al servidor de respaldo...");
            cambiarSvRespaldo();
        } else {
            connectedToPrimary = true;
            System.out.println("Conectado al servidor **primario**.");
        }

        if (server == null) {
            throw new RemoteException("No se pudo conectar a ningún servidor.");
        }
    }


    private void cambiarSvRespaldo() {
        server = establecerConexion(host, backupPort, "server");
        if (server == null) {
            System.err.println("No se pudo conectar al servidor de respaldo.");
            terminarEjecucion();
        } else {
            System.out.println("Conectado al servidor **de respaldo**.");
            connectedToPrimary = false;
        }
    }


    private InterfazDeServer establecerConexion(String host, int port, String nombre) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            return (InterfazDeServer) registry.lookup(nombre);
        } catch (Exception e) {
            System.err.println("Error al conectar con " + nombre + " en puerto " + port + ": " + e.getMessage());
            return null;
        }
    }

    private String traducirCategoria(String categoriaEnIngles) {
        Map<String, String> traducciones = new HashMap<>();
        traducciones.put("men's clothing", "Ropa de Hombre");
        traducciones.put("women's clothing", "Ropa de Mujer");
        traducciones.put("jewelery", "Joyería");
        traducciones.put("electronics", "Electrónica");

        return traducciones.getOrDefault(categoriaEnIngles, categoriaEnIngles);
    }

    public ArrayList<Map<String, Object>> mostrarProdCategoria(Scanner scanner) throws RemoteException {
        Object[] productos = server.getProductos();
        Map<String, ArrayList<Map<String, Object>>> productosPorCategoria = new HashMap<>();

        for (Object obj : productos) {
            @SuppressWarnings("unchecked")
            Map<String, Object> producto = (Map<String, Object>) obj;
            String categoria = (String) producto.get("category");
            productosPorCategoria.computeIfAbsent(categoria, c -> new ArrayList<>()).add(producto);
        }

        System.out.println("Categorías disponibles:");
        ArrayList<String> categorias = new ArrayList<>(productosPorCategoria.keySet());
        for (int i = 0; i < categorias.size(); i++) {
            System.out.println((i + 1) + ". " + traducirCategoria(categorias.get(i)));
        }

        int opcionCategoria;
        while (true) {
            System.out.print("Seleccione una categoría (número): ");
            String entrada = scanner.nextLine();
            try {
                opcionCategoria = Integer.parseInt(entrada);
                if (opcionCategoria >= 1 && opcionCategoria <= categorias.size()) break;
                System.out.println("Opción fuera de rango. Intente nuevamente.");
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Debe ingresar un número.");
            }
        }

        String categoriaSeleccionada = categorias.get(opcionCategoria - 1);
        ArrayList<Map<String, Object>> filtrados = productosPorCategoria.get(categoriaSeleccionada);

        System.out.println("Productos en la categoría: " + traducirCategoria(categoriaSeleccionada));
        for (Map<String, Object> prod : filtrados) {
            System.out.println("ID: " + prod.get("id"));
            System.out.println("Nombre: " + prod.get("title"));
            System.out.println("Precio: $" + prod.get("price"));
            System.out.println("Rating: " + prod.get("rate") + " (" + prod.get("count") + " valoraciones)");
            System.out.println("Descripción: " + prod.get("description"));
            System.out.println("-------------------------");
        }

        return filtrados;
    }

    public void mostrarProductos(Scanner scanner) throws RemoteException {
        mostrarProdCategoria(scanner);
    }

    private void terminarEjecucion() {
        running = false;
        System.err.println("Terminando ejecución debido a la falta de respuesta del servidor.");
        System.exit(1);
    }

    public void comprarProducto(Scanner scanner) throws RemoteException {
        if (usuarioLogueado == null) {
            System.out.println("Debe iniciar sesión primero.");
            return;
        }

        ArrayList<Map<String, Object>> productosFiltrados = mostrarProdCategoria(scanner);

        Map<String, Object> productoSeleccionado = null;
        int idProducto;
        while (productoSeleccionado == null) {
            System.out.print("Ingrese el ID del producto a comprar: ");
            try {
                idProducto = Integer.parseInt(scanner.nextLine());
                for (Map<String, Object> prod : productosFiltrados) {
                    if (((Integer) prod.get("id")) == idProducto) {
                        productoSeleccionado = prod;
                        break;
                    }
                }
                if (productoSeleccionado == null) System.out.println("ID no válido. Seleccione un ID listado.");
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Debe ingresar un número.");
            }
        }

        server.agregarHistorial(usuarioLogueado.getId(), (Integer) productoSeleccionado.get("id"));
        System.out.println("¡Compra realizada!");

        Object[] todosProductos = server.getProductos();
        Map<Integer, Map<String, Object>> productosMap = new HashMap<>();
        for (Object obj : todosProductos) {
            @SuppressWarnings("unchecked")
            Map<String, Object> prod = (Map<String, Object>) obj;
            productosMap.put((Integer) prod.get("id"), prod);
        }

        ArrayList<Persona> compradores = server.getPersonasQueCompraronProducto((Integer) productoSeleccionado.get("id"));
        compradores.removeIf(p -> p.getId() == usuarioLogueado.getId());

        Map<Integer, Map<String, Object>> recomendaciones = new HashMap<>();
        for (Persona p : compradores) {
            ArrayList<Historial> hist = server.getHistorialPorUsuario(p.getId());
            for (int i = hist.size() - 1; i >= 0; i--) {
                int pid = hist.get(i).getIdProducto();
                if (pid != (Integer) productoSeleccionado.get("id") && productosMap.containsKey(pid)) {
                    recomendaciones.put(pid, productosMap.get(pid));
                    break;
                }
            }
        }

        if (recomendaciones.isEmpty()) {
            System.out.println("Nadie más ha comprado este producto aún.");
            String categoria = (String) productoSeleccionado.get("category");
            System.out.println("Recomendaciones basadas en categoría (" + traducirCategoria(categoria) + "):");
            productosMap.values().stream()
                .filter(prod -> prod.get("category").equals(categoria))
                .forEach(prod -> {
                    System.out.println("ID: " + prod.get("id"));
                    System.out.println("Nombre: " + prod.get("title"));
                    System.out.println("Precio: $" + prod.get("price"));
                    System.out.println("-------------------------");
                });
        } else {
            System.out.println("Personas que compraron este producto también compraron:");
            recomendaciones.values().forEach(prod -> {
                System.out.println("ID: " + prod.get("id"));
                System.out.println("Nombre: " + prod.get("title"));
                System.out.println("Precio: $" + prod.get("price"));
                System.out.println("-------------------------");
            });
        }

        System.out.print("¿Desea seguir comprando? (s/n) ");
        if (scanner.nextLine().equalsIgnoreCase("s")) {
            comprarProducto(scanner);
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

    public void mostrarPersonas() throws RemoteException {
        ArrayList<Persona> personas = server.getPersonas();
        int cuenta = 0;
        for (Persona p : personas) {
            System.out.println("ID: " + p.getId());
            System.out.println("Nombre: " + p.getNombre());
            System.out.println("Apellido: " + p.getApellido());
            System.out.println("Correo: " + p.getCorreo());
            System.out.println("Contraseña: " + p.getContraseña());
            System.out.println("---------------");
            cuenta++;
        }
        System.out.println("Cantidad de personas registradas: " + cuenta);
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
            int idProd = h.getIdProducto();
            for (Object obj : productos) {
                @SuppressWarnings("unchecked")
                Map<String, Object> prod = (Map<String, Object>) obj;
                if ((Integer) prod.get("id") == idProd) {
                    System.out.println("Nombre: " + prod.get("title"));
                    System.out.println("Precio: $" + prod.get("price"));
                    System.out.println("Categoría: " + traducirCategoria((String) prod.get("category")));
                    System.out.println("Rating: " + prod.get("rate") + " (" + prod.get("count") + " valoraciones)");
                    System.out.println("Descripción: " + prod.get("description"));
                    System.out.println("-------------------------");
                    break;
                }
            }
        }
    }
}
