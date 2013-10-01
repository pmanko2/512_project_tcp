package ResImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;


public class MiddlewareServer 
{
	protected RMHashtable m_itemHT = new RMHashtable();
	
	public MiddlewareServer(){};
	
	public static void main(String[] args)
	{
		ServerSocket middlewareSocket = null;
		boolean listening = true;
		int port;
		MiddlewareServer currentClass = new MiddlewareServer();
		
		if(args.length > 0)
		{
			port = Integer.parseInt(args[0]);
		}
		else
		{
			port = 1107;
		}
		
		try
		{
			middlewareSocket = new ServerSocket(port);
			System.out.println("Server is listening on port " + middlewareSocket.getLocalPort());
		}
		catch(IOException e)
		{
			System.err.println("Could not listen on port");
			System.exit(-1);
		}
		
		while(listening)
		{
			try {
				System.out.println("Trying to connect to new socket");
				Socket socket = middlewareSocket.accept();
				
				System.out.println("Creating new socket thread: " + socket.getInetAddress());
				new ClientHandler(socket, currentClass).start();
				
			} catch (IOException e) {
				System.out.println("Input/Output error" + e.toString());
			}
		}
		
		try {
			middlewareSocket.close();
			System.out.println("Server Socket has been closed");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}
	
	// Reads a data item
    protected RMItem readData( int id, String key )
    {
        synchronized(m_itemHT) {
            return (RMItem) m_itemHT.get(key);
        }
    }

    // Writes a data item
    @SuppressWarnings("unchecked")
    protected void writeData( int id, String key, RMItem value )
    {
        synchronized(m_itemHT) {
            m_itemHT.put(key, value);
        }
    }
    
    // Remove the item out of storage
    protected RMItem removeData(int id, String key) {
        synchronized(m_itemHT) {
            return (RMItem)m_itemHT.remove(key);
        }
    }
    
    public RMHashtable getCustomerReservations(int id, int customerID)
            throws RemoteException
        {
            Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
            Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
            if ( cust == null ) {
                Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                return null;
            } else {
                return cust.getReservations();
            } // if
        }
}
