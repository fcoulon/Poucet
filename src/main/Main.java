package main;

import html.HtmlGenerator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import cfg.CfgBuilder;
import cfg.ControlFlowGraph;
import spoon.compiler.SpoonCompiler;
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessingManager;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.QueueProcessingManager;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

public class Main {
	public static void main(String[] args) {
		
		final List<ControlFlowGraph> methods = new ArrayList<ControlFlowGraph>();
		
		/*
		 * Spoon processs
		 */
		//Setup the factory
		StandardEnvironment env = new StandardEnvironment();
		try {
			System.out.println("Loading...");
			//Locate libraries
			//Spoon ask the thread's classloader to retrieve classses
			ClassLoader libLoader = new URLClassLoader(getDependencies(getClasspath(args[1])), Thread.currentThread().getContextClassLoader());
			Thread.currentThread().setContextClassLoader(libLoader);
			
//			Setup the factory
//			StandardEnvironment env = new StandardEnvironment();
//			env.setVerbose(true);
//	        env.setDebug(true);
			DefaultCoreFactory f = new DefaultCoreFactory();
	        Factory factory = new FactoryImpl(f, env);
	        CfgBuilder.factory = factory;
			
	        //Build the model
	        SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);
	        for(String sourceFolder : getSources(args[0])){
	        	compiler.addInputSource(new File(sourceFolder));
	        }
			compiler.build();
			
			System.out.println("Loading done.");
			
			System.out.println("Processing...");
			env.setInputClassLoader(ClassLoader.getSystemClassLoader());
			
			ProcessingManager processorManager = new QueueProcessingManager(factory);
			processorManager.addProcessor(new AbstractProcessor<CtExecutable>() {
				@Override
				public void process(CtExecutable method) {
					methods.add(new ControlFlowGraph(method));
				}
			});
			processorManager.process();
			System.out.println("Processing done.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		 * Build html pages
		 */
		System.out.println("Building html...");
		HtmlGenerator.build(methods, env, "Output");
		System.out.println("Html done");
	}

	//TODO: improve this
	private static List<String> getSources(String argSource) {
		List<String> res = new ArrayList<String>();
		res.add(argSource);
		return res;
	}

	//TODO: improve this
	private static List<String> getClasspath(String argClasspath) {
		List<String> res = new ArrayList<String>();
		res.add(argClasspath);
		return res;
	}
	
	private static URL[] getDependencies(String folder){
		File folderDir = new File(folder);
		folderDir.isDirectory();
		File[] libs = folderDir.listFiles();
		
		ArrayList<URL> urls = new ArrayList<URL>();
		for(File file : libs){
			if(file.isDirectory()){
				URL[] deeperFolders = getDependencies(file.getAbsolutePath());
				for(URL r : deeperFolders){
					urls.add(r);
				}
			}
			else{
				try {
//					if(file.getName().endsWith(".jar")){
						urls.add(file.toURI().toURL());
//					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
		return urls.toArray(new URL[urls.size()]);
	}
	//Get all libraries dependencies
	private static URL[] getDependencies(List<String> folders){
		
		List<URL[]> urls = new ArrayList<URL[]>();
		for(String folder : folders){
			urls.add(getDependencies(folder));
		}
		
		int size = 0;
		for(URL[] url : urls){
			size += url.length;
		}
		
		URL[] res = new URL[size];
		size = 0;
		for(int i = 0; i < urls.size(); i++){
			URL[] url = urls.get(i);
			for(int j = 0; j < url.length; j++){
				res[size+j] = url[j];
			}
			size += url.length;
		}
		
		for(URL url : res){
			System.out.println(url);
		}
		
		return res;
	}
}
