package event;

import java.util.ArrayList;

public class LampEventGen {
	private static ArrayList<LampEventListener> listeners = new ArrayList<LampEventListener>();

	public static synchronized void addEventListener (LampEventListener l) {
		listeners.add(l);
	}
	public static synchronized void removeEventListener (LampEventListener l) {
		listeners.remove(l);
	}
	
	public static synchronized void fireLampEvent (LampEvent event) {
		for (LampEventListener listen : listeners) {
			listen.handleEvent(event);
		}
	}
}
