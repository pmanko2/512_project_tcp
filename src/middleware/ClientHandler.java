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
	private Socket carsSocket;
	private Socket flightsSocket;
	private Socket roomsSocket;
	private static String CARS_RM;
	private static String FLIGHTS_RM;
	private static String ROOMS_RM;
	private static int RM_PORT;
	
	
	
	public ClientHandler(Socket server)
	{
		super("ClientHandler");
		this.clientSocket = server;
		
		// three different rm's depending on method
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
		
		openRMSockets();
		
		try
		{
			// create input reader and output streams
			clientInput = clientSocket.getInputStream();
			inputReader = new BufferedReader(new InputStreamReader(clientInput));
			out = new DataOutputStream(clientSocket.getOutputStream());
			
			String clientRequest;
			
			// keep listening to client requests -- infinite loop
			while(true)
			{
				// loop until reader has nothing left to read from socket
				while((clientRequest = inputReader.readLine()) != null)
				{
					System.out.println(clientRequest);
					
					JSONObject jsonRequest = new JSONObject(clientRequest);
					String rmResponse = processJson(jsonRequest);
					
					// right now we return this - need to return RM response
					// wait for RM response before returning
					out.writeBytes(rmResponse + "\n");
					out.flush();
					break;
				}
				
				//System.out.println("Stopped reading client input -- continuing to listen in while loop");
			}
			

		} catch(IOException e)
		{
			System.out.println("Input/Output Exception");
		} catch (JSONException e) 
		{
			System.out.println("json error: " + e.toString());
		}
		
	}
	
	// method that processes jsonobject 
	// new socket is opened and connection to RM is made
	private String processJson(JSONObject toProcess)
	{
		String rmResponse = null;
		
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
			rmResponse = sendJSONToRM(rmServer, request.toString());
						
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rmResponse;
	}
	
	// depending on what method was called we choose the appropriate RM server
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
	
	private void openRMSockets()
	{
		try {
			carsSocket = new Socket(CARS_RM, RM_PORT);
			flightsSocket = new Socket(FLIGHTS_RM, RM_PORT);
			roomsSocket = new Socket(ROOMS_RM, RM_PORT);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// open connection to RM server and passes json string through -- returns JSON response string
	@SuppressWarnings("resource")
	private String sendJSONToRM(String rmServer, String json)
	{
		DataOutputStream toRM = null;
		BufferedReader fromRM = null;
		String response = "Response Error";
		
		try
		{

			if(rmServer.equals(CARS_RM))
			{
				toRM = new DataOutputStream(carsSocket.getOutputStream());
				fromRM = new BufferedReader(new InputStreamReader(carsSocket.getInputStream()));
			}
			else if(rmServer.equals(FLIGHTS_RM))
			{
				toRM = new DataOutputStream(flightsSocket.getOutputStream());
				fromRM = new BufferedReader(new InputStreamReader(carsSocket.getInputStream()));
			}
			else
			{
				toRM = new DataOutputStream(roomsSocket.getOutputStream());
				fromRM = new BufferedReader(new InputStreamReader(carsSocket.getInputStream()));
			}
			
			toRM.writeBytes(json + "\n");
			toRM.flush();
			
			
			// wait for response from server and eventually we will have to pass this response back
			// to the client
			while((response = fromRM.readLine()) != null)
			{
				System.out.println(rmServer + " Response: " + response);
				
				return response;
				
			}
			
		} catch(UnknownHostException e) {
			System.err.println("Host not recognized");
			System.exit(1);
		} catch(IOException e){
			System.err.println("IO exception occurred in the connection");
			System.exit(1);
		}
		
		return response;
	}
}
