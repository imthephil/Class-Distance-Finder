package graphanalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class Project {	

	final private SimpleDateFormat SDF = new SimpleDateFormat("dd MMM yyyy"); 

	private String group;
	private String artifact; 
	private ArrayList<String> orderedVersions;

	private ArrayList<String> orderedDates; 


	public Project(String group, String artifact){
		this.group = group; 
		this.artifact = artifact; 
		this.orderedVersions = new ArrayList<String>(); 
		this.orderedDates = new ArrayList<String>(); 

		File f = new File("Project/"+this.group+"/"+this.artifact); 
		File[] fs = f.listFiles(); 
		System.out.println(fs.length);
		HashMap<Date,String> versions = new HashMap<Date, String>(); 
		for(int i=0;i<fs.length;i++){
			if(fs[i].isDirectory()){
				versions.put(getDate(fs[i]),fs[i].getName());
			}
		}
		ArrayList<Date> sortedKeys=new ArrayList<Date>(versions.keySet());
		Collections.sort(sortedKeys);


		for(int i=0;i<sortedKeys.size();i++){
			String version = versions.get(sortedKeys.get(i));
			//String date = SDF.format(sortedKeys.get(i));
			this.orderedVersions.add(version);
			this.orderedDates.add(SDF.format(sortedKeys.get(i)));
		}
	}

	private Date getDate(File file) {

		File aFile = new File(file.getPath()+"/date.txt");
		if(!aFile.exists())return null; 
		FileReader fr;
		BufferedReader input = null;   
		try {
			fr = new FileReader(aFile);
			input =  new BufferedReader(fr);

			return new Date(Long.parseLong(input.readLine())) ;


		}
		catch(Exception e){
			return null; 
		}
	}
	
	
	
	public void compareVersions(){
		ArrayList<String> summary= new ArrayList<String>();
		summary.add("v1,date(v1),v2,date(v2),classes removed, classes added, dependencies removed, dependencies added");
		String version = this.orderedVersions.get(0);
		String date = this.orderedDates.get(0);
		Network n1 = new Network("Project/"+this.group+"/"+this.artifact+"/"+version+"/network.net",version+"("+date+")"); 
		for (int i = 1;i<this.orderedVersions.size();i++){
			version = this.orderedVersions.get(i);
			date = this.orderedDates.get(i);
			Network n2 = new Network("Project/"+this.group+"/"+this.artifact+"/"+version+"/network.net",version+"("+date+")"); 
			int[] classes = n1.classCompare(n2);
			int[] dependencies= n1.depCompare(n2);
			String n1Name = n1.getName().replace("(",",").replace(")", "");
			String n2Name = n2.getName().replace("(",",").replace(")", "");
			
			summary.add(n1Name+","+n2Name+","+classes[0]+","+classes[1]+","+dependencies[0]+","+dependencies[1]);
			writeInFile(summary);
			n1 = n2; 		
		}
	}

	private void writeInFile(ArrayList<String> summary) {
		String netFile ="Project/"+group+"/"+artifact+"/evolution.csv";

		FileWriter fstream;
		BufferedWriter out =null;;
		try {
			fstream = new FileWriter(netFile);

			out = new BufferedWriter(fstream);
			for(int i=0;i<summary.size();i++){
				out.write(summary.get(i)+"\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 		
	}

}
