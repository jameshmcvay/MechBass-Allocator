package solver.tests;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class Sequence extends javax.sound.midi.Sequence{

	public Sequence(float divisionType, int resolution, int numTracks)
			throws InvalidMidiDataException {
		super(divisionType, resolution, numTracks);
	}

	public boolean equals(Object o){
		if(o instanceof Sequence){
			Sequence seq = (Sequence) o;
			if(seq.getTracks().length != this.getTracks().length) return false;
//			System.out.printf("Track array length equal\n");
			for(int i = 0; i < this.getTracks().length; i++){
				Track tr1 = this.getTracks()[i];
				Track tr2 = seq.getTracks()[i];
				if(tr1.size()!=tr2.size()) return false;
//				System.out.printf("Track %d length equal\n",i);
				for(int j = 0; j < tr1.size()-1; j++){
					MidiEvent e1 = tr1.get(j);
					MidiEvent e2 = tr2.get(j);
					if(e1.getTick() != e2.getTick()) return false;
//					System.out.printf("Event %d tick equal\n",j);
					if(e1.getMessage() instanceof ShortMessage && e2.getMessage() instanceof ShortMessage){
						ShortMessage shrt1 = (ShortMessage) e1.getMessage();
						ShortMessage shrt2 = (ShortMessage) e2.getMessage();
						if(shrt1.getChannel() != shrt2.getChannel() ||
								shrt1.getCommand() != shrt2.getCommand() ||
								shrt1.getData1() != shrt2.getData1() ||
								shrt1.getData2() != shrt2.getData2()){
//							System.out.printf("Message1: %d, %d, %d, %d Message2: %d, %d, %d, %d",shrt1.getChannel(), shrt1.getCommand() , shrt1.getData1(), shrt1.getData2(), shrt2.getChannel(), shrt2.getCommand() , shrt2.getData1(), shrt2.getData2());
							return false;
						}
					}
//					else if(e1.getMessage() instanceof MetaMessage && e2.getMessage() instanceof MetaMessage){
//						MetaMessage mta1 = (MetaMessage) e1.getMessage();
//						MetaMessage mta2 = (MetaMessage) e2.getMessage();
//						if(mta1.getType() != mta2.getType() ||
//								mta1.getData() != mta2.getData()) return false;
//					}
//					else if(e1.getMessage() instanceof SysexMessage && e2.getMessage() instanceof SysexMessage){
//						SysexMessage sys1 = (SysexMessage) e1.getMessage();
//						SysexMessage sys2 = (SysexMessage) e2.getMessage();
//						if(sys1.getData() != sys2.getData())return false;
//					}
					else{
						return false;
					}
//					System.out.printf("Event %d messages equal\n",j);
				}
			}
		}
		return true;
	}
}
