package middleware;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClientHandler extends Thread
{
	private Socket clientSocket;
	private static String CARS_RM;
	private static String FLIGHTS_RM;
	private static String ROOMS_RM;
	private static int RM_PORT;
	
	
	
	public ClientHandler(Socket server)
	{
		super("ClientHandler");
		this.clientSocket = server;
		
		CARS_RM = "lab2-10.cs.mcgill.ca";
		FLIGHTS_RM = "lab2-11.cs.mcgill.ca";
		ROOMS_RM = "lab2-12.cs.mcgill.ca";
		RM_PORT = 1107;
	}

	@Override
	public void run() 
	{
		InputStream clientInput = null;
		BufferedReader inputReader = null;
		DataOutputStream out = null; 
		
		try
		{
			clientInput = clientSocket.getInputStream();
			inputReader = new BufferedReader(new InputStreamReader(clientInput));
			out = new DataOutputStream(clientSocket.getOutputStream());
			
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
			
			String rmServer = chooseRMServer(method);
			openRMSocketConnection(rmServer, request.toString());
						
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String chooseRMServer(String method)
	{
		if(method.equals("new_car") || method.equals("delete_car") || method.equals("query_car_location")
				|| method.equals("query_car_price"))
		{
			return CARS_RM;
		}
		else if(method.equals("new_flight") || method.equals("delete_flight") 
				|| method.equals("query_flight_location") || method.equals("query_flight_price"))
		{
			return FLIGHTS_RM;
		}
		else
		{
			return ROOMS_RM;
		}
	}
	
	private void openRMSocketConnection(String rmServer, String json)
	{
		Socket clientSocket = null;
		DataOutputStream toRM = null;
		BufferedReader fromRM = null;
		
		try
		{
			clientSocket = new Socket(rmServer, RM_PORT);
			toRM = new DataOutputStream(clientSocket.getOutputStream());
			fromRM = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			toRM.writeBytes(json + "\n");
			toRM.flush();
			
			String response;
			
			while((response = fromRM.readLine()) != null)
			{
				System.out.println("Server Response: " + response);
				
			}
			
		} catch(UnknownHostException e) {
			System.err.println("Host not recognized");
			System.exit(1);
		} catch(IOException e){
			System.err.println("IO exception occurred in the connection");
			System.exit(1);
		}
	}
}
