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
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;


/**
 * @author Martin Scharm
 *
 */
public class SBMLFunctionDefinition
extends SBMLGenericIdNameObject
implements DiffReporter
{
	private MathML math;
	
	public SBMLFunctionDefinition (DocumentNode functionDefinition, SBMLModel sbmlModel) throws BivesSBMLParseException
	{
		super (functionDefinition, sbmlModel);
		
		Vector<TreeNode> maths = functionDefinition.getChildrenWithTag ("math");
		if (maths.size () != 1)
			throw new BivesSBMLParseException ("FunctionDefinition "+id+" has "+maths.size ()+" math elements. (expected exactly one element)");
		math = new MathML ((DocumentNode) maths.elementAt (0));
	}
	
	public MathML getMath ()
	{
		return math;
	}

	@Override
	public MarkupElement reportMofification (ClearConnectionManager conMgmt, DiffReporter docA, DiffReporter docB, MarkupDocument markupDocument)
	{
		SBMLFunctionDefinition a = (SBMLFunctionDefinition) docA;
		SBMLFunctionDefinition b = (SBMLFunctionDefinition) docB;
		if (a.getDocumentNode ().getModification () == 0 && b.getDocumentNode ().getModification () == 0)
			return null;
		
		String idA = a.getNameAndId (), idB = b.getNameAndId ();
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
		MarkupElement me = new MarkupElement (markupDocument.insert (getNameAndId ()));
		BivesTools.genMathHtmlStats (null, math.getDocumentNode (), me, markupDocument);
		//me.addValue (markupDocument.insert ("inserted"));
		return me;
	}
	
	@Override
	public MarkupElement reportDelete (MarkupDocument markupDocument)
	{
		MarkupElement me = new MarkupElement (markupDocument.delete (getNameAndId ()));
		BivesTools.genMathHtmlStats (math.getDocumentNode (), null, me, markupDocument);
		//me.addValue (markupDocument.delete ("deleted"));
		return me;
	}
}
