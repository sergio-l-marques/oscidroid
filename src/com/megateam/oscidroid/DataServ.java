package com.megateam.oscidroid;


//http://www.marioalmeida.eu/2014/02/21/how-to-do-android-ipc-using-messenger-to-a-remote-service/

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;


import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

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
  static final int MSG_SET_WINDOW_PREVIEW_SIZE = 11;
  
  
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
              		
              		Bundle b = new Bundle();
              		//t("numChannels"), msg.getData().getInt("maskChannels"), msg.getData().getInt("windowSize")
                    b.putInt("numChannels", 4);
                    b.putInt("maskChannels", getEnabledChannelsMaskDataThread());
                    b.putInt("windowSize", getWindowPreviewSizeDataThread());
                    m.setData(b);
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
              	L.i( "Service : Client said hello!");
                  Toast.makeText(getApplicationContext(),
                          "Service : Client said hello!", Toast.LENGTH_SHORT)
                          .show();
                  if (msg.obj!=null) {
                	dpv=(DisplayView) msg.obj;
                  	createDataThread(dpv);
                  }
                  break;
              case MSG_START_STOP:
                   L.i( String.format("Service MSG_START_STOP dataThread %s", (dataThread==null)?"null":"ok"));
                   stopStartDataThread();
                   /*if (osciAppContext.dataThread!=null)
                  	 osciAppContext.dataThread.startStop();*/
              	break;
              case MSG_DRAW_DISPLAY:
                  L.i( String.format("Service MSG_DRAW_DISPLAY dataThread %s", (dataThread==null)?"null":"ok"));
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
              	L.i( "Service stopSelf");
              	stopSelf();
              	break;
              case MSG_ADD_SOURCE:
              	L.i( String.format("MSG_ADD_SOURCE %d", msg.arg1));
              	enableChannel(msg.arg1);
              	break;
              case MSG_DEL_SOURCE:
              	L.i( String.format("MSG_DEL_SOURCE %d", msg.arg1));
              	disableChannel(msg.arg1);
              	break;
              case MSG_SET_ATTENUATION:
              	L.i( String.format("MSG_SET_ATTENUATION %d %d", msg.arg1, msg.arg2));
              	setAttenuation2Thread(msg.arg1, msg.arg2);
              	break;
              case MSG_SET_OFFSET:
                	L.i( String.format("MSG_SET_OFFSET %d %d", msg.arg1, msg.arg2));
                	setOffset2Thread(msg.arg1, msg.arg2);
                	break;
              case MSG_SET_WINDOW_PREVIEW_SIZE:
                	L.i( String.format("MSG_SET_WINDOW_PREVIEW_SIZE %d %d", msg.arg1, msg.arg2));
                	setWindowPreviewSize(msg.arg1);
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

      dataThread=new DataThread();

      //osciAppContext=(OsciApp)getApplicationContext();
      L.i( "Service onCreate END");
  }
  
  
  
	//private genData gData;
  	private usbData uData;


	@Override
	public void onStart(Intent intent, int startId) {

		uData=new usbData(getApplicationContext(), intent, MainActivity.numPointsPerChan);
		//gData=new genData(MainActivity.numPointsPerChan);
		
		dataThread.setOsciDevType(uData);
	
        
		L.i( "Service onStart");
	}

	@Override
	public void onDestroy() {
		
      L.i( "Service onDestroy");
      
      //if (osciAppContext.dataThread!=null)
		//osciAppContext.dataThread.stopThread();
		
      L.i( "Service onDestroy END");
		//Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
	}
	
	void createDataThread(DisplayView dpv) {
		dataThread.setDisplayView(dpv);
	}

	void enableChannel(int chIdx) {
		dataThread.enableChannel(chIdx);
	}
	void disableChannel(int chIdx) {
		dataThread.disableChannel(chIdx);
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
	int getWindowPreviewSizeDataThread() {
		return dataThread.getWindowPreviewSize();
	}
	void setAttenuation2Thread(int channel, int attenuation) {
		dataThread.setAttenuation(channel, attenuation);
	}
	void setOffset2Thread(int channel, int offset) {
		dataThread.setOffset(channel, offset);
	}
	void setWindowPreviewSize(int size) {
		dataThread.setWindowPreviewSize(size);
	}
}