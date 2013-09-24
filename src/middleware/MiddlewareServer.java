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
		
		while(true)
		{

			try {
				socket = middlewareSocket.accept();
			} catch (IOException e) {
				System.out.println("Input/Output error" + e.toString());
			}
			
			new ClientHandler(socket).run();
		}
			
		
	}
}
