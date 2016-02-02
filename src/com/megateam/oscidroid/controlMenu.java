package com.megateam.oscidroid;


import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.ToggleButton;
//import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnDrawListener;
//import android.view.WindowInsets;

public class controlMenu extends FragmentActivity {
	
	private ArrayList<TabHost.TabSpec> tabHostList = new ArrayList<TabHost.TabSpec>();
	private int numChannels=0;
	private Handler handler2UI;
	
	
    /** Messenger for sending messages to the service. */
    private Messenger mServiceMessenger = null;
    /** Messenger for receiving messages from the service. */
    private Messenger mClientMessenger = null;

    /**
     * Target we publish for clients to send messages to IncomingHandler. Note
     * that calls to its binder are sequential!
     */
    private IncomingHandler handler;

    /**
     * Handler thread to avoid running on the main thread (UI)
     */
    private HandlerThread handlerThread;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mBound;

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {

        public IncomingHandler(HandlerThread thr) {
            super(thr.getLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DataServ.MSG_SAY_HELLO:
                Toast.makeText(getApplicationContext(),
                        "ctrlMenu Client : Service said hello!", Toast.LENGTH_SHORT)
                        .show();
                break;
            case DataServ.MSG_NUM_CHANNELS:
            	numChannels=msg.getData().getInt("numChannels");
            	L.i(String.format("ctrlMenu Client : Service said %d Channels %X!", numChannels,msg.getData().getInt("maskChannels")));
            	
            	handler2UI.post(new createChannelToggleBtnAndTabs(msg.getData().getInt("numChannels"), msg.getData().getInt("maskChannels"), msg.getData().getInt("windowSize")));
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service. We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mServiceMessenger = new Messenger(service);

            
            L.i( String.format("onServiceConnected --> %s", className.getClassName()));
            
            
            // Now that we have the service messenger, lets send our messenger
            Message msg = Message.obtain(null, DataServ.MSG_REGISTER, 0, 0);
            msg.replyTo = mClientMessenger;

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
            
            
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mServiceMessenger = null;
            mBound = false;
        }
    };

    private TabHost tabHost;
    private SeekBar windowPreviewSeekBar;
	private Button btnStartStop, btnHello;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		L.i( String.format("ctrlMenu: onCreate"));

		handler2UI= new Handler();
		
        handlerThread = new HandlerThread("IPChandlerThread");
        handlerThread.start();
        handler = new IncomingHandler(handlerThread);
        mClientMessenger = new Messenger(handler);

        Intent i = new Intent("com.megateam.oscidroid.ACTION_BIND");
        //i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //Intent i = new Intent("com.example.rtg_v1");
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        //bindService(getIntent(), mConnection, Context.BIND_AUTO_CREATE);

		setContentView(R.layout.control_menu);


		tabHost = (TabHost)findViewById(R.id.tab_host);
		tabHost.setup();

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
	        public void onTabChanged(String arg0) {
	        	setTabBackgroundColor(tabHost);
	        }
	    });
		
		TabSpec spec = tabHost.newTabSpec("settings");
		spec.setContent(R.id.tab_settings);
		spec.setIndicator("settings");
		tabHost.addTab(spec);
		tabHostList.add(spec);
		
		spec = tabHost.newTabSpec("trigger");
		spec.setContent(R.id.tab_trigger);
		spec.setIndicator("trigger");
		tabHost.addTab(spec);
		tabHostList.add(spec);

			//set Windows tab as default (zero based)
		tabHost.setCurrentTab(0);
		setTabBackgroundColor(tabHost);

		btnStartStop = (Button) findViewById(R.id.stopstart);
    	btnStartStop.setOnClickListener(onClickListenerCB);
    	
		btnHello = (Button) findViewById(R.id.hello);
    	btnHello.setOnClickListener(onClickListenerCB);
    	
    	//ToggleButton toggleButtonCh = (ToggleButton) findViewById(R.id.windowButton);
    	//toggleButtonCh.setOnClickListener(onClickListenerCB);
    	
    	windowPreviewSeekBar=(SeekBar)(findViewById(R.id.windowSeekBar));
    	windowPreviewSeekBar.setMax(MainActivity.numPointsPerChan-1);
    	windowPreviewSeekBar.setProgress(MainActivity.numPointsPerChan-1);
    	windowPreviewSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar windowPreviewSeekBar) {
			}
			@Override
			public void onStartTrackingTouch(SeekBar windowPreviewSeekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar windowPreviewSeekBar, int progress, boolean fromUser) {
				if(fromUser){
					L.i(String.format("ctrlMenu Client : onProgressChanged progress-->%d", progress));
					setWindowPreviewSize(progress+1);
					//drawDisplay();
				}
			}
		});
	}
	
	private void setTabBackgroundColor(TabHost tabHost) {

		for (int k = 0; k < tabHost.getTabWidget().getChildCount(); k++) {
			//tabHost.getTabWidget().getChildAt(k).setBackgroundResource(R.drawable.tab_bg);
            tabHost.getTabWidget().getChildAt(k).setBackgroundColor(Color.TRANSPARENT); // unselected
            tabHost.getTabWidget().getChildTabViewAt(k).setSelected(false);

            final TextView tv = (TextView) tabHost.getTabWidget().getChildAt(k).findViewById(android.R.id.title);
			if (tv != null) tv.setTextColor(Color.GRAY);
            
        }

        tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(Color.TRANSPARENT); // selected
        tabHost.getTabWidget().getChildTabViewAt(tabHost.getCurrentTab()).setSelected(true);
	}
	
	
	OnClickListener onClickListenerCB = new OnClickListener() {
		public void onClick(View v) {
			final int id = v.getId();
			
			L.i( String.format("ctrlMenu: channelLL.setOnClickListener id %d", id));
			switch (id) {
			case R.id.hello:
				sayHello();
				break;
			case R.id.stopstart:
				stopStart();
				break;
			default:
				L.i( String.format("ctrlMenu: id %d %d", id, numChannels));
				if ((id>=1001)&&(id<=1000+numChannels)) {
					
					L.i( String.format("ctrlMenu: %s", ((ToggleButton)v).isChecked()?"addSource":"delSource"));
					if ( ((ToggleButton)v).isChecked() ) {
						addSource(id-1000-1);
						for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
							final TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
							if (tv == null)	continue;
							L.i( String.format("ctrlMenu: id %s", tv.getText()));
							if (tv.getText().equals(String.format("channel%d", id-1001+1))){
								L.i( String.format("ctrlMenu: id setEnabled %d", id));
								tabHost.getTabWidget().getChildTabViewAt(i).setEnabled(true);
								break;
							}
						}
					} else {
						delSource(id-1000-1);
						for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
							final TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
							if (tv == null) continue;
							if (tv.getText().equals(String.format("channel%d", id-1001+1))){
								tabHost.getTabWidget().getChildTabViewAt(i).setEnabled(false);
								break;
							}
						}
					}
				}
				break;
			}
		}
	};
	
	
    class createChannelToggleBtnAndTabs implements Runnable { 
        private int numChannels, enableChannelsMask, windowPreviewSize;
    	private LinearLayout tglChannelLL;
    	@SuppressWarnings("unused")
		private channelTab chanTab;
        
        public createChannelToggleBtnAndTabs(int numChannels, int enableChannelsMask, int windowPreviewSize) { 
            this.numChannels = numChannels;
            this.enableChannelsMask=enableChannelsMask;
            this.windowPreviewSize=windowPreviewSize;
        } 
        @Override 
        public void run() { 

        	L.i( String.format("ctrlMenu: numChannels %d enableChannelsMask %X windowPreviewSize %d", numChannels, enableChannelsMask, windowPreviewSize));
        	
        	windowPreviewSeekBar.setProgress(windowPreviewSize);
        	
        	tglChannelLL = (LinearLayout) findViewById(R.id.toggleChannelsLL);
    		
    		for (int i=0;i<numChannels;i++) {
        		
    			ToggleButton  toggleButtonCh = new ToggleButton(getApplicationContext());
            	toggleButtonCh.setTextColor(Color.BLACK);
            	toggleButtonCh.setText(String.format("channel%d", i+1));
            	toggleButtonCh.setId(1001+i);
            	toggleButtonCh.setOnClickListener(onClickListenerCB);

				boolean selected;
				if ( (enableChannelsMask&(1<<i)) != 0) {
					selected=true;
					//reAddSource() ---> apenas adicionamos se não estiver ja' adicionado!!!!!!
					///////addSource(i);
				} else {
					selected=false;
				}
				toggleButtonCh.setChecked(selected);


				tglChannelLL.addView(toggleButtonCh,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,  LinearLayout.LayoutParams.WRAP_CONTENT));
        	}

    		chanTab=new channelTab(getApplicationContext(), handler2UI, mServiceMessenger, tabHost, tabHostList, numChannels, enableChannelsMask);
    		
    		
    		tabHost.clearAllTabs();  // clear all tabs from the tabhost
    		for(TabHost.TabSpec spec : tabHostList) // add all that you remember back
    		   tabHost.addTab(spec);
    	    
    		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
    			final TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
    			if (tv == null) continue;
    			if (tv.getText().toString().startsWith("channel")){
    				int chIdx=Integer.parseInt(tv.getText().toString().substring(String.format("channel").length(), tv.getText().toString().length()))-1;
    				boolean enabled;
    				
    				if ( (enableChannelsMask&(1<<chIdx)) != 0) enabled=true;
    				else enabled=false;
    				
    				tabHost.getTabWidget().getChildTabViewAt(i).setEnabled(enabled);
    				
    				L.i( String.format("ctrlMenu: %s setEnabled %s", tv.getText(), (enabled)?"true":"false"));
    			}
    		}
    		
			setTabBackgroundColor(tabHost);
        } 
    } 

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    	//handlerThread.quitSafely();
        handlerThread.quit();
	}


	public void sayHello() {
        if (!mBound)
            return;

        // Create and send a message to the service, using a supported 'what'
        // value
        Message msg = Message.obtain(null, DataServ.MSG_SAY_HELLO, 0, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void stopStart() {
        if (!mBound)
            return;

        // Create and send a message to the service, using a supported 'what'
        // value
        Message msg = Message.obtain(null, DataServ.MSG_START_STOP, 0, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void stopService() {
        if (!mBound)
            return;

        // Create and send a message to the service, using a supported 'what'
        // value
        Message msg = Message.obtain(null, DataServ.MSG_STOP_SERVICE, 0, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void addSource(int chIdx) {
    	
        Message msg = Message.obtain(null, DataServ.MSG_ADD_SOURCE, chIdx, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void delSource(int chIdx) {
    	
        Message msg = Message.obtain(null, DataServ.MSG_DEL_SOURCE, chIdx, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void setWindowPreviewSize(int size) {
    	
        Message msg = Message.obtain(null, DataServ.MSG_SET_WINDOW_PREVIEW_SIZE, size, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /*public void drawDisplay() {
    	
        Message msg = Message.obtain(null, DataServ.MSG_DRAW_DISPLAY, 0, 0);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }*/
}
