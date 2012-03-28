package graphanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Network {

	private HashMap<String,Integer> classes; 
	private HashMap<Integer,String> invClasses; 
	private HashMap<String,String> dependencies; 
	private String name; 
	public Network(String filename, String name){
		this.classes = new HashMap<String, Integer>(); 
		this.dependencies = new HashMap<String,String>();		
		this.invClasses = new HashMap<Integer,String>(); 
		this.name = name; 
		File aFile = new File(filename);
		FileReader fr;
		BufferedReader input = null;   
		try {
			fr = new FileReader(aFile);
			input =  new BufferedReader(fr);
			String line = null; //not declared within while loop
			int state = 0; 
			while (( line = input.readLine()) != null){
				if(line.startsWith("*Vertices"))state = 1; 
				else if(line.startsWith("*Arcs"))state = 2; 
				else if(line.startsWith("*Edges"))state = 3; 
				else include(line,state);
			}
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
		finally {

			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void include(String line, int state) {
		String[] li = line.split(" ");
		switch(state){
		case 1: 
			int val = Integer.parseInt(li[0]);
			this.classes.put(li[1],val);
			this.invClasses.put(val, li[1]);
			break; 
		case 2:
			String[] s = line.split(" ");
			String va1 = this.invClasses.get(Integer.parseInt(s[0]));
			String va2 = this.invClasses.get(Integer.parseInt(s[1]));
			this.dependencies.put(va1+" - "+va2 , line);
			break; 
		}
	}

	public HashMap<String, Integer> getClasses() {
		return classes;
	}

	public void setClasses(HashMap<String, Integer> classes) {
		this.classes = classes;
	}

	public HashMap<String, String> getDependencies() {
		return dependencies;
	}
	public int[] depCompare(Network n2) {
		System.out.println("Dependencies diff between: " + this.getName()+ " and " + n2.getName());

		int removed = 0;
		int added = 0; 
		for (String n1cl : this.getDependencies().keySet()) {
			if (n2.getDependencies().get(n1cl) == null){
				System.out.println("R: " + n1cl);
				removed ++;
			}
		}

		//System.out.println("Added classes");

		for (String n2cl : n2.getDependencies().keySet()) {
			if (this.getDependencies().get(n2cl) == null){
				System.out.println("A: " + n2cl);
				added++;
			}

		}
		return new int[]{removed,added};
	}


	public String getName(){
		return this.name; 
	}
	public int[] classCompare( Network n2) {
		System.out.println("Classes diff between: " + this.getName()+ " and " + n2.getName());
		// And Finally print the differences
		//System.out.println("Removed classes");
		int removed = 0;
		int added =0; 
		for (String n1cl : this.getClasses().keySet()) {
			if (n2.getClasses().get(n1cl) == null){
				System.out.println("R: " + n1cl);
				removed ++;
			}

		}

		//System.out.println("Added classes");

		for (String n2cl : n2.getClasses().keySet()) {
			if (this.getClasses().get(n2cl) == null){
				System.out.println("A: " + n2cl);
				added ++;
			}
		}

		return new int[]{removed, added}; 
	}


}
