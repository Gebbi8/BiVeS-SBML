/**
 * 
 */
package de.unirostock.sems.bives.sbml.parser;

import java.util.Vector;

import de.unirostock.sems.bives.algorithm.ClearConnectionManager;
import de.unirostock.sems.bives.ds.DiffReporter;
import de.unirostock.sems.bives.ds.MathML;
import de.unirostock.sems.bives.markup.MarkupDocument;
import de.unirostock.sems.bives.markup.MarkupElement;
import de.unirostock.sems.bives.sbml.exception.BivesSBMLParseException;
import de.unirostock.sems.bives.tools.BivesTools;
import de.unirostock.sems.xmltools.ds.DocumentNode;
import de.unirostock.sems.xmltools.ds.TreeNode;


/**
 * @author Martin Scharm
 *
 */
public class SBMLInitialAssignment
	extends SBMLSBase
	implements DiffReporter
{
	private SBMLSBase symbol;
	private MathML math;
	
	/**
	 * @param documentNode
	 * @param sbmlDocument
	 * @throws BivesSBMLParseException
	 */
	public SBMLInitialAssignment (DocumentNode documentNode,
		SBMLModel sbmlModel) throws BivesSBMLParseException
	{
		super (documentNode, sbmlModel);
		
		String tmp = documentNode.getAttribute ("symbol");
		symbol = sbmlModel.getCompartment (tmp);
		if (symbol == null)
			symbol = sbmlModel.getSpecies (tmp);
		if (symbol == null)
			symbol = sbmlModel.getParameter (tmp);
		if (symbol == null)
			symbol = sbmlModel.getSpeciesReference (tmp);
		if (symbol == null)
			throw new BivesSBMLParseException ("symbol "+tmp+" of initial assignment unmappable.");
		

		Vector<TreeNode> maths = documentNode.getChildrenWithTag ("math");
		if (maths.size () != 1)
			throw new BivesSBMLParseException ("initial assignment has "+maths.size ()+" math elements. (expected exactly one element)");
		math = new MathML ((DocumentNode) maths.elementAt (0));
	}
	
	public SBMLSBase getSymbol ()
	{
		return symbol;
	}
	
	public MathML getMath ()
	{
		return math;
	}
	
	@Override
	public MarkupElement reportMofification (ClearConnectionManager conMgmt, DiffReporter docA, DiffReporter docB, MarkupDocument markupDocument)
	{
		SBMLInitialAssignment a = (SBMLInitialAssignment) docA;
		SBMLInitialAssignment b = (SBMLInitialAssignment) docB;
		if (a.getDocumentNode ().getModification () == 0 && b.getDocumentNode ().getModification () == 0)
			return null;
		
		String idA = SBMLModel.getSidName (a.symbol), idB = SBMLModel.getSidName (b.symbol);
		MarkupElement me = null;
		if (idA.equals (idB))
			me = new MarkupElement (idA);
		else
			me = new MarkupElement (markupDocument.delete (idA) + " "+markupDocument.rightArrow ()+" " + markupDocument.insert (idB));
		
		BivesTools.genAttributeHtmlStats (a.documentNode, b.documentNode, me, markupDocument);
		BivesTools.genMathHtmlStats (a.math.getDocumentNode (), b.math.getDocumentNode (), me, markupDocument);
		
		return me;
	}
	
	@Override
	public MarkupElement reportInsert (MarkupDocument markupDocument)
	{
		MarkupElement me = new MarkupElement (markupDocument.insert (SBMLModel.getSidName (symbol)));
		me.addValue (markupDocument.insert ("inserted"));
		return me;
	}
	
	@Override
	public MarkupElement reportDelete (MarkupDocument markupDocument)
	{
		MarkupElement me = new MarkupElement (markupDocument.delete (SBMLModel.getSidName (symbol)));
		me.addValue (markupDocument.delete ("deleted"));
		return me;
	}
}
