package com.att.aro.ui.view.menu.datacollector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import com.att.aro.core.peripheral.pojo.AttenuatorModel;
import com.att.aro.ui.commonui.DataCollectorSelectNStartDialog;

public class AttnrPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private AttnrRadioGroupPanel attnrRadioGPanel;
	private AttnrPanelHolder attnrHolderPanel;
	private AttenuatorModel miniAtnr;
	private DataCollectorSelectNStartDialog parent;
	
	public AttnrPanel(DataCollectorSelectNStartDialog parent, AttenuatorModel miniAtnr){
		setLayout(new GridBagLayout());
		this.parent = parent;
		this.miniAtnr = miniAtnr;
 		add(getAttnrRadioGP(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
				GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));		
 		add(getAttnrHolder(), new GridBagConstraints(0, 1, 5, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
}
	
	public AttnrRadioGroupPanel getAttnrRadioGP(){
		if(attnrRadioGPanel == null){
			attnrRadioGPanel = new AttnrRadioGroupPanel(this,miniAtnr, parent);
		}
		return attnrRadioGPanel;
	}
	
	public AttnrPanelHolder getAttnrHolder(){
		if(attnrHolderPanel == null){
			attnrHolderPanel = new AttnrPanelHolder();			
		}
		return attnrHolderPanel;
	}	
	
	void setAttenuateEnable(boolean isEnabled) {
		if (!isEnabled) {
			getAttnrRadioGP().reset();
		}
		getAttnrRadioGP().enableComponents(getAttnrRadioGP(), isEnabled);
	}
}
