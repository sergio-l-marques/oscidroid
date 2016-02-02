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
import android.widget.Toast;

public class usbData implements sourceIface {
	private int numPointsPerChannel;

	private static int MAX_NUM_CHANNELS=4;
	
	private Context context;

	private UsbAccessory mAccessory = null;
	ParcelFileDescriptor mFileDescriptor = null;
	private FileOutputStream mFout = null;
	private FileInputStream mFin = null;
	private PendingIntent mPermissionIntent = null;

	private ObjectInputStream mSerialIn=null;
	
//	private boolean readUsbThread=false;
	
	private int enableChannelsMask=0;
	private int windowSize;

	
//	private BlockingQueue<byte[]> queue;
	
	public usbData(Context context, Intent intent, int numPointsPerChannel) {
		L.i("usbData: begin");
		
		this.context=context;
		this.numPointsPerChannel=numPointsPerChannel;
		
//		queue = new LinkedBlockingQueue<byte[]>();
		enableChannelsMask=0;
		this.windowSize=numPointsPerChannel;
		
    	UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    	if (manager==null) L.d("USB: manager is null!!!!!");
        
        if(intent.getAction().equals("android.hardware.usb.action.USB_ACCESSORY_ATTACHED")){
        	L.d("USB: Action is usb");
        	synchronized (this) {
        		UsbAccessory accessory=null;
        		
        		accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
	        	mAccessory = accessory;

	        	try{
	        		mFileDescriptor = manager.openAccessory(accessory);
	        	}catch(IllegalArgumentException e){
	        		e.printStackTrace();
	        		return;
	        	}catch(NullPointerException e){
	        		e.printStackTrace();
	        		return;
	        	}

    			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
    			if (mFin ==null) mFin = new FileInputStream(fd);
    			if (mFout==null) mFout = new FileOutputStream(fd);
        	}
        }else{
        	UsbAccessory[] accessories = manager.getAccessoryList();
        	if (accessories!=null) {
            	for(UsbAccessory a : accessories){
            		L.i("accessory: "+a.getManufacturer());
            		if(a.getManufacturer().equals("Manufacturer")){
            			mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.megateam.oscidroid.USBPERMISSION"),0);
            			manager.requestPermission(a,mPermissionIntent);
            			L.i("USB: permission requested");

        	        	try{
        	        		mFileDescriptor = manager.openAccessory(a);
        	        	}catch(IllegalArgumentException e){
        	        		e.printStackTrace();
        	        		return;
        	        	}catch(NullPointerException e){
        	        		e.printStackTrace();
        	        		return;
        	        	}
        	        	
        	        	if (mFileDescriptor!=null) {
            	        	FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            	        	mFin = new FileInputStream(fd);
            	        	mFout = new FileOutputStream(fd);
        	        	}
            			break;
            		}
            	}
        	} else {
        		L.i("USB: accessories!=null");
        	}
        }
    //    new Thread(new Task()).start();
        if (mFout==null||mFin ==null) {
        	L.d("USB: mFout==null||mFin ==null!!!!!");
        }
        
        IntentFilter i = new IntentFilter();
        i.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        i.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        i.addAction("com.megateam.oscidroid.USBPERMISSION");
        context.registerReceiver(mUsbReceiver,i);
        mUsbReceiverRegisteredFlag=true;
        
	}

    void close() {
    	L.i("usbData: close");
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
		if (mFileDescriptor!=null)
			try {
				mFileDescriptor.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		if (mUsbReceiverRegisteredFlag) {
	    	context.unregisterReceiver(mUsbReceiver);
	    	mUsbReceiverRegisteredFlag=false;
		}
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
	public int fetchData(DisplayView dpv) {
		int numBytes=0;
				
		//L.i(String.format("USB: fetchData"));
		if (mFin!=null) {
			try {
				numBytes=mFin.read(uBuff, 0, 8192);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
		}
		
		ByteBuffer cfgStruct=ByteBuffer.wrap(uBuff);
		cfgStruct.order(ByteOrder.LITTLE_ENDIAN);

		//version
		cfgStruct.get();
        //mode
		cfgStruct.get();
        //xDiv
		cfgStruct.getInt();
		//windowPreviewSize
		this.windowSize=cfgStruct.getInt();
		dpv.setWindowPreviewSize(this.windowSize);
		
		{//trigger
	        //channelNum
			cfgStruct.get();
	        //mode
			cfgStruct.get();
	        //level
			cfgStruct.get();
	        //holdoff;
			cfgStruct.getInt();
	        //position
			cfgStruct.getInt();
		}

        for (int i=0;i<MAX_NUM_CHANNELS;i++){
	        //adminStatus
        	byte adminStatus;
	        dpv.setChannelAdimn(i, adminStatus=cfgStruct.get());
	        if (adminStatus!=0) enableChannelsMask|=(1<<i);
	        //yDiv
	        dpv.setChannelYdiv(i, cfgStruct.getInt());
	        //yOffset;
	        dpv.setChannelYOffset(i, cfgStruct.get());
	        //coupling 0->DC; 1->AC
	        dpv.setChannelCoupling(i, cfgStruct.get());
        } 

        return 0;
	}
    
	@Override
	public int getNumChannels() {
		return MAX_NUM_CHANNELS;
	}

	@Override
	public int setOsciMode(byte mode) {
		if (mFout!=null) {
			byte b[]=new byte[4];
			ByteBuffer cmd=ByteBuffer.wrap(b);
			cmd.order(ByteOrder.LITTLE_ENDIAN);

			//OSCIDROID_MSG_CMD_CODE_CHANNEL_ADMIN 0x81
			//typedef struct {
			//    unsigned char chIdx;
			//    unsigned char adminStatus;
			//} __attribute__ ((packed)) OSCIDROID_MSG_CMD_CHANNEL_ADMIN;

			cmd.put((byte) 0x01);//version
			cmd.put((byte) 0x01);//command code
			cmd.put((byte) mode);//chIdx
			L.i(String.format("USB: b.length --> %d ", b.length));
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
	public int setOsciWindowSize(int size) {
		if (mFout!=null) {
			
			byte b[]=new byte[6];
			ByteBuffer cmd=ByteBuffer.wrap(b);
			cmd.order(ByteOrder.LITTLE_ENDIAN);

			//OSCIDROID_MSG_CMD_CODE_GENERAL_WINDOW_SIZE 0x03
			//typedef struct {
		    //    unsigned int  windowSize;
			//} __attribute__ ((packed)) OSCIDROID_MSG_CMD_GENERAL_WINDOW_SIZE;
		
			cmd.put((byte) 0x01);//version
			cmd.put((byte) 0x03);//command code
			cmd.putInt(size);//chIdx
			L.i(String.format("USB: b.length --> %d ", b.length));
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
	public int getOsciWindowSize() {
		return this.windowSize;
	}

	private int sendChannelAdminCmd(byte chNum, byte adminStatus) {
		if (mFout!=null) {
			byte b[]=new byte[4];
			ByteBuffer cmd=ByteBuffer.wrap(b);
			cmd.order(ByteOrder.LITTLE_ENDIAN);

			//OSCIDROID_MSG_CMD_CODE_CHANNEL_ADMIN 0x81
			//typedef struct {
			//    unsigned char chIdx;
			//    unsigned char adminStatus;
			//} __attribute__ ((packed)) OSCIDROID_MSG_CMD_CHANNEL_ADMIN;

			cmd.put((byte) 0x01);//version
			cmd.put((byte) 0x81);//command code
			cmd.put((byte) chNum);//chIdx
			cmd.put((byte) adminStatus);//enable
			L.i(String.format("USB: b.length --> %d ", b.length));
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
	public int enableChannel(int chNum) {

		L.i(String.format("USB: enableChannel --> %d %s", chNum, (mFout!=null)?"mFout!=null":"mFout==null"));
		return sendChannelAdminCmd((byte)chNum, (byte) 0x01);
	}

	@Override
	public int disableChannel(int chNum) {

		L.i(String.format("USB: disableChannel --> %d %s", chNum, (mFout!=null)?"mFout!=null":"mFout==null"));
		return sendChannelAdminCmd((byte)chNum, (byte) 0x00);
	}

	
	public int getEnabledChannelsMask() {

		return enableChannelsMask;
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
	public int setAttenuation(int channel, int attenuation) {

		if (mFout!=null) {
			byte b[]=new byte[7];
			ByteBuffer cmd=ByteBuffer.wrap(b);
			cmd.order(ByteOrder.LITTLE_ENDIAN);

			//OSCIDROID_MSG_CMD_CODE_CHANNEL_Y_DIV
			//typedef struct {
			//    unsigned char chIdx;
			//        unsigned int  yDiv;
			//} __attribute__ ((packed)) OSCIDROID_MSG_CMD_CHANNEL_Y_DIV;

			cmd.put((byte) 0x01);//version
			cmd.put((byte) 0x82);//command code
			cmd.put((byte) channel);//chIdx
			cmd.putInt(attenuation);
			L.i(String.format("USB: b.length --> %d ", b.length));
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
	public int setOffset(int channel, int offset) {
		if (mFout!=null) {
			byte b[]=new byte[7];
			ByteBuffer cmd=ByteBuffer.wrap(b);
			cmd.order(ByteOrder.LITTLE_ENDIAN);

			//OSCIDROID_MSG_CMD_CODE_CHANNEL_YOFFSET
			//typedef struct {
			//    unsigned char chIdx;
			//        unsigned int  yOffset;
			//} __attribute__ ((packed)) OSCIDROID_MSG_CMD_CHANNEL_YOFFSET;

			cmd.put((byte) 0x01);//version
			cmd.put((byte) 0x83);//command code
			cmd.put((byte) channel);//chIdx
			cmd.putInt(offset);
			L.i(String.format("USB: b.length --> %d ", b.length));
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
	
	private static boolean mUsbReceiverRegisteredFlag=false;
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
	                Toast.makeText(context, "USB Attached!", Toast.LENGTH_SHORT).show();
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						mAccessory = accessory;
			        	FileDescriptor fd = null;
			        	try{
			        		fd = manager.openAccessory(accessory).getFileDescriptor();
			        	}catch(IllegalArgumentException e){
			        		e.printStackTrace();
			        	}catch(NullPointerException e){
			        		e.printStackTrace();
			        	}
			        	mFout = new FileOutputStream(fd);
			        	mFin = new FileInputStream(fd);
					} else {
						L.d("USB: permission denied for accessory " + accessory);
					}
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				L.d("USB Detached!");
                Toast.makeText(context, "USB Detached!", Toast.LENGTH_SHORT).show();

				UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(mAccessory)) {
					if(mFout != null){
						try {
							mFout.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						mFout=null;
					}
					if(mFin != null) {
						try {
							mFin.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						mFin=null;
					}
					mAccessory=null;
					
					//context.unregisterReceiver(mUsbReceiver);
					//mUsbReceiverRegisteredFlag=false;
				}
			}else if("com.megateam.oscidroid.USBPERMISSION".equals(action)){
				L.i("permission answered");
                Toast.makeText(context, "USB permission answered!", Toast.LENGTH_SHORT).show();
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
		                	L.i("added accessory");
		        			break;
		        		}
		        	}
				}
			}
		}
	};
}
