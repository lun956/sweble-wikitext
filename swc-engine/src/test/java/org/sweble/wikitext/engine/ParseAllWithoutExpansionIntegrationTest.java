/**
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nürnberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sweble.wikitext.engine;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sweble.wikitext.engine.config.WikiConfigImpl;
import org.sweble.wikitext.engine.utils.CompilerTestBase;
import org.sweble.wikitext.parser.AstNodeTypes;
import org.sweble.wikitext.parser.WtEntityMap;
import org.sweble.wikitext.parser.nodes.WtParagraph;
import org.sweble.wikitext.parser.nodes.WtPreproWikitextPage;
import org.sweble.wikitext.parser.nodes.WtTemplateArgument;
import org.sweble.wikitext.parser.nodes.WtTemplateParameter;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtNodeList;
import org.sweble.wikitext.parser.utils.WtPrinter;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.Warning;

public class ParseAllWithoutExpansionIntegrationTest
{
	private CompilerTestBase ctb;
	
	private Compiler compiler;
	
	private WikiConfigImpl config;
	
	// =========================================================================
	
	@Before
	public void setUp() throws Exception
	{
		ctb = new CompilerTestBase();
		config = ctb.getConfig();
		compiler = ctb.getCompiler();
	}
	
	// =========================================================================
	
	@Test
	public void testParseAndExpandParametersToDefaultValues() throws Exception
	{
		String wikitext = ""
				+ "''' some bold text '''\n"
				+ "{{{1|''italic default value''}}}\n"
				+ "{{some template|{{{2|[[some link]]}}}}}";
		
		PageTitle title = PageTitle.make(config, "-");
		PageId pageId = new PageId(title, -1);
		CompiledPage page = compiler.postprocess(pageId, wikitext, null);
		
		new ParameterToDefaultValueResolver(pageId).go(page);
		
		// We don't want to see the log
		page.setLog(null);
		
		String printedAst = WtPrinter.print(page);
		
		String expected = ""
				+ "CompiledPage(\n"
				+ "  Properties:\n"
				+ "    {N} entityMap = null\n"
				+ "    {N} log = null\n"
				+ "    {N} warnings = []\n"
				+ "\n"
				+ "  Page([\n"
				+ "    WtParagraph([\n"
				+ "      WtBold(\n"
				+ "        Properties:\n"
				+ "          {N} rtd = RTD[ \"'''\" <o> \"'''\" ]\n"
				+ "\n"
				+ "        [ WtText(\" some bold text \") ]\n"
				+ "      )\n"
				+ "      Newline(\"\\n\")\n"
				+ "      WtItalics(\n"
				+ "        Properties:\n"
				+ "          {N} rtd = RTD[ \"''\" <o> \"''\" ]\n"
				+ "\n"
				+ "        [ WtText(\"italic default value\") ]\n"
				+ "      )\n"
				+ "      Newline(\"\\n\")\n"
				+ "      WtTemplate(\n"
				+ "        Properties:\n"
				+ "          {N} precededByNewline = true\n"
				+ "          {N} rtd = RTD[ \"{{\" <o> \"\" <o> \"}}\" ]\n"
				+ "\n"
				+ "        [ WtText(\"some template\") ]\n"
				+ "        [\n"
				+ "          WtTemplateArgument(\n"
				+ "            Properties:\n"
				+ "              {N} hasName = false\n"
				+ "              {N} rtd = RTD[ \"|\" <o> \"\" <o> \"\" ]\n"
				+ "\n"
				+ "            [ ]\n"
				+ "            [\n"
				+ "              WtInternalLink(\n"
				+ "                Properties:\n"
				+ "                  {N} postfix = \"\"\n"
				+ "                  {N} prefix = \"\"\n"
				+ "                  {N} rtd = RTD[ \"[[some link\" <o> \"]]\" ]\n"
				+ "                  {N} target = \"some link\"\n"
				+ "\n"
				+ "                WtLinkTitle([ ])\n"
				+ "              )\n"
				+ "            ]\n"
				+ "          )\n"
				+ "        ]\n"
				+ "      )\n"
				+ "    ])\n"
				+ "  ])\n"
				+ ")\n";
		
		assertEquals(expected, printedAst);
	}
	
	// =========================================================================
	
	protected final class ParameterToDefaultValueResolver
			extends
				AstVisitor
	{
		private PageId pageId;
		
		private WtEntityMap entityMap;
		
		private List<Warning> warnings;
		
		// =====================================================================
		
		public ParameterToDefaultValueResolver(PageId pageId)
		{
			this.pageId = pageId;
		}
		
		// =====================================================================
		
		public WtNode visit(WtNode n)
		{
			mapInPlace(n);
			return n;
		}
		
		public WtNode visit(CompiledPage n)
		{
			this.warnings = n.getWarnings();
			this.entityMap = n.getEntityMap();
			mapInPlace(n);
			return n;
		}
		
		public WtNode visit(WtTemplateParameter n) throws CompilerException
		{
			WtTemplateArgument defValArg = n.getDefaultValue();
			if (defValArg == null)
				return n;
			
			WtNodeList defVal = defValArg.getValue();
			
			// Shortcut for all those empty default values
			if (defVal.isEmpty())
				return defValArg;
			
			WtPreproWikitextPage pprAst = new WtPreproWikitextPage(
					defVal, warnings, entityMap);
			
			CompiledPage parsed = compiler.postprocessPpOrExpAst(pageId, pprAst);
			
			WtNodeList content = parsed.getPage().getContent();
			
			// The parser of course thinks that the given wikitext is a 
			// individual page and will wrap even single line text into a 
			// paragraph node. We try to catch at least simple cases to improve
			// the resulting AST
			if (content.size() == 1 && content.get(0).getNodeType() == AstNodeTypes.NT_PARAGRAPH)
				content = ((WtParagraph) content.get(0)).getContent();
			
			return content;
		}
	}
	
}
