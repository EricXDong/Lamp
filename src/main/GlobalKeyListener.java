package main;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import event.LampEvent;
import event.LampEventGen;

public class GlobalKeyListener implements NativeKeyListener {
	private boolean shiftDown;
	private boolean altDown;
	
	public GlobalKeyListener () {
		shiftDown = altDown = false;
	}
	
    public void nativeKeyPressed (NativeKeyEvent e) {
        //	Pressing shift
        if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT_L || e.getKeyCode() == NativeKeyEvent.VC_SHIFT_R) {
            shiftDown = true;
        }
        //	Pressing alt
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT_L || e.getKeyCode() == NativeKeyEvent.VC_ALT_R) {
        	altDown = true;
        }
        //	Pressing spacebar with shift and alt down
        if (e.getKeyCode() == NativeKeyEvent.VC_SPACE && shiftDown && altDown) {
        	LampEventGen.fireLampEvent(new LampEvent(this, LampEvent.EVENTS.HOTKEY_PRESSED));
        }
    }

    public void nativeKeyReleased (NativeKeyEvent e) {
    	//  Released shift
        if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT_L || e.getKeyCode() == NativeKeyEvent.VC_SHIFT_R) {
            shiftDown = false;
        }
        //	Released alt
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT_L || e.getKeyCode() == NativeKeyEvent.VC_ALT_R) {
        	altDown = false;
        }
    }

    public void nativeKeyTyped (NativeKeyEvent e) {}
}