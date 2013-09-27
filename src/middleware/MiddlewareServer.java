package middleware;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MiddlewareServer 
{
	public static void main(String[] args)
	{
		ServerSocket middlewareSocket = null;
		Socket socket = null;
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
		}
		catch(IOException e)
		{
			System.err.println("Could not listen on port");
			System.exit(-1);
		}
		
		while(listening)
		{

			try {
				socket = middlewareSocket.accept();
			} catch (IOException e) {
				System.out.println("Input/Output error" + e.toString());
			}
			
			new ClientHandler(socket).run();
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
