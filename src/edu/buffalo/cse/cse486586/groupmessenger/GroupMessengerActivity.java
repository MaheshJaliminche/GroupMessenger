package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
//import edu.buffalo.cse.cse486586.groupmessenger.GroupMessengerActivity.ClientTask;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static String SequencerPort;
    Uri providerUri = Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger.provider");
    static final int SERVER_PORT = 10000;
    private static int ownVectorClock [] = {0, 0, 0,0,0};
    private static int receivedVectorClock[] ={0,0,0,0,0};
    public static int globalSequenceNo = -1;
   // private Hashtable<String,String> msgWaitingQueue = new Hashtable<String,String>();
    private Hashtable<Integer,String> seqWaitingQueue = new Hashtable<Integer,String>();
    static String myPort;   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        
       TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
       String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
       myPort = String.valueOf((Integer.parseInt(portStr) * 2));
       SequencerPort=myPort;
        try {
            
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        
        ContentValues keyValueToInsert = new ContentValues();
          keyValueToInsert.put("key", "key");
          keyValueToInsert.put("value","value");
        
           Uri newUri = getContentResolver().insert(
      		    providerUri,    // assume we already created a Uri object with our provider URI
      		    keyValueToInsert
      		    );    
        
        
        
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs in a total-causal order.
         */
        
        final EditText editText = (EditText) findViewById(R.id.editText1);
        final Button button = (Button) findViewById(R.id.button4);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                //TextView localTextView = (TextView) findViewById(R.id.textView1);
               // localTextView.append("\t" + msg); // This is one way to display a string.
                //int a = 1;
               new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg,"Message");

            }
        });
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            
            BufferedReader br;
           try {
                while(true)
                {
                br = new BufferedReader(new InputStreamReader(serverSocket.accept().getInputStream()));
                String msgReceived=(br.readLine()); //msg received on socket
                
                String msgReceivedArr[]= msgReceived.split(" "); // split of received msg
                int AVDno=ChkMsgFrom(myPort);
               // String key=  msgReceivedArr[2]+String.valueOf(receivedVectorClock[msgFrom]);
                if(msgReceivedArr[3].equalsIgnoreCase("Message"))
                {
		            if(SequencerPort.equalsIgnoreCase(REMOTE_PORT0))
	                {
	                   Sequencer(msgReceivedArr[0]);
	                }
                }
                else
                {
                	//mesg ,msgtype ,seqno
                	seqWaitingQueue.put(Integer.valueOf(msgReceivedArr[2]), msgReceivedArr[0]);
                	while(seqWaitingQueue.containsKey(ownVectorClock[AVDno]))
                	{
                		String Key=String.valueOf(ownVectorClock[AVDno]);
                		String Value=seqWaitingQueue.get(ownVectorClock[AVDno]);
                		seqWaitingQueue.remove(ownVectorClock[AVDno]);
                		//----insert into DB
                		ContentValues keyValueToInsert = new ContentValues();
        		        
      		            keyValueToInsert.put("key",Key );
      		            keyValueToInsert.put("value",Value);
      		            Uri newUri = getContentResolver().insert(
      		      		    providerUri,    // assume we already created a Uri object with our provider URI
      		      		    keyValueToInsert
      		      		    );    
      		            //------------------- 
      		            ownVectorClock[AVDno]+=1;
      		            publishProgress(seqWaitingQueue.get(ownVectorClock[AVDno])+" "+String.valueOf(ownVectorClock[AVDno]));
      		            
                	}
                }
                
                }
            } catch (IOException e) {
            
                Log.e(TAG, "error in displaying content");
            }    
            
             
           return null;
        }
        
            
        private void Sequencer(String Message)
        {
        	globalSequenceNo++;
           	new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,Message,"Order",String.valueOf(globalSequenceNo));
        }
        
        public int ChkMsgFrom(String AVD)
         {
             if(AVD.equalsIgnoreCase(REMOTE_PORT0))
            return 0;
             if(AVD.equalsIgnoreCase(REMOTE_PORT1))
                 return 1;
             if(AVD.equalsIgnoreCase(REMOTE_PORT2))
                 return 2;
             if(AVD.equalsIgnoreCase(REMOTE_PORT3))
                 return 3;
             if(AVD.equalsIgnoreCase(REMOTE_PORT4))
                 return 4;
            return 5;
                        
             
         }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            return;
        }
    }
 
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
           
        	 String MessageToSend;
                if(msgs[3].equalsIgnoreCase("Order"))
                {
                   MessageToSend= msgs[0]+" "+msgs[1]+" "+msgs[2];//Message,messagetype,sequenceno
                   SendMessage(MessageToSend);
                    
                }
                else
                {
                   String msgToSend = msgs[0];
                   msgToSend=msgToSend.trim();             
                   MessageToSend= msgToSend+" "+msgs[1];
                   SendMessage(MessageToSend);
                }
                 
            return null;
        }

        
    }

    public void SendMessage(String messageToSend)
    {
        try{
         int portNo[]={11108,11112,11116,11120,11124};
         
         for (int i = 0; i < portNo.length; i++) {
                Socket socket=new Socket("10.0.2.2",portNo[i]);
                PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                 /*ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
                 outToServer.writeObject(messageToSend);
                 outToServer.flush();
                 outToServer.close();*/
              pw.println(messageToSend); 
              pw.flush(); 
              pw.close();
                 
              //Thread.sleep(1000);
              socket.close();
                
            }
        } catch (UnknownHostException e) {
            Log.e(TAG, "ClientTask UnknownHostException");
        } catch (IOException e) {
            Log.e(TAG, "ClientTask socket IOException");
        }
    }

    


}