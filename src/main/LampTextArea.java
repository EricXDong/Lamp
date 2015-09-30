package main;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class LampTextArea extends JTextPane {
	private static final long serialVersionUID = 1L;
	
	Action selectLineAction;
	
	public LampTextArea () {
		super();
		this.setEditable(false);
		this.setText("hi\nasdlkfajs\naifwoifj\nasjdf");
		
		try {
			DefaultHighlighter hi = new DefaultHighlighter();
			Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);

			hi.addHighlight(1, 2, painter);
			this.setHighlighter(hi);
		}
		catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		
		//	Find the select line action for the text area
		selectLineAction = null;
		for (Action action : this.getActions()) {
			if (action.getValue(Action.NAME).equals(DefaultEditorKit.selectLineAction)) {
				selectLineAction = action;
				break;
			}
		}
		
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked (MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					selectLineAction.actionPerformed(null);
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
		    public void mouseReleased(MouseEvent e) {}
			@Override
		    public void mouseEntered(MouseEvent e) {}
			@Override
		    public void mouseExited(MouseEvent e) {}
		});
	}
}
