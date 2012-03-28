package network;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class MyClasse {
	private ArrayList<Element> subclasses; 
	private HashMap<String,Integer> features; 
	private HashMap<String,Integer> classes; 

	private String name; 
	private String packageName; 
	private String projectName; 

	public MyClasse(String name, String packageName,String projectName, Element news){
		this.subclasses = new ArrayList<Element>(); 
		this.features = new HashMap<String,Integer>(); 
		this.classes = new HashMap<String,Integer>(); 
		this.packageName = packageName;
		this.name = name; 
		this.projectName = projectName; 

		this.subclasses.add(news);
	}

	public void addSubClass(Element news){
		subclasses.add(news);
	}

	public void getDependencies(){

		for(int i = 0;i<this.subclasses.size();i++){
			NodeList outs = this.subclasses.get(i).getElementsByTagName("outbound"); 
			for (int j=0;j<outs.getLength();j++){
				String depName = outs.item(j).getTextContent();
				
				if(depName.contains(this.projectName)){
					String type = outs.item(j).getAttributes().getNamedItem("type").getTextContent();
					if(type.equals("feature")){
						depName = analyzeFeature(depName);
						Integer count = this.features.get(depName);    
						if(!this.name.equals(depName))
							this.features.put(depName, (count==null) ? 1 : count+1);
					}
					else if(type.equals("class")){
						depName = analyzeClass(depName);
						Integer count = this.classes.get(depName);          
						if(!this.name.equals(depName))
							this.classes.put(depName, (count==null) ? 1 : count+1);
					}
				}
			}
		}
	}



	public HashMap<String,Integer> getSumFeatureClasses(){
		HashMap<String,Integer> sum = new HashMap<String,Integer>(); 
		for( String key: this.features.keySet() ){
			Integer count = sum.get(key);          
			sum.put(key, (count==null) ? this.features.get(key) : count+this.features.get(key));		
		}
		for( String key: this.classes.keySet() ){
			Integer count = sum.get(key);          
			sum.put(key, (count==null) ? this.classes.get(key) : count+this.classes.get(key));		
		}
		return sum; 	
	}
	private String analyzeClass(String depName) {
		String s = depName; 
		if(s.contains("$")){
			s = s.split("\\$")[0];
		}
		return s; 
	}

	private String analyzeFeature(String depName) {		
		String s = depName; 
		if(s.contains("(")){
			s = s.split("\\(")[0]; 

		}
		s = s.substring(0, s.lastIndexOf("."));

		if(s.contains("$")){
			s = s.split("\\$")[0];
		}
		return s; 
	}

	public String toString(){
		String s = this.name +"("+this.packageName+")"+"\r\n";
		s+="\t Classes"+"\r\n";
		for( String key: this.classes.keySet() ){
			s +="\t\t"+key +"-"+ this.classes.get(key)+"\r\n";		
		}
		s+="\t Features"+"\r\n";
		for( String key: this.features.keySet() ){
			s +="\t\t"+key +" - "+ this.features.get(key)+"\r\n";		
		}
		return s; 
	}
}
