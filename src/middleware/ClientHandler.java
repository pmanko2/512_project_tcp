package middleware;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClientHandler extends Thread
{
	private Socket server;
	
	public ClientHandler(Socket server)
	{
		super("ClientHandler");
		this.server = server;
	}

	@Override
	public void run() 
	{
		InputStream clientInput = null;
		BufferedReader inputReader = null;
		DataOutputStream out = null; 
		
		try
		{
			clientInput = server.getInputStream();
			inputReader = new BufferedReader(new InputStreamReader(clientInput));
			out = new DataOutputStream(server.getOutputStream());
			
			String clientRequest;
			
			while((clientRequest = inputReader.readLine()) != null)
			{
				System.out.println(clientRequest);
				
				JSONObject jsonRequest = new JSONObject(clientRequest);
				processJson(jsonRequest);
				
				out.writeBytes("Received and processed json\n");
				out.flush();
			}
			

		} catch(IOException e)
		{
			System.out.println("Input/Output Exception");
		} catch (JSONException e) 
		{
			System.out.println("json error: " + e.toString());
		}
		
	}
	
	private void processJson(JSONObject toProcess)
	{
		try {
			System.out.println("JSONObject is: " + toProcess.toString());
			JSONObject request = toProcess.getJSONObject("request");
			
			String method = request.getString("method");
			JSONArray paramArray = request.getJSONArray("parameters");
			
			System.out.println("Method: " + method);
			
			for(int i = 0; i < paramArray.length(); i++)
			{
				System.out.println("Param " + i + ": " + paramArray.get(i));
			}
						
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
