package eu.sshoc.TavernaDv_tool;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SshocAPI {
	private Process process = null;
	private InputStream inputStream = null;
	private Hashtable<String, ArrayList<String[]>> allDataSetList;
//	private static Logger log = null;
	
//	public SshocAPI (Logger ref) {
//		log = ref;
//	}
//	
//	public SshocAPI () {
//		log = Logger.getAnonymousLogger();
//	}
	
	//FIXME this data must be loaded from config file (if the server change)
	private static String HOSTNAMESSHOC;//"http://146.48.85.197:8080/Dataverse_tool-0.0.1-SNAPSHOT";//"http://localhost:8080";
	public SshocAPI() {
		Properties properties = new Properties();
		try {
			properties.load(SshocAPI.class.getClassLoader().getResourceAsStream("./config.properties"));
		}catch (IOException e) {
			throw new RuntimeException("\nINSERT - Errore nel caricamento della configurazione", e);
			//log.severe("\nINSERT - Errore nel caricamento della configurazione");
		}
		if("".equalsIgnoreCase(properties.getProperty("door"))) {
			HOSTNAMESSHOC = properties.getProperty("protocol")+properties.getProperty("hostname")+properties.getProperty("serviceName");
		}else {
			HOSTNAMESSHOC = properties.getProperty("protocol")+properties.getProperty("hostname")+":"+properties.getProperty("door")+properties.getProperty("serviceName");
		}
		System.out.println("SshocAPI: "+"hostName: "+HOSTNAMESSHOC);
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}

	public BufferedReader getBufferedReader() {
		return new BufferedReader(new InputStreamReader(inputStream));
	}
	
	public Hashtable<String, ArrayList<String[]>> getAllDataSetList(){
		return this.allDataSetList;
	}
	

	/**
	 * must be invoked at the end of the operation on inputStream
	 */
	public void terminateProcess() {
		if (process != null) process.destroy();
	}
	
	public int listDv(String API_TOKEN) {
		String service = HOSTNAMESSHOC+"/sshoc/dvtool/listdv";
		String command = "curl -X GET "+service+"?token="+API_TOKEN+" -H accept:text/xml";
		ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
		//processBuilder.directory(new File("/home/"));
		process = null;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try { 
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		inputStream = process.getInputStream();
		int exitCode = process.exitValue();
		System.out.println("SshocAPI: "+exitCode);
		// process.destroy();
		return (exitCode==0)?0:-1;
	}//end method
	
	public int listDataset(String API_TOKEN, String dvName) {
		String service = HOSTNAMESSHOC+"/sshoc/dvtool/listdatasets";
		String command = "curl -X GET "+service+"?dvname="+dvName+"&token="+API_TOKEN+" -H accept:text/xml";
		ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
		processBuilder.directory(new File("/home/"));
		process = null;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try { 
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		inputStream = process.getInputStream();
		int exitCode = process.exitValue();
		// process.destroy();
		return (exitCode==0)?0:-1;
	}//end method

	public int listAllDataset(String API_TOKEN) {
		this.listDv(API_TOKEN);
		String resp = this.getOutput();
		System.out.println("SshocAPI: "+"ALL DATAVERSE:\n"+resp);
		ArrayList<String> dvList = parsingXmlDvlist(resp);
		//try {this.inputStream.reset();} catch (IOException e) {e.printStackTrace();}
		allDataSetList = new Hashtable<String, ArrayList<String[]>>();
		for(String dvrs: dvList) {
			this.listDataset(API_TOKEN, dvrs);
			resp = this.getOutput();
			System.out.println("SshocAPI: "+dvrs+" HAVE DATASETS:\n"+resp);
			allDataSetList.put(dvrs, parsingXmlDSetlist(resp));
			//try {this.inputStream.reset();} catch (IOException e) {e.printStackTrace();}
		}
		int exitCode = process.exitValue();
		// process.destroy();
		return (exitCode==0)?0:-1;
	}//end method
	
	public int createDataverse(String API_TOKEN, String filepath, String parentName) {
		String service = HOSTNAMESSHOC+"/sshoc/dvtool/createdataverse";
		String command = "curl -X POST "+service+"?file="+filepath+"&parentfold="+parentName+"&token="+API_TOKEN+" -H  accept:text/plain";
		ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
//		processBuilder.directory(new File("/home/"));//perhaps to add in definitive version where parameters not are constant
		process = null;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try { 
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		inputStream = process.getInputStream();
		int exitCode = process.exitValue();
		// process.destroy();
		return (exitCode==0)?0:-1;
	}//end method

	public int createDataset(String API_TOKEN, String filepath, String parentName) {
		String service = HOSTNAMESSHOC+"/sshoc/dvtool/createdataset";
		String command = "curl -X POST "+service+"?file="+filepath+"&parentfold="+parentName+"&token="+API_TOKEN+" -H  accept:text/plain";
		ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
//		processBuilder.directory(new File("/home/"));perhaps to add in definitive version where parameters not are constant
		process = null;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try { 
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		inputStream = process.getInputStream();
		int exitCode = process.exitValue();
		// process.destroy();
		return (exitCode==0)?0:-1;
	}//end method

	/**
	 * Ask to Sshoc Rest Service to save a file on dataverse
	 * @param API_TOKEN 
	 * @param file
	 * @param dsname
	 * @return an exit code. 0 if there was a correct termination or -1 if there was an error.
	 */
	public int saveData(String API_TOKEN, File file, String dsname) {
		int exitCode = 0;
		String loadService = HOSTNAMESSHOC+"/uploadfile";
		String service = HOSTNAMESSHOC+"/sshoc/dvtool/savedata";
		String loadFileCommand = "curl -X POST "+loadService+" -H accept:*/* -H Content-Type:multipart/form-data -F file=@"+file.getAbsolutePath();
		String command = "curl -X POST "+service+"?dsname="+dsname+"&file="+file.getName()+"&token="+API_TOKEN+" -H accept:text/plain";
//		processBuilder = new ProcessBuilder("curl", "-X", "POST", loadService, "-H", "accept:*/*", "-H", "Content-Type:multipart/form-data", "-F", "file=@"+file.getAbsolutePath());
		ProcessBuilder processBuilder = new ProcessBuilder(loadFileCommand.split(" "));
		process = null;
		try {
			System.out.println("SshocAPI: "+"LoadCommand: "+loadFileCommand);
//			System.out.println("SshocAPI: "+"absolutePath: "+file.getAbsolutePath());
			process = processBuilder.start();
		} catch (IOException e) {
			System.out.println("SshocAPI: "+"errore nel process builder for loadFileCommand");
			e.printStackTrace();
		}
		System.out.println("SshocAPI: "+"executing loading");
		try { 
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		exitCode = process.exitValue();
		System.out.println("SshocAPI: "+"exitCode1 = "+exitCode);
//		process.destroy();
//		System.out.println("SshocAPI: "+"process desroyed");
//		processBuilder.directory(new File("/home/"));//perhaps to add in definitive version where parameters not are constant
		if(exitCode==0) {//3 is a message to license expired peraps we may ignore adding || exitCode==3 in condition
//			try {
//				this.wait(1000);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
			//TODO try to commutate in http request!
			processBuilder = new ProcessBuilder(command.split(" "));
			try {
				System.out.println("SshocAPI: "+"Command: "+command);
				System.out.println("SshocAPI: "+"fileName: "+file.getName());
				process = processBuilder.start();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			System.out.println("SshocAPI: "+"executing to dataverse");
			try { 
				process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			inputStream = process.getInputStream();
			try {
				exitCode = process.exitValue();
			} catch (java.lang.IllegalThreadStateException e) {
				e.printStackTrace();
			}
		}
		System.out.println("SshocAPI: "+"exitCode = "+exitCode);
		// process.destroy();
		return (exitCode==0)?0:-1;//3 is a message to license expired peraps we may ignore adding || exitCode==3 in condition
	}//end method

	
	/**
	 * Parsing dataverses xml list Dataverse's response
	 * @param response xml from Dataverse
	 * @return
	 */
	private static ArrayList<String> parsingXmlDvlist(String response) {
		//FOR XML PARSING
		ArrayList<String> aryOfDv = new ArrayList<String>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		ByteArrayInputStream input = null;
		try {
			input = new ByteArrayInputStream(response.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = builder.parse(input);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		NodeList nList = doc.getElementsByTagName("collection");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	               Element eElement = (Element) nNode;
	               aryOfDv.add(eElement.getElementsByTagName("atom:title").item(0).getTextContent());
			}
		}
		return aryOfDv;
	}//endmethod
	/**
	 * Parsing datasets xml list Dataverse's response
	 * @param response xml from Dataverse
	 * @return A List of two string: first string is the Dataset "id", second string is the Dataset name.
	 */
	private static ArrayList<String[]> parsingXmlDSetlist(String response) {
		//FOR XML PARSING
		ArrayList<String[]> aryOfDs = new ArrayList<String[]>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		ByteArrayInputStream input = null;
		try {
			input = new ByteArrayInputStream(response.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = builder.parse(input);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		NodeList nList = doc.getElementsByTagName("entry");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	               Element eElement = (Element) nNode;
	               String[] result = new String[2];
	               result[0] = eElement.getElementsByTagName("id").item(0).getTextContent();
	               result[1] = eElement.getElementsByTagName("title").item(0).getTextContent();
	               aryOfDs.add(result);
			}
		}
		return aryOfDs;
	}//endmethod
	/**
	 * @param rd
	 * @return
	 * @throws IOException
	 */
	private String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}//end method
	/**
	 * @param dataverseApi 
	 * @return the message from Dataverse server
	 */
	private String getOutput(){
		BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
		String testo = null;
		try {
			testo = this.readAll(rd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(testo==null || testo.equals("")) testo = "no valid response";
		return testo;
	}//end method

}//end Class
