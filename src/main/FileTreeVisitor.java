package main;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

public class FileTreeVisitor extends Thread {
	
	public File rootDir;
	public ArrayList<String[]> executables;
	
	public FileTreeVisitor (File root) {
		rootDir = root;
		executables = new ArrayList<String[]>();
	}
	
	@Override
	public void run () {
		ArrayList<FileTreeVisitor> allThreads = new ArrayList<FileTreeVisitor>();
		
		if (rootDir.listFiles() == null) {
			//	Don't have permission to some folders
			return;
		}
		
		//	Iterate through all files in folder
		for (File f : rootDir.listFiles()) {
			if (f.isFile()) {
				if (FilenameUtils.getExtension(f.getAbsolutePath()).equals("exe")) {
					//	Add to executables if .exe
					executables.add(new String[] {f.getName().toLowerCase(), f.getAbsolutePath()});
				}
				//	Sleep a bit to reduce CPU usage
				try {
					Thread.sleep(5);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else {
				//	Create new thread for subtree
				FileTreeVisitor visit = new FileTreeVisitor(f);
				visit.start();
				allThreads.add(visit);
			}
		}
		
		//	Wait for all sub threads to finish
		for (FileTreeVisitor t : allThreads) {
			try {
				t.join();
				//	Add all its execs to ours when finished
				for (String[] exec : t.executables) {
					executables.add(exec);
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
