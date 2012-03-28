package jardownloads;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class CreateVersions {
	private String group;
	private String artifact;
	final private SimpleDateFormat SDF = new SimpleDateFormat("E MMM dd yyyy"); 
	private ArrayList<String> versions; 



	public CreateVersions(String group,String artifact){
		this.group = group; 
		this.artifact = artifact; 

		try {
			this.versions = retrieveVersions(); 
			CreateFolders(this.versions);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public  ArrayList<String> retrieveVersions() throws ParserConfigurationException, MalformedURLException, SAXException, IOException{
		String url = "http://repository.sonatype.org/service/local/lucene/search?g="+this.group+"&a="+this.artifact;
		System.out.println(url);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new URL(url).openStream());
		NodeList tm =doc.getElementsByTagName("version"); 
		ArrayList<String> versions = new ArrayList<String>(); 
		for (int i = 0;i< tm.getLength();i++){
			String val = tm.item(i).getTextContent();
			if(!val.contains("-SNAPSHOT"))
				versions.add(val);
		}
		return versions;
	}
	public  void CreateFolders(ArrayList<String> versions){
		String path = "Project/" + this.group;
		new File(path).mkdir(); 
		path += "/"+this.artifact; 
		new File(path).mkdir(); 
		
		
		

		try{
			// Create file 
			FileWriter fstream = new FileWriter(path+"/versions.txt");
			BufferedWriter out =new BufferedWriter(fstream);
			boolean te = true; 
			for(int i =0;i<versions.size();i++){
				String vers = versions.get(i);
				File folder = new File(getFolderPath(vers));
				te = te && folder.mkdir(); 
				System.out.println("Version: "+ vers);
				includePOMS(vers);
				JarDownload(vers);
				DependenciesXML(vers);
				String date = getDate(vers);
				out.write(date+","+vers+"\r\n");
			}
			out.close();
		}

		catch(IOException e){

		}	
	}
		
	
	public void includePOMS(String version){	
		System.out.println("\t Downloading POM");
		String url = "https://repository.sonatype.org/service/local/artifact/maven/redirect";
		url += "?r=central-proxy";//Repo ID
		url += "&g=" + this.group; //Group ID
		url += "&a="+ this.artifact; //Artifact ID 
		url += "&v=" + version; 
		url += "&e="+"pom";	
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(new URL(url).openStream());
			Source source = new DOMSource(doc);
			File file = new File(getFolderPath(version)+"POM.xml");
			Result result = new StreamResult(file);
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public void JarDownload(String version){
		System.out.println("\t Downloading JAR");

		String url = "https://repository.sonatype.org/service/local/artifact/maven/redirect";
		url += "?r=central-proxy";//Repo ID
		url += "&g=" + this.group; //Group ID
		url += "&a="+ this.artifact; //Artifact ID 
		url += "&v=" + version; 
		url += "&e="+"jar";	
		DownloadFile(url,getJarPath(version));


	}


	public int DependenciesXML(String version){
		System.out.println("\t Extracting Dependencies");

		String folder = getFolderPath(version);
		String cmd = "Librairies/DependencyFinder/bin/DependencyExtractor.bat";
		cmd += " -xml -out "+folder+"dependencies.xml " +folder+"jar.jar"; 
		int exit = 0; 
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			exit = p.waitFor(); 

		} catch (Exception e) {
			e.printStackTrace();
		}
		return exit; 





	}




	public void DownloadFile(String source, String destination){

		try {
			URL url = new  URL(source);
			BufferedInputStream in = new BufferedInputStream(url.openStream());
			FileOutputStream fos = new FileOutputStream(destination);
			BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
			byte data[] = new byte[1024];
			int x=0; 
			while((x = in.read(data,0,1024))>=0)
			{
				bout.write(data,0,x);
			}
			bout.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getDate(String version){
		System.out.println("\t Getting Date");

		long time = 0; 

		try {
			JarFile jar = new JarFile(getJarPath(version));
			Enumeration<?> enume = jar.entries();
			while (enume.hasMoreElements()) {
				JarEntry file = (JarEntry) enume.nextElement();
				if(file.getName().contains("META-INF/MANIFEST.MF")){
					time = file.getTime(); 					
				}

			}	
		}catch (IOException e) {
			e.printStackTrace();
		}

		BufferedWriter out =null;

		try{
			// Create file 
			FileWriter fstream = new FileWriter(getFolderPath(version)+"date.txt");
			out = new BufferedWriter(fstream);

			out.write(Long.toString(time)+"\r\n");
			out.write(SDF.format(new Date(time)));
		}

		catch(Exception e){
			//Close the output stream

		}
		finally {
			try {
				out.close();
			} catch (IOException e) {}
		}
		return Long.toString(time)+", "+ SDF.format(new Date(time)); 
	}

	public String getJarPath(String version){
		return "Project/" + this.group+"/"+this.artifact+"/"+version+"/jar.jar";
	}
	public String getFolderPath(String version){
		return "Project/" + this.group+"/"+this.artifact+"/"+version+"/";
	}

	public ArrayList<String> getVersions(){
		return this.versions;

	}
}