package cfg;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import spoon.compiler.Environment;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import de.java2html.Java2Html;
import de.java2html.options.JavaSourceConversionOptions;


public class MyPrinter extends DefaultJavaPrettyPrinter{
	
	Map<CtElement,Integer> startLine = new IdentityHashMap();
	Map<CtElement,Integer> endLine = new IdentityHashMap();
	
	ControlFlowGraph cfg;
	
	int line = 1;

	public MyPrinter(Environment env, ControlFlowGraph c) {
		super(env);
		cfg = c;
	}
	
	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		int s = line;
		super.visitCtMethod(m);
		int e = line;
		
		startLine.put(m, s);
		endLine.put(m, e);
	}
	
	@Override
	public <T, A extends T> void visitCtAssignment(
			CtAssignment<T, A> assignement) {
		int s = line;
		super.visitCtAssignment(assignement);
		int e = line;
		
		startLine.put(assignement, s);
		endLine.put(assignement, e);
	}
	
	@Override
	public <T> void visitCtInvocation(CtInvocation<T> arg0) {
		int s = line;
		super.visitCtInvocation(arg0);
		int e = line;
		
		startLine.put(arg0, s);
		endLine.put(arg0, e);
	}
	
	@Override
	public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
		int s = line;
		super.visitCtLocalVariable(localVariable);
		int e = line;
		
		startLine.put(localVariable, s);
		endLine.put(localVariable, e);
	}
	
	@Override
	public <R> void visitCtReturn(CtReturn<R> returnStatement) {
		int s = line;
		super.visitCtReturn(returnStatement);
		int e = line;
		
		startLine.put(returnStatement, s);
		endLine.put(returnStatement, e);
	}
	
	public int getStartLine(CtElement e){
		return startLine.get(e);
	}
	
	public int getEndLine(CtElement e){
		return endLine.get(e);
	}
	
	@Override
	public DefaultJavaPrettyPrinter writeln() {
		line++;
		return super.writeln();
	}

	public String addBlockTags(String document) {
		String old = document;
		String[] oldLines = old.split("\n");
		
		for(BasicBlock node: cfg.getAllNode()){
			List<CtCodeElement> lines = node.getElements();
			if(lines.size() != 0){
				
				//Find first & end lines of the block
				Integer start = startLine.get(lines.get(0));
				Integer end = endLine.get(lines.get(lines.size()-1));
				
				if(start != null && end != null){
					//Tag the block
					String openTag = "\n<div id=\""+node.getID()+"\">\n";
					String closeTag = "\n</div>\n";
					
					oldLines[start] = openTag + oldLines[start];
					oldLines[end] = oldLines[end] + closeTag;
				}
			}
		}
		
		StringBuffer res = new StringBuffer();
		for(String l : oldLines){
			res.append(l);
			res.append("\n");
		}
		
		return res.toString();
	}
	
	private String newDocument(String document){ //TODO:find a better name :)
		int begin = document.indexOf("<code>") + 6;
		int end = document.indexOf("</code>");
		
		String code = document.substring(begin, end);
		String codeWithTags = addBlockTags(code);
		
		StringBuffer res = new StringBuffer();
		res.append(document.substring(0, begin));
		res.append(codeWithTags);
		res.append(document.substring(end));
		
		return res.toString();
	}
	
	@Override
	public String toString() {
		String old = super.toString();
		
		JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
		options.setShowLineNumbers(true);
		String document = Java2Html.convertToHtmlPage(old,options);
		
		String code = newDocument(document);
		
		return code;
	}
}
