package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;

import org.apache.commons.io.FilenameUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import util.Util;
import event.LampEvent;
import event.LampEventGen;
import event.LampEventListener;

/**
 * TODO: Show waiting window if user tries to open search before paths have been parsed
 * TODO: Implement add/remove buttons for aliases
 * TODO: Can't add the same alias/path twice
 * @author Eric Dong
 */

public class Lamp implements LampEventListener {
	/********	Static variables	********/
	
	public static final String pathsDir = "./data/paths.txt";
	public static final String aliasDir = "./data/alias.txt";
	
	/********	Member variables	********/
	private HashSet<String> searchPaths;
	private HashMap<String, String> aliases;
	private HashMap<String, String> executables;
	
	private JFrame searchBox;
	private JFrame settingsMenu;
	private JTextField textField;
	
	private JList<String> searchPathJList;
	private JList<String> aliasJList;
	
	private boolean toggleSearch;
	
	/***********************************/
	/********	Public Methods	********/
	/***********************************/
	
	public Lamp (ArrayList<String> searchPaths, HashMap<String, String> aliases) {
		this.searchPaths = new HashSet<String>();
		
		//	Add to hash set
		for (String path : searchPaths) {
			this.searchPaths.add(path);
		}
		this.aliases = aliases;
		toggleSearch = false;
		executables = new HashMap<String, String>();
		
        this.updateDatabase(searchPaths);
        System.out.println("Database ready!");
		this.initUI();
	}
	
	//	Listen for hotkey event to open search
	public void handleEvent (LampEvent event) {
		if (event.myEvent == LampEvent.EVENTS.HOTKEY_PRESSED) {
			this.toggleSearchBox();
		}
	}
	
	//	Searches for executable matching search query
	public void searchQuery (String query) {
		String ogQuery = query;
		
		//	Check if entered an alias
		if (aliases.get(query) != null) {
			query = aliases.get(query);
		}
		
		//	Append .exe if not present
		if (!query.substring(query.length() - 4).equals(".exe")) {
			query += ".exe";
		}
		query = query.toLowerCase();
		
		String execPath = executables.get(query);
		if (execPath == null) {
			//	Not found
			JOptionPane.showMessageDialog(null, "Application: " + ogQuery + " not found");
			textField.setText("");
		}
		else {
			try {
				Runtime.getRuntime().exec(execPath);
				this.toggleSearchBox();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/***************************************/
	/********	Static Methods		********/
	/***************************************/
	
	public static void main (String[] args) {
		//	Read from data files
		BufferedReader br = null;
		try {
			//	Search paths
			br = new BufferedReader(new FileReader(pathsDir));
			
			ArrayList<String> paths = new ArrayList<String>();
			for (String line = null; (line = br.readLine()) != null;) {
				paths.add(line);
			}
			br.close();
			
			//	Aliases
			br = new BufferedReader(new FileReader(aliasDir));
			
			HashMap<String, String> aliases = new HashMap<String, String>();
			for (String line = null; (line = br.readLine()) != null;) {
				//	Format = alias:true-name
				String[] split = line.split(":");
				aliases.put(split[0], split[1]);
			}
			
			//	Initialize Lamp
			new Lamp (paths, aliases);
			
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				br.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//	Start up the key listener
		try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook!");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
	}

	/***************************************/
	/********	Private Methods		********/
	/***************************************/
	
	/**	Set up logs and parse file directories
	 *	@param paths: list of paths to parse
	 */
	private void updateDatabase (ArrayList<String> paths) {
		//	Go down directory tree of each path
		for (String path : paths) {
			File f = new File(path);
			
			if (f.isFile()) {
				if (FilenameUtils.getExtension(f.getAbsolutePath()).equals(".exe")) {
					executables.put(f.getName().toLowerCase(), f.getAbsolutePath());
				}
			}
			else {
				try {
					//	Kick off a visitor and let it do its thang
					FileTreeVisitor visitor = new FileTreeVisitor(f);
					visitor.start();
					visitor.join();
					
					//	Store/remove all exec info
					for (String[] exec : visitor.executables) {
						executables.put(exec[0], exec[1]);
					}
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		LampEventGen.fireLampEvent(new LampEvent(this, LampEvent.EVENTS.DONE_UPDATING));
	}
	
	//	Toggles search box visible/hidden
	private void toggleSearchBox() {
		toggleSearch = !toggleSearch;
		searchBox.setVisible(toggleSearch);
		textField.setText("");
	}
	
	//	Initialize the GUI
	private void initUI () {
		/********	Create searchBox JFrame	********/
		searchBox = new JFrame("Lamp");
		searchBox.setSize(600, 110);
		Util.centerComponentOnScreen(searchBox);
		
		//	Create menu
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem settingsItem = new JMenuItem("Settings...");
		settingsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent ae) {
				if (!settingsMenu.isVisible()) {
					settingsMenu.setVisible(true);
				}
			}
		});
		JMenuItem exitItem = new JMenuItem("Quit Lamp");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent ae) {
				System.exit(0);
			}
		});
		
		menu.add(settingsItem);
		menu.add(exitItem);
		menuBar.add(menu);
		searchBox.setJMenuBar(menuBar);
		
		//	Create JPanel
		JPanel thePane = new JPanel(new BorderLayout());
		
		//	Create text field
		textField = new JTextField();
		textField.setColumns(30);
		textField.setFont(new Font("SansSerif", Font.PLAIN, 28));
		thePane.add(textField);
		
		searchBox.getContentPane().add("Center", thePane);
		
		/********	Create settingsMenu JFrame	********/
		settingsMenu = new JFrame("Settings");
		settingsMenu.setSize(600, 400);
		Util.centerComponentOnScreen(settingsMenu);
		
		//	Parent panel for both JLists
		JPanel settingsPanel = new JPanel(new GridLayout(1, 2));
		//	Child panels
		JPanel pathPanel = new JPanel(new BorderLayout());
		JPanel aliasPanel = new JPanel(new BorderLayout());
		
		//	Create JList for displaying all paths
		searchPathJList = this.createAndAttachJList(pathPanel, this.searchPaths.toArray(new String[this.searchPaths.size()]));
		
		//	Convert all entries in alias map to string array
		String[] arrayForm = new String[this.aliases.size()];
		Iterator<Map.Entry<String, String>> it = this.aliases.entrySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			arrayForm[i] = "\"" + entry.getKey() + "\" = \"" + entry.getValue() + "\"";
			++i;
		}
		//	Create JList for displaying all aliases
		aliasJList = this.createAndAttachJList(aliasPanel, arrayForm);
		
		//	Add to settings panel
		settingsPanel.add(pathPanel);
		settingsPanel.add(aliasPanel);
		
		//	Create JLabels for the JLists
		JLabel label = new JLabel("Search Paths", SwingConstants.CENTER);
		label.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		label.setFont(new Font("SansSerif", Font.BOLD, 16));
		pathPanel.add(label, BorderLayout.NORTH);
		
		label = new JLabel("Aliases", SwingConstants.CENTER);
		label.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		label.setFont(new Font("SansSerif", Font.BOLD, 16));
		aliasPanel.add(label, BorderLayout.NORTH);
		
		//	Create Add/Remove buttons for both panels
		JPanel buttonPanel = new JPanel();
		JButton pathAddButton = new JButton("Add...");
		pathAddButton.setPreferredSize(new Dimension(80, 25));
		JButton pathRemoveButton = new JButton("Remove");
		pathRemoveButton.setPreferredSize(new Dimension(80, 25));
		buttonPanel.add(pathAddButton);
		buttonPanel.add(pathRemoveButton);
		pathPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		buttonPanel = new JPanel();
		JButton aliasAddButton = new JButton("Add...");
		aliasAddButton.setPreferredSize(new Dimension(80, 25));
		JButton aliasRemoveButton = new JButton("Remove");
		aliasRemoveButton.setPreferredSize(new Dimension(80, 25));
		buttonPanel.add(aliasAddButton);
		buttonPanel.add(aliasRemoveButton);
		aliasPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		//	Add OK/Cancel buttons
		JPanel okCancelPanel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.setPreferredSize(new Dimension(100, 30));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(new Dimension(100, 30));
		
		okCancelPanel.add(okButton);
		okCancelPanel.add(cancelButton);
		
		settingsMenu.getContentPane().add(okCancelPanel, BorderLayout.SOUTH);
		settingsMenu.getContentPane().add(settingsPanel, BorderLayout.CENTER);
		
		/********	LISTENERS	********/
		
		ArrayList<String> execsDeleted = new ArrayList<String>();
		ArrayList<String> execsAdded = new ArrayList<String>();
		
		//	OK button
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				//	Write the path updates to file
				BufferedWriter bw = null;
				try {
					bw = new BufferedWriter(new FileWriter(pathsDir, false));
					ListModel<String> model = searchPathJList.getModel();
					
					//	Iterate through the paths
					for (int i = 0; i < model.getSize(); i++) {
						String path = model.getElementAt(i);
						bw.write(path);
						bw.newLine();
					}
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}
				finally {
					try {
						bw.close();
					}
					catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
				//	Remove deleted items from executables, if any
				for (int i = 0; i < execsDeleted.size(); ++i) {
					String toRemove = execsDeleted.get(i);
					searchPaths.remove(execsDeleted.get(i));	//	Remove from search paths
					
					//	Remove all executables containing the search path
					for (Iterator<Map.Entry<String, String>> it = executables.entrySet().iterator(); it.hasNext(); ) {
						Map.Entry<String, String> entry = it.next();
						if (entry.getValue().contains(toRemove)) {
							it.remove();
						}
					}
				}
				execsDeleted.clear();
				
				//	Add the added items to executables, if any
				if (execsAdded.size() > 0) {
					//	Tell user to sit tight
					JDialog mess = Util.createMessageWindow("Parsing new search paths...", "Lampin around", 60);
					
					//	Register a listener for done updating event
					LampEventGen.addEventListener(new LampEventListener () {
						@Override
						public void handleEvent (LampEvent event) {
							if (event.myEvent == LampEvent.EVENTS.DONE_UPDATING) {
								mess.dispose();
							}
						}
					});
					
					//	Use separate thread so isn't blocked by modality
					Thread t = new Thread() {
						@Override
						public void run () {
							updateDatabase(execsAdded);
						}
					};
					t.start();
					
					//	Show the blocking dialog
					mess.setModalityType(ModalityType.APPLICATION_MODAL);
					mess.setVisible(true);
					
					execsAdded.clear();
				}
				
				settingsMenu.dispose();
			}
		});
		
		//	Cancel button
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				//	Put removed items back in the list
				DefaultListModel<String> model = (DefaultListModel<String>)searchPathJList.getModel();
				for (int i = 0; i < execsDeleted.size(); ++i) {
					model.addElement(execsDeleted.get(i));
				}
				
				//	Take added items away from the list
				for (int i = 0; i < execsAdded.size(); ++i) {
					for (int j = 0; j < model.size(); ++j) {
						if (execsAdded.get(i).equals(model.getElementAt(j))) {
							model.remove(j);
							--j;
						}
					}
				}
				
				searchPathJList.setModel(model);
				execsDeleted.clear();
				execsAdded.clear();
				settingsMenu.dispose();
			}
		});
		
		//	Add path button
		pathAddButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				//	Open dialog for user to select folder
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int ret = fc.showOpenDialog(settingsPanel);
				
				if (ret == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					execsAdded.add(f.getAbsolutePath());
					
					//	Add file to JList
					DefaultListModel<String> model = (DefaultListModel<String>)searchPathJList.getModel();
					model.addElement(f.getAbsolutePath());
					searchPathJList.setModel(model);
				}
			}
		});
		
		//	Remove path button
		pathRemoveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				//	Remove the items from JList and add to list of to-be-deleted
				execsDeleted.addAll(Util.removeSelectedItemsFromJList(searchPathJList));
			}
		});
		
		//	Add alias button
		aliasAddButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				JDialog aliasDialog = new JDialog();
			}
		});
		
		//	Remove alias button
		aliasRemoveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				
			}
		});
		
		//	Listen for events from the key listener
        LampEventGen.addEventListener(this);
		
		//	Called if searchBox is opened
		searchBox.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened (WindowEvent e) {
				//	Simulate click to focus on window (requesting focus doesn't work)
				int[] center = Util.getCenterScreenCoords();
				Util.simulateMouseClick(center[0], center[1]);
			}
		});
		
		//	In case user clicks close button, set the toggle flag correctly
		searchBox.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing (WindowEvent we) {
				Lamp.this.toggleSearch = false;
			}
		});
		
		//	Enter key
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent ae) {
				Lamp.this.searchQuery(textField.getText());
			}
		});
	}
	
	//	Create a JList and attach it to parent
	private JList<String> createAndAttachJList (JPanel parent, String[] contents) {
		//	Create model and add to it
		DefaultListModel<String> model = new DefaultListModel<String>();
		for (String s : contents) {
			model.addElement(s);
		}
		
		//	Create JList with the model
		JList<String> list = new JList<String>(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		
		//	Attach to scroll pane and add border/padding
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(new CompoundBorder(
			BorderFactory.createEmptyBorder(10, 20, 5, 20),
			BorderFactory.createLineBorder(Color.BLACK, 2))
		);
		parent.add(scrollPane, BorderLayout.CENTER);
		
		return list;
	}
}
