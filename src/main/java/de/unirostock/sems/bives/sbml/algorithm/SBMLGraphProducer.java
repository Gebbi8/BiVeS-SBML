/**
 * 
 */
package de.unirostock.sems.bives.sbml.algorithm;

import java.util.HashMap;
import java.util.List;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.bives.algorithm.SimpleConnectionManager;
import de.unirostock.sems.bives.algorithm.GraphProducer;
import de.unirostock.sems.bives.ds.rn.ReactionNetworkCompartment;
import de.unirostock.sems.bives.ds.rn.ReactionNetworkReaction;
import de.unirostock.sems.bives.ds.rn.ReactionNetworkSubstance;
import de.unirostock.sems.bives.exception.BivesUnsupportedException;
import de.unirostock.sems.bives.sbml.parser.SBMLCompartment;
import de.unirostock.sems.bives.sbml.parser.SBMLDocument;
import de.unirostock.sems.bives.sbml.parser.SBMLModel;
import de.unirostock.sems.bives.sbml.parser.SBMLReaction;
import de.unirostock.sems.bives.sbml.parser.SBMLSimpleSpeciesReference;
import de.unirostock.sems.bives.sbml.parser.SBMLSpecies;
import de.unirostock.sems.bives.sbml.parser.SBMLSpeciesReference;
import de.unirostock.sems.xmlutils.comparison.Connection;
import de.unirostock.sems.xmlutils.ds.DocumentNode;


/**
 * The Class SBMLGraphProducer to create the graphs.
 *
 * @author Martin Scharm
 */
public class SBMLGraphProducer
extends GraphProducer
{
	
	/** The SBML documents A and B. */
	private SBMLDocument sbmlDocA, sbmlDocB;
	
	/** The connection manager. */
	private SimpleConnectionManager conMgmt;
	
	/**
	 * Instantiates a new SBML graph producer for difference graphs.
	 *
	 * @param conMgmt the connection manager
	 * @param sbmlDocA the original document
	 * @param sbmlDocB the modified document
	 */
	public SBMLGraphProducer (SimpleConnectionManager conMgmt, SBMLDocument sbmlDocA, SBMLDocument sbmlDocB)
	{
		super (false);
		this.sbmlDocA = sbmlDocA;
		this.sbmlDocB = sbmlDocB;
		this.conMgmt = conMgmt;
	}
	
	/**
	 * Instantiates a new SBML graph producer for single document graphs.
	 *
	 * @param sbmlDoc the SBML document
	 */
	public SBMLGraphProducer (SBMLDocument sbmlDoc)
	{
		super (true);
		this.sbmlDocA = sbmlDoc;
	}
	


	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.GraphProducer#produceCRN()
	 */
	@Override
	protected void produceCRN ()
	{
		try
		{
			processCrnA ();
			if (single)
				crn.setSingleDocument ();
			else
				processCrnB ();
		}
		catch (BivesUnsupportedException e)
		{
			LOGGER.error (e, "something bad happened");
		}
		
		if (crn.getSubstances ().size () < 1)
			crn = null;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.GraphProducer#produceHierarchyGraph()
	 */
	@Override
	protected void produceHierarchyGraph ()
	{
		// nothing to do for SBML
		hn = null;
	}
	
	/**
	 * Process Chemical Reaction Network of the original document.
	 *
	 * @throws BivesUnsupportedException the bives unsupported exception
	 */
	protected void processCrnA () throws BivesUnsupportedException
	{
		SBMLModel modelA = sbmlDocA.getModel ();
		LOGGER.info ("searching for compartments in A");
		HashMap<String, SBMLCompartment> compartments = modelA.getCompartments ();
		for (SBMLCompartment c : compartments.values ())
			crn.setCompartment (c.getDocumentNode (), new ReactionNetworkCompartment (crn, c.getNameOrId (), null, c.getDocumentNode (), null));
		
		LOGGER.info ("searching for species in A");
		HashMap<String, SBMLSpecies> species = modelA.getSpecies ();
		for (SBMLSpecies s : species.values ())
			crn.setSubstance (s.getDocumentNode (), new ReactionNetworkSubstance (crn, s.getNameOrId (), null, s.getDocumentNode (), null, crn.getCompartment (s.getCompartment ().getDocumentNode ()), null));
		
		LOGGER.info ("searching for reactions in A");
		HashMap<String, SBMLReaction> reactions = modelA.getReactions ();
		for (SBMLReaction r : reactions.values ())
		{
			ReactionNetworkReaction reaction = new ReactionNetworkReaction (crn, r.getNameOrId (), null, r.getDocumentNode (), null, null, null, r.isReversible ());
			if (r.getCompartment () != null)
				reaction.setCompartmentA (crn.getCompartment (r.getCompartment ().getDocumentNode ()));
			crn.setReaction (r.getDocumentNode (), reaction);
			
			List<SBMLSpeciesReference> sRefs = r.getReactants ();
			for (SBMLSpeciesReference sRef : sRefs)
			{
				reaction.addInputA (crn.getSubstance (sRef.getSpecies ().getDocumentNode ()), sRef.getSBOTerm ());
			}
			
			sRefs = r.getProducts ();
			for (SBMLSpeciesReference sRef : sRefs)
			{
				reaction.addOutputA (crn.getSubstance (sRef.getSpecies ().getDocumentNode ()), sRef.getSBOTerm ());
			}
			
			List<SBMLSimpleSpeciesReference> ssRefs = r.getModifiers ();
			for (SBMLSimpleSpeciesReference sRef : ssRefs)
			{
				SBMLSpecies spec = sRef.getSpecies ();
					reaction.addModA (crn.getSubstance (spec.getDocumentNode ()), sRef.getSBOTerm ());
			}
		}
	}
	
	/**
	 * Process Chemical Reaction Network of the modified document.
	 *
	 * @throws BivesUnsupportedException the bives unsupported exception
	 */
	protected void processCrnB () throws BivesUnsupportedException
	{
		SBMLModel modelB = sbmlDocB.getModel ();
		LOGGER.info ("searching for compartments in B");
		HashMap<String, SBMLCompartment> compartments = modelB.getCompartments ();
		for (SBMLCompartment c : compartments.values ())
		{
			DocumentNode cDoc = c.getDocumentNode ();
			Connection con = conMgmt.getConnectionForNode (cDoc);
			if (con == null)
			{
				// no equivalent in doc a
				crn.setCompartment (c.getDocumentNode (), new ReactionNetworkCompartment (crn, c.getNameOrId (), null, c.getDocumentNode (), null));
			}
			else
			{
				ReactionNetworkCompartment comp = crn.getCompartment (con.getPartnerOf (cDoc));
				comp.setDocB (cDoc);
				comp.setLabelB (c.getNameOrId ());
				crn.setCompartment (cDoc, comp);
			}
		}
		
		LOGGER.info ("searching for species in B");
		HashMap<String, SBMLSpecies> species = modelB.getSpecies ();
		for (SBMLSpecies s : species.values ())
		{
			DocumentNode sDoc = s.getDocumentNode ();
			Connection c = conMgmt.getConnectionForNode (sDoc);
			if (c == null)
			{
				// no equivalent in doc a
				crn.setSubstance (sDoc, new ReactionNetworkSubstance (crn, null, s.getNameOrId (), null, sDoc, null, crn.getCompartment (s.getCompartment ().getDocumentNode ())));
			}
			else
			{
				ReactionNetworkSubstance subst = crn.getSubstance (c.getPartnerOf (sDoc));
				subst.setDocB (sDoc);
				subst.setLabelB (s.getNameOrId ());
				subst.setCompartmentB (crn.getCompartment (s.getCompartment ().getDocumentNode ()));
				crn.setSubstance (sDoc, subst);
			}
		}
		
		LOGGER.info ("searching for reactions in B");
		HashMap<String, SBMLReaction> reactions = modelB.getReactions ();
		for (SBMLReaction r : reactions.values ())
		{
			DocumentNode rNode = r.getDocumentNode ();
			Connection c = conMgmt.getConnectionForNode (rNode);
			ReactionNetworkReaction reaction = null;
			if (c == null)
			{
				// no equivalent in doc a
				reaction = new ReactionNetworkReaction (crn, null, r.getNameOrId (), null, r.getDocumentNode (), null, null, r.isReversible ());
				crn.setReaction (rNode, reaction);
			}
			else
			{
				reaction = crn.getReaction (c.getPartnerOf (rNode));
				reaction.setDocB (rNode);
				reaction.setLabelB (r.getNameOrId ());
				crn.setReaction (rNode, reaction);
			}
			if (r.getCompartment () != null)
				reaction.setCompartmentB (crn.getCompartment (r.getCompartment ().getDocumentNode ()));
				
			List<SBMLSpeciesReference> sRefs = r.getReactants ();
			for (SBMLSpeciesReference sRef : sRefs)
			{
				reaction.addInputB (crn.getSubstance (sRef.getSpecies ().getDocumentNode ()), sRef.getSBOTerm ());
			}
			
			sRefs = r.getProducts ();
			for (SBMLSpeciesReference sRef : sRefs)
			{
				reaction.addOutputB (crn.getSubstance (sRef.getSpecies ().getDocumentNode ()), sRef.getSBOTerm ());
			}
			
			List<SBMLSimpleSpeciesReference> ssRefs = r.getModifiers ();
			for (SBMLSimpleSpeciesReference sRef : ssRefs)
			{
				SBMLSpecies spec = sRef.getSpecies ();
				//if (spec.getSBOTerm () == null)
					reaction.addModB (crn.getSubstance (spec.getDocumentNode ()), sRef.getSBOTerm ());
			}
		}
	}
	
}
