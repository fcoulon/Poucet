package cfg;

import helper.FileHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import spoon.reflect.declaration.CtExecutable;

/**
 * Main class that analyze the source code, compute the control flow graph
 * of the listeners and write them in .dot files format.
 */
public class ControlFlowGraph {
	
	/**
	 * All nodes of the control flow graph
	 */
	List<BasicBlock> nodes = new ArrayList<BasicBlock>();
	
	/**
	 * Internal representation
	 */
	SubGraph graph;
	
	/**
	 * Source of the control flow graph
	 */
	CtExecutable method;
	
	/**
	 * Create a control flow graph from this method
	 */
	public ControlFlowGraph(CtExecutable<?> method) {
		this.method = method;
		nodes = new ArrayList<BasicBlock>();
		graph = CfgBuilder.build(method,this);
		clean();
	}
	
	/**
	 * Remove unnecessary Connector nodes
	 */
	private void clean(){
		ArrayList<BasicBlock> toBeRemoved = new ArrayList<BasicBlock>();
		for(BasicBlock node : this.nodes){
			if(	node instanceof Connector && node.getChildren().size() == 1){
				BasicBlock child = node.getChildren().get(0);
				if(node.getCondition(child) == null){
					toBeRemoved.add(node);
					for(BasicBlock parent : node.getParents()){
						parent.getChildren().remove(node);
						parent.addChild(child, parent.getCondition(node));
						parent.conditions.remove(node);
					}
				}
			}
			else if(node instanceof Connector && node.getParents().size() == 0 && node.getChildren().size() == 0){
				toBeRemoved.add(node);
			}
		}
		this.nodes.removeAll(toBeRemoved);
	}
	
	public void removeNode(BasicBlock node){
		nodes.remove(node);
	}
	
	public void addNode(BasicBlock node){
		nodes.add(node);
	}
	
	public List<BasicBlock> getAllNode(){
		return nodes;
	}
	
	/**
	 * Find all possibles execution path
	 */
	public List<List<BasicBlock>> getExecutionPaths(){
		return  ExecutionPath.getPaths(graph);
	}
	
	/**
	 * Write the cfg in .dot format in the file 'outFolder'/'filename'
	 */
	public void writeDotGraph(String outFolder, String fileName){
		StringBuffer fileContent = new StringBuffer();
		fileContent.append("digraph OutputGraph { stylesheet=\"style.css\"");
		for(BasicBlock node : this.nodes){
			fileContent.append(node);
		}
		fileContent.append("}");
		
		FileHelper.writeFile(outFolder,fileName+".dot",fileContent.toString());
		System.out.println("CFG saved in " + outFolder + "/" + fileName);
	}
	
	/**
	 * Write the cfg in .svg format in the file 'outFolder'/'filename'
	 */
	public void writeSvgGraph(String inputFile, String outPutFile){
		
		Runtime runtime = Runtime.getRuntime();
        Process p;
		try {
			p = runtime.exec("dot -Tsvg "+ inputFile +" -o " +outPutFile);
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		System.out.println(outPutFile + " created");
	}
	
	/**
	 * Get the source of this control flow graph
	 */
	public CtExecutable getExecutable(){
		return method;
	}
	
	/**
	 * Return the top block
	 */
	public BasicBlock getEntryBlock(){
		return graph.getEntry();
	}
	
	/**
	 * Return the top block
	 */
	public BasicBlock getExitBlock(){
		return graph.getExit();
	}
}
