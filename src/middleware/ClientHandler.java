package middleware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
		BufferedReader inputReader = null;
		
		try
		{
			clientInput = server.getInputStream();
			inputReader = new BufferedReader(new InputStreamReader(clientInput));
			
			String clientRequest;
			
			while((clientRequest = inputReader.readLine()) != null)
			{
				JSONObject jsonRequest = new JSONObject(clientRequest);
				processJson(jsonRequest);
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
