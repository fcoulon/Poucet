package cfg;

import helper.FileHelper;

public class SVGHelper {
	
	final static String FILTER_FILE_NAME = "filters.svg";
	final static String CSS_FILE_NAME = "style.css";
	
	public static void generateFilterFile(String outputDir){
		StringBuffer res = new StringBuffer();
		
		res.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		res.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
		res.append("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
		
		res.append("<defs>");
		res.append("<filter id=\"shadow\">");
		res.append("<feGaussianBlur in=\"SourceAlpha\" stdDeviation=\"1.5\" result=\"flou\"/>"); 
		res.append("<feOffset in=\"flou\" dx=\"3\" dy=\"3\" result=\"flouDécalé\"/>");
		res.append("<feMerge>");
		res.append("<feMergeNode in=\"flouDécalé\"/>"); 
		res.append("<feMergeNode in=\"SourceGraphic\"/>"); 
		res.append("</feMerge>");
		res.append("</filter>");

		res.append("<linearGradient id=\"degrade\" x1=\"0\" y1=\"0\" x2=\"100%\" y2=\"100%\">");
		res.append("<stop offset=\"0%\" id=\"stop1\" style=\"stop-color:cornflowerblue;\"/>");
		res.append("<stop offset=\"40%\" id=\"stop2\" style=\"stop-color:chartreuse;\"/>");
		res.append("<stop offset=\"100%\" id=\"stop3\" style=\"stop-color:cornflowerblue;\"/>");
		res.append("</linearGradient>");
		res.append("</defs>");
		
		res.append("</svg>");
		
		FileHelper.writeFile(outputDir, FILTER_FILE_NAME, res.toString());
	}

	public static void genererateCssFile(String outputDir){
		StringBuffer res = new StringBuffer();
		
		res.append(".node > polygon{");
		res.append("    filter : url(filters.svg#shadow);");
		res.append("   fill : url(filters.svg#degrade);");
		res.append("}");

		res.append(".node > ellipse{");
		res.append("filter : url(filters.svg#shadow);");
		res.append("fill : url(filters.svg#degrade);");
		res.append("}");

		res.append(".edge{");
		res.append("filter : url(filters.svg#shadow);");
		res.append("}");
		
		FileHelper.writeFile(outputDir, CSS_FILE_NAME, res.toString());
	}
}
