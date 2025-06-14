package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface InterfazDeServer extends Remote {
    public ArrayList<Persona> getPersonas() throws RemoteException;
    public Persona Persona(String nombre, int edad) throws RemoteException; 
    public boolean agregarPersona(String nombre, String apellido, String contraseña, String correo) throws RemoteException;
    public boolean actualizarUsuario(Persona persona) throws RemoteException;
    public boolean borrarUsuario(int idUsuario) throws RemoteException;

    Object[] getProductos() throws RemoteException;
    ArrayList<Historial> getHistorialPorUsuario(int idUsuario) throws RemoteException;
    Persona login(String correo, String contraseña) throws RemoteException;
    void agregarHistorial(int idUsuario, int idProducto) throws RemoteException;
    ArrayList<Persona> getPersonasQueCompraronProducto(int idProducto) throws RemoteException;
    public int heartbeat() throws RemoteException;
    
    boolean requestMutex() throws RemoteException;
    void releaseMutex() throws RemoteException;
}
