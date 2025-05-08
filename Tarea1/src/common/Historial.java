package common;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Historial implements Serializable {
	private int id;
	private int idUsuario;
	private int idProducto;
	
	public Historial(int id, int idUsuario, int idProducto) {
	
		this.id = id;
		this.idUsuario = idUsuario;
		this.idProducto = idProducto;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}

	public int getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(int idProducto) {
		this.idProducto = idProducto;
	}
	
	
	
}
