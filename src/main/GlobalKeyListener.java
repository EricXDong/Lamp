package main;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import event.LampEvent;
import event.LampEventGen;

public class GlobalKeyListener implements NativeKeyListener {
	public static long timeout = 1000;	//	How long user has to press all the hotkeys
	private boolean shiftDown;
	private boolean ctrlDown;
	private long startTime;
	
	public GlobalKeyListener () {
		shiftDown = ctrlDown = false;
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);
	}
	
    public void nativeKeyPressed (NativeKeyEvent e) {
        //	Pressing shift
        if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT_L || e.getKeyCode() == NativeKeyEvent.VC_SHIFT_R) {
            shiftDown = true;
            startTime = System.currentTimeMillis();
        }
        //	Pressing ctrl
        if (shiftDown && (e.getKeyCode() == NativeKeyEvent.VC_CONTROL_L || e.getKeyCode() == NativeKeyEvent.VC_CONTROL_R)) {
        	boolean isTimeout = System.currentTimeMillis() - startTime > timeout;
        	shiftDown = ctrlDown = !isTimeout;	//	Reset if timed out
        }
        //	Pressing spacebar with shift and ctrl down
        if (e.getKeyCode() == NativeKeyEvent.VC_SPACE && shiftDown && ctrlDown) {
        	if (System.currentTimeMillis() - startTime <= timeout) {
        		//	Didn't time out
        		LampEventGen.fireLampEvent(new LampEvent(this, LampEvent.EVENTS.HOTKEY_PRESSED));
        	}
        	shiftDown = ctrlDown = false;
        }
    }
 
    public void nativeKeyReleased (NativeKeyEvent e) {
    	//  Released shift
        if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT_L || e.getKeyCode() == NativeKeyEvent.VC_SHIFT_R) {
            shiftDown = false;
        }
        //	Released alt
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT_L || e.getKeyCode() == NativeKeyEvent.VC_ALT_R) {
        	ctrlDown = false;
        }
    }

    public void nativeKeyTyped (NativeKeyEvent e) {}
}