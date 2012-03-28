package graphanalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;


import org.jgrapht.Graph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class PajekDMS {


	private String[] vertices ;


	
	public PajekDMS(String group, String artifact, String version) {
		

		String inFilename = "Project/"+group+"/"+artifact+"/"+version+"/network.net";
		String outFilenameDir = "Project/"+group+"/"+artifact+"/"+version+"/dmsDir.csv";
		String outFilenameUnDir = "Project/"+group+"/"+artifact+"/"+version+"/dmsUndir.csv";
		
		
		DefaultDirectedGraph<String, DefaultEdge> gDir =
			new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		
		SimpleGraph<String, DefaultEdge> gUnDir =
			new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		
		Graph<String,DefaultEdge> gDirFin = createGraph(inFilename,gDir);
		Graph<String,DefaultEdge> gUnDirFin = createGraph(inFilename,gUnDir);

		
		double[][] matDir = getMatrix(gDirFin);
		double[][] matUnDir = getMatrix(gUnDirFin);
		
		printMatrix(matDir,outFilenameDir);		
		printMatrix(matUnDir,outFilenameUnDir);



	}		



	private double[][] getMatrix(Graph<String, DefaultEdge> g) {
		FloydWarshallShortestPaths<String,DefaultEdge> fwsp = new FloydWarshallShortestPaths<String, DefaultEdge>(g);

		int nbvert = this.vertices.length; 
		double[][] mat = new double[nbvert][nbvert];
		for(int i=1;i<nbvert;i++){
			for(int j=1;j<nbvert;j++){
				mat[i][j] = fwsp.shortestDistance(this.vertices[i],this.vertices[j]);
			}
		}

		return mat;
	}


	private void printMatrix(double[][] mat,String filename) {

		try {
			File file = new File(filename);
			Writer output = new BufferedWriter(new FileWriter(file));
			String s="";
			output.write(mat.length+"\r\n");
			for(int i=1;i<mat[0].length;i++){
				s+=","+this.vertices[i];
				
			}
			output.write(s);
			
			for (int i=1;i<mat.length;i++){
				s = this.vertices[i];
				for(int j=1;j<mat[0].length;j++){
					s+=","+ mat[i][j];
				}
				output.write("\r\n"+s);
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private Graph<String,DefaultEdge> createGraph(String filename, Graph<String,DefaultEdge>g){ 
		
		int sit =0; 
		try{
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				if(strLine.startsWith("*Vertices")){
					this.vertices = new String[Integer.parseInt(strLine.split(" ")[1])+1];
					sit =1; 
				}
				else if (strLine.startsWith("*Arcs"))sit =2; 
				else if(sit ==1){
					String[] stp = strLine.split(" ");
					this.vertices[Integer.parseInt(stp[0])] = stp[1];
					g.addVertex(stp[1]);
				}
				else if(sit ==2){
					String[] stp = strLine.split(" ");
					g.addEdge(this.vertices[Integer.parseInt(stp[0])],this.vertices[Integer.parseInt(stp[1])]);
				}
			}
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			e.printStackTrace();
		}
		return g;
	}
}
