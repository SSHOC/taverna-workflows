package eu.sshoc.TavernaDv_tool.ui.serviceprovider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class ExampleServiceProvider extends AbstractConfigurableServiceProvider<ExampleServiceProviderConfig> 
implements ConfigurableServiceProvider<ExampleServiceProviderConfig>, CustomizedConfigurePanelProvider<ExampleServiceProviderConfig> {
	//,  ServiceDescriptionProvider { 
	
	public ExampleServiceProvider() {
		super(new ExampleServiceProviderConfig());
	}
	
	private static final URI providerId = URI
		.create("http://146.48.85.197:8080/Dataverse_tool-0.0.1-SNAPSHOT/sshoc/dvtool/");
	
	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		// Use callback.status() for long-running searches
		// callBack.status("Resolving example services");

		List<ServiceDescription> results = new ArrayList<ServiceDescription>();

		// FIXME: Implement the actual service search/lookup instead
		// of dummy for-loop
		for (int i = 1; i <= getConfiguration().getNumberOfService(); i++) {		
			switch(i) {
			default:ExampleServiceDesc service;
			case 1:
				service = new ExampleServiceDesc();
				service.setExampleString("/listdv");
				service.setExampleUri(getConfiguration().getUri());
				service.setDescription("List of all dataverse");
				results.add(service);
				break;
			case 2:
				service = new ExampleServiceDesc();
				service.setExampleString("/listdatasets");
				service.setExampleUri(getConfiguration().getUri());
				service.setDescription("List of all dataset in to specified dataverse");
				results.add(service);
				break;
			case 3:
				service = new ExampleServiceDesc();
				service.setExampleString("/createdataverse");
				service.setExampleUri(getConfiguration().getUri());
				service.setDescription("create a new dataverse");
				results.add(service);
				break;
			case 4:
				service = new ExampleServiceDesc();
				service.setExampleString("/createdataset");
				service.setExampleUri(getConfiguration().getUri());
				service.setDescription("create a new dataset");
				results.add(service);
				break;
			case 5:
				service = new ExampleServiceDesc();
				service.setExampleString("/savedata");
				service.setExampleUri(getConfiguration().getUri());
				service.setDescription("add a file in a dataset");
				results.add(service);
				break;
			}
			// Populate the service description bean
//			service.setExampleString("Example " + i);
//			service.setExampleUri(URI.create("http://localhost:8192/service"));
//			service.setExampleUri(getConfiguration().getUri());
			
			// Optional: set description
//			service.setDescription("Service example number " + i);
//			results.add(service);
//			if (results.size() >= 2) {
//			    callBack.partialResults(results);
//			    results.clear();
//			} else {
//			    callBack.status("Pretending to look up services");
//			}
			
			callBack.partialResults(results);
//		    try {
//		        Thread.sleep(5000);
//		    } catch (InterruptedException e) {
//		        return;
//		    }
		}

		// partialResults() can also be called several times from inside
		// for-loop if the full search takes a long time
//		callBack.partialResults(results); //moved up to inside the for-loop

		// No more results will be coming
		callBack.finished();
	}

	/**
	 * Icon for service provider
	 */
	public Icon getIcon() {
		return ExampleServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	public String getName() {
		return "My example service";
	}
	
	@Override
	public String toString() {
		return "My example service" + getConfiguration().getUri();
	}
	
	public String getId() {
		return providerId.toASCIIString();
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		return Arrays.asList(getConfiguration().getUri());
	}
	
	@Override
	public List<ExampleServiceProviderConfig> getDefaultConfigurations(){
		ExampleServiceProviderConfig a = new ExampleServiceProviderConfig();
		a.setUri(URI.create("http://146.48.85.197:8080/Dataverse_tool-0.0.1-SNAPSHOT/sshoc/dvtool"));
		a.setNumberOfService(5);
//		a.setUri(URI.create("http://localhost:8181/serviceA"));
//		ExampleServiceProviderConfig b = new ExampleServiceProviderConfig();
//		b.setUri(URI.create("http://fish.com/serviceB"));
//		b.setNumberOfService(2);
//		return Arrays.asList(a, b);
		return Arrays.asList(a);
	}

	@Override
	public void createCustomizedConfigurePanel(CustomizedConfigureCallBack<ExampleServiceProviderConfig> callBack) {
		// Possible choices for drop-down box
		URI[] uris = new URI[3];
		uris[0] = URI.create("http://example.com/serviceA");
		uris[1] = URI.create("http://example.com/serviceB");
		uris[2] = URI.create("http://example.com/serviceC");
		// Dialogue with drop-down
		String message = "Choose the example service URI";
		String title = "Which service?";
		Object uri = JOptionPane.showInputDialog(null, message, title, 
				JOptionPane.PLAIN_MESSAGE, null, uris, uris[0]);
		// Return a new provider configuration
		ExampleServiceProviderConfig config = new ExampleServiceProviderConfig();
		config.setUri((URI) uri);
		callBack.newProviderConfiguration(config);
	}

}
