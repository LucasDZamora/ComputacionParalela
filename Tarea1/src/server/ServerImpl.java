package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.Historial;
import common.InterfazDeServer;
import common.Persona;


public class ServerImpl implements InterfazDeServer{
    
    private ArrayList<Persona> Bd_personas = new ArrayList<>();
    private ArrayList<Historial> Bd_historial = new ArrayList<>();
    private HashMap<Integer, ArrayList<Persona>> productoClientesMap = new HashMap<>();

    public ServerImpl() throws RemoteException {
        conectarBD();
        getProductos();
        UnicastRemoteObject.exportObject(this, 0);
    }

    private void conectarBD() {
        Connection connection = null;
        Statement query = null;
        ResultSet resultados = null;

        try {
            String url = "jdbc:mysql://localhost:3306/tienda";
            String username = "root";
            String password_BD = "";

            connection = DriverManager.getConnection(url, username, password_BD);
            query = connection.createStatement();

            // Cargar usuarios
            ResultSet rsUsuarios = query.executeQuery("SELECT * FROM usuario");
            while (rsUsuarios.next()) {
                Bd_personas.add(new Persona(
                    rsUsuarios.getInt("id"),
                    rsUsuarios.getString("nombre"),
                    rsUsuarios.getString("apellido"),
                    rsUsuarios.getString("contraseña"),
                    rsUsuarios.getString("correo")
                ));
            }

            // Cargar historial
            ResultSet rsHistorial = query.executeQuery("SELECT * FROM historial");
            while (rsHistorial.next()) {
                Bd_historial.add(new Historial(
                    rsHistorial.getInt("id"),
                    rsHistorial.getInt("id_usuario"),
                    rsHistorial.getInt("id_producto")
                ));
            }

            // Construir mapa de productos
            for (Historial h : Bd_historial) {
                int idProducto = h.getIdProducto();
                Persona persona = findPersonaById(h.getIdUsuario());
                if (persona != null) {
                    productoClientesMap.computeIfAbsent(idProducto, k -> new ArrayList<>())
                                      .add(persona);
                }
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Persona findPersonaById(int idUsuario) {
        for (Persona p : Bd_personas) {
            if (p.getId() == idUsuario) return p;
        }
        return null;
    }

    // Resto de métodos implementados...

    @Override
    public void agregarHistorial(int idUsuario, int idProducto) throws RemoteException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            // 1. Conexión a la BD
            String url = "jdbc:mysql://localhost:3306/tienda";
            connection = DriverManager.getConnection(url, "root", "");
            
            // 2. Insertar en SQL
            String sql = "INSERT INTO historial (id_usuario, id_producto) VALUES (?, ?)";
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, idUsuario);
            statement.setInt(2, idProducto);
            statement.executeUpdate();
            
            // 3. Actualizar mapa en memoria
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int newId = generatedKeys.getInt(1);
                Bd_historial.add(new Historial(newId, idUsuario, idProducto));
                
                Persona persona = findPersonaById(idUsuario);
                if (persona != null) {
                    productoClientesMap.computeIfAbsent(idProducto, k -> new ArrayList<>())
                                      .add(persona);
                }
            }
            
        } catch (SQLException e) {
            throw new RemoteException("Error al guardar en BD: " + e.getMessage());
        } finally {
            // Cerrar recursos
            try { if (statement != null) statement.close(); } catch (SQLException e) {}
            try { if (connection != null) connection.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public ArrayList<Persona> getPersonasQueCompraronProducto(int idProducto) throws RemoteException {
        // Filtrar solo usuarios distintos al actual si es necesario
        return new ArrayList<>(productoClientesMap.getOrDefault(idProducto, new ArrayList<>()));
    }

	@Override
	public ArrayList<common.Persona> getPersonas() throws RemoteException {
		// TODO Auto-generated method stub
		return Bd_personas;
	}

	@Override
	public common.Persona Persona(String nombre, int edad) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void agregarPersona(String nombre, int edad) throws RemoteException {
	}


	@Override
	public Object[] getProductos() throws RemoteException {
	    String output = null;

	    try {
	        URL apiUrl = new URL("https://fakestoreapi.com/products");
	        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
	        connection.setRequestMethod("GET");

	        int responseCode = connection.getResponseCode();

	        if (responseCode == HttpURLConnection.HTTP_OK) {
	            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	            StringBuilder response = new StringBuilder();
	            String inputLine;

	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }

	            in.close();
	            output = response.toString();
	        } else {
	            System.out.println("Error al conectar a la API. Código de respuesta: " + responseCode);
	            return null;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }

	    try {
	        ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode jsonArray = objectMapper.readTree(output);
	        ArrayList<Map<String, Object>> productos = new ArrayList<>();

	        for (JsonNode node : jsonArray) {
	            Map<String, Object> producto = new HashMap<>();
	            producto.put("id", node.get("id").asInt());
	            producto.put("title", node.get("title").asText());
	            producto.put("price", node.get("price").asDouble());
	            producto.put("description", node.get("description").asText());
	            producto.put("category", node.get("category").asText());
	            producto.put("rate", node.get("rating").get("rate").asDouble());
	            producto.put("count", node.get("rating").get("count").asInt());

	            productos.add(producto);
	        }

	        return productos.toArray(); // Devuelve el arreglo de mapas
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return null;
	}


	@Override
	public ArrayList<Historial> getHistorialPorUsuario(int idUsuario) throws RemoteException {
	    ArrayList<Historial> historialUsuario = new ArrayList<>();
	    for (Historial h : Bd_historial) {
	        if (h.getIdUsuario() == idUsuario) {
	            historialUsuario.add(h);
	        }
	    }
	    return historialUsuario;
	}


	@Override

	public Persona login(String correo, String contraseña) throws RemoteException {
	    for (Persona persona : Bd_personas) {
	        if (persona.getCorreo().equals(correo) && persona.getContraseña().equals(contraseña)) {
	            return persona;
	        }
	    }
	    return null; // Login fallido
	}







}
