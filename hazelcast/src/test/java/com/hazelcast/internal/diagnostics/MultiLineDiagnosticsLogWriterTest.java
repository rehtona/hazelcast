package com.hazelcast.internal.diagnostics;

import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Locale;

import static com.hazelcast.util.StringUtil.LINE_SEPARATOR;
import static org.junit.Assert.assertEquals;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class MultiLineDiagnosticsLogWriterTest extends HazelcastTestSupport {

    protected DiagnosticsLogWriter writer;
    private CharArrayWriter out = new CharArrayWriter();

    @Before
    public void setupLogWriter() {
        writer = new MultiLineDiagnosticsLogWriter();
        writer.init(new PrintWriter(out));
    }

    // test for https://github.com/hazelcast/hazelcast/issues/9085
    @Test
    public void writeKeyValueEntry_nullValue() {
        writer.startSection("SomeSection");
        writer.writeKeyValueEntry("s", null);
        writer.endSection();

        String actual = out.toString();

        // we need to get rid of the time/date prefix
        actual = actual.substring(actual.indexOf("SomeSection"));

        assertEquals("" +
                "SomeSection[" + LINE_SEPARATOR +
                "                          s=null]" + LINE_SEPARATOR, actual);
    }

    @Test
    public void test() {
        writer.startSection("SomeSection");
        writer.writeKeyValueEntry("boolean", true);
        writer.writeKeyValueEntry("long", 10L);
        writer.startSection("SubSection");
        writer.writeKeyValueEntry("integer", 10);
        writer.endSection();
        writer.writeKeyValueEntry("string", "foo");
        writer.writeKeyValueEntry("double", 11d);
        writer.writeEntry("foobar");
        writer.endSection();

        String actual = out.toString();

        // we need to get rid of the time/date prefix
        actual = actual.substring(actual.indexOf("SomeSection"));

        assertEquals("" +
                "SomeSection[" + LINE_SEPARATOR +
                "                          boolean=true" + LINE_SEPARATOR +
                "                          long=10" + LINE_SEPARATOR +
                "                          SubSection[" + LINE_SEPARATOR +
                "                                  integer=10]" + LINE_SEPARATOR +
                "                          string=foo" + LINE_SEPARATOR +
                "                          double=11.0" + LINE_SEPARATOR +
                "                          foobar]" + LINE_SEPARATOR, actual);
    }

    @Test
    public void writeLong() {
        assertLongValue(0);
        assertLongValue(10);
        assertLongValue(100);
        assertLongValue(1000);
        assertLongValue(10000);
        assertLongValue(100000);
        assertLongValue(1000000);
        assertLongValue(10000000);
        assertLongValue(100000000);
        assertLongValue(1000000000);
        assertLongValue(10000000000L);
        assertLongValue(100000000000L);
        assertLongValue(1000000000000L);
        assertLongValue(10000000000000L);
        assertLongValue(100000000000000L);
        assertLongValue(1000000000000000L);
        assertLongValue(10000000000000000L);
        assertLongValue(100000000000000000L);
        assertLongValue(1000000000000000000L);

        assertLongValue(-10);
        assertLongValue(-100);
        assertLongValue(-1000);
        assertLongValue(-10000);
        assertLongValue(-100000);
        assertLongValue(-1000000);
        assertLongValue(-10000000);
        assertLongValue(-100000000);
        assertLongValue(-1000000000);
        assertLongValue(-10000000000L);
        assertLongValue(-100000000000L);
        assertLongValue(-1000000000000L);
        assertLongValue(-10000000000000L);
        assertLongValue(-100000000000000L);
        assertLongValue(-1000000000000000L);
        assertLongValue(-10000000000000000L);
        assertLongValue(-100000000000000000L);
        assertLongValue(-1000000000000000000L);

        assertLongValue(1);
        assertLongValue(345);
        assertLongValue(83883);
        assertLongValue(1222333);
        assertLongValue(11122233);
        assertLongValue(111222334);
        assertLongValue(1112223344);

        assertLongValue(-1);
        assertLongValue(-345);
        assertLongValue(-83883);
        assertLongValue(-1222333);
        assertLongValue(-11122233);
        assertLongValue(-111222334);
        assertLongValue(-1112223344);

        assertLongValue(Integer.MIN_VALUE);
        assertLongValue(Integer.MAX_VALUE);
        assertLongValue(Long.MIN_VALUE);
        assertLongValue(Long.MAX_VALUE);
    }

    private void assertLongValue(long value) {
        CharArrayWriter out = new CharArrayWriter();
        MultiLineDiagnosticsLogWriter writer = new MultiLineDiagnosticsLogWriter();
        writer.init(new PrintWriter(out));

        writer.writeLong(value);

        String expected = String.format(Locale.US, "%,d", value);

        assertEquals(expected, out.toString());
    }
}
