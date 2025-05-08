package common;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Persona implements Serializable{
	
	private int id;
	private String nombre;
	private String apellido;
	private String contraseña;
	private String correo;
	
	public Persona(int id, String nombre, String apellido, String contraseña, String correo) {
		this.id = id;
		this.nombre = nombre;
		this.apellido = apellido;
		this.contraseña = contraseña;
		this.correo = correo;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getContraseña() {
		return contraseña;
	}

	public void setContraseña(String contraseña) {
		this.contraseña = contraseña;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}
	
	
	

	
}
