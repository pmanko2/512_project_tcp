package ResImpl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClientHandler extends Thread
{
	private Socket clientSocket;
	private Socket carsSocket;
	private Socket flightsSocket;
	private Socket roomsSocket;
	private MiddlewareServer middleware;
	private static String CARS_RM;
	private static String FLIGHTS_RM;
	private static String ROOMS_RM;
	private static int RM_PORT;
	
	
	
	public ClientHandler(Socket server, MiddlewareServer middleware)
	{
		super("ClientHandler");
		this.clientSocket = server;
		this.middleware = middleware;
		
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
			
			if(needsMiddlewareProcessing(method))
			{
				return processInMiddleWare(method, paramArray);
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
				fromRM = new BufferedReader(new InputStreamReader(flightsSocket.getInputStream()));
			}
			else
			{
				toRM = new DataOutputStream(roomsSocket.getOutputStream());
				fromRM = new BufferedReader(new InputStreamReader(roomsSocket.getInputStream()));
			}
			
			toRM.writeBytes(json + "\n");
			toRM.flush();
			
			
			// wait for response from server and eventually we will have to pass this response back
			// to the client
			while((response = fromRM.readLine()) != null)
			{
				System.out.println(rmServer + " Response: " + response);
				
				break;
				
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
	
	private boolean needsMiddlewareProcessing(String method)
	{
		return method.equals("new_customer") || method.equals("delete_customer") || method.equals("query_customer")
				|| method.equals("reserve_car") || method.equals("reserve_flight") 
				|| method.equals("reserve_room");
	}
	
	private String processInMiddleWare(String method, JSONArray paramArray)
	{
		try
		{
			if(method.equals("new_customer"))
			{
				int customerID = newCustomer(paramArray.getInt(0));
				return returnResponse(customerID, "new_customer");
			}
			else if(method.equals("query_customer"))
			{
				String custInfo = queryCustomerInfo(paramArray.getInt(0), paramArray.getInt(1));
				return returnResponse(custInfo, "query_customer");
			}
			else if(method.equals("reserve_car"))
			{
				boolean reserved = reserveCar(paramArray.getInt(0), paramArray.getInt(1), paramArray.getString(2));
				return returnResponse(reserved, "reserve_car");
			}
			else if(method.equals("reserve_flight"))
			{
				boolean reserved = reserveFlight(paramArray.getInt(0), paramArray.getInt(1), paramArray.getInt(2));
				return returnResponse(reserved, "reserve_flight");
			}
			else if(method.equals("reserve_room"))
			{
				boolean reserved = reserveRoom(paramArray.getInt(0), paramArray.getInt(1), paramArray.getString(2));
				return returnResponse(reserved, "reserve_room");
			}
			else if(method.equals("delete_customer"))
			{
				
			}
			else
			{
				
			}

				
		} catch(JSONException e)
		{
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean reserveFlight(int id, int customer, int flightNumber)
			throws RemoteException, JSONException {
			
        return reserveItem(id, customer, Flight.getKey(flightNumber), String.valueOf(flightNumber));
	}

	public boolean reserveCar(int id, int customer, String location)
			throws RemoteException, JSONException {

        return reserveItem(id, customer, Car.getKey(location), location);
	}

	public boolean reserveRoom(int id, int customer, String location)
			throws RemoteException, JSONException {

        return reserveItem(id, customer, Hotel.getKey(location), location);
	}
	
	protected boolean reserveItem(int id, int customerID, String key, String location) throws JSONException {
	    	try 
	    	{
	    		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
		        // Read customer object if it exists (and read lock it)
		        Customer cust = (Customer) middleware.readData( id, Customer.getKey(customerID) );        
		        if ( cust == null ) {
		            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
		            return false;
		        } 
		        
		        //parse key to find out if item is a car, flight, or room
		        String delims = "[-]";
		        String[] tokens = key.split(delims);
		        
		        String item = null;
		        
		        // check if the item is available
		        //if it's a flight
		        if (tokens[0].equals("flight"))
		        {
		        	JSONObject request = new JSONObject();
		        	request.put("method", "reserve_item");
		        	
		        	JSONArray params = new JSONArray();
		        	params.put(0, id);
		        	params.put(1, key);
		        	
		        	request.put("parameters", params);
		        	
		        	item = sendJSONToRM(FLIGHTS_RM, request.toString());
		        }
		        //else if the item is a car
		        else if (tokens[0].equals("car"))
		        {
		        	JSONObject request = new JSONObject();
		        	request.put("method", "reserve_item");
		        	
		        	JSONArray params = new JSONArray();
		        	params.put(0, id);
		        	params.put(1, key);
		        	
		        	request.put("parameters", params);
		        	
		        	item = sendJSONToRM(CARS_RM, request.toString());
		        }
		        //otherwise it's a room
		        else
		        {
		        	JSONObject request = new JSONObject();
		        	request.put("method", "reserve_item");
		        	
		        	JSONArray params = new JSONArray();
		        	params.put(0, id);
		        	params.put(1, key);
		        	
		        	request.put("parameters", params);
		        	
		        	item = sendJSONToRM(ROOMS_RM, request.toString());
		        }
		        
		        JSONObject resItemReturn = new JSONObject(item);
		        
		        boolean isNull = resItemReturn.getBoolean("is_null");
		        int count = resItemReturn.getInt("count");
		        int price = resItemReturn.getInt("price");
		        
		        if (isNull) {
		            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
		            return false;
		        } else if (count==0) {
		            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
		            return false;
		        } else {            
		            cust.reserve( key, location, price);        
		            middleware.writeData( id, cust.getKey(), cust );
		            
		            // decrease the number of available items in the storage
		            String itemReserved;
		            boolean resource_updated = false;
		            
		            if (tokens[0].equals("flight"))
			        {
			        	JSONObject request = new JSONObject();
			        	request.put("method", "item_reserved");
			        	
			        	JSONArray params = new JSONArray();
			        	params.put(0, id);
			        	params.put(1, key);
			        	
			        	request.put("parameters", params);

			        	Trace.info(request.toString());
			        	
			        	itemReserved = sendJSONToRM(FLIGHTS_RM, request.toString());
			        }
			        //else if the item is a car
			        else if (tokens[0].equals("car"))
			        {
			        	JSONObject request = new JSONObject();
			        	request.put("method", "item_reserved");
			        	
			        	JSONArray params = new JSONArray();
			        	params.put(0, id);
			        	params.put(1, key);
			        	
			        	request.put("parameters", params);
			        	Trace.info(request.toString());
			        	
			        	itemReserved = sendJSONToRM(CARS_RM, request.toString());
			        }
			        //otherwise it's a room
			        else
			        {
			        	JSONObject request = new JSONObject();
			        	request.put("method", "item_reserved");
			        	
			        	JSONArray params = new JSONArray();
			        	params.put(0, id);
			        	params.put(1, key);
			        	
			        	request.put("parameters", params);
			        	
			        	itemReserved = sendJSONToRM(ROOMS_RM, request.toString());
			        }

		            JSONObject itemReservedReturn = new JSONObject(itemReserved);
		            resource_updated = itemReservedReturn.getBoolean("response");
		            
		            if (resource_updated)
		            {
			            Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
			            return true;
		            }
		            else 
		            {
		            	return false;
		            }
		        } 
	    	}catch(JSONException e)
	    	{
	    		e.printStackTrace();
	    		return false;
	    	}
	    }
	 
	    public int newCustomer(int id)
	            throws RemoteException
        {
            Trace.info("INFO: RM::newCustomer(" + id + ") called" );
            // Generate a globally unique ID for the new customer
            int cid = Integer.parseInt( String.valueOf(id) +
                                    String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                    String.valueOf( Math.round( Math.random() * 100 + 1 )));
            Customer cust = new Customer( cid );
            middleware.writeData( id, cust.getKey(), cust );
            Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
            return cid;
        }

        // I opted to pass in customerID instead. This makes testing easier
        public boolean newCustomer(int id, int customerID )
            throws RemoteException
        {
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
            Customer cust = (Customer) middleware.readData( id, Customer.getKey(customerID) );
            if ( cust == null ) {
                cust = new Customer(customerID);
                middleware.writeData( id, cust.getKey(), cust );
                Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
                return true;
            } else {
                Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
                return false;
            } // else
        }
        
 	   // return a bill
        public String queryCustomerInfo(int id, int customerID)
            throws RemoteException
        {
            Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
            Customer cust = (Customer) middleware.readData( id, Customer.getKey(customerID) );
            if ( cust == null ) {
                Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
            } else {
                    String s = cust.printBill();
                    Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                    System.out.println( s );
                    return s;
            } // if
        }
        
        // Deletes customer from the database. 
    	public boolean deleteCustomer(int id, int customerID)
    	        throws RemoteException, JSONException
    	    {
    	        Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
    	        Customer cust = (Customer) middleware.readData( id, Customer.getKey(customerID) );
    	        if ( cust == null ) {
    	            Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
    	            return false;
    	        } else {            
    	            // Increase the reserved numbers of all reservable items which the customer reserved. 
    	            RMHashtable reservationHT = cust.getReservations();
    	            for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {        
    	                String reservedkey = (String) (e.nextElement());
    	                ReservedItem reserveditem = cust.getReservedItem(reservedkey);
    	                Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );

    	                //determine whether this item is a flight,room, or car
    	                String key = reserveditem.getKey();  
    	                String delims = "[-]";
    	    	        String[] tokens = key.split(delims);
    	    	        
    	    	        //int id, int customerID, String key, ReservedItem reserveditem
    	    	        
    	    	        JSONObject json = new JSONObject();
			        	JSONObject request = new JSONObject();
			        	request.put("method", "item_unreserve");
			        	
			        	JSONArray params = new JSONArray();
			        	params.put(0, id);
			        	params.put(1, customerID);
			        	params.put(2, key);
			        	
			        	JSONObject reserved = new JSONObject();
			        	reserved.put("key", reserveditem.getKey());
			        	reserved.put("count", reserveditem.getCount());
			        	
			        	params.put(3, reserved.toString());
    	    	        
    	    	        String item = null;
    	    	        // check if the item is available
    	    	        //if it's a flight
    	    	        if (tokens[0].equals("flight"))
    	    	        {
    			        	
    			        	item = sendJSONToRM(FLIGHTS_RM, json.toString());
    	    	        }
    	    	        //else if the item is a car
    	    	        else if (tokens[0].equals("car"))
    	    	        {
    	    	        	item = sendJSONToRM(CARS_RM, json.toString());
    	    	        }
    	    	        //otherwise it's a room
    	    	        else
    	    	        {
    	    	        	item = sendJSONToRM(ROOMS_RM, json.toString());
    	    	        }              
    	            }
    	            
    	            // remove the customer from the storage
    	            middleware.removeData(id, cust.getKey());
    	            
    	            Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
    	            return true;
    	        } // if
    	    }
        
        // overloaded methods to handle sending rm information back to client (middleware)
	    private String returnResponse(boolean response, String method)
	    {
	    	String responseString = "";
	    	JSONObject responseJson = new JSONObject();
	    	
	    	try {
	    		responseJson.put("method", method);
				responseJson.put("response_type", "boolean");
				responseJson.put("response", response);
				System.out.println("RM Response is: " + responseJson.toString());
				
				responseString = responseJson.toString();
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	    	
	    	return responseString;
	    }
	    
	    private String returnResponse(int response, String method)
	    {
	    	JSONObject responseJson = new JSONObject();
	    	String responseString = "";
	    	
	    	try {
	    		responseJson.put("method", method);
				responseJson.put("response_type", "integer");
				responseJson.put("response", response);
				System.out.println("RM Response is: " + responseJson.toString());

				responseString = responseJson.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	return responseString;
	    	
	    }
	    
	    private String returnResponse(String response, String method)
	    {
	    	JSONObject responseJson = new JSONObject();
	    	String responseString = "";
	    	
	    	try {
	    		responseJson.put("method", method);
				responseJson.put("response_type", "string");
				responseJson.put("response", response);
				System.out.println("RM Response is: " + responseJson.toString());
				
				responseString = responseJson.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	return responseString;
	    }
}
