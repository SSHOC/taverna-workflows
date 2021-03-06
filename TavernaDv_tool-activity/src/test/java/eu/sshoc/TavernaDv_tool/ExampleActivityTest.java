package eu.sshoc.TavernaDv_tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import org.junit.Before;
import org.junit.Test;

public class ExampleActivityTest {

	private ExampleActivityConfigurationBean configBean;

	private ExampleActivity activity = new ExampleActivity();

	@Before
	public void makeConfigBean() throws Exception {
		configBean = new ExampleActivityConfigurationBean();
		configBean.setExampleString("something");
		configBean
				.setExampleUri(URI.create("http://localhost:8080/myEndPoint"));
	}

	//@Test(expected = ActivityConfigurationException.class)
	public void invalidConfiguration() throws ActivityConfigurationException {
		ExampleActivityConfigurationBean invalidBean = new ExampleActivityConfigurationBean();
		invalidBean.setExampleString("invalidExample");
		// Should throw ActivityConfigurationException
		activity.configure(invalidBean);
	}

	//@Test
	public void executeAsynch() throws Exception {
		activity.configure(configBean);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("firstInput", "hello");

		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put("simpleOutput", String.class);
//		expectedOutputTypes.put("moreOutputs", String.class);
		expectedOutputTypes.put("moreOutputs", ExternalReferenceSPI.class);
		
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputTypes);

		assertEquals("Unexpected outputs", 2, outputs.size());
		assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", outputs.get("simpleOutput"));
		ErrorDocument errorDoc = (ErrorDocument) outputs.get("moreOutputs");
		assertEquals("java.lang.Exception: There are no more values",
		             errorDoc.getExceptionMessage());

	}
	
	//@Test
	public void checkSumOfExtraData() throws Exception {
		configBean.setExampleString("specialCase");
		activity.configure(configBean);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("firstInput", "hello");
		inputs.put("extraData", Arrays.asList("Test1".getBytes("utf8"),
		           "Test2".getBytes("utf8")));
		
		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put("simpleOutput", String.class);
		expectedOutputTypes.put("moreOutputs", ExternalReferenceSPI.class);
		expectedOutputTypes.put("report", String.class);
		
		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputTypes);

		assertEquals("Unexpected outputs", 3, outputs.size());
		// SHA1 checksum of "Test1Test2"
		assertEquals("35bceb434ff8e69fb89b829e461c921a28b423b3", outputs.get("simpleOutput"));
		assertEquals("Checksum of 2 items", outputs.get("report"));
		
		ErrorDocument errorDoc = (ErrorDocument) outputs.get("moreOutputs");
		assertEquals("java.lang.Exception: There are no more values",
		             errorDoc.getExceptionMessage());
	}
	
	//@Test
	public void reConfiguredActivity() throws Exception {
		assertEquals("Unexpected inputs", 0, activity.getInputPorts().size());
		assertEquals("Unexpected outputs", 0, activity.getOutputPorts().size());

		activity.configure(configBean);
		assertEquals("Unexpected inputs", 1, activity.getInputPorts().size());
		assertEquals("Unexpected outputs", 2, activity.getOutputPorts().size());

		activity.configure(configBean);
		// Should not change on reconfigure
		assertEquals("Unexpected inputs", 1, activity.getInputPorts().size());
		assertEquals("Unexpected outputs", 2, activity.getOutputPorts().size());
	}

	//@Test
	public void reConfiguredSpecialPorts() throws Exception {
		activity.configure(configBean);

		ExampleActivityConfigurationBean specialBean = new ExampleActivityConfigurationBean();
		specialBean.setExampleString("specialCase");
		specialBean.setExampleUri(URI
				.create("http://localhost:8080/myEndPoint"));
		activity.configure(specialBean);		
		// Should now have added the optional ports
		assertEquals("Unexpected inputs", 2, activity.getInputPorts().size());
		assertEquals("Unexpected outputs", 3, activity.getOutputPorts().size());
	}

	//@Test
	public void configureActivity() throws Exception {
		Set<String> expectedInputs = new HashSet<String>();
		expectedInputs.add("firstInput");

		Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("simpleOutput");
		expectedOutputs.add("moreOutputs");

		activity.configure(configBean);

		Set<ActivityInputPort> inputPorts = activity.getInputPorts();
		assertEquals(expectedInputs.size(), inputPorts.size());
		for (ActivityInputPort inputPort : inputPorts) {
			assertTrue("Wrong input : " + inputPort.getName(), expectedInputs
					.remove(inputPort.getName()));
		}

		Set<OutputPort> outputPorts = activity.getOutputPorts();
		assertEquals(expectedOutputs.size(), outputPorts.size());
		for (OutputPort outputPort : outputPorts) {
			assertTrue("Wrong output : " + outputPort.getName(),
					expectedOutputs.remove(outputPort.getName()));
		}
	}
}
