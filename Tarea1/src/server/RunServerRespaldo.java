package server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import common.InterfazDeServer;

public class RunServerRespaldo {
	public static void main (String [] args) throws RemoteException{
		InterfazDeServer server = new ServerImpl();
		Registry registry = LocateRegistry.createRegistry(1010);
		
		try {
			registry.bind("server", server);
			
			
		}catch ( RemoteException | AlreadyBoundException e) {
			
		}
		
		System.out.println("Servidor respaldo arriba!");
		
	}
}
