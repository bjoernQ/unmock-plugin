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
package de.mobilej.testproject;

import android.hardware.Sensor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import de.mobilej.ABridge;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Tests that callFloat is invoked in ABridge
 * Created by bjoern on 01.06.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ABridge.class)
public class DelegateClassWithFloatTest {

    @Test
    public void testCallFloat() throws Exception {
        Sensor sensor = Whitebox.newInstance(Sensor.class);

        mockStatic(ABridge.class);
        when(ABridge.callFloat(eq("android.hardware.Sensor.getPower()"), any(Object.class), any(Object[].class))).thenReturn(42f);

        assertEquals(42f, sensor.getPower(), 0f);
    }
}
