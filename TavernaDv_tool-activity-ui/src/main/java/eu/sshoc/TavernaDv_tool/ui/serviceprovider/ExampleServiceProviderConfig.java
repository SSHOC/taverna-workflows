package eu.sshoc.TavernaDv_tool.ui.serviceprovider;

import java.net.URI;

import net.sf.taverna.t2.lang.beans.PropertyAnnotated;
import net.sf.taverna.t2.lang.beans.PropertyAnnotation;

public class ExampleServiceProviderConfig extends PropertyAnnotated{
	private URI uri = URI.create("http://example.com");
	private int numberOfService = 5;
	
	@PropertyAnnotation(preferred=true)
	public URI getUri() {
		return uri;
	}
	
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	@PropertyAnnotation(displayName="Number of service")
	public int getNumberOfService() {
		return numberOfService;
	}
	
	public void setNumberOfService(int numberOfService) {
		this.numberOfService = numberOfService;
	}
	
}
