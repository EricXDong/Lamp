package event;

import java.util.EventObject;

public class LampEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	
	public EVENTS myEvent;
	
	public enum EVENTS {
		HOTKEY_PRESSED,
		DONE_UPDATING
	};

	public LampEvent (Object source, EVENTS event) {
		super(source);
		myEvent = event;
	}
}
