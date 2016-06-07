package dk.nota.oxygen.common;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultEditorKit;
import ro.sync.exml.workspace.api.standalone.ui.PopupMenu;

public class ConsoleWindow extends JFrame {
	
	private PopupMenu contextMenu;
	private JScrollPane scrollPane;
	private JTextArea textArea;
	
	public ConsoleWindow(String title) {
		super(title);
		setupTextArea();
		setupScrollPane();
		setupContextMenu();
		setContentPane(scrollPane);
		pack();
		setVisible(true);
	}
	
	public static void main(String[] args) {
		ConsoleWindow consoleWindow = new ConsoleWindow("Test");
		for (int i = 1; i <= 200; i++) consoleWindow.writeToConsole("Test");
	}
	
	private void setupContextMenu() {
		contextMenu = new PopupMenu();
		Action copyAction = new DefaultEditorKit.CopyAction();
		copyAction.putValue(Action.NAME, "Copy");
		copyAction.putValue(Action.LARGE_ICON_KEY, new ImageIcon(getClass()
				.getResource("/images/Copy24.png")));
		copyAction.putValue(Action.SMALL_ICON, new ImageIcon(getClass()
				.getResource("/images/Copy16.png")));
		contextMenu.add(copyAction);
	}
	
	private void setupScrollPane() {
		scrollPane = new JScrollPane(textArea);
		scrollPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}
	
	private void setupTextArea() {
		textArea = new JTextArea(25, 100);
		textArea.setBorder(new EmptyBorder(5, 5, 5, 5));
		textArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
		textArea.setLineWrap(true);
		textArea.addMouseListener(new ContextMenuListener());
		textArea.setEditable(false);
	}
	
	public void writeToConsole(String text) {
		if (textArea.getCaretPosition() == 0) textArea.append(text);
		else textArea.append("\n" + text);
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}
	
	private class ContextMenuListener extends MouseAdapter {
		
		public void mousePressed(MouseEvent event) {
			showContextMenu(event);
		}
		
		public void mouseReleased(MouseEvent event) {
			showContextMenu(event);
		}
		
		private void showContextMenu(MouseEvent event) {
			if (event.isPopupTrigger()) contextMenu.show(event.getComponent(),
					event.getX(), event.getY());
		}
		
	}

}
