package network;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class AnalyzeDependencies {
	String filename; 
	HashMap<String,MyClasse> myclasses; 
	String artifact; 
	String group; 
String version; 

	public AnalyzeDependencies(String group,String artifact,String version, String pack) {
		this.myclasses = new HashMap<String,MyClasse>();
		this.artifact = artifact; 
		this.group = group; 
		this.version = version; 
		this.filename = "Project/"+group+"/"+artifact+"/"+version+"/dependencies.xml";
		
		
		try{
			File fXmlFile = new File(this.filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			NodeList packagesList = doc.getElementsByTagName("package");
			for(int i =0; i<packagesList.getLength();i++){
				Element EPackage = ((Element) packagesList.item(i));
				String PackageName = EPackage.getElementsByTagName("name").item(0).getTextContent();
				if(PackageName.contains(pack)){
					NodeList classesList = EPackage.getElementsByTagName("class");
					for(int j=0;j<classesList.getLength();j++){
						Element EClass = ((Element) classesList.item(j));
						String ClasseName = EClass.getElementsByTagName("name").item(0).getTextContent();
						ClasseName = ClasseName.split("\\$")[0];
						if(myclasses.containsKey(ClasseName))myclasses.get(ClasseName).addSubClass(EClass);
						else myclasses.put(ClasseName, new MyClasse(ClasseName,PackageName,pack,EClass));

					}
				}
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
		getDependencies();
		summary();
		createPajekFile();
	}

	public void getDependencies(){
		for( String key: this.myclasses.keySet() )
			this.myclasses.get(key).getDependencies();
	}


	public void summary(){
		for( String key: this.myclasses.keySet() ){
			System.out.println(this.myclasses.get(key));		
		}
	}


	public void createPajekFile(){
		// First we get the vertices 
		HashMap<String,Integer> vertices = new HashMap<String,Integer>(); 
		HashMap<String,Integer> edges = new HashMap<String,Integer>();
		int count = 1; 			// Will give an index to all the classes 
		
		for( String key: this.myclasses.keySet() ){ // For all the in classes
			if(!vertices.containsKey(key))vertices.put(key, count++);	//if its not already in then add
			HashMap<String,Integer> deps = this.myclasses.get(key).getSumFeatureClasses(); // get all dependencies 
			for( String outp: deps.keySet() ){ // For each dependency
				if(!vertices.containsKey(outp))vertices.put(outp, count++);	 // Add to vertices if needed 
				String link = vertices.get(outp) +" " + vertices.get(key); 
				Integer countEdg = edges.get(link);          
				edges.put(link, (countEdg==null) ? deps.get(outp) : count+deps.get(outp));		
			}
		}


		
		try{
			String netFile ="Project/"+group+"/"+artifact+"/"+version+"/network.net";

			FileWriter fstream = new FileWriter(netFile);
			BufferedWriter out = new BufferedWriter(fstream);
			//Ordering Vertices
			out.write("*Vertices "+vertices.size()+"\r\n");
			String[] vertord = new String[vertices.size()+1];
			vertord[0] = " "; 
			for (String vertice: vertices.keySet()){
				vertord[vertices.get(vertice)] = vertices.get(vertice)+" "+vertice; 
			}
			for (int i =1; i<vertord.length;i++)
				out.write(vertord[i]+"\r\n");

			out.write("*Arcs \r\n");
			for (String edge: edges.keySet()){
				out.write(edge +"\r\n"); // Not Weighted Graph
				
				//out.write(edge +" "+ edges.get(edge)+"\r\n"); // Weighted graph
			}


			out.close();
		}catch (Exception e){//Catch exception if any
			e.printStackTrace();
		}
	}









}


