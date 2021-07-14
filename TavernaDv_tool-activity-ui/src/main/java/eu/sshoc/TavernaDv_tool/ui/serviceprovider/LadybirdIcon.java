package eu.sshoc.TavernaDv_tool.ui.serviceprovider;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import eu.sshoc.TavernaDv_tool.ExampleActivity;
import eu.sshoc.TavernaDv_tool.ExampleActivityConfigurationBean;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class LadybirdIcon implements ActivityIconSPI {

	private Icon icon;
	
	@Override
	public int canProvideIconScore(Activity<?> activity) {
		if (activity instanceof ExampleActivity) {
	        ExampleActivity exampleActivity = (ExampleActivity) activity;
	        ExampleActivityConfigurationBean configuration = exampleActivity.getConfiguration();
	        if (configuration.getExampleString().equals("ladybird")) {
	            return DEFAULT_ICON + 100;
	        }
	    }
	    return NO_ICON;
	}

	@Override
	public Icon getIcon(Activity<?> activity) {
		if (icon == null) {
			icon = new ImageIcon(ExampleServiceIcon.class.getResource("/ladybird.png"));
			}
		return icon;
	}

}
