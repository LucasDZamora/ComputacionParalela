package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
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
		
	public ServerImpl() throws RemoteException{
		conectarBD();
		getProductos();
		UnicastRemoteObject.exportObject(this,0);
	}
	
	private ArrayList<Persona> Bd_personas= new ArrayList<>();
	private ArrayList<Historial> Bd_historial= new ArrayList<>();
	
	
	public void conectarBD() {
		Connection connection = null;
		Statement query = null;
		//PreparedStatement test = null;
		ResultSet resultados = null;
		
		try {
			
			String url = "jdbc:mysql://localhost:3306/tienda";
			String username= "root";
			String password_BD ="";
			
			connection = DriverManager.getConnection(url, username, password_BD);
			
			query = connection.createStatement();
			String sql= "SELECT * FROM usuario";
			resultados = query.executeQuery(sql);
			
			while(resultados.next()) {
				int id = resultados.getInt("id");
				String nombre = resultados.getString("nombre");
				String apellido = resultados.getString("apellido");
				String contraseña = resultados.getString("contraseña");
				String correo = resultados.getString("correo");
				
				Persona Persona = new Persona (id,nombre,apellido,contraseña,correo); 
				Bd_personas.add(Persona);
				System.out.println(id + " " + nombre + " " + apellido + " " + contraseña + " " + correo);
			}
			
			String sqlHistorial = "SELECT * FROM historial";
			ResultSet rsHistorial = query.executeQuery(sqlHistorial);

			while (rsHistorial.next()) {
			    int id = rsHistorial.getInt("id");
			    int idUsuario = rsHistorial.getInt("id_usuario");
			    int idProducto = rsHistorial.getInt("id_producto");
			    Bd_historial.add(new Historial(id, idUsuario, idProducto));
			}
			
			System.out.println("Conexion exitosa");
			connection.close();

		} catch(SQLException e) {
			e.printStackTrace();
			System.out.println("Error BD");
		}
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
	public String getDataFromApi() {
		String output = null;
		 
		try {
            // URL de la API REST, el listado de APIs públicas está en: 
			// https://github.com/juanbrujo/listado-apis-publicas-en-chile
            URL apiUrl = new URL("https://fakestoreapi.com/products");

            // Abre la conexión HTTP
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

            // Configura la solicitud (método GET en este ejemplo)
            connection.setRequestMethod("GET");

            // Obtiene el código de respuesta
            int responseCode = connection.getResponseCode();

            // Procesa la respuesta si es exitosa
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Lee la respuesta del servidor
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                // Cierra la conexión y muestra la respuesta
                in.close();
                output = response.toString();
            } else {
                System.out.println("Error al conectar a la API. Código de respuesta: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		//Como resultado tenemos un String output que contiene el JSON de la respuesta de la API
		return output;
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
