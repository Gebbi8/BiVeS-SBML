/**
 * 
 */
package de.unirostock.sems.bives.sbml.parser;

import de.unirostock.sems.bives.algorithm.ClearConnectionManager;
import de.unirostock.sems.bives.ds.DiffReporter;
import de.unirostock.sems.bives.markup.MarkupDocument;
import de.unirostock.sems.bives.markup.MarkupElement;
import de.unirostock.sems.bives.sbml.exception.BivesSBMLParseException;
import de.unirostock.sems.bives.tools.BivesTools;
import de.unirostock.sems.xmlutils.ds.DocumentNode;


/**
 * @author Martin Scharm
 *
 */
public class SBMLAlgebraicRule
	extends SBMLRule
{
	
	/**
	 * @param documentNode
	 * @param sbmlDocument
	 * @throws BivesSBMLParseException
	 */
	public SBMLAlgebraicRule (DocumentNode documentNode, SBMLModel sbmlModel)
		throws BivesSBMLParseException
	{
		super (documentNode, sbmlModel);
		type = SBMLRule.ALGEBRAIC_RULE;
	}

	@Override
	public MarkupElement reportMofification (ClearConnectionManager conMgmt, DiffReporter docA, DiffReporter docB, MarkupDocument markupDocument)
	{
		SBMLAlgebraicRule a = (SBMLAlgebraicRule) docA;
		SBMLAlgebraicRule b = (SBMLAlgebraicRule) docB;
		if (a.getDocumentNode ().getModification () == 0 && b.getDocumentNode ().getModification () == 0)
			return null;

		MarkupElement me = new MarkupElement ("AlgebraicRule");
		
		BivesTools.genAttributeHtmlStats (a.documentNode, b.documentNode, me, markupDocument);
		BivesTools.genMathHtmlStats (a.math.getDocumentNode (), b.math.getDocumentNode (), me, markupDocument);
		
		return me;
	}
	
	@Override
	public MarkupElement reportInsert (MarkupDocument markupDocument)
	{
		MarkupElement me = new MarkupElement (markupDocument.insert ("AlgebraicRule"));
		BivesTools.genAttributeHtmlStats (null, documentNode, me, markupDocument);
		//me.addValue (markupDocument.insert ("inserted"));
		return me;
	}
	
	@Override
	public MarkupElement reportDelete (MarkupDocument markupDocument)
	{
		MarkupElement me = new MarkupElement (markupDocument.delete ("AlgebraicRule"));
		BivesTools.genAttributeHtmlStats (documentNode, null, me, markupDocument);
		//me.addValue (markupDocument.delete ("deleted"));
		return me;
	}
	
}
