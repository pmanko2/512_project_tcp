package middleware;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientHandler implements Runnable
{
	private Socket server;
	
	public ClientHandler(Socket server)
	{
		this.server = server;
	}

	@Override
	public void run() 
	{
		InputStream clientInput = null;
		
		try
		{
			clientInput = server.getInputStream();
		} catch(IOException e)
		{
			System.out.println("Input/Output Exception");
		}
		
	}
}
