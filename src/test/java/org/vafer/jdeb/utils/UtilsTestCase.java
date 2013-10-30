/*
 * Copyright 2013 The jdeb developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vafer.jdeb.utils;


import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class UtilsTestCase extends TestCase {

    public void testStripPath() throws Exception {
        assertEquals("foo/bar", Utils.stripPath(0, "foo/bar"));

        assertEquals("bar", Utils.stripPath(1, "foo/bar"));

        assertEquals("bar/baz", Utils.stripPath(1, "foo/bar/baz"));
        assertEquals("baz", Utils.stripPath(2, "foo/bar/baz"));

        assertEquals("foo/", Utils.stripPath(0, "foo/"));
        assertEquals("", Utils.stripPath(1, "foo/"));
        assertEquals("foo/", Utils.stripPath(2, "foo/"));
    }

    private String convert(String s) throws Exception {
        byte[] data = Utils.toUnixLineEndings(new ByteArrayInputStream(s.getBytes("UTF-8")));
        return new String(data, "UTF-8");
    }

    public void testNewlineConversionLF() throws Exception {
        String expected = "test\ntest\n\ntest\n";
        String actual = convert("test\ntest\n\ntest");
        assertEquals(expected, actual);
    }

    public void testNewlineConversionCRLF() throws Exception {
        String expected = "test\ntest\n\ntest\n";
        String actual = convert("test\r\ntest\r\n\r\ntest");
        assertEquals(expected, actual);
    }

    public void testNewlineConversionCR() throws Exception {
        String expected = "test\ntest\n\ntest\n";
        String actual = convert("test\rtest\r\rtest");
        assertEquals(expected, actual);
    }

    public void testReplaceVariables() {
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("version", "1.2.3");
        variables.put("name", "jdeb");
        variables.put("url", "https://github.com/tcurdt/jdeb");

        VariableResolver resolver = new MapVariableResolver(variables);

        // main case
        String result = Utils.replaceVariables(resolver, "Version: [[version]]", "[[", "]]");
        assertEquals("Version: 1.2.3", result);

        // multiple variables in the same expression
        result = Utils.replaceVariables(resolver, "[[name]] [[version]]", "[[", "]]");
        assertEquals("jdeb 1.2.3", result);

        // collision with script syntax
        result = Utils.replaceVariables(resolver, "if [[ \"${HOST_TYPE}\" -eq \"admin\" ]] ; then", "[[", "]]");
        assertEquals("if [[ \"${HOST_TYPE}\" -eq \"admin\" ]] ; then", result);

        // mixed valid and unknown variables
        result = Utils.replaceVariables(resolver, "[[name]] [[test]]", "[[", "]]");
        assertEquals("jdeb [[test]]", result);
    }

    public void testVersionConversion() {
        Calendar cal = new GregorianCalendar(2013, Calendar.FEBRUARY, 17);
        assertEquals("should match", "1.0", Utils.convertToDebianVersion("1.0", null));
        assertEquals("should match", "1.0~SNAPSHOT", Utils.convertToDebianVersion("1.0+SNAPSHOT", null));
        assertEquals("should match", "1.0~SNAPSHOT", Utils.convertToDebianVersion("1.0-SNAPSHOT", null));
        assertEquals("should match", "1.0~20130217000000", Utils.convertToDebianVersion("1.0+SNAPSHOT", cal.getTime()));
        assertEquals("should match", "1.0~RC2", Utils.convertToDebianVersion("1.0-RC2", cal.getTime()));
        assertEquals("should match", "1.0~alpha3", Utils.convertToDebianVersion("1.0-alpha3", cal.getTime()));
        assertEquals("should match", "1.0~Beta+4", Utils.convertToDebianVersion("1.0.Beta-4", cal.getTime()));
        assertEquals("should match", "1.0~rc7", Utils.convertToDebianVersion("1.0rc7", cal.getTime()));
    }
}
