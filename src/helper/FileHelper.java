package helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class FileHelper {
	/**
	 * Write 'content' in 'dir'/'file' 
	 */
	public static void writeFile(String dir, String file, String content){
		try{
			File newDir = new File(dir);
			newDir.mkdirs();
			
			FileWriter fw = new FileWriter(dir+"/"+file, false);
			BufferedWriter output = new BufferedWriter(fw);
			output.write(content);
			output.flush();
			output.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
