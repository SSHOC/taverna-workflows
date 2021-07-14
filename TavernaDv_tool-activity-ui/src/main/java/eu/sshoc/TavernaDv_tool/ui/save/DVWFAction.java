/*******************************************************************************
 * Copyright 2018 VRE4EIC Consortium
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package eu.sshoc.TavernaDv_tool.ui.save;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.google.gson.JsonObject;

import eu.sshoc.TavernaDv_tool.SshocAPI;
import eu.sshoc.TavernaDv_tool.util.Common;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;
import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.t2.annotation.AnnotationAssertion;

import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.annotationbeans.Author;
import net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
import net.sf.taverna.t2.workbench.file.*;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.impl.FileDataflowInfo;
import net.sf.taverna.t2.workbench.file.impl.SafeFileOutputStream;
import net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener;


public class DVWFAction extends AbstractDataflowPersistenceHandler implements DataflowPersistenceHandler {
	private static final DVFlowType VRE4EIC_FLOW_FILE_TYPE = new DVFlowType();
//	private static final String USERID = "dataverseAdmin";
//	private static final String PSWD = "d4r1c0rd4r3";
//	private static final String DVRSDirectoryFILES = "/home/filippo/workspaceProva/RemoteDataverseFiles";
//	private static final String IniTOfDOI = "doi:10.5072/FK2/";
//	private static final int NumOfCharForDOI = 87;
	
	private static Logger logger = Logger.getLogger(DVWFAction.class);
    
	public DVWFAction() {
		super();
		Properties properties = new Properties();
		try {
			properties.load(DVWFAction.class.getClassLoader().getResourceAsStream("./config.properties"));
		}catch (IOException e) {
			System.out.println("\nINSERT - Errore nel caricamento della configurazione");
			//log.severe("\nINSERT - Errore nel caricamento della configurazione");
		}
		try {
			if("".equalsIgnoreCase(properties.getProperty("dvdoor"))) {
				Common.setEvreUrl(new URL(properties.getProperty("dvprotocol")+properties.getProperty("dvhostname")));
			}else {
				Common.setEvreUrl(new URL(properties.getProperty("dvprotocol")+properties.getProperty("dvhostname")+":"+properties.getProperty("dvdoor")));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		Common.setToken(properties.getProperty("apiToken"));
//		Common.setUserId(USERID);
//		Common.setPassword(PSWD);
		Common.setDsName(properties.getProperty("datasetId"));
		System.out.println("DVWFAction: "+Common.getDsName());
		/*
		//FIXME se possibile spostare il codice ad ogni apertura di un workflow (non soltanto la prima)
		//TODO provare a creare sottocartella contenente tutti i dataset in dataverse
		//Testing existence
		if(!Files.isDirectory(Paths.get(DVRSDirectoryFILES))) {
			try {//creating dir
				Files.createDirectory(Paths.get(DVRSDirectoryFILES));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//fill dir
		SshocAPI api = new SshocAPI();
		System.out.println("exec");		
		int result = api.listAllDataset(TOKEN);
		System.out.println("result Of listAllDataset:"+result);
		Hashtable<String, ArrayList<String[]>> alldatasetList = api.getAllDataSetList();
		System.out.println("alldatasetList size:"+alldatasetList.size());		
		Iterator<Entry<String, ArrayList<String[]>>> setOfAlldatasetList = alldatasetList.entrySet().iterator();
		while(setOfAlldatasetList.hasNext()){
			Entry<String, ArrayList<String[]>> entry = setOfAlldatasetList.next();
			if(!Files.isDirectory(Paths.get(DVRSDirectoryFILES+"/"+entry.getKey()))) {
				try {//create a directory foreach dataverse
					Files.createDirectory(Paths.get(DVRSDirectoryFILES+"/"+entry.getKey()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ArrayList<String[]> listOfDataset = entry.getValue();
			for(String[] dataset: listOfDataset){//create a sub-directory foreach dataset
				if(!Files.isDirectory(Paths.get(DVRSDirectoryFILES+"/"+entry.getKey()+"/"+dataset[1]+dataset[0].substring(NumOfCharForDOI)))) {
					try {
						Files.createDirectory(Paths.get(DVRSDirectoryFILES+"/"+entry.getKey()+"/"+dataset[1]+dataset[0].substring(NumOfCharForDOI)));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				//TODO estrarre i file dai rispettivi dataset e inserirli nelle directory create
			}
		}
		*/
	}

	public DVWFAction getAction() {
		return this;
	}


	@Override
	public List<FileType> getSaveFileTypes() {
		net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener test;
		
		return Arrays.<FileType> asList(VRE4EIC_FLOW_FILE_TYPE);
	}

	@Override
	public List<FileType> getOpenFileTypes() {
		return Arrays.<FileType> asList(VRE4EIC_FLOW_FILE_TYPE);
	}
	
	
	@Override
	public List<Class<?>> getOpenSourceTypes() {
		return Arrays.<Class<?>> asList(InputStream.class, URL.class,
				File.class);
		
	}

	@Override
	public List<Class<?>> getSaveDestinationTypes() {
		return Arrays.<Class<?>> asList(File.class, OutputStream.class);
	}

	@Override
	public DataflowInfo openDataflow(FileType fileType, Object source) throws OpenException {

		if (!getOpenFileTypes().contains(fileType)) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		InputStream inputStream;
		Date lastModified = null;
		Object canonicalSource = source;
		if (source instanceof InputStream) {
			inputStream = (InputStream) source;
		} else if (source instanceof File) {
			try {
				inputStream = new FileInputStream((File) source);
			} catch (FileNotFoundException e) {
				throw new OpenException("Could not open file " + source + ":\n" + e.getLocalizedMessage(), e);
			}
		} else if (source instanceof URL) {
			URL url = ((URL) source);
			try {
				URLConnection connection = url.openConnection();
				connection.setRequestProperty("Accept", "text/xml");
				inputStream = connection.getInputStream();
				if (connection.getLastModified() != 0) {
					lastModified = new Date(connection.getLastModified());
				}
			} catch (IOException e) {
				throw new OpenException("Could not open connection to URL "
						+ source+ ":\n" + e.getLocalizedMessage(), e);
			}
			if (url.getProtocol().equalsIgnoreCase("file")) {
				try {
					canonicalSource = new File(url.toURI());
				} catch (URISyntaxException e) {
					logger.warn("Invalid file URI created from " + url);
				}
			}
		} else {
			throw new IllegalArgumentException("Unsupported source type "
					+ source.getClass());
		}

		final Dataflow dataflow;
		try {
			dataflow = openDataflowStream(inputStream);
		} finally {
			if (!(source instanceof InputStream)) {
				// We created the stream, we'll close it
				try {
					inputStream.close();
				} catch (IOException ex) {
					logger.warn("Could not close inputstream " + inputStream,
							ex);
				}
			}
		}
		if (canonicalSource instanceof File) {
			return new FileDataflowInfo(VRE4EIC_FLOW_FILE_TYPE,
					(File) canonicalSource, dataflow);
		}
		return new DataflowInfo(VRE4EIC_FLOW_FILE_TYPE, canonicalSource, dataflow,
				lastModified);
	
		
	}
	
	protected Dataflow openDataflowStream(InputStream workflowXMLstream)
			throws OpenException {
		XMLDeserializer deserializer = new XMLDeserializerImpl();
		SAXBuilder builder = new SAXBuilder();
		Document document;
		try {
			document = builder.build(workflowXMLstream);
		} catch (JDOMException e) {
			throw new OpenException("Could not parse XML of the workflow", e);
		} catch (IOException e) {
			throw new OpenException("Could not open the workflow file for parsing", e);
		}

		Dataflow dataflow;
		try {
			dataflow = deserializer.deserializeDataflow(document
					.getRootElement());
		} catch (DeserializationException e) {
			throw new OpenException("Could not deserialise the workflow", e);
		} catch (EditException e) {
			throw new OpenException("Could not construct the workflow", e);
		}
		return dataflow;
	}

	@Override
	public DataflowInfo saveDataflow(Dataflow dataflow, FileType fileType, Object destination) throws SaveException {
		 JsonObject ob=new JsonObject();
		if (!getSaveFileTypes().contains(fileType)) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		DataflowValidationReport dvr = dataflow.checkValidity();
		// Saving an invalid dataflow is OK. Validity check is done to get predicted depth for output (if possible)
		
		OutputStream outStream;
		if (destination instanceof File) {
			try {
				outStream = new SafeFileOutputStream((File) destination);
			} catch (IOException e) {
				throw new SaveException("Can't create workflow file "
						+ destination + ":\n" + e.getLocalizedMessage(), e);
			}
			Common.setDestinationPath(((File)destination).getAbsolutePath());//added to recall destination
		} else if (destination instanceof OutputStream) {
			outStream = (OutputStream) destination;
			Common.setDestinationPath(outStream.toString());//added to recall destination
		} else {
			throw new IllegalArgumentException("Unsupported destination type "
					+ destination.getClass());
		}
		boolean saved = false;
		try {
			String wfTitle="", wfCreator="", wfDescr="";
			saveDataflowToStream(dataflow, outStream);
			Set<? extends AnnotationChain> wfData=dataflow.getAnnotations();
			for (AnnotationChain ann: wfData) {
				List <AnnotationAssertion<?>> annas=ann.getAssertions();
				for ( net.sf.taverna.t2.annotation.AnnotationAssertion aa:annas) {
					System.out.println(aa.getDetail());
					
					if (aa.getDetail() instanceof net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle) {
						System.out.println(((DescriptiveTitle)aa.getDetail()).getText());
						wfTitle=((DescriptiveTitle)aa.getDetail()).getText();
						}
					if (aa.getDetail() instanceof FreeTextDescription) {
						System.out.println(((FreeTextDescription)aa.getDetail()).getText());
						wfDescr=((FreeTextDescription)aa.getDetail()).getText();
						}
					if (aa.getDetail() instanceof Author) {
						System.out.println(((Author)aa.getDetail()).getText());
						wfCreator=((Author)aa.getDetail()).getText();
						}
				}
				
			}
			ob.addProperty("wf_name", wfTitle);
			ob.addProperty("wf_creator", wfCreator);
			ob.addProperty("wf_description", wfDescr);
			//ob.addProperty("wf_id", dataflow.getIdentifier());
			ob.addProperty("user_name", Common.getUserId());
			ob.addProperty("url", destination.toString());
			//ob.addProperty("token", Common.getToken().replaceAll("\"", "").trim());
			this.saveInfoToVre4EICCatalogue(ob);
			saved = true;
		} finally {
			if (!(destination instanceof OutputStream)) {
				// Only close if we opened the stream
				try {
					if (! saved && outStream instanceof SafeFileOutputStream) {
						((SafeFileOutputStream)outStream).rollback();
					}					
					outStream.close();
				} catch (IOException e) {
					System.out.println("Could not close stream"+e.toString());
				}
			}
		}

		if (destination instanceof File) {
			return new FileDataflowInfo(VRE4EIC_FLOW_FILE_TYPE, (File) destination,
					dataflow);
		}
		return new DataflowInfo(VRE4EIC_FLOW_FILE_TYPE, destination, dataflow);

	
	}

	protected void saveInfoToVre4EICCatalogue(JsonObject obj) {
		SaveWFSwingWorker worker = new SaveWFSwingWorker( obj.toString());
		SaveWFInProgressDialog dialog = new SaveWFInProgressDialog(worker);		
		worker.execute();

		// Give a chance to the SwingWorker to finish so we do not have to display 
		// the dialog if checking of the object is quick (so it won't flicker on the screen)
		try {
		    Thread.sleep(500);
		} catch (InterruptedException ex) {

		}
		if (!worker.isDone()){
		    dialog.setVisible(true); // this will block the GUI
		}
		/*
			try (FileWriter file = new FileWriter("/home/cesare/file1.txt")) {
			file.write(obj.toString());
			System.out.println("Successfully registered the info in in the catalogue...");
			System.out.println("JSON Object: " + obj);
		} catch (IOException e) {
			
			e.printStackTrace();
		}*/
		
	}
	protected void saveDataflowToStream(Dataflow dataflow,
			OutputStream fileOutStream) throws SaveException {
		BufferedOutputStream bufferedOutStream = new BufferedOutputStream(
				fileOutStream);
		XMLOutputter outputter = new XMLOutputter();

		XMLSerializer serialiser = new XMLSerializerImpl();
		serialiser.setProducedBy(ApplicationConfig.getInstance().getName());
		Element serialized;
		try {
			serialized = serialiser.serializeDataflow(dataflow);
		} catch (SerializationException e) {
			throw new SaveException("Could not serialize " + dataflow, e);
		}

		try {
			outputter.output(serialized, bufferedOutStream);
			bufferedOutStream.flush();
		} catch (IOException e) {
			throw new SaveException("Can't write workflow:\n" + e.getLocalizedMessage(), e);
		}
	}

	@Override
	public boolean wouldOverwriteDataflow(Dataflow arg0, FileType fileType, Object destination, DataflowInfo lastDataflowInfo) {

		if (!getSaveFileTypes().contains(fileType)) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		if (!(destination instanceof File)) {
			return false;
		}

		File file;
		try {
			file = ((File) destination).getCanonicalFile();
		} catch (IOException e) {
			return false;
		}
		if (!file.exists()) {
			return false;
		}
		if (lastDataflowInfo == null) {
			return true;
		}
		Object lastDestination = lastDataflowInfo.getCanonicalSource();
		if (!(lastDestination instanceof File)) {
			return true;
		}
		File lastFile = (File) lastDestination;
		if (! lastFile.getAbsoluteFile().equals(file)) {
			return true;
		}
		
		
		Date lastModified = new Date(file.lastModified());
		if (lastModified.equals(lastDataflowInfo.getLastModified())) {
			return false;
		}
		return true;
	
	}

}
