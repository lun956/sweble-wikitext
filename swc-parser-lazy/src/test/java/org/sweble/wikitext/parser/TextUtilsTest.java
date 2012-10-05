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
package org.sweble.wikitext.parser;

import org.junit.Assert;
import org.junit.Test;
import org.sweble.wikitext.parser.utils.TextUtils;
import org.sweble.wikitext.parser.utils.WtPrinter;

public class TextUtilsTest
{
	@Test
	public void testStringToAst()
	{
		final String actual =
				WtPrinter.print(TextUtils.stringToAst("H&llo <Welt>!"));
		
		final String expected =
				"[\n" +
						"  WtText(\"H\")\n" +
						"  WtXmlEntityRef(\n" +
						"    Properties:\n" +
						"      {N} name = \"amp\"\n" +
						"      {N} resolved = \"&\"\n" +
						"      {N} rtd = RTD[ \"&amp;\" ]\n" +
						"  )\n" +
						"  WtText(\"llo \")\n" +
						"  WtXmlEntityRef(\n" +
						"    Properties:\n" +
						"      {N} name = \"lt\"\n" +
						"      {N} resolved = \"<\"\n" +
						"      {N} rtd = RTD[ \"&lt;\" ]\n" +
						"  )\n" +
						"  WtText(\"Welt\")\n" +
						"  WtXmlEntityRef(\n" +
						"    Properties:\n" +
						"      {N} name = \"gt\"\n" +
						"      {N} resolved = \">\"\n" +
						"      {N} rtd = RTD[ \"&gt;\" ]\n" +
						"  )\n" +
						"  WtText(\"!\")\n" +
						"]\n";
		
		Assert.assertEquals(expected, actual);
	}
}
