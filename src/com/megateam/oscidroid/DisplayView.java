package com.megateam.oscidroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

@SuppressLint("WrongCall")
public class DisplayView extends SurfaceView implements SurfaceHolder.Callback{
	
	private static final int MAX_NUM_CHANNELS=4;

	private SurfaceHolder holder;
    //private DataThread dataThread = null;
    private Paint channelPaint;
    private int numChannels;
    private int pointsPerChannel;
    private Path[] channelPath;
    private float[][] chPoints;
    private int surfaceHeight, surfaceWitdh;
    private float[] channelYoffset;
    private float xFactor, yFactor;
    private boolean[] channelOn;
    
    private boolean previewWindow=false;
    private int previewWindowSize;
    private int xOffset=0;
    
    private Messenger mServiceMessenger;
    
    public DisplayView(Context context, Messenger mServiceMessenger, int pointsPerChannel) {
    	super(context);
          
    	this.mServiceMessenger=mServiceMessenger;
    	
    	this.previewWindowSize=MainActivity.numPointsPerChan;
    	//OsciApp osciAppContext=((OsciApp) context);
    	
    	L.i( String.format(String.format("DisplayView 1")));
  
    	holder = getHolder();
    	holder.addCallback(this);

    	this.numChannels=0;
    	this.pointsPerChannel=pointsPerChannel;
    	
    	channelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    	channelPaint.setColor(Color.WHITE);
    	channelPaint.setStyle(Paint.Style.FILL_AND_STROKE);

    	channelOn=new boolean[DisplayView.MAX_NUM_CHANNELS];

    	channelPath=new Path[DisplayView.MAX_NUM_CHANNELS];
    	chPoints=new float[DisplayView.MAX_NUM_CHANNELS][this.pointsPerChannel];
    	channelYoffset=new float[DisplayView.MAX_NUM_CHANNELS];
    	
    	for (int i=0;i<channelOn.length;i++) {
    		channelOn[i]=false;
    		channelPath[i]=new Path();
    		channelYoffset[i]=0;
    	}
    	L.i( String.format("DisplayView 2"));
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	
    	
    	//osciAppContext.dataThread.stopThread();
    	L.i( String.format("surfaceDestroyed"));
        // Now that we have the service messenger, lets send our messenger
        Message msg = Message.obtain(null, DataServ.MSG_STOP_SERVICE, 0, 0);
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

    	
    }
	
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	
    	surfaceHeight=holder.getSurfaceFrame().height();
    	surfaceWitdh=holder.getSurfaceFrame().width();
    	xFactor=surfaceWitdh/(float)pointsPerChannel;
    	yFactor=surfaceHeight/(float)256;
    	
    	L.i( String.format(String.format("surfaceCreated 1 %d %d %f %f", surfaceHeight, surfaceWitdh, xFactor, yFactor)));
        Message msg = Message.obtain(null, DataServ.MSG_SAY_HELLO, 0, 0);
        msg.obj=this;
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
	
	 @Override
	 public void surfaceChanged(SurfaceHolder holder, int format,
	               int width, int height) {
		 
		 surfaceHeight=height;//holder.getSurfaceFrame().height();
		 surfaceWitdh=width;//holder.getSurfaceFrame().width();
		 xFactor=surfaceWitdh/(float)pointsPerChannel;
		 yFactor=surfaceHeight/(float)256;
	 }

	 public int getNumPointsPerChannel() {
		 return pointsPerChannel;
	 }

	 public int getNumChannels() {
		 return numChannels;
	 }
	 
	 public boolean getChannelState(int chNum) {
		 return channelOn[chNum];
	 }
	 
	 public void newChannel(int chNum) {
		 if (!channelOn[chNum]) {
			 for (int i=0;i<this.pointsPerChannel;i++) {
				 chPoints[chNum][i]=0;
			 }
			 channelYoffset[chNum]=0;//256/2;///0/*surfaceHeight/2*/;
			 channelOn[chNum]=true;
			 numChannels++;
		 }
	 }

	 public void delChannel(int chNum) {
		 if (channelOn[chNum]) {
			 numChannels--;
			 channelOn[chNum]=false;
		 }
	 }

	 public void setChannelYoffset(int chNum, byte offset) {
		 channelYoffset[chNum]+=offset;
	 }
	 
	 public void setChannelPersistence(boolean presistFlag) {
	 }

	 public void setDisplayXOffset(float offset) {
		 
		 if (offset/xFactor>this.pointsPerChannel-(previewWindowSize))
			 this.xOffset=this.pointsPerChannel-(previewWindowSize);
		 else if (offset/xFactor<0/*(this.pointsPerChannel/numDisplayPerPreviewWindow)*/)
			 this.xOffset=0;
		 else 
			 this.xOffset=(int) (offset/xFactor);//(offset*xFactor);

		 L.i( String.format(String.format("displaView: setDisplayXOffset %d", this.xOffset)));
	 }

	 public void setWindowPreviewSize(int size) {

		 
		 L.i( String.format(String.format("displaView: setWindowPreviewSize xOffset %d size %d", this.xOffset, size)));

		 if (this.xOffset+size>=this.pointsPerChannel) {
			 this.xOffset=(this.pointsPerChannel-size);
		 } 
		 L.i( String.format(String.format("displaView: setWindowPreviewSize xOffset %d", this.xOffset)));

		 previewWindowSize=size;

		 if (size==this.pointsPerChannel) {
			 previewWindow=false;
		 } else {
			 previewWindow=true;
		 }
	 }
	 
	 public int getWindowPreviewSize() {
		 return previewWindowSize;
	 }
	 
	 public void setPoints(int chNum, byte[] point) {
		 for (int i=0;i<point.length;i++) {
			 //L.i( String.format(String.format("displaView: setPoints %f", (float)point[i])));
			 chPoints[chNum][i]=(float)((int)(point[i]&0xFF));//para que seja "unsigned byte"
		 }
	 }

	  @Override
	  public boolean onTouchEvent(MotionEvent event) {
		  super.onTouchEvent(event);

		  L.i( String.format(String.format("onTouchEvent: %f", event.getX()))); //L Message

		  return true;
	  }
	 
	 @Override
	 protected void onDraw(Canvas canvas) {
		 float yFactorAux, xFactorAux, yOffset=0;

		 int windowPreviewHeightFactor=4;
		 
		 canvas.drawColor(Color.WHITE);
          
		 channelPaint.setColor(Color.BLUE);
		 channelPaint.setStrokeWidth(1.5f);
		 channelPaint.setStyle(Paint.Style.STROKE);

		 if (this.previewWindow) {
			 
			 for (int i=0;i<channelOn.length;i++) {
				 
				 if (numChannels>0) {
					 canvas.drawRect(this.xOffset*xFactor, 0, this.xOffset*xFactor+previewWindowSize*xFactor, surfaceHeight/windowPreviewHeightFactor, channelPaint);
				 }
				 
				 if (channelOn[i]==true) {

					 if (channelPath[i].isEmpty()==false) channelPath[i].rewind();
		              
					 //L.i( String.format(String.format("displaView: chPoints[i][0] %f channelYoffset[i] %f yFactor %f ", chPoints[i][0],(float)channelYoffset[i], (yFactor*1/3))));
					 channelPath[i].moveTo(0, (chPoints[i][0]+(float)chPoints[i][0]+channelYoffset[i])*(yFactor/windowPreviewHeightFactor)+yOffset);
					 for (int j=1;j<pointsPerChannel;j++) {
						 channelPath[i].lineTo(j*xFactor, (chPoints[i][j]+channelYoffset[i])*(yFactor/windowPreviewHeightFactor)+yOffset);
					 }

					 canvas.drawPath(channelPath[i], channelPaint);
				 }
			 }

			 yFactorAux=yFactor*((float)(windowPreviewHeightFactor-1)/(float)windowPreviewHeightFactor);
			 yOffset=surfaceHeight/windowPreviewHeightFactor;
		 } else {
			 yFactorAux=yFactor;
			 yOffset=0;
		 }

		 xFactorAux=xFactor*((float)this.pointsPerChannel/(float)previewWindowSize);
		 
		 for (int i=0;i<channelOn.length;i++) {
 
			 if (channelOn[i]==true) {

				 if (channelPath[i].isEmpty()==false) channelPath[i].rewind();
	              
				 channelPath[i].moveTo(0, (chPoints[i][this.xOffset]+channelYoffset[i])*yFactorAux+yOffset);
				 for (int j=this.xOffset+1;j<this.xOffset+previewWindowSize;j++) {
					 channelPath[i].lineTo((j-this.xOffset)*xFactorAux, (chPoints[i][j]+channelYoffset[i])*yFactorAux+yOffset);
				 }

				 canvas.drawPath(channelPath[i], channelPaint);
			 }
		 }
	 }
}