package middleware;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MiddlewareServer 
{
	public static void main(String[] args)
	{
		ServerSocket middlewareSocket = null;
		boolean listening = true;
		int port;
		
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
				new ClientHandler(socket).start();
				
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
}
