package com.megateam.oscidroid;


import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

public class channelTab {
	private TabHost tabHost;
	private int numChannels, enableChannelsMask;
	private Handler handler2UI;
	private Messenger mServiceMessenger;
	private final Context context;
	
	channelTab(final Context context, Handler handler2UI,  Messenger messenger, TabHost tabHost, ArrayList<TabHost.TabSpec> tabHostList, int numChannels, int enableChannelsMask) {
		this.tabHost=tabHost;
		this.numChannels=numChannels;
		this.enableChannelsMask=enableChannelsMask;
		this.handler2UI=handler2UI;
		this.mServiceMessenger=messenger;
		this.context=context;
		
		TabSpec[] ts=new TabSpec[4]; 
		TabContentFactory tcf;
		
		tcf=new TabHost.TabContentFactory() {
	        public View createTabContent(String tag) {                                   
	        	
	        	L.d("ctrlMenu TAG --> "+tag);
	        	int idx=Integer.parseInt(tag.substring(tag.length()-1));   					            	

	        	LinearLayout LL_H = new LinearLayout(context);
	            LL_H.setOrientation(LinearLayout.HORIZONTAL);
	            LL_H.setWeightSum(2f);
	            LL_H.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

	            LinearLayout.LayoutParams llvParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 2.0f);
	            llvParams.height=LayoutParams.MATCH_PARENT;
	            llvParams.width=0;
	            llvParams.weight=1.0f;

	        	LinearLayout LL_V1 = new LinearLayout(context);
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

	            
	            Button buttonCh = new Button(context);
	            buttonCh.setTextColor(Color.BLACK);
	      		buttonCh.setId(idx*1000+1);
	      		buttonCh.setOnClickListener(onClickListenerChannelCB);
	      		buttonCh.addOnLayoutChangeListener(onLayoutChangeListenerButtonCB);
	            LL_V1.addView(buttonCh,bParams);
	            
	            TextView textView = new TextView(context);
	            textView.setGravity(Gravity.CENTER);
	            textView.setText("AMPLITUDE");
	            LL_V1.addView(textView,tParams);
	            
	            buttonCh = new Button(context);
	            buttonCh.setTextColor(Color.BLACK);
	      		buttonCh.setId(idx*1000+0);
	      		buttonCh.setOnClickListener(onClickListenerChannelCB);
	      		buttonCh.addOnLayoutChangeListener(onLayoutChangeListenerButtonCB);
	            LL_V1.addView(buttonCh,bParams);
	            
	            LL_H.addView(LL_V1);
	            
	           
	        	LinearLayout LL_V2 = new LinearLayout(context);
	            LL_V2.setOrientation(LinearLayout.VERTICAL);
	            LL_V2.setWeightSum(7f);
	            LL_V2.setLayoutParams(llvParams);

	            buttonCh = new Button(context);
	            buttonCh.setTextColor(Color.BLACK);
	      		buttonCh.setId(idx*1000+3);
	      		buttonCh.setOnClickListener(onClickListenerChannelCB);
	      		buttonCh.setOnLongClickListener(onLongClickListenerChannelCB);
	      		buttonCh.addOnLayoutChangeListener(onLayoutChangeListenerButtonCB);
	      		buttonCh.setLayoutParams(bParams);
	            LL_V2.addView(buttonCh);

	            textView = new TextView(context);
	            textView.setGravity(Gravity.CENTER);
	            textView.setText("OFFSET");
	            LL_V2.addView(textView,tParams);
	            
	            buttonCh = new Button(context);
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
	    	ts[i]=tabHost.newTabSpec(String.format("CHANNEL%d", i+1));
			ts[i].setIndicator(String.format("channel%d", i+1));
			ts[i].setContent(tcf);
			tabHost.addTab(ts[i]);
			tabHostList.add(ts[i]);
	    }
	}

	
	boolean mOngoing=false;
	
	class mOngoingRunnable implements Runnable {
		private int chIdx, offset;
		
        public mOngoingRunnable(int chIdx, int offset) { 
        
        	this.chIdx=chIdx;
        	this.offset=offset;
        } 

        @Override 
        public void run() { 
        	L.i( String.format("ctrlMenu: long press!!!"));
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
			L.i( String.format("ctrlMenu: id %d chIdx %d operIdx %d", id, chIdx, operIdx));
			
			switch (operIdx) {
			case 0: 
				setAttenuation(chIdx, +1); 
				break;
			case 1: 
				setAttenuation(chIdx, -1); 
				break;
			case 2: 
				setOffset(chIdx, +1);
			    if (mOngoing) {
			        mOngoing = false;
			    } 

				break;
			case 3: 
				setOffset(chIdx, -1); 
			    if (mOngoing) {
			        mOngoing = false;
			    }
				break;
			default:			
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
			L.i( String.format("ctrlMenu: onLayoutChangeListenerButtonCB id %d chIdx %d operIdx %d", id, chIdx, operIdx));
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
	    
	    BitmapDrawable bmap = (BitmapDrawable) context.getResources().getDrawable(id);
	    
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
	    
	    bmapScaled = new BitmapDrawable(context.getResources(),Bitmap.createScaledBitmap(bmap.getBitmap(), newBmapWidth, newBmapHeight, false));
	    bmapScaled.setGravity(Gravity.CENTER);
	    bmapScaled.setBounds(0, 0, button.getWidth(), button.getHeight());
	    
	    return bmapScaled;
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
