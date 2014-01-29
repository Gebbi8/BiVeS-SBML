/**
 * 
 */
package de.unirostock.sems.bives.sbml.parser;

import de.unirostock.sems.xmltools.ds.DocumentNode;


/**
 * @author Martin Scharm
 *
 */
public class SBMLGenericObject
{
	protected DocumentNode documentNode;
	protected SBMLModel sbmlModel;
	
	public SBMLGenericObject (DocumentNode documentNode, SBMLModel sbmlModel)
	{
		this.documentNode = documentNode;
		this.sbmlModel = sbmlModel;
	}
	
	public DocumentNode getDocumentNode ()
	{
		return documentNode;
	}
	
	public SBMLModel getModel ()
	{
		return sbmlModel;
	}
}
