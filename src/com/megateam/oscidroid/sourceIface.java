package com.megateam.oscidroid;

public interface sourceIface {
	byte[] getSamples(int channel);
	float[] getMeasures(int channel);
	int setTriger(int channel, int type);
	void setAttenuation(int channel, int attenuation);
	void setOffset(int channel, int offset);
}
