package com.megateam.oscidroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.SurfaceHolder;

@SuppressLint("WrongCall")
public class DataThread extends Thread {
	
	private static final long SLEEP_TIME = 1;
	//private static final int MAX_NUM_CHANNELS = 4;
	
	//private SurfaceHolder surfaceHolder=null;
	private boolean exitFlag=false, stopFlag=false, drawDisplayFlag=false;
	private DisplayView dpv=null;
	private sourceIface dataIface=null; //new sourceIface[MAX_NUM_CHANNELS];
	//private boolean [] channelEnable=new boolean[MAX_NUM_CHANNELS];  
	
	public DataThread() {
		super();

		L.i( String.format("DataThread: new")); //L Message
		exitFlag = false;
		
		super.start();
	}

	public void setDisplayView(DisplayView dpv){
		this.dpv=dpv;
	}
	
	public void setOsciDevType(sourceIface srcIface) {
		dataIface=srcIface;
	}
	
	public int enableChannel(int chNum) {
		
		L.i( String.format("DataThread: enableChannel %d",  chNum)); //L Message
		
		//TODO enviar esta informação de forma automática para a classe dpv
		dpv.newChannel(chNum);
		dataIface.enableChannel(chNum);
		return 0;
	}
	
	public int disableChannel(int chNum) {
		
		L.i( String.format("DataThread: disableChannel %d",  chNum)); //L Message
		
		//TODO enviar esta informação de forma automática para a classe dpv
		dpv.delChannel(chNum);
		dataIface.disableChannel(chNum);

		return 0;
	}

	void setAttenuation(int channel, int attenuation) {

		dataIface.setAttenuation(channel, attenuation);
	}
	
	void setOffset(int channel, int offset) {
		
		dataIface.setOffset(channel, offset);
		dpv.setChannelYoffset(channel, (byte) offset);
	}

	public int getEnabledChannelsMask() {
		int mask=0x0f;
		
/*		for (int i=0;i<MAX_NUM_CHANNELS;i++) {
			if (channelEnable[i]==true) mask|=(1<<i);
		}*/
		
		return mask;
	}
	
	void setWindowPreviewSize(int size) {
		dpv.setWindowPreviewSize(size);
	}
	
	int getWindowPreviewSize() {
		return dpv.getWindowPreviewSize();
	}
	
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
		L.i( String.format("DataThread stopThread")); //L Message
	}

	public void run() {
		
		Canvas c = null;
		while (exitFlag==false)	{
			//if (exitFlag==true) {
			if ((stopFlag==false)&&(dpv!=null)) {
				
				c = null;
				try	{
					c = dpv.getHolder().lockCanvas();
					synchronized (dpv.getHolder()) {
						if ((c != null) && dataIface!=null) {
							
							dataIface.fetchData();

							for (int i=0; i<dataIface.getNumChannels();i++){
								dpv.setPoints(i, dataIface.getSamples(i));
							}
							
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
				//L.i( String.format("DataThread: stopFlag %s dpvPtr %s",stopFlag?"true":"false", (osciAppContext.displayView==null)?"null":"notNull")); //L Message
				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		L.i( String.format("exit Thread")); //L Message
	}
}
