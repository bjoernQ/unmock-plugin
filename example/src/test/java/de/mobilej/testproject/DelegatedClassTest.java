package de.mobilej.testproject;

import android.content.res.Resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.mobilej.ABridge;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Makes sure that delegating all methods of a class works.
 * <p>
 * Here we test a subclass of android.os.AsyncTask.
 * <p>
 * Created by bjoern on 14.05.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Resources.class, ABridge.class})
public class DelegatedClassTest {

    @Test
    public void testDelegateClass() throws Exception {
        mockStatic(ABridge.class);

        final boolean[] called = new boolean[]{false};
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                called[0] = true;
                return null;
            }
        }).when(ABridge.class, "callObject", eq("android.os.AsyncTask.execute(java.lang.Object[])"), anyObject(), any(Object[].class));

        AsyncTaskSubclass sut = new AsyncTaskSubclass();
        sut.execute();

        assertTrue(called[0]);
    }
}
