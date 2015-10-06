package com.megateam.oscidroid;


//http://www.marioalmeida.eu/2014/02/21/how-to-do-android-ipc-using-messenger-to-a-remote-service/

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

public class DataServ extends Service {
  private DataThread dataThread=null;
	
  static final int MSG_REGISTER = 1;
  static final int MSG_SAY_HELLO = 2;
  static final int MSG_START_STOP = 3;
  static final int MSG_STOP_SERVICE = 4;
  static final int MSG_ADD_SOURCE = 5;
  static final int MSG_DEL_SOURCE = 6;
  static final int MSG_NUM_CHANNELS = 7;
  static final int MSG_SET_ATTENUATION = 8;
  static final int MSG_SET_OFFSET = 9;
  static final int MSG_DRAW_DISPLAY = 10;

  private DisplayView dpv=null;
  
  /**
   * Handler of incoming messages from clients.
   */
  class IncomingHandler extends Handler {
  	
      @Override
      public void handleMessage(Message msg) {
          	switch (msg.what) {
              case MSG_REGISTER:
                  /*
                   * Do whatever we want with the client messenger: Messenger
                   * clientMessenger = msg.replyTo
                   */
                  /*Toast.makeText(getApplicationContext(),
                          "Service : received client Messenger!",
                          Toast.LENGTH_SHORT).show();*/
              	if (msg.replyTo!=null) {
              		Messenger mess = msg.replyTo; //retrieves messenger from the message
              		Message m = new Message();
              		m.what=MSG_NUM_CHANNELS;
              		m.arg1=4;
              		m.arg2=getEnabledChannelsMaskDataThread();
              		try {
							mess.send(m);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
              	}
                  break;
              case MSG_SAY_HELLO:
                  /*
                   * Do whatever we want with the client messenger: Messenger
                   * clientMessenger = msg.replyTo
                   */
              	Log.i("RTG", "Service : Client said hello!");
                  Toast.makeText(getApplicationContext(),
                          "Service : Client said hello!", Toast.LENGTH_SHORT)
                          .show();
                  if (msg.obj!=null) {
                	dpv=(DisplayView) msg.obj;
                  //if (msg.arg2==69) {
                	//dpv=MainActivity.displayView;
                  	createDataThread(dpv);
                  }
                  break;
              case MSG_START_STOP:
                   Log.i("RTG", String.format("Service MSG_START_STOP dataThread %s", (dataThread==null)?"null":"ok"));
                   stopStartDataThread();
                   /*if (osciAppContext.dataThread!=null)
                  	 osciAppContext.dataThread.startStop();*/
              	break;
              case MSG_DRAW_DISPLAY:
                  Log.i("RTG", String.format("Service MSG_DRAW_DISPLAY dataThread %s", (dataThread==null)?"null":"ok"));
              	drawDisplayDataThread();                	
              	break;
              //case MSG_SOURCE_GEN: 
              //	osciAppContext.dataThread=new DataThread(new genData(osciAppContext.numPointsPerChan), osciAppContext); 
              //	break;
              //case MSG_SOURCE_USB: 
              //	osciAppContext.dataThread=new DataThread(new usbData(osciAppContext.numPointsPerChan), osciAppContext); 
              //	break;
              //case MSG_SOURCE_NET: 
              //	osciAppContext.dataThread=new DataThread(new netData(osciAppContext.numPointsPerChan), osciAppContext); 
              //	break;
              case MSG_STOP_SERVICE:
              	Log.i("RTG", "Service stopSelf");
              	stopSelf();
              	break;
              case MSG_ADD_SOURCE:
              	Log.i("RTG", String.format("MSG_ADD_SOURCE %d", msg.arg1));
             		addChannel2Thread(msg.arg1);
              	break;
              case MSG_DEL_SOURCE:
              	Log.i("RTG", String.format("MSG_DEL_SOURCE %d", msg.arg1));
             		delChannel2Thread(msg.arg1);
              	
             		//dataThread.addChannel(0, 0, 0);
              	break;
              case MSG_SET_ATTENUATION:
              	Log.i("RTG", String.format("MSG_SET_ATTENUATION %d %d", msg.arg1, msg.arg2));
              	setAttenuation2Thread(msg.arg1, msg.arg2);
              	break;
              case MSG_SET_OFFSET:
              	Log.i("RTG", String.format("MSG_SET_OFFSET %d %d", msg.arg1, msg.arg2));
              	setOffset2Thread(msg.arg1, msg.arg2);
              	break;
              default:
                  super.handleMessage(msg);
              }
      	//}
      }
  }

  /**
   * Target we publish for clients to send messages to IncomingHandler.Note
   * that calls to its binder are sequential!
   */
  final Messenger mServiceMessenger = new Messenger(new IncomingHandler());

  /**
   * When binding to the service, we return an interface to our messenger for
   * sending messages to the service.
   */
  @Override
  public IBinder onBind(Intent intent) {
      Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT)
              .show();
      return mServiceMessenger.getBinder();
  }

  @Override
  public void onCreate() {
      super.onCreate();
      /*
       * You might want to start this Service on Foreground so it doesnt get
       * killed
       */
      //osciAppContext=(OsciApp)getApplicationContext();
      Log.i("RTG", "Service onCreate END");
  }
  
	@Override
	public void onStart(Intent intent, int startId) {

		// For time consuming an long tasks you can launch a new thread here...
		//Toast.makeText(this, String.format("Service Started %X", osciAppContext.sourceType), Toast.LENGTH_LONG).show();

		//if (osciAppContext==null) osciAppContext=(OsciApp)getApplicationContext();
		//
		//osciAppContext=(OsciApp)getApplicationContext();
		//if (osciAppContext.dataThread!=null) {
	    //    if (osciAppContext.sourceType==osciAppContext.SOURCE_TYPE_GEN) {
	    //    	osciAppContext.dataThread=new DataThread(new genData(osciAppContext.numPointsPerChan), osciAppContext);
	    //        Log.i("RTG", "Starting Generator dataThread");
	    //    } else if (osciAppContext.sourceType==osciAppContext.SOURCE_TYPE_NET) {
	    //    	osciAppContext.dataThread=new DataThread(new netData(osciAppContext.numPointsPerChan), osciAppContext);
	    //    } else if(osciAppContext.sourceType==osciAppContext.SOURCE_TYPE_USB) {
	    //    	osciAppContext.dataThread=new DataThread(new usbData(osciAppContext.numPointsPerChan), osciAppContext);
	    //    } else {
	    //    	;
	    //    }
		//} else {
		//	Log.i("RTG", "Service onStart osciAppContext.dataThread=null!!!!!!!");
		//}
		Log.i("RTG", "Service onStart");
	}

	
	@Override
	public void onDestroy() {
		
      Log.i("RTG", "Service onDestroy");

      //if (osciAppContext.dataThread!=null)
		//osciAppContext.dataThread.stopThread();
		
      Log.i("RTG", "Service onDestroy END");
		//Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
  }
	
	private genData gData;
	void createDataThread(DisplayView dpv) {
		dataThread=new DataThread(dpv);
		gData=new genData(MainActivity.numPointsPerChan);
	}

	void addChannel2Thread(int chIdx) {
		dataThread.addChannel(gData, chIdx);
	}
	void delChannel2Thread(int chIdx) {
		dataThread.delChannel(chIdx);
	}
	void stopStartDataThread() {
		dataThread.startStop();
	}
	void drawDisplayDataThread() {
		dataThread.drawDisplay();
	}
	int getEnabledChannelsMaskDataThread() {
		return dataThread.getEnabledChannelsMask();
	}
	void setAttenuation2Thread(int channel, int attenuation) {
		dataThread.setAttenuation(channel, attenuation);
	}
	void setOffset2Thread(int channel, int offset) {
		dataThread.setOffset(channel, offset);
	}

}