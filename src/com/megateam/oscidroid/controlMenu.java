package com.megateam.oscidroid;


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
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.ToggleButton;
import android.util.Log;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnDrawListener;
import android.view.WindowInsets;

public class controlMenu extends FragmentActivity {

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
            	numChannels=msg.arg1;
            	Log.i("RTG",String.format("ctrlMenu Client : Service said %d Channels %X!", numChannels, msg.arg2));
            	
            	handler2UI.post(new createChannelToggleBtnAndTabs(msg.arg1, msg.arg2));
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

            
            Log.i("RTG", String.format("onServiceConnected --> %s", className.getClassName()));
            
            
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

	
    
    
    
	//private DataServConn dataServConn;
	private LinearLayout channelLL;
    private TabHost host;
	private Button btnStartStop, btnHello;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("RTG", String.format("ctrlMenu: onCreate"));

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

    	host = (TabHost)findViewById(R.id.tab_host);
		host.setup();

		//TabWidget tabWidget = host.getTabWidget();
        //
		//for(int i=0; i<tabWidget.getChildCount(); i++){
		//    View v = tabWidget.getChildAt(i);
        //
		//    // Look for the title view to ensure this is an indicator and not a divider.
		//    TextView tv = (TextView)v.findViewById(android.R.id.title);
		//    if(tv == null) {
		//        continue;
		//    }
		//    v.setBackgroundResource(R.drawable.your_tab_selector_drawable);
		//}
		
		TabSpec spec = host.newTabSpec("settings");
		spec.setContent(R.id.tab_settings);
		spec.setIndicator("settings");
		host.addTab(spec);

		spec = host.newTabSpec("trigger");
		spec.setContent(R.id.tab_trigger);
		spec.setIndicator("trigger");
		host.addTab(spec);

 		//set Windows tab as default (zero based)
		host.setCurrentTab(0);
		
		btnStartStop = (Button) findViewById(R.id.stopstart);
    	btnStartStop.setOnClickListener(onClickListenerCB);
    	
		btnHello = (Button) findViewById(R.id.hello);
    	btnHello.setOnClickListener(onClickListenerCB);

	}
	
	boolean mOngoing=false;
	//mOngoingRunnable teste;
	
	class mOngoingRunnable implements Runnable {
		private int chIdx, offset;
		
        public mOngoingRunnable(int chIdx, int offset) { 
        
        	this.chIdx=chIdx;
        	this.offset=offset;
        } 

        @Override 
        public void run() { 
        	Log.i("RTG", String.format("ctrlMenu: long press!!!"));
        	setOffset(chIdx, offset);
        	if (mOngoing) handler2UI.postDelayed(new mOngoingRunnable(chIdx, offset), 50);
        }		
	}

	
	OnLongClickListener onLongClickListenerChannelCB = new OnLongClickListener() {
		public boolean onLongClick(View v) {
			final int id = v.getId(), chIdx, operIdx;

			chIdx=id/1000-1;
			operIdx=id%1000;
			
			if (operIdx==2) handler2UI.post(new mOngoingRunnable(chIdx, +10));
			else if (operIdx==3) handler2UI.post(new mOngoingRunnable(chIdx, -10));
			
		    mOngoing = true;
		    return false;
		}
	};
	
	OnClickListener onClickListenerChannelCB = new OnClickListener() {
		public void onClick(View v) {
			final int id = v.getId(), chIdx, operIdx;
			
			chIdx=id/1000-1;
			operIdx=id%1000;
			Log.i("RTG", String.format("ctrlMenu: id %d chIdx %d operIdx %d", id, chIdx, operIdx));
			
			switch (operIdx) {
			case 0: 
				setAttenuation(chIdx, +1); 
				break;
			case 1: 
				setAttenuation(chIdx, -1); 
				break;
			case 2: 
				setOffset(chIdx, +10);
			    if (mOngoing) {
			        mOngoing = false;
			    } 

				break;
			case 3: 
				setOffset(chIdx, -10); 
			    if (mOngoing) {
			        mOngoing = false;
			    }
				break;
			default:			
			}
		}
	};
	
	OnClickListener onClickListenerCB = new OnClickListener() {
		public void onClick(View v) {
			final int id = v.getId();
			
			Log.i("RTG", String.format("ctrlMenu: channelLL.setOnClickListener id %d", id));
			switch (id) {
			case R.id.hello:
				sayHello();
				break;
			case R.id.stopstart:
				stopStart();
				break;
			default:
				Log.i("RTG", String.format("ctrlMenu: id %d %d", id, numChannels));
				if ((id>=1001)&&(id<=1000+numChannels)) {
					
					Log.i("RTG", String.format("ctrlMenu: %s", ((ToggleButton)v).isChecked()?"addSource":"delSource"));
					if ( ((ToggleButton)v).isChecked() ) {
						addSource(id-1000-1);
						for (int i = 0; i < host.getTabWidget().getChildCount(); i++) {
							final TextView tv = (TextView) host.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
							if (tv == null)	continue;
							Log.i("RTG", String.format("ctrlMenu: id %s", tv.getText()));
							if (tv.getText().equals(String.format("channel%d", id-1001+1))){
								Log.i("RTG", String.format("ctrlMenu: id setEnabled %d", id));
								host.getTabWidget().getChildTabViewAt(i).setEnabled(true);
								break;
							}
						}
					} else {
						delSource(id-1000-1);
						for (int i = 0; i < host.getTabWidget().getChildCount(); i++) {
							final TextView tv = (TextView) host.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
							if (tv == null) continue;
							if (tv.getText().equals(String.format("channel%d", id-1001+1))){
								host.getTabWidget().getChildTabViewAt(i).setEnabled(false);
								break;
							}
						}
					}
				}
				break;
			}
		}
	};
	
	OnLayoutChangeListener onLayoutChangeListenerButtonCB = new OnLayoutChangeListener() {          

		@Override
		public void onLayoutChange(View arg0, int arg1, int arg2, int arg3,
				int arg4, int arg5, int arg6, int arg7, int arg8) {
			final int id = arg0.getId(), chIdx, operIdx;
			
			chIdx=id/1000-1;
			operIdx=id%1000;
			Log.i("RTG", String.format("ctrlMenu: onLayoutChangeListenerButtonCB id %d chIdx %d operIdx %d", id, chIdx, operIdx));
			switch (operIdx) {
			case 0: 
			case 2: 
				arg0.setBackground(setButtonBackground((Button)arg0, R.drawable.down_arrow_50));
				break;
			case 1: 
			case 3: 
				arg0.setBackground(setButtonBackground((Button)arg0, R.drawable.up_arrow_50));
				break;
			default:			
			}
			
		}
	};
	
	private Drawable setButtonBackground(Button button, int id) {
		BitmapDrawable bmapScaled;
		
	    int height = button.getHeight();
	    int width = button.getWidth();
	    
	    BitmapDrawable bmap = (BitmapDrawable) this.getResources().getDrawable(id);
	    
	    float bmapWidth = bmap.getBitmap().getWidth();
	    float bmapHeight = bmap.getBitmap().getHeight();
	     
	    float wRatio = width / bmapWidth;
	    float hRatio = height / bmapHeight;
	     
	    float ratioMultiplier = wRatio;
	    // Untested conditional though I expect this might work for landscape mode
	    if (hRatio < wRatio) {
	    	ratioMultiplier = hRatio;
	    }
	     
	    int newBmapWidth = (int) (bmapWidth*ratioMultiplier);
	    int newBmapHeight = (int) (bmapHeight*ratioMultiplier);
	    
	    bmapScaled = new BitmapDrawable(getResources(),Bitmap.createScaledBitmap(bmap.getBitmap(), newBmapWidth, newBmapHeight, false));
	    bmapScaled.setGravity(Gravity.CENTER);
	    bmapScaled.setBounds(0, 0, button.getWidth(), button.getHeight());
	    
	    return bmapScaled;
	}
	
    class createChannelToggleBtnAndTabs implements Runnable { 
        private int numChannels, enableChannelsMask; 
        
        public createChannelToggleBtnAndTabs(int numChannels, int enableChannelsMask) { 
            this.numChannels = numChannels;
            this.enableChannelsMask=enableChannelsMask;
        } 
        @Override 
        public void run() { 

        	Log.i("RTG", String.format("ctrlMenu: numChannels %d enableChannelsMask %X", numChannels, enableChannelsMask));
    		channelLL = (LinearLayout) findViewById(R.id.channel);
    		
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
					addSource(i);
				} else {
					selected=false;
				}
				toggleButtonCh.setChecked(selected);


            	channelLL.addView(toggleButtonCh,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,  LinearLayout.LayoutParams.WRAP_CONTENT));
        	}

        	
    		TabSpec[] ts=new TabSpec[4]; 
    		TabContentFactory tcf;
       	
        	tcf=new TabHost.TabContentFactory(){
                public View createTabContent(String tag) {                                   
                	
                	Log.i("RTG","ctrlMenu TAG --> "+tag);
                	int idx=Integer.parseInt(tag.substring(tag.length()-1));   					            	

                	LinearLayout LL_H = new LinearLayout(getApplicationContext());
    	            LL_H.setOrientation(LinearLayout.HORIZONTAL);
    	            LL_H.setWeightSum(2f);
    	            LL_H.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

    	            LinearLayout.LayoutParams llvParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 2.0f);
    	            llvParams.height=LayoutParams.MATCH_PARENT;
    	            llvParams.width=0;
    	            llvParams.weight=1.0f;

                	LinearLayout LL_V1 = new LinearLayout(getApplicationContext());
    	            LL_V1.setOrientation(LinearLayout.VERTICAL);
    	            LL_V1.setWeightSum(7f);
    	            LL_V1.setLayoutParams(llvParams);

    				LinearLayout.LayoutParams bParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f);
    				bParams.width=LayoutParams.MATCH_PARENT;
    				bParams.height=0;
    				bParams.weight=3.0f;

    				LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f);
    				tParams.width=LayoutParams.MATCH_PARENT;
    				tParams.height=0;
    				tParams.weight=1.0f;

    	            
    	            Button buttonCh = new Button(getApplicationContext());
    	            buttonCh.setTextColor(Color.BLACK);
    	      		buttonCh.setId(idx*1000+1);
    	      		buttonCh.setOnClickListener(onClickListenerChannelCB);
    	      		buttonCh.addOnLayoutChangeListener(onLayoutChangeListenerButtonCB);
    	            LL_V1.addView(buttonCh,bParams);
    	            
    	            TextView textView = new TextView(getApplicationContext());
    	            textView.setGravity(Gravity.CENTER);
    	            textView.setText("AMPLITUDE");
    	            LL_V1.addView(textView,tParams);
    	            
    	            buttonCh = new Button(getApplicationContext());
    	            buttonCh.setTextColor(Color.BLACK);
    	      		buttonCh.setId(idx*1000+0);
    	      		buttonCh.setOnClickListener(onClickListenerChannelCB);
    	      		buttonCh.addOnLayoutChangeListener(onLayoutChangeListenerButtonCB);
    	            LL_V1.addView(buttonCh,bParams);
    	            
    	            LL_H.addView(LL_V1);
    	            
   	            
                	LinearLayout LL_V2 = new LinearLayout(getApplicationContext());
    	            LL_V2.setOrientation(LinearLayout.VERTICAL);
    	            LL_V2.setWeightSum(7f);
    	            LL_V2.setLayoutParams(llvParams);

    	            buttonCh = new Button(getApplicationContext());
    	            buttonCh.setTextColor(Color.BLACK);
    	      		buttonCh.setId(idx*1000+3);
    	      		buttonCh.setOnClickListener(onClickListenerChannelCB);
    	      		buttonCh.setOnLongClickListener(onLongClickListenerChannelCB);
    	      		buttonCh.addOnLayoutChangeListener(onLayoutChangeListenerButtonCB);
    	      		buttonCh.setLayoutParams(bParams);
    	            LL_V2.addView(buttonCh);

    	            textView = new TextView(getApplicationContext());
    	            textView.setGravity(Gravity.CENTER);
    	            textView.setText("OFFSET");
    	            LL_V2.addView(textView,tParams);
    	            
    	            buttonCh = new Button(getApplicationContext());
    	            buttonCh.setTextColor(Color.BLACK);
    	      		buttonCh.setId(idx*1000+2);
    	      		buttonCh.setOnClickListener(onClickListenerChannelCB);
    	      		buttonCh.setOnLongClickListener(onLongClickListenerChannelCB);
    	      		buttonCh.addOnLayoutChangeListener(onLayoutChangeListenerButtonCB);
    	      		buttonCh.setLayoutParams(bParams);
    	            LL_V2.addView(buttonCh);
    	            
    	            LL_H.addView(LL_V2);
                	return (LL_H);
                }       
            };


            for (int i=0;i<numChannels;i++)	{
            	ts[i]=host.newTabSpec(String.format("CHANNEL%d", i+1));
        		ts[i].setIndicator(String.format("channel%d", i+1));
        		ts[i].setContent(tcf);
        		host.addTab(ts[i]);
            }
            
			for (int i = 0; i < host.getTabWidget().getChildCount(); i++) {
				final TextView tv = (TextView) host.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
				if (tv == null) continue;
				if (tv.getText().toString().startsWith("channel")){
					int chIdx=Integer.parseInt(tv.getText().toString().substring(String.format("channel").length(), tv.getText().toString().length()))-1;
					boolean enabled;
					
					if ( (enableChannelsMask&(1<<chIdx)) != 0) enabled=true;
					else enabled=false;
					
					host.getTabWidget().getChildTabViewAt(i).setEnabled(enabled);
					
					
					Log.i("RTG", String.format("ctrlMenu: %s setEnabled %s", tv.getText(), (enabled)?"true":"false"));
				}
			}
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
    public void setAttenuation(int chIdx, int attenuation) {
    	
        Message msg = Message.obtain(null, DataServ.MSG_SET_ATTENUATION, chIdx, attenuation);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void setOffset(int chIdx, int offset) {
    	
        Message msg = Message.obtain(null, DataServ.MSG_SET_OFFSET, chIdx, offset);
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
