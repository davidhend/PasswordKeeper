package com.example.passwordkeeper_rev2;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class Authenticate {

    private static final String TAG = "bluetooth1";
    
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private static OutputStream outStream = null;
     
    // SPP UUID service 
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
   
    // MAC-address of Bluetooth module
	// You will need to change the address to match your bluetooth adapter 
    private static String address = "00:12:08:17:13:14"; 
    
	public void onCreate(Bundle savedInstanceState) {
		//super.onCreate();
		//Toast.makeText(Authenticate.this,"Authenticated ...", Toast.LENGTH_LONG).show();

		btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
        
        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
         
        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
         
        try {
          btSocket = createBluetoothSocket(device);
        } catch (IOException e1) {
      	 errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
        }
                  
        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();
         
        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
          btSocket.connect();
          Log.d(TAG, "...Connection ok...");
        } catch (IOException e) {
          try {
            btSocket.close();
          } catch (IOException e2) {
            errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
          }
        }
           
        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");
       
        try {
          outStream = btSocket.getOutputStream();
        } catch (IOException e) {
          errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
        
        //send encryption key to password keeper
		//you will want to replace the 0's with random numbers, i.e. 17, 2, 56, 30, 82, 73
		//there are 32 values total that make up the encryption key
        sendData("start:0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0:end");
        
	   	if (outStream != null) {
	     	  try {
	     	    outStream.flush();
	     	  } catch (IOException e) {
	     	    errorExit("Fatal Error", "In onStop() and failed to flush output stream: " + e.getMessage() + ".");
	     	  }
	   	} 
	     	 
	    try {
	      btSocket.close();
	    } catch (IOException e2) {
	      errorExit("Fatal Error", "In onStop() and failed to close socket." + e2.getMessage() + ".");
	    }  

	}
	
	
	 private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
		    if(Build.VERSION.SDK_INT >= 10){
		        try {
		            final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
		            return (BluetoothSocket) m.invoke(device, MY_UUID);
		        } catch (Exception e) {
		            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
		        }
		    }
		    return  device.createRfcommSocketToServiceRecord(MY_UUID);
		}

	
	//@Override
	public void onDestroy() {
		//super.onDestroy();		
		//Toast.makeText(this, "Disconnected ...", Toast.LENGTH_LONG).show();
	   	if (outStream != null) {
	     	  try {
	     	    outStream.flush();
	     	  } catch (IOException e) {
	     	    errorExit("Fatal Error", "In onStop() and failed to flush output stream: " + e.getMessage() + ".");
	     	  }
	   	} 
	     	 
	    try {
	      btSocket.close();
	    } catch (IOException e2) {
	      errorExit("Fatal Error", "In onStop() and failed to close socket." + e2.getMessage() + ".");
	    }  
	}
	
	
 private void checkBTState() {
     if(btAdapter==null) { 
       errorExit("Fatal Error", "Bluetooth not support");
     } else {
       if (btAdapter.isEnabled()) {
         Log.d(TAG, "...Bluetooth ON...");
       } else {
     	Log.d(TAG, "...Turn On Bluetooth...");
       }
     }
   }
  
   private static void errorExit(String title, String message){
     //Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
     //finish();
   }
  
   static void sendData(String message) {
     byte[] msgBuffer = message.getBytes();
  
     Log.d(TAG, "...Send data: " + message + "...");
  
     try {
       outStream.write(msgBuffer);
     } catch (IOException e) {
       String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
       if (address.equals("00:00:00:00:00:00")) 
         msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
       	msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
        
       	errorExit("Fatal Error", msg);       
     }
   }

}


