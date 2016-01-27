package com.megateam.oscidroid;

public interface sourceIface {
	int getNumChannels();
	int enableChannel(int chNum);
	int disableChannel(int chNum);
	void fetchData();
	byte[] getSamples(int chNum);
	float[] getMeasures(int chNum);
	int setTriger(int chNum, int type);
	void setAttenuation(int chNum, int attenuation);
	void setOffset(int chNum, int offset);
}
