package html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cfg.BasicBlock;
import cfg.Connector;
import cfg.ControlFlowGraph;
import cfg.MyPrinter;
import spoon.compiler.Environment;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtSimpleType;

public class HtmlGenerator {
private static void genHmtlPages(String fileName, CtExecutable method, ControlFlowGraph ctrlFlowGraph, Environment environment, String outputFolder){ //TODO: clean this function
		
		String scriptHead = "<head><script src=\"lib/svg-pan-zoom.min.js\"></script></head>";
		String script = "    <script>"+
						      "window.onload = function() {"+
						        "svgPanZoom('#"+fileName.replace(".", "_").replace("@", "_").replace("$", "_")+"', {"+
						          "zoomEnabled: true,"+
						          "controlIconsEnabled: true"+
						        "});"+
						      "};" +
						    "</script>";
		
		String zoomPanSvg_cfg = "<embed id=\""+fileName.replace(".", "_").replace("$", "_").replace("@", "_")+"\" type=\"image/svg+xml\" src=\"dotFolder/"+fileName+".svg\" width=\"100%\" height=\"600\"/>";
		String zoomPanSvg_defuse = "<embed id=\""+fileName.replace(".", "_").replace("$", "_").replace("@", "_")+"\" type=\"image/svg+xml\" src=\"defUseFolder/"+fileName+".svg\" width=\"100%\" height=\"250\"/>";
		
//		String cfg = "<!DOCTYPE html><html><body><img src=\"dotFolder/"+fileName+".dot.svg\"></body></html>";
//		String defuse = "<!DOCTYPE html><html><body><img src=\"defUseFolder/"+fileName+".dot.svg\"></body></html>";
		String cfg = "<!DOCTYPE html><html>"+scriptHead+"<body>"+zoomPanSvg_cfg+script+"</body></html>";
		String defuse = "<!DOCTYPE html><html>"+scriptHead+"<body>"+zoomPanSvg_defuse+script+"</body></html>";
//		JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
//		options.setShowLineNumbers(true);
//		String code = Java2Html.convertToHtmlPage(method.toString(),options);
		MyPrinter printer = new MyPrinter(environment,ctrlFlowGraph);
		printer.scan(method);
		String code = printer.toString();
		
		StringBuffer scriptPart = new StringBuffer();
		for(BasicBlock block : ctrlFlowGraph.getAllNode()){
			if(!(block instanceof Connector)){
				scriptPart.append("addColor(\""+ block.getID() +"\");\n");
			}
		}
		String main = "<!DOCTYPE html>" +
				"<html><body onload=\'myLoader()\'>" +
				"<h1>"+ fileName +"<h1>" +
				"<table width=\"100%\">" +
				"<tr>" +
				  "<td width=\"40%\"><iframe src=\""+fileName +"_code.html\" id=\"code_frame\" width=\"100%\" height=\"600\"></iframe></td>" +
				  "<td width=\"60%\"><iframe src=\""+fileName +"_cfg.html\" id=\"cfg_frame\" width=\"100%\" height=\"600\"></iframe></td>" +
				"</tr>" +
				"<tr>" +
				  "<td colspan=\"2\"><iframe src=\""+fileName +"_defuse.html\" id=\"defuse_frame\" width=\"100%\" height=\"250\"></iframe></td>" +
				"</tr>" +
				"</table>" +
				
	"<script>" +
			"function addColor(nodeID){" +
				"var if_cfg= document.getElementById('cfg_frame');" +
				"var node = if_cfg.contentWindow.document.getElementById(\""+fileName.replace(".", "_").replace("@", "_").replace("$", "_")+"\").getSVGDocument().getElementById(nodeID);" +
				"node.addEventListener(\"mouseover\", colorOver, false);" +
				"node.addEventListener(\"mouseout\", colorOut, false);" +
			"}" +
	
			"function colorOver(evt){" +
				"var id = evt.currentTarget.id;" +
				"var if_code = document.getElementById('code_frame');" +
				"var lines = if_code.contentWindow.document.getElementById(id);"+ 
				"lines.setAttribute('style','background-color:#F0D8A8;');" +
			"}"+ 
	
			"function colorOut(evt){" +
				"var id = evt.currentTarget.id;" +
				"var if_code = document.getElementById('code_frame');" +
				"var lines = if_code.contentWindow.document.getElementById(id);" +
				"lines.setAttribute('style','background-color:write');" +
			"}" +
	
			"function myLoader(){" + 
	
				scriptPart.toString() +
	
			"}" +
	"</script>" +
				
				"</body>" +
				"</html> ";
		
		
		
		ControlFlowGraph.writeFile(outputFolder+"/html/", fileName +"_defuse.html", defuse.toString());
		ControlFlowGraph.writeFile(outputFolder+"/html/", fileName +"_cfg.html", cfg.toString());
		ControlFlowGraph.writeFile(outputFolder+"/html/", fileName +"_code.html", code.toString());
		ControlFlowGraph.writeFile(outputFolder+"/html/", fileName +"_main.html", main.toString());
		
	}
	
	private static void genHtmlIndex(List<String> methodsIndex, String outputFolder){
		
		StringBuffer methods = new StringBuffer();
		Collections.sort(methodsIndex, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String[] parts = o1.split("\\.");
				String[] methodNameTmp = parts[parts.length-1].split("@");
				String o1Name = methodNameTmp[0];
				if(methodNameTmp.length == 2){
					o1Name = methodNameTmp[methodNameTmp.length-2];
				}
				String className = parts[parts.length-2];
				String name1 = className +"."+ o1Name;
				
				parts = o2.split("\\.");
				methodNameTmp = parts[parts.length-1].split("@");
				String o2Name = methodNameTmp[0];
				if(methodNameTmp.length == 2){
					o2Name = methodNameTmp[methodNameTmp.length-2];
				}
				className = parts[parts.length-2];
				String name2 = className +"."+ o2Name;
				
				return name1.compareTo(name2);
			}
		});
		for(String method : methodsIndex){
			//Expected name: some.package.className.methodName@someID
			String[] parts = method.split("\\.");
			String[] methodNameTmp = parts[parts.length-1].split("@");
			String methodName = methodNameTmp[0];
			if(methodNameTmp.length == 2){
				methodName = methodNameTmp[methodNameTmp.length-2];
			}
			String className = parts[parts.length-2];
			methods.append("<p><a href=\""+method+"_main.html\" target=\"graph_frame\">"+ className +"."+ methodName+ "</a></p>");
		}
		
		String methodIndex = "<!DOCTYPE html><html><body>"+methods.toString()+"</body></html>";
		
		String index = "<!DOCTYPE html>" +
				"<html><body>" +
				"<table width=\"100%\">" +
				"<tr>" +
				  "<td width=\"20%\">"+ 
				  "<iframe name=\"index_frame\" src=\"html/methodIndex.html"+"\" width=\"100%\" height=\"1000\"></iframe>" +
				  "</td>" +
				  "<td><iframe name=\"graph_frame\" width=\"100%\" height=\"1000\"></iframe></td>" +
				"</tr>" +
				"</table>" +
				"</body>" +
				"</html> ";
		
		ControlFlowGraph.writeFile(outputFolder+"/html/", "methodIndex.html", methodIndex.toString());
		ControlFlowGraph.writeFile(outputFolder, "index.html", index.toString());
	}
	
	public static void build(List<ControlFlowGraph> cfgs, Environment environment, String outputFolder){
		List<String> methodsIndex = new ArrayList<String>();
		int ID = 0;
		
		for(ControlFlowGraph cfg : cfgs){
			CtExecutable method = cfg.getExecutable();
			CtSimpleType<?> clazz = method.getDeclaringType();
			
			String methodID = clazz.getQualifiedName() + "." + method.getSimpleName();
			if(methodsIndex.contains(methodID)){ //check if the name is already registered
				methodID = methodID + "@" + ID;
				ID++;
			}
			methodsIndex.add(methodID);
			
			genHmtlPages(methodID, cfg.getExecutable(), cfg, environment, outputFolder);
			
			cfg.writeDotGraph(outputFolder+"/html/dotFolder/", methodID);
			cfg.writeSvgGraph(outputFolder+"/html/dotFolder/"+methodID+".dot", outputFolder+"/html/dotFolder/"+methodID+".svg");
		}
		
		genHtmlIndex(methodsIndex, outputFolder);
	}
}
