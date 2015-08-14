/*
   Copyright (C) 2015 Björn Quentin

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package de.mobilej.examplelib;

import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.JsonWriter;
import android.util.SparseIntArray;
import android.util.Xml;

import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.io.StringWriter;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Some simple tests which prove that the "unmocked" Android classes are actually working.
 * <p/>
 * Created by bjorn on 12.02.15.
 */

public class SimpleTest {

    @Test
    public void testBase64() {
        assertEquals("Hello World", new String(Base64.decode(
                Base64.encodeToString("Hello World".getBytes(), Base64.NO_WRAP), Base64.NO_WRAP)));
    }

    @Test
    public void testContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("foo", "bar");
        assertTrue(cv.containsKey("foo"));
    }

    @Test
    public void testEditable() {
        Editable e = Editable.Factory.getInstance().newEditable("Hello World");
        e.append("!!!!");

        assertEquals("Hello World!!!!", e.toString());
    }

    @Test
    public void testIntent() {
        Intent i = new Intent("TEST");

        assertEquals("TEST", i.getAction());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(TextUtils.isEmpty(""));
    }

    @Test
    public void testJsonWriter() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        jw.beginObject();
        jw.name("test");
        jw.value("world");
        jw.endObject();
        jw.flush();

        assertEquals("{\"test\":\"world\"}", sw.toString());

    }

    @Test
    public void testLocation() {
        Location loc = new Location("GPS");
        loc.setLongitude(50);
        loc.setLatitude(8);

        Location loc2 = new Location("GPS");
        loc2.setLongitude(50);
        loc2.setLatitude(9);

        assertEquals(110598.56, loc.distanceTo(loc2), 0.5);
    }

    @Test
    public void testLocationManagerIsNotTaken() {
        // will raise an error on a copied LocationManager
        mock(LocationManager.class);
    }

    @Test
    public void testPullParser() throws Exception {
        XmlPullParser pp = Xml.newPullParser();
        pp.setInput(new StringReader("<a/>"));
        pp.nextTag();

        assertEquals("a", pp.getName());

    }

    @Test
    public void testSpannable() {
        final Spannable text = new SpannableString("Test http://www.test.de !");
        Linkify.addLinks(text, Linkify.WEB_URLS);
        assertEquals(
                "<p dir=\"ltr\">Test <a href=\"http://www.test.de\">http://www.test.de</a> !</p>\n",
                Html.toHtml(text));
    }

    @Test
    public void testSparseIntArray() {
        SparseIntArray sia = new SparseIntArray();
        sia.put(12, 999);
        sia.put(99, 12);

        assertEquals(999, sia.get(12));
    }

}
