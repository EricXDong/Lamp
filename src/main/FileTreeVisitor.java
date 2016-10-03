package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FilenameUtils;

public class FileTreeVisitor implements Callable<FileTreeVisitor> {
	public final int maxThreads = 80;
	
	public File rootDir;
	public ArrayList<String[]> executables;
	
	public FileTreeVisitor (File root) {
		rootDir = root;
		executables = new ArrayList<String[]>();
	}
	
	@Override
	public FileTreeVisitor call () {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);
		List<Future<FileTreeVisitor>> futures = new ArrayList<>();
		
		if (rootDir.listFiles() == null) {
			//	Don't have permission to some folders
			return this;
		}
		
		//	Iterate through all files in folder
		for (File f : rootDir.listFiles()) {
			if (f.isFile()) {
				if (FilenameUtils.getExtension(f.getAbsolutePath()).equals("exe")) {
					//	Add to executables if .exe
					executables.add(new String[] {f.getName().toLowerCase(), f.getAbsolutePath()});
				}
			}
			else {
				//	Create new thread for subtree
				FileTreeVisitor visit = new FileTreeVisitor(f);
				futures.add(executor.submit(visit));
			}
		}
		
		//	Wait for all sub threads to finish
		for (Future<FileTreeVisitor> future: futures) {
			//	Add all its execs to ours when finished
			try {
				FileTreeVisitor visit = future.get();
				for (String[] exec : visit.executables) {
					executables.add(exec);
				}
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return this;
	}
}
