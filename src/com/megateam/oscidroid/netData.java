package com.megateam.oscidroid;

public class netData implements sourceIface{
	public netData(int numPointsPerChannel) {
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
	public int setAttenuation(int channel, int attenuation) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public int setOffset(int channel, int offset) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public int getNumChannels() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int enableChannel(int chNum) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int disableChannel(int chNum) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int fetchData(DisplayView dpv) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEnabledChannelsMask() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setOsciMode(byte mode) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setOsciWindowSize(int size) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getOsciWindowSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
