package com.megateam.oscidroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.SurfaceHolder;

@SuppressLint("WrongCall")
public class DataThread extends Thread {
	
	private static final long SLEEP_TIME = 20;
	private static final int MAX_NUM_CHANNELS = 4;
	
	//private SurfaceHolder surfaceHolder=null;
	private boolean exitFlag=false, stopFlag=false, drawDisplayFlag=false;
	private DisplayView dpv=null;
	private sourceIface[] dataIface=new sourceIface[MAX_NUM_CHANNELS];
	private boolean [] channelEnable=new boolean[MAX_NUM_CHANNELS];  
	
	//public DataThread(sourceIface dataIface, OsciApp osciApp) {
	//	super();
	//	this.dataIface=dataIface;
	//	osciAppContext = osciApp;
	//	
	//	Log.i("RTG", String.format("DataThread new")); //Log Message
	//	exitFlag = false;
	//	super.start();
	//}
	public DataThread(DisplayView dpv) {
		super();
		Log.i("RTG", String.format("DataThread: new")); //Log Message
		this.dpv=dpv;
		exitFlag = false;
		
		for (int i=0; i<MAX_NUM_CHANNELS;i++) {
			channelEnable[i]=false;
		}
		
		super.start();
	}
	
	
	public int addChannel(sourceIface srcIface, int chId) {
		
		Log.i("RTG", String.format("DataThread: addChannel %d",  chId)); //Log Message
		dataIface[chId]=srcIface;
		
		dpv.newChannel(chId);
		channelEnable[chId]=true;
		return 0;
	}
	
	public int delChannel(int chNum) {
		
		Log.i("RTG", String.format("DataThread: delChannel %d",  chNum)); //Log Message
		
		channelEnable[chNum]=false;
		dpv.delChannel(chNum);

		return 0;
	}

	void setAttenuation(int channel, int attenuation) {

		dataIface[channel].setAttenuation(channel, attenuation);
	}
	
	void setOffset(int channel, int offset) {
		
		dataIface[channel].setOffset(channel, offset);
		dpv.setChannelYoffset(channel, (byte) offset);
	}

	public int getEnabledChannelsMask() {
		int mask=0;
		
		for (int i=0;i<MAX_NUM_CHANNELS;i++) {
			if (channelEnable[i]==true) mask|=(1<<i);
		}
		
		return mask;
	}
	
	void enableWindowPreview(boolean enable) {
		dpv.enableWindowPreview(enable);
	}
	void setWindowPreviewSize(int size) {
		dpv.setWindowPreviewSize(size);
	}
	int getWindowPreviewSize() {
		return dpv.getWindowPreviewSize();
	}
	
	//public void startThread(DisplayView dpv) {
	//	Log.i("RTG", String.format(String.format("startThread"))); //Log Message
	//	
	//	this.dpv = dpv;
	//	this.surfaceHolder = dpv.getHolder();
	//			
	//	exitFlag = false;
	//	
	//	super.start();
	//}

	public void startStop() {
		if (this.stopFlag==true) this.stopFlag=false;
		else this.stopFlag=true;
	}

	public void drawDisplay() {
		if (this.stopFlag==true) {
			this.stopFlag=false;
			this.drawDisplayFlag=true;
		}
	}

	public void stopThread() {
		exitFlag = true;
		
		//try {
		//	super.join();
		//} catch (InterruptedException e) {
		//	e.printStackTrace();
		//}
		Log.i("RTG", String.format("DataThread stopThread")); //Log Message
	}

	public void run() {
		
		Canvas c = null;
		while (exitFlag==false)	{
			//if (exitFlag==true) {
			if (stopFlag==false) {
				
				c = null;
				try	{
					c = dpv.getHolder().lockCanvas();
					synchronized (dpv.getHolder()) {
						if (c != null) {
							if (channelEnable[0]&&!drawDisplayFlag) dpv.setPoints(0, dataIface[0].getSamples(0));
							if (channelEnable[1]&&!drawDisplayFlag) dpv.setPoints(1, dataIface[1].getSamples(1));
							dpv.onDraw(c);
						}
					}
					sleep(SLEEP_TIME);
				}
				catch(InterruptedException ie) { 
				} finally {
					// do this in a finally so that if an exception is thrown
					// we don't leave the Surface in an inconsistent state
					if (c != null){
						dpv.getHolder().unlockCanvasAndPost(c);
					}
				}
				if (drawDisplayFlag==true) {
					stopFlag=true;
					drawDisplayFlag=false;
				}
			} else {
				//Log.i("RTG", String.format("DataThread: stopFlag %s dpvPtr %s",stopFlag?"true":"false", (osciAppContext.displayView==null)?"null":"notNull")); //Log Message
				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		Log.i("RTG", String.format("exit Thread")); //Log Message
	}
}
