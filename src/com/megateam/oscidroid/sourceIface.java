package com.megateam.oscidroid;

public interface sourceIface {
	int getNumChannels();
	int setOsciMode(byte mode);
	int setOsciWindowSize(int size);
	public int getOsciWindowSize();
	int enableChannel(int chNum);
	int disableChannel(int chNum);
	int fetchData(DisplayView dpv);
	byte[] getSamples(int chNum);
	float[] getMeasures(int chNum);
	int setTriger(int chNum, int type);
	int setAttenuation(int chNum, int attenuation);
	int setOffset(int chNum, int offset);
	int getEnabledChannelsMask();
}
