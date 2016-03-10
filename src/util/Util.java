package util;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Dialog.ModalityType;
import java.awt.event.InputEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import event.LampEvent;
import event.LampEventGen;
import event.LampEventListener;

public class Util {
	//	Simulate mouse click at (x, y)
	public static void simulateMouseClick (int x, int y) {
		try {
			Robot bot = new Robot();
			int mask = InputEvent.BUTTON1_DOWN_MASK;
			bot.mouseMove(x, y);
			bot.mousePress(mask);
			bot.mouseRelease(mask);
		}
		catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	//	Calculates the center point of the screen
	public static int[] getCenterScreenCoords () {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		return new int[] {dim.width / 2, dim.height / 2};
	}
	
	//	Centers the Component on screen
	public static void centerComponentOnScreen (Component comp) {
		int[] center = Util.getCenterScreenCoords();
		Dimension dim = comp.getSize();
		comp.setLocation(
			center[0] - dim.width / 2,
			center[1] - dim.height / 2
		);
	}
	
	//	Remove selected items from the JList
	public static <T> ArrayList<T> removeSelectedItemsFromJList (JList<T> jList) {
		DefaultListModel<T> model = (DefaultListModel<T>)jList.getModel();
		ArrayList<T> itemsRemoved = new ArrayList<T>();
		int[] selected = jList.getSelectedIndices();
		
		//	Remove each selected item
		for (int i = 0; i < selected.length; i++) {
			T item = model.getElementAt(selected[i]);
			itemsRemoved.add(item);		//	Add to ArrayList for deletion
			model.remove(selected[i]);
			
			//	Decrease all succeeding indices by 1 because of the removal
			for (int j = i + 1; j < selected.length; ++j) {
				--selected[j];
			}
		}
		jList.setModel(model);
		
		return itemsRemoved;
	}
	
	//	Create and return an instance of a JFrame message window
	public static JDialog createMessageWindow (String message, String title, int height) {
		JDialog mess = new JDialog();
		mess.setSize(10 * message.length(), height);
		mess.setUndecorated(true);
		mess.setTitle(title);
		mess.setResizable(false);
		Util.centerComponentOnScreen(mess);
		
		JPanel textPane = new JPanel(new GridBagLayout());
		textPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		JLabel label = new JLabel(message);
		label.setFont(new Font("SansSerif", Font.BOLD, 14));
		textPane.add(label);
		mess.add(textPane, BorderLayout.CENTER);
		
		return mess;
	}
}
