import graphanalysis.PajekDMS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import jardownloads.CreateVersions;
import network.AnalyzeDependencies;




public class Main {
	public static void main(String[] args) {
		String artifact ="abdera-core";
		String group = "org.apache.abdera"; 
		String pack = "abdera";
		//String artifact ="glassfish";
		//String group = "org.glassfish.core"; 
		//String pack = "glassfish";
		CreateVersions cv = new CreateVersions(group, artifact);
		ArrayList<String> versions = cv.getVersions();
		for(int i=0;i<versions.size();i++){
			System.out.println("###### VERSIONS " + versions.get(i) +" ###########");
			new AnalyzeDependencies(group,artifact,versions.get(i),pack);
			new PajekDMS(group,artifact,versions.get(i));		
		}

	}
}