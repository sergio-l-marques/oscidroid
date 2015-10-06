package com.megateam.oscidroid;


public class usbData implements sourceIface{
	private int numPointsPerChannel;

	public usbData(int numPointsPerChannel) {
		
		this.numPointsPerChannel=numPointsPerChannel;
	}
	
	@Override
	public byte[] getSamples(int channel) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOffset(int channel, int offset) {
		// TODO Auto-generated method stub
		
	}

}
