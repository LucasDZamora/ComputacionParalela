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
    private boolean inUse;
    
    public ServerImpl() throws RemoteException {
        conectarBD();
        getProductos();
        UnicastRemoteObject.exportObject(this, 0);
    }

    private void conectarBD() {
        Connection connection = null;
        Statement query = null;

        try {
            String url = "jdbc:mysql://localhost:3306/tienda";
            String username = "root";
            String password_BD = "";

            connection = DriverManager.getConnection(url, username, password_BD);
            query = connection.createStatement();

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

            ResultSet rsHistorial = query.executeQuery("SELECT * FROM historial");
            while (rsHistorial.next()) {
                Bd_historial.add(new Historial(
                    rsHistorial.getInt("id"),
                    rsHistorial.getInt("id_usuario"),
                    rsHistorial.getInt("id_producto")
                ));
            }

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


    @Override
    public void agregarHistorial(int idUsuario, int idProducto) throws RemoteException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            String url = "jdbc:mysql://localhost:3306/tienda";
            connection = DriverManager.getConnection(url, "root", "");
            
            String sql = "INSERT INTO historial (id_usuario, id_producto) VALUES (?, ?)";
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, idUsuario);
            statement.setInt(2, idProducto);
            statement.executeUpdate();
            
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
        } {
            try { if (statement != null) statement.close(); } catch (SQLException e) {}
            try { if (connection != null) connection.close(); } catch (SQLException e) {}
        }
    }

    @Override
    public ArrayList<Persona> getPersonasQueCompraronProducto(int idProducto) throws RemoteException {
        return new ArrayList<>(productoClientesMap.getOrDefault(idProducto, new ArrayList<>()));
    }

	@Override
	public ArrayList<common.Persona> getPersonas() throws RemoteException {
		// TODO Auto-generated method stub
        while (true) {
            if (requestMutex()) {
                System.out.println("Tengo permiso para iniciar la sección crítica.");
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Aún no tengo permiso...");
        }

        int duracionSleep = 8000;
        System.out.println("Obteniendo todos los usuarios. Tiempo estimado: " + duracionSleep + " ms.");

        try {
            Thread.sleep(duracionSleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
        	return Bd_personas;
        } finally {
            releaseMutex();
        }
	}

	@Override
	public common.Persona Persona(String nombre, int edad) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Persona buscarUsuarioPorCorreo(String correo) throws RemoteException {
	    for (Persona p : Bd_personas) {
	        if (p.getCorreo().equalsIgnoreCase(correo)) {
	            return p;
	        }
	    }
	    return null; 
	}


	@Override
	public boolean agregarPersona(String nombre, String apellido, String correo, String contraseña) throws RemoteException {

	    while (true) {
	        if (requestMutex()) {
	            System.out.println("Tengo permiso para iniciar la sección crítica.");
	            break;
	        }
	        try {
	            Thread.sleep(2000);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        System.out.println("Aún no tengo permiso...");
	    }

	    Connection connection = null;
	    PreparedStatement checkStmt = null;
	    PreparedStatement insertStmt = null;

	    try {
	        String url = "jdbc:mysql://localhost:3306/tienda";
	        String username = "root";
	        String password = "";

	        connection = DriverManager.getConnection(url, username, password);

	        // Verificar si el correo ya existe
	        String checkSql = "SELECT COUNT(*) FROM usuario WHERE correo = ?";
	        checkStmt = connection.prepareStatement(checkSql);
	        checkStmt.setString(1, correo);
	        ResultSet rs = checkStmt.executeQuery();
	        if (rs.next() && rs.getInt(1) > 0) {
	            System.out.println("El correo ya está registrado. No se puede agregar el usuario.");
	            return false;
	        }

	        // Si el correo no existe, proceder con el insert
	        String insertSql = "INSERT INTO usuario (nombre, apellido, contraseña, correo) VALUES (?, ?, ?, ?)";
	        insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
	        insertStmt.setString(1, nombre);
	        insertStmt.setString(2, apellido);
	        insertStmt.setString(3, contraseña);
	        insertStmt.setString(4, correo);
	        insertStmt.executeUpdate();
	        
	        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
	        if (generatedKeys.next()) {
	            int nuevoId = generatedKeys.getInt(1); 
	            Persona nuevaPersona = new Persona(nuevoId, nombre, apellido, contraseña, correo);
	            Bd_personas.add(nuevaPersona); 
	        }

	        int duracionSleep = 8000;
	        System.out.println("Iniciando inserción. Tiempo estimado: " + duracionSleep + " ms.");

	        try {
	            Thread.sleep(duracionSleep);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }

	        return true;

	    } catch (SQLException e) {
	        System.out.println("Insert fallido! No se pudo agregar al usuario.");
	        return false;

	    } finally {
	        releaseMutex();
	        System.out.println("Inserción finalizada para: " + nombre + ".");

	        try {
	            if (checkStmt != null) checkStmt.close();
	            if (insertStmt != null) insertStmt.close();
	            if (connection != null) connection.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
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

	        return productos.toArray(); 
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
	
	@Override
	public int heartbeat() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public Persona getUsuarioPorId(int idUsuario) throws RemoteException {
	    while (true) {
	        if (requestMutex()) {
	            System.out.println("Tengo permiso para iniciar la sección crítica.");
	            break;
	        }
	        try {
	            Thread.sleep(2000);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        System.out.println("Aún no tengo permiso...");
	    }

	    Connection connection = null;
	    PreparedStatement statement = null;
	    ResultSet rs = null;

	    try {
	        String url = "jdbc:mysql://localhost:3306/tienda";
	        String username = "root";
	        String password_BD = "";

	        connection = DriverManager.getConnection(url, username, password_BD);

	        String sql = "SELECT * FROM usuario WHERE id = ?";
	        statement = connection.prepareStatement(sql);
	        statement.setInt(1, idUsuario);
	        rs = statement.executeQuery();
	        
	        int duracionSleep = 8000;
	        System.out.println("Obteniendo usuario. Tiempo estimado: " + duracionSleep + " ms.");

	        try {
	            Thread.sleep(duracionSleep);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }

	        if (rs.next()) {
	            return new Persona(
	                rs.getInt("id"),
	                rs.getString("nombre"),
	                rs.getString("apellido"),
	                rs.getString("contraseña"),
	                rs.getString("correo")
	            );
	        } else {
	            return null;
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return null;

	    } finally {
	        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
	        try { if (statement != null) statement.close(); } catch (SQLException e) { e.printStackTrace(); }
	        try { if (connection != null) connection.close(); } catch (SQLException e) { e.printStackTrace(); }
	        releaseMutex();
	    }
	}


	
	@Override
	public synchronized boolean requestMutex() throws RemoteException {
		// TODO Auto-generated method stub
		if(inUse) {
			return false;
		} else { 
			inUse = true;
			return true;
		}
	}
	
	@Override
	public synchronized void releaseMutex() throws RemoteException {
		// TODO Auto-generated method stub
		inUse = false;
	}

	@Override
	public boolean actualizarUsuario(Persona persona) throws RemoteException {
	    while (true) {
	        if (requestMutex()) {
	            System.out.println("Tengo permiso para iniciar la sección crítica.");
	            break;
	        }
	        try {
	            Thread.sleep(2000);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        System.out.println("Aún no tengo permiso...");
	    }

	    Connection connection = null;
	    PreparedStatement statement = null;

	    try {
	        String url = "jdbc:mysql://localhost:3306/tienda";
	        String username = "root";
	        String password_BD = "";

	        connection = DriverManager.getConnection(url, username, password_BD);

	        String sql = "UPDATE usuario SET nombre=?, apellido=?, correo=?, contraseña=? WHERE id=?";
	        statement = connection.prepareStatement(sql);
	        statement.setString(1, persona.getNombre());
	        statement.setString(2, persona.getApellido());
	        statement.setString(3, persona.getCorreo());
	        statement.setString(4, persona.getContraseña());
	        statement.setInt(5, persona.getId());

	        int duracionSleep = 8000;
	        System.out.println("Actualizando usuario... Tiempo estimado: " + duracionSleep + " ms.");
	        try {
	            Thread.sleep(duracionSleep);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }

	        int filasActualizadas = statement.executeUpdate();

	        if (filasActualizadas > 0) {
	            // Actualizar en memoria
	            Persona personaEnMemoria = findPersonaById(persona.getId());
	            if (personaEnMemoria != null) {
	                personaEnMemoria.setNombre(persona.getNombre());
	                personaEnMemoria.setApellido(persona.getApellido());
	                personaEnMemoria.setCorreo(persona.getCorreo());
	                personaEnMemoria.setContraseña(persona.getContraseña());
	            }
	            return true;
	        } else {
	            return false;
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;

	    } finally {
	        releaseMutex();
	        try {
	            if (statement != null) statement.close();
	            if (connection != null) connection.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}

	@Override
	public boolean borrarUsuario(int idUsuario) throws RemoteException {
	    
	    while (true) {
	        if (requestMutex()) {
	            System.out.println("Tengo permiso para iniciar la sección crítica.");
	            break;
	        }
	        try {
	            Thread.sleep(2000);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        System.out.println("Aún no tengo permiso...");
	    }

	    Connection connection = null;
	    PreparedStatement statement = null;

	    try {
	        String url = "jdbc:mysql://localhost:3306/tienda";
	        String username = "root";
	        String password_BD = "";

	        connection = DriverManager.getConnection(url, username, password_BD);

	        int duracionSleep = 8000;
	        System.out.println("Eliminando usuario... Tiempo estimado: " + duracionSleep + " ms.");
	        try {
	            Thread.sleep(duracionSleep);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }

	        // Primero borramos su historial
	        String deleteHistorial = "DELETE FROM historial WHERE id_usuario=?";
	        statement = connection.prepareStatement(deleteHistorial);
	        statement.setInt(1, idUsuario);
	        statement.executeUpdate();
	        statement.close();

	        // Luego borramos el usuario
	        String deleteUsuario = "DELETE FROM usuario WHERE id=?";
	        statement = connection.prepareStatement(deleteUsuario);
	        statement.setInt(1, idUsuario);
	        int filasBorradas = statement.executeUpdate();

	        // Limpiar estructuras en memoria
	        Bd_personas.removeIf(p -> p.getId() == idUsuario);
	        Bd_historial.removeIf(h -> h.getIdUsuario() == idUsuario);
	        productoClientesMap.forEach((k, lista) -> lista.removeIf(p -> p.getId() == idUsuario));

	        return filasBorradas > 0;

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        releaseMutex();
	        try {
	            if (statement != null) statement.close();
	            if (connection != null) connection.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}




}
