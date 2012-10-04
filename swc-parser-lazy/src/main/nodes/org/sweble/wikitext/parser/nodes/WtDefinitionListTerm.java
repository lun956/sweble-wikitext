package org.sweble.wikitext.parser.nodes;

/**
 * <h1>WtDefinitionListTerm</h1>
 */
public class WtDefinitionListTerm
		extends
			WtContentNodeMarkTwo
{
	private static final long serialVersionUID = 1L;
	
	// =========================================================================
	
	public WtDefinitionListTerm()
	{
		super();
	}
	
	public WtDefinitionListTerm(WtNodeList content)
	{
		super(content);
	}
	
	@Override
	public int getNodeType()
	{
		return org.sweble.wikitext.parser.AstNodeTypes.NT_DEFINITION_TERM;
	}
}
