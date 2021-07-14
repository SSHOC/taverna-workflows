package eu.sshoc.TavernaDv_tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.*;

public class ExampleActivity extends
		AbstractAsynchronousActivity<ExampleActivityConfigurationBean>
		implements AsynchronousActivity<ExampleActivityConfigurationBean> {

	/*
	 * Best practice: Keep port names as constants to avoid misspelling. This
	 * would not apply if port names are looked up dynamically from the service
	 * operation, like done for WSDL services.
	 */
	private static final String IN_FIRST_INPUT = "firstInput";
	private static final String IN_SECOND_INPUT = "secondInput";
	private static final String IN_THIRD_INPUT = "thirdInput";	
	private static final String IN_EXTRA_DATA = "extraData";
	private static final String OUT_MORE_OUTPUTS = "moreOutputs";
	private static final String OUT_SIMPLE_OUTPUT = "simpleOutput";
	private static final String OUT_REPORT = "report";
	
	//FIXME load right token and other variable
	private static final String API_TOKEN = "eb445a67-9935-4e0e-9da3-f3aadd387fe9";
	private static final String DV_NAME = "firstDataverse";
	private static final String DV_PARENT = "firstDataverse";
	private static final String JSON_DV_PATH = "src/main/resources/dataverse-complete.json";
	private static final String JSON_DS_PATH = "src/main/resources/dataset-finch1.json";
	private static final String DATA_PATH = "/home/filippo/workspaceProva/DvWorkflow1.dvflow";
	private static final String DS_ID = "doi:10.5072/FK2/3JK338";
	
	private ExampleActivityConfigurationBean configBean;
	
	@Override
	public void configure(ExampleActivityConfigurationBean configBean)
			throws ActivityConfigurationException {

		// Any pre-config sanity checks
		if (configBean.getExampleString().equals("invalidExample")) {
			throw new ActivityConfigurationException(
					"Example string can't be 'invalidExample'");
		}
		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		this.configBean = configBean;

		// OPTIONAL: 
		// Do any server-side lookups and configuration, like resolving WSDLs

		// myClient = new MyClient(configBean.getExampleUri());
		// this.service = myClient.getService(configBean.getExampleString());

		
		// REQUIRED: (Re)create input/output ports depending on configuration
		configurePorts();
	}

	protected void configurePorts() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();

		// FIXME: Replace with your input and output port definitions
		// Hard coded input port, expecting a single String
		if (configBean.getExampleString().equals("/listdv")) {
			addInput(IN_FIRST_INPUT, 0, true, null, String.class);
			// Optional ports depending on configuration
		}else {
			addInput(IN_FIRST_INPUT, 0, true, null, String.class);
			if (configBean.getExampleString().equals("specialCase")) {
				// depth 1, ie. list of binary byte[] arrays
				addInput(IN_EXTRA_DATA, 1, true, null, byte[].class);
				addOutput(OUT_REPORT, 0);
			}
			if (configBean.getExampleString().equals("/listdatasets")) {
				addInput(IN_SECOND_INPUT, 0, true, null, String.class);
			}else{
				addInput(IN_SECOND_INPUT, 0, true, null, String.class);
				addInput(IN_THIRD_INPUT, 0, true, null, String.class);
			}
		}
		// Single value output port (depth 0)
		addOutput(OUT_SIMPLE_OUTPUT, 0);
		// Output port with list of values (depth 1)
		addOutput(OUT_MORE_OUTPUTS, 1);

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		// Don't execute service directly now, request to be run ask to be run
		// from thread pool and return asynchronously
		callback.requestRun(new Runnable() {
			
			public void run() {
				InvocationContext context = callback
						.getContext();
				ReferenceService referenceService = context
						.getReferenceService();
				// Resolve inputs 				
				String firstInput = (String) referenceService.renderIdentifier(inputs.get(IN_FIRST_INPUT), 
						String.class, context);
				String configString = configBean.getExampleString();
				// Support our configuration-dependendent input
				boolean optionalPorts = configString.equals("specialCase"); 
				
				List<byte[]> special = null;
				// We'll also allow IN_EXTRA_DATA to be optionally not provided
				if (optionalPorts && inputs.containsKey(IN_EXTRA_DATA)) {
					// Resolve as a list of byte[]
					special = (List<byte[]>) referenceService.renderIdentifier(
							inputs.get(IN_EXTRA_DATA), byte[].class, context);
				}
				
				//FIXME added for test purposes
//				T2Reference reference = inputs.get(IN_EXTRA_DATA);
//				System.out.println("Reference " + reference + " is depth " + reference.getDepth());
				
				// Do the actual service invocation
//				try {
//					results = this.service.invoke(firstInput, special)
//				} catch (ServiceException ex) {
//					callback.fail("Could not invoke Example service " + configBean.getExampleUri(),
//							ex);
//					// Make sure we don't call callback.receiveResult later 
//					return;
				//				}
				// Register outputs
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				// Do the actual service invocation 2
				SshocAPI sshocSv = new SshocAPI();
				T2Reference simpleRef;
				int resultcode;
				String output;
				switch(configString) {
				//FIXME use first line to test but the second in real service
				//FIXME all out.print are for testing only replace with log in log-file
				//FIXME use first line of sshocSv.service to test but the second in real service for all case
				case "/listdv":
					//list of dataverse
					resultcode = sshocSv.listDv(API_TOKEN);
//					int resultcode = sshocSv.listDataverse(firstInput);
					System.out.println("done resultcode="+resultcode);
					output = getOutput(sshocSv);
					System.out.println("follow output: \n"+output);
					simpleRef = referenceService.register(output, 0, true, context);
					outputs.put(OUT_SIMPLE_OUTPUT, simpleRef);
					sshocSv.terminateProcess();
					sshocSv.terminateProcess();
					break;
				case "/listdatasets":
					//list of datasets in a dataverse
					resultcode = sshocSv.listDataset(API_TOKEN, DV_NAME);
//					int resultcode = sshocSv.listDataverse(firstInput, secondInput);
					System.out.println("done resultcode="+resultcode);
					output = getOutput(sshocSv);
					System.out.println("follow output: \n"+output);
					simpleRef = referenceService.register(output, 0, true, context);
					outputs.put(OUT_SIMPLE_OUTPUT, simpleRef);
					sshocSv.terminateProcess();
					break;
				case "/createdataverse":
					//create a new dataverse
					resultcode = sshocSv.createDataverse(API_TOKEN, JSON_DV_PATH, DV_PARENT);
//					int resultcode = sshocSv.listDataverse(firstInput, thirdInput, secondInput);
					System.out.println("done resultcode="+resultcode);
					output = getOutput(sshocSv);
					System.out.println("follow output: \n"+output);
					simpleRef = referenceService.register(output, 0, true, context);
					outputs.put(OUT_SIMPLE_OUTPUT, simpleRef);
					sshocSv.terminateProcess();
					break;
				case "/createdataset":
					//ceeate a new dataset in a dataverse
					resultcode = sshocSv.createDataset(API_TOKEN, JSON_DS_PATH, DV_NAME);
//					int resultcode = sshocSv.listDataverse(firstInput, thirdInput, secondInput);
					System.out.println("done resultcode="+resultcode);
					output = getOutput(sshocSv);
					System.out.println("follow output: \n"+output);
					simpleRef = referenceService.register(output, 0, true, context);
					outputs.put(OUT_SIMPLE_OUTPUT, simpleRef);
					sshocSv.terminateProcess();
					break;
				case "/savedata":
					//add a data(workflow) in a dataset
					resultcode = sshocSv.saveData(API_TOKEN, new File(DATA_PATH), DS_ID);
//					int resultcode = sshocSv.listDataverse(firstInput, thirdInput, secondInput);
					System.out.println("done resultcode="+resultcode);
					output = getOutput(sshocSv);
					System.out.println("follow output: \n"+output);
					simpleRef = referenceService.register(output, 0, true, context);
					outputs.put(OUT_SIMPLE_OUTPUT, simpleRef);
					sshocSv.terminateProcess();
					break;
				default:
					//example
					MessageDigest digest;
					try {
						digest = MessageDigest.getInstance("SHA");
					} catch (NoSuchAlgorithmException e) {
						callback.fail("SHA algorithm not installed", e);
						return;
					}
					if (special != null) {
						for (byte[] data : special) {
							digest.update(data);
						}
					}
					byte[] checksumBinary = digest.digest();
					String checkSum = Hex.encodeHexString(checksumBinary);

					//				String simpleValue = "simple";
					//				T2Reference simpleRef = referenceService.register(simpleValue, 0, true, context);
					simpleRef = referenceService.register(checkSum, 0, true, context);
					outputs.put(OUT_SIMPLE_OUTPUT, simpleRef);
				}

				// For list outputs, only need to register the top level list
//				Old Code
//				List<String> moreValues = new ArrayList<String>();
//				moreValues.add("Value 1");
//				moreValues.add("Value 2");
//				T2Reference moreRef = referenceService.register(moreValues, 1, true, context);
//				outputs.put(OUT_MORE_OUTPUTS, moreRef);
//				New Code
				Exception moreValues = new Exception("There are no more values");
				T2Reference moreRef = referenceService.register(moreValues, 1, true, context);
				outputs.put(OUT_MORE_OUTPUTS, moreRef);
				
				if (optionalPorts) {
					// Populate our optional output port					
					// NOTE: Need to return output values for all defined output ports
//					String report = "Everything OK";
					String report;
					if (special != null) {
					    report = "Checksum of " + special.size() + " items";
					} else {
					    report = "Checksum of empty list";
					}
					outputs.put(OUT_REPORT, referenceService.register(report,
							0, true, context));
				}
				
				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
				callback.receiveResult(outputs, new int[0]);
			}
		});
	}

	@Override
	public ExampleActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	private static String getOutput(SshocAPI SshocSV){
		System.out.println("loadingP1...");
		InputStream is = SshocSV.getInputStream();				
		String testo = null;
		try {
			 InputStreamReader isReader = new InputStreamReader(is);
		      //Creating a BufferedReader object
		      BufferedReader reader = new BufferedReader(isReader);
		      StringBuffer sb = new StringBuffer();
		     
		      while((testo = reader.readLine())!= null){
		         sb.append(testo);
		      }

			//testo = new String(is.readAllBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("loadingP2...");
		if(testo!=null && !testo.equals("")) return testo;
		else return "NONE";
	}
	
}
