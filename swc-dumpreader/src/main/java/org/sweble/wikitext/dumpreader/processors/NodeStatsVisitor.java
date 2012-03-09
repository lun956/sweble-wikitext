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

package org.sweble.wikitext.dumpreader.processors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sweble.wikitext.engine.Page;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.utils.StringUtils;

public final class NodeStatsVisitor
		extends
			AstVisitor
{
	private final Map<String, Integer> nodeCounts =
			new HashMap<String, Integer>();
	
	private int varCount = 0;
	
	private int varEmptyCount = 0;
	
	private int varChildCount = 0;
	
	private int varMaxChildCount = -1;
	
	private int fixCount = 0;
	
	private int fixChildCount = 0;
	
	private int leafCount = 0;
	
	// =========================================================================
	
	public static String run(Page page)
	{
		return (String) new NodeStatsVisitor().go(page);
	}
	
	@Override
	protected Object after(AstNode node, Object result)
	{
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		out.println("Analysis:");
		
		List<String> nodeNames = new ArrayList<String>(nodeCounts.keySet());
		Collections.sort(nodeNames);
		
		int sum = 0;
		for (String name : nodeNames)
		{
			int count = nodeCounts.get(name);
			sum += count;
			
			String space = StringUtils.strrep('.', 80 - name.length());
			
			out.format("  %s %s %5d\n", name, space, count);
		}
		
		out.println("  " + StringUtils.strrep('=', 87));
		out.format("  %s%5d\n", StringUtils.strrep(' ', 82), sum);
		
		out.println();
		out.println("  Nodes with variable number of children: ");
		out.format("     Count:   %5d\n", varCount);
		out.format("     Leaf:    %5d\n", varEmptyCount);
		out.format("     Biggest: %5d\n", varMaxChildCount);
		out.format("     Average:    %5.2f\n", varChildCount / (float) (varCount - varEmptyCount));
		
		out.println();
		out.println("  Nodes with fixed number of children: ");
		out.format("     Inner:   %5d\n", fixCount);
		out.format("     Leafs:   %5d\n", leafCount);
		out.format("     Average:    %5.2f\n", fixChildCount / (float) fixCount);
		
		return super.after(node, writer.toString());
	}
	
	public void visit(AstNode n)
	{
		if (n != null)
		{
			countNode(n);
			
			int size = n.size();
			if (n.isList())
			{
				++varCount;
				varChildCount += size;
				if (n.isEmpty())
					++varEmptyCount;
				if (size > varMaxChildCount)
					varMaxChildCount = size;
			}
			else
			{
				if (!n.isEmpty())
				{
					++fixCount;
					fixChildCount += size;
				}
				else
				{
					++leafCount;
				}
			}
			
			iterate(n);
		}
	}
	
	private void countNode(AstNode n)
	{
		Class<? extends AstNode> clazz = n.getClass();
		String name = clazz.getName();
		
		Integer i = nodeCounts.get(name);
		if (i == null)
			i = 0;
		
		nodeCounts.put(name, i + 1);
	}
}