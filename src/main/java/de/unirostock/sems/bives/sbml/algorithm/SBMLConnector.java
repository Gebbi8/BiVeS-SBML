/**
 * 
 */
package de.unirostock.sems.bives.sbml.algorithm;

import java.util.List;
import java.util.Vector;

import de.unirostock.sems.bives.algorithm.Connector;
import de.unirostock.sems.bives.algorithm.general.XyDiffConnector;
import de.unirostock.sems.bives.ds.SBOTerm;
import de.unirostock.sems.bives.exception.BivesConnectionException;
import de.unirostock.sems.bives.sbml.parser.SBMLDocument;
import de.unirostock.sems.xmltools.comparison.Connection;
import de.unirostock.sems.xmltools.ds.DocumentNode;
import de.unirostock.sems.xmltools.ds.TreeDocument;
import de.unirostock.sems.xmltools.ds.TreeNode;


/**
 * @author Martin Scharm
 *
 */
public class SBMLConnector
	extends Connector
{
	private Connector preprocessor;
	private SBMLDocument sbmlDocA, sbmlDocB;

	public SBMLConnector (SBMLDocument sbmlDocA, SBMLDocument sbmlDocB)
	{
		super ();
		this.sbmlDocA = sbmlDocA;
		this.sbmlDocB = sbmlDocB;
	}
	
	public SBMLConnector (Connector preprocessor)
	{
		super ();
		this.preprocessor = preprocessor;
	}
	

	@Override
	public void init (TreeDocument docA, TreeDocument docB) throws BivesConnectionException
	{
		// TODO: maybe preporcessing -> instead of id's use annotations/ontologies etc
		// use id's
		// use variables for rules
		super.init (sbmlDocA.getTreeDocument (), sbmlDocB.getTreeDocument ());

		// preprocessor connects by id and stuff
		// xy propagates connections
		XyDiffConnector id = new XyDiffConnector (new SBMLConnectorPreprocessor (sbmlDocA, sbmlDocB));
		id.init (docA, docB);
		id.findConnections ();

		conMgmt = id.getConnections ();
		//System.out.println (conMgmt);
		
	}
	
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.xmldiff.algorithm.Connector#findConnections()
	 */
	@Override
	protected void connect ()
	{
		//System.out.println ("vorher:");
		//System.out.println (conMgmt);
		
		// post processing
		
		// following nodes cannot have a connection with changes...
		List<DocumentNode> lists = docA.getNodesByTag ("listOfModifiers");
		lists.addAll (docA.getNodesByTag ("listOfProducts"));
		lists.addAll (docA.getNodesByTag ("listOfReactants"));
		lists.addAll (docA.getNodesByTag ("listOfEventAssignments"));
		lists.addAll (docA.getNodesByTag ("modifierSpeciesReference"));
		lists.addAll (docA.getNodesByTag ("speciesReference"));
		lists.addAll (docA.getNodesByTag ("trigger"));
		lists.addAll (docA.getNodesByTag ("eventAssignment"));
		lists.addAll (docA.getNodesByTag ("delay"));
		lists.addAll (docA.getNodesByTag ("priority"));
		for (DocumentNode tn : lists)
		{
			Connection con = conMgmt.getConnectionForNode (tn);
			if (con == null)
				continue;
			TreeNode partner = con.getTreeB ();
			if (tn.networkDiffers (partner, conMgmt, con))
			{
				/*System.out.println ("network differs: ");
				System.out.println ("nwd: " + tn.getXPath ());
				System.out.println ("nwd: " + partner.getXPath ());*/
				conMgmt.dropConnection (tn);
			}
			
			
			/*boolean unconnect = false;
			for (Connection c : cons)
			{
				TreeNode partner = c.getTreeB ();
				if (tn.networkDiffers (partner, conMgmt, c))
				{
					unconnect = true;
					break;
				}
			}
			if (unconnect)
			{
				//System.out.println ("dropping connections of " + tn.getXPath ());
				conMgmt.dropConnection (tn);
			}
			/*if (tn)
			{
				System.out.println ("dropping connections of " + tn.getXPath ());
				conMgmt.dropConnections (tn);
			}*/
		}
		
		// different kind of modifiers?
		for (TreeNode tn : docA.getNodesByTag ("modifierSpeciesReference"))
		{
			Connection con = conMgmt.getConnectionForNode (tn);
			if (con == null)
				continue;
			DocumentNode a = (DocumentNode) con.getTreeA ();
			DocumentNode b = (DocumentNode) con.getTreeB ();
			
			if (!cmpSBO (sbmlDocA.getModel ().getFromNode (a).getSBOTerm (), sbmlDocB.getModel ().getFromNode (b).getSBOTerm ()))
				conMgmt.dropConnection (con);
			
			/*String modA = sbmlDocA.getModel ().getFromNode (a).getSBOTerm ().resolvModifier ();
			String modB = sbmlDocB.getModel ().getFromNode (b).getSBOTerm ().resolvModifier ();
			if (!modA.equals (modB))
				conMgmt.dropConnection (con);
			
			/*
			for (Connection c : cons)
			{
				DocumentNode a = (DocumentNode) c.getTreeA ();
				DocumentNode b = (DocumentNode) c.getTreeB ();
				if (!ChemicalReactionNetwork.resolvModSBO (a.getAttribute ("sboTerm")).equals (ChemicalReactionNetwork.resolvModSBO (a.getAttribute ("sboTerm"))))
					conMgmt.dropConnection (c);
			}*/
			
		}

		//System.out.println ("nachher:");
		//System.out.println (conMgmt);
	}

	/**
	 * Compare SBO terms. Returns true if both terms have equal meaning, or false if they differ.
	 *
	 * @param a SBOTerm 1
	 * @param b SBOTerm 2
	 * @return true, if same meaning
	 */
	private boolean cmpSBO (SBOTerm a, SBOTerm b)
	{
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		
		return a.resolvModifier ().equals (b.resolvModifier ());
	}
	
}
