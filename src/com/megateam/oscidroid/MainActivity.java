package com.megateam.oscidroid;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

public class MainActivity extends Activity {
	//private DataServConn dataServConn;
	public static DisplayView displayView;
	public static Handler mainActivityHandler;
	
	public final static int numPointsPerChan=1024;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Log.i("RTG", "Starting Activity");

		mainActivityHandler = new Handler();
		//LinearLayout myLayout = (LinearLayout) findViewById(R.id.main);
		
		Log.i("RTG", "Start service");
		startService(new Intent(this, DataServ.class));
		
		
		//dataServConn= new DataServConn(getApplicationContext(), null);
		//if (dataServConn.bindService()==true) {
		//	Log.i("RTG", "Service bind!!!");
		//	dataServConn.sayHello();
		//} else {
		//	Log.i("RTG", "Service NOT BOUND!!!");
		//}
                               
        Intent i = new Intent("com.megateam.oscidroid.ACTION_BIND");
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
		
		
		//dataThread=new DataThread(new genData(numPointsPerChan));
		////dataThread=new DataThread(new usbData(numPointsPerChan));
		////dataThread=new DataThread(new netData(numPointsPerChan));
		

		//osciAppContext.displayView=new DisplayView(this, osciAppContext.numPointsPerChan);
		//osciAppContext.displayView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
		//myLayout.addView(osciAppContext.displayView);
		//
		//osciAppContext.displayView.newChannel(0);
		//osciAppContext.displayView.newChannel(1);
        //
		//osciAppContext.displayView.setClickable(true);
		//osciAppContext.displayView.setOnTouchListener(new OnTouchListener(){
		//	public boolean onTouch(View v, MotionEvent event) {
        //
		//		//v.getContext()
		//		
	    //        float screenX = event.getX();
	    //        float screenY = event.getY();
	    //        float viewX = screenX - v.getLeft();
	    //        float viewY = screenY - v.getTop();
		//			
	    //        Intent intent;
	    //            
		//		Log.i("RTG", String.format("onTouch %f %f %f %f %d", screenX, screenY, viewX, viewY, v.getHeight())); //Log Message
		//		
		//		switch (event.getAction()) {
		//		case MotionEvent.ACTION_DOWN:
		//			Log.i("RTG", String.format(String.format("ACTION_DOWN"))); //Log Message
		//			intent = new Intent(v.getContext(), controlMenu.class);
		//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//			v.getContext().startActivity(intent);
		//			break;
		//		case MotionEvent.ACTION_MOVE:
		//			Log.i("RTG", String.format(String.format("ACTION_MOVE"))); //Log Message
		//			break;
		//		case MotionEvent.ACTION_UP:
		//			Log.i("RTG", String.format(String.format("ACTION_UP"))); //Log Message
		//			break;
		//		}
        //        return false;
        //    }
		//});
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		Log.i("RTG", "MainActivity: onStart"); //Log Message
		//dataServConn.bindService();
		//dataServConn.addSource(1,2,3);
		//dataServConn.sayHello();
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Log.i("RTG", "MainActivity: onDestroy"); //Log Message
		unbindService(mConnection);
		
		//dataServConn.stopService();
		//dataServConn.unbindService();
    	//dataServConn.closeConn();   	

		//stopService(new Intent(this, DataServ.class));
		//Log.i("RTG", "MainActivity: onDestroy->stopService"); //Log Message
	}
	
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
   class createControlMenuIntent implements Runnable { 
       
       public createControlMenuIntent() {

       } 
   
       @Override 
       public void run() { 
    	   Intent intent = new Intent(getApplicationContext(), controlMenu.class);
    	   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	   getApplicationContext().startActivity(intent);
       }
   } 

	
	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

	
    /** Messenger for sending messages to the service. */
    Messenger mServiceMessenger = null;
	
	//////////////////////////////////////////////////////////
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service. We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mServiceMessenger = new Messenger(service);

            
            
            Log.i("RTG", String.format("onServiceConnected -----> %s", className.getClassName()));
            
            
            // Now that we have the service messenger, lets send our messenger
            Message msg = Message.obtain(null, DataServ.MSG_SAY_HELLO, 0, 0);
            //msg.replyTo = mClientMessenger;

            /*
             * In case we would want to send extra data, we could use Bundles:
             * Bundle b = new Bundle(); b.putString("key", "hello world");
             * msg.setData(b);
             */

            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            
    		LinearLayout myLayout = (LinearLayout) findViewById(R.id.main);
    		
    		displayView=new DisplayView(getApplicationContext(), mServiceMessenger, numPointsPerChan);
    		displayView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
    		myLayout.addView(displayView);
    		
    		displayView.setClickable(true);
    		displayView.setOnTouchListener(new OnTouchListener(){
    			public boolean onTouch(View v, MotionEvent event) {
            
    				//v.getContext()
    				
    	            float screenX = event.getX();
    	            float screenY = event.getY();
    	            float viewX = screenX - v.getLeft();
    	            float viewY = screenY - v.getTop();
    					
    	            Intent intent;
    	                
    				Log.i("RTG", String.format("onTouch %f %f %f %f %d", screenX, screenY, viewX, viewY, v.getHeight())); //Log Message
    				
    				switch (event.getAction()) {
    				case MotionEvent.ACTION_DOWN:
    					Log.i("RTG", String.format(String.format("ACTION_DOWN %f", viewX))); //Log Message
    					break;
    				case MotionEvent.ACTION_MOVE:
    					Log.i("RTG", String.format(String.format("ACTION_MOVE %f", viewX))); //Log Message
    					displayView.setDisplayXOffset(viewX);
    		            Message msg = Message.obtain(null, DataServ.MSG_DRAW_DISPLAY, 0, 0);
    		            try {
    		                mServiceMessenger.send(msg);
    		            } catch (RemoteException e) {
    		                e.printStackTrace();
    		            }

    					break;
    				case MotionEvent.ACTION_UP:
    					Log.i("RTG", String.format(String.format("ACTION_UP"))); //Log Message
    					mainActivityHandler.post(new createControlMenuIntent());
    					
    					/*intent = new Intent(v.getContext(), controlMenu.class);
    					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    					v.getContext().startActivity(intent);*/
    					break;
    				}
                    return false;
                }
    		});
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mServiceMessenger = null;
        }
    };

}
