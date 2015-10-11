package com.megateam.oscidroid;

public class genData implements sourceIface {
	
	private int numPointsPerChannel;
	private float[] attenuation;
	///private char[] offset;
	private final static int maxNumChannels=4; 

	public genData(int numPointsPerChannel) {
		
		this.numPointsPerChannel=numPointsPerChannel;

		this.attenuation = new float[maxNumChannels];
		///this.offset= new char[maxNumChannels];
		for (int i=0;i<maxNumChannels;i++) {
			this.attenuation[i]=1;
			///this.offset[i]=256/2;
		}

		L.i("RTG", String.format("genData new")); //Log Message
	}
	
	@Override
	public byte[] getSamples(int channel) {
		switch (channel) {
		case 0: return generateDataChar1(); 
		case 1: return generateDataChar2();
		default: return null;
		}
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

	
	int fase=0;
	
	private byte[] generateDataChar1() {
		
		int chanHeightGraph=256;
		
	      int count = this.numPointsPerChannel;
	      byte[] values = new byte[count];
	      for (int i=0; i<count; i++) {
	          //double y=(chanHeightGraph/2)*Math.cos(2*Math.PI/50*x+fase)+chanHeightGraph/2 + seriesOffset[0];
	          double y=(chanHeightGraph/2)*Math.cos(2*Math.PI/(this.numPointsPerChannel/5)*i+fase)/*+this.offset[0]*/;
	          
	          ///y+=this.offset[0];
	          
	          values[i] = (byte)y;
	          //values[i] = (char) ((char)y + this.offset[0]);
	          
	          //Log.i("RTG", String.format("genData: %d", (int) values[i])); //Log Message
	          values[i] *= this.attenuation[0];

	          //Log.i("RTG", String.format("genData: %d", (int) values[i]));
	          //if ((char) ((char)y + this.offset[0])>255) values[i] = 255;
	          //else values[i] = (char) ((char)y + this.offset[0]);
	          
	          //values[i] = (char) ((char)y + this.offset[0]);
	          
	          //float f=(float)y;
	          //if (i<10) Log.i("RTG", String.format("generateDataChar1: y %f %d", f, values[i])); //Log Message
	          //if (i<10) Log.i("RTG", String.format("generateDataChar1: y  %f value %d", f, (char)y)); //Log Message
	      }
			
	      if (fase>=360)fase=0;
	      else fase+=30; 

	      return values;
		}

		private byte[] generateDataChar2() {
			int chanHeightGraph=256;

			int count = this.numPointsPerChannel;
			byte[] values = new byte[count];
	      
			for (int i=0; i<count; i++) {
	      
				double y=(((i+fase)%(this.numPointsPerChannel/6))/(this.numPointsPerChannel/12))*(chanHeightGraph*0.90)-chanHeightGraph*0.5+chanHeightGraph*0.05;
	          
		          ///y+=this.offset[1];
		          
		          values[i] = (byte)y;
		          
		          values[i] *= this.attenuation[1];
			}
            if (fase>=360)fase=0;
	        else fase+=30; 
	      
			return values;
		}

		@Override
		public void setAttenuation(int channel, int attenuation) {
			
			if (attenuation==+1) {
				this.attenuation[channel]*=0.5;
				///this.offset[channel]*=2;
			} else {
				this.attenuation[channel]*=2;
				///this.offset[channel]*=0.5;
			}
		}

		@Override
		public void setOffset(int channel, int offset) {
			///Log.i("RTG", String.format("setOffset: %d %d %d", (int)this.offset[channel]+(int)offset, (int)this.offset[channel], (int)offset));
			//if ((int)this.offset[channel]+(int)offset<255) {
				///this.offset[channel]+=offset;
			//}
		}
		
		//O 128-->0V 255-->+V 0-->-V
		
		
		
		
}