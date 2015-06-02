package de.mobilej.testproject;

import android.net.Uri;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Makes sure that renaming a class works.
 * <p/>
 * Here we test android.net.Uri which depends on java.nio.charset.Charsets internally.
 * This class isn't part of Java SE so we need to get it from the real Android.jar.
 * However since it's located in the Java package we cannot load it from the regular classpath.
 * See the build.gradle of this example to see how java.nio.charset.Charsets is renamed.
 * <p/>
 * Created by bjoernquentin on 02.06.15.
 */
public class RenamedClassNeededTest {

    @Test
    public void testUri() throws Exception {
        Uri uri = Uri.parse("http://www.google.de?q=Unmock+Plugin");

        assertEquals("Unmock Plugin", uri.getQueryParameter("q"));
    }
}
