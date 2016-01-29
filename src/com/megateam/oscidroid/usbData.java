package com.megateam.oscidroid;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;



public class usbData implements sourceIface {
	private int numPointsPerChannel;
	private Context context;

	private UsbAccessory mAccessory = null;
	ParcelFileDescriptor mFileDescriptor = null;
	private FileOutputStream mFout = null;
	private FileInputStream mFin = null;
	private PendingIntent mPermissionIntent = null;

	
	private ObjectInputStream mSerialIn=null;
	
	
	private boolean readUsbThread=false;
//	private BlockingQueue<byte[]> queue;
	
	public usbData(Context context, Intent intent, int numPointsPerChannel) {
		L.i("usbData: begin");
		
		this.context=context;
		this.numPointsPerChannel=numPointsPerChannel;
		
//		queue = new LinkedBlockingQueue<byte[]>();
		readUsbThread=false;
		
        IntentFilter i = new IntentFilter();
        i.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        i.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        i.addAction("com.megateam.oscidroid.USBPERMISSION");
        context.registerReceiver(mUsbReceiver,i);
		
    	UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        
        if(intent.getAction().equals("android.hardware.usb.action.USB_ACCESSORY_ATTACHED")){
        	L.d("USB: Action is usb");
        	synchronized (this) {
        		UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
	        	mAccessory = accessory;
	        	
	        	try{
	        		mFileDescriptor = manager.openAccessory(accessory);
	        	}catch(IllegalArgumentException e){
	        		//finish();
	        	}catch(NullPointerException e){
	        		//finish();
	        	}
	        	
	        	FileDescriptor fd = mFileDescriptor.getFileDescriptor();
	        	mFin = new FileInputStream(fd);
	        	mFout = new FileOutputStream(fd);
	        	
	        	readUsbThread=true;
        	}
        }else{
        	UsbAccessory[] accessories = manager.getAccessoryList();
        	if (accessories!=null) {
            	for(UsbAccessory a : accessories){
            		L.i("accessory: "+a.getManufacturer());
            		if(a.getManufacturer().equals("Manufacturer")){
            			mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent("ch.serverbox.android.usbtest.USBPERMISSION"),0);
            			manager.requestPermission(a,mPermissionIntent);
            			L.i("USB: permission requested");

        	        	try{
        	        		mFileDescriptor = manager.openAccessory(a);
        	        	}catch(IllegalArgumentException e){
        	        		//finish();
        	        	}catch(NullPointerException e){
        	        		//finish();
        	        	}
        	        	
        	        	FileDescriptor fd = mFileDescriptor.getFileDescriptor();
        	        	mFin = new FileInputStream(fd);
        	        	mFout = new FileOutputStream(fd);
            			readUsbThread=true;
            			break;
            		}
            	}
        	} else {
        		L.i("USB: accessories!=null");
        	}
        }
    //    new Thread(new Task()).start();
        
	}

    void close() {
    	L.i("close");
		if(mFout != null)
			try {
				mFout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		if(mFin != null)
			try {
				mFin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	context.unregisterReceiver(mUsbReceiver);
    }

    //class Task extends Thread {
    //    @Override
    //    public void run() {
    //    	int numBytes=0;
    //    	byte[] buff=new byte[8192];
    //    	        	
    //    	while (true) {
    //    		if (readUsbThread==true) {
    //    			L.i("USB: Vou fazer mFin.read");
    //    			try {
    //    				numBytes=mFin.read(buff, 0, 8192);
    //    			} catch (IOException e1) {
    //    				// TODO Auto-generated catch block
    //    				e1.printStackTrace();
    //    			}
    //    			//L.i(String.format("USB: Consegui ldr %d bytes", numBytes));
    //
    //    			if (queue.size()<2) {
    //        			try {
    //						queue.put(buff);
    //					} catch (InterruptedException e) {
    //						// TODO Auto-generated catch block
    //						e.printStackTrace();
    //					}
    //        			//L.i(String.format("USB: escrivi na queue"));
    //    			}
    //    		}
    //    	}
    //    }
    //}

	
	
	//static byte[] testeByteBuffer = new byte[8192]; 
    
    static byte[] uBuff=new byte[8192];
    
	@Override
	public int getNumChannels() {
		return 4;
	}

	static int xdiv=1;
	
	@Override
	public int enableChannel(int chNum) {

		L.i(String.format("USB: enableChannel --> %d %s", chNum, (mFout!=null)?"mFout!=null":"mFout==null"));
		
		if (mFout!=null) {
			byte b[]=new byte[7];
			ByteBuffer cmd=ByteBuffer.wrap(b);
			cmd.order(ByteOrder.LITTLE_ENDIAN);
			
			cmd.put((byte) 0x01);
			cmd.put((byte) 0x82);
			cmd.put((byte) 0x00);
			cmd.putInt(xdiv);
			L.i(String.format("USB: b.length --> %d ", b.length));
			try {
				mFout.write(b);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			xdiv++;

			
			//byte b[]=new byte[4];
			//b[0]=(byte)0x01;
			//b[1]=(byte)0x81;
			//b[2]=(byte)0x04;
			//b[3]=(byte)0x05;
			//L.i(String.format("USB: b.length --> %d ", b.length));
			//try {
			//	mFout.write(b);
			//} catch (IOException e) {
			//	// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public int disableChannel(int chNum) {
		ByteBuffer cmd=ByteBuffer.allocateDirect(4);

		if (mFout!=null) {
			cmd.put((byte) 0x01);
			cmd.put((byte) 0x81);
			cmd.put((byte) 0x02);
			cmd.put((byte) 0x03);
			byte b[]=new byte[cmd.remaining()];
			try {
				mFout.write(b);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public void fetchData() {
		int numBytes=0;
				
		//L.i(String.format("USB: fetchData"));
		if (mFin!=null) {
			try {
				numBytes=mFin.read(uBuff, 0, 8192);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		ByteBuffer cfgStruct=ByteBuffer.wrap(uBuff);
		cfgStruct.order(ByteOrder.LITTLE_ENDIAN);
		L.i(String.format("USB: fetch %d %d %d %d %d", cfgStruct.get(), cfgStruct.get(), cfgStruct.getInt(), cfgStruct.get(), cfgStruct.getInt()));
		//versao modo xdiv admin ydiv
	}
	
	@Override
	public byte[] getSamples(int channel) {

		//try {
		//	testeByteBuffer=queue.take();
		//} catch (InterruptedException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		////L.i(String.format("USB: li da queue"));
        //
		//return Arrays.copyOfRange(testeByteBuffer, 8192-1024, 8192);
		
		int idx=4-channel;
		
		return Arrays.copyOfRange(uBuff, 8192-(idx*1024), 8192-((idx-1)*1024));
	}

	@Override
	public float[] getMeasures(int channel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int setTriger(int channel, int type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAttenuation(int channel, int attenuation) {
		//mFout.write(data.getBytes());
	}

	@Override
	public void setOffset(int channel, int offset) {
		// TODO Auto-generated method stub
		
	}
	
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			L.i("BroadcastReceiver mUsbReceiver: onReceive");
			UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
			
			if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					L.d("USB Attached!");
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						mAccessory = accessory;
			        	FileDescriptor fd = null;
			        	try{
			        		fd = manager.openAccessory(accessory).getFileDescriptor();
			        	}catch(IllegalArgumentException e){
			        		
			        	}catch(NullPointerException e){
			        		
			        	}
			        	mFout = new FileOutputStream(fd);
			        	mFin = new FileInputStream(fd);

			        	readUsbThread=true;
					} else {
						L.d("USB: permission denied for accessory " + accessory);
					}
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(mAccessory)) {
					if(mFout != null)
						try {
							mFout.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					if(mFin != null)
						try {
							mFin.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					
					readUsbThread = false;
					mAccessory = null;
					
					context.unregisterReceiver(mUsbReceiver);
					
				}
			}else if("ch.serverbox.android.usbtest.USBPERMISSION".equals(action)){
				L.i("permission answered");
				if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
		        	UsbAccessory[] accessories = manager.getAccessoryList();

		        	for(UsbAccessory a : accessories){
		        		L.i("accessory: "+a.getManufacturer());
		        		if(a.getManufacturer().equals("Manufacturer")){
		        			mAccessory = a;
		                	FileDescriptor fd = null;
		                	try{
		                		fd = manager.openAccessory(a).getFileDescriptor();
		                	}catch(IllegalArgumentException e){
		                		//finish();
		                	}catch(NullPointerException e){
		                		//finish();
		                	}
		                	mFout = new FileOutputStream(fd);
		                	mFin = new FileInputStream(fd);
							readUsbThread = true;
		                	L.i("added accessory");
		        			break;
		        		}
		        	}
				}
			}
		}
	};
}
