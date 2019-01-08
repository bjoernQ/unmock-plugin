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

import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.format.DateUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.mobilej.ABridge;
import libcore.icu.LocaleData;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * For some simple helper we need a complex mock setup.
 * <p>
 * Here we use DateUtils.formatElapsedTime which internally uses libcore.icu and needs some strings
 * from system resources to work.
 * <p>
 * The ICU setup is somewhat problematic and here it comes handy that the plugin will change every
 * native method to call de.mobilej.ABridge which can be easily mocked.
 * <p>
 * Created by bjorn on 16.02.15.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({Resources.class, ABridge.class})
public class ComplexSetupNeededTest {

    @Test
    public void testFormatElapsedTime() {
        mockStatic(ABridge.class);

        // ABridge.callBoolean is called by the patched ICU class
        // with the parameters:
        // Signature of the original method
        // "this" (since we are called from a static method it is null)
        // array of original method's parameters
        when(ABridge.callBoolean(
                eq("libcore.icu.ICU.initLocaleDataImpl(java.lang.String,libcore.icu.LocaleData)"),
                isNull(), any(Object[].class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                LocaleData ld = (LocaleData) ((Object[]) (invocation.getArguments()[2]))[1];

                ld.amPm = new String[]{"AM", "PM"};
                ld.NaN = "NaN";
                ld.currencyPattern = "¤#,##0.00";
                ld.currencySymbol = "$";
                ld.eras = new String[]{"BC", "AD"};
                ld.exponentSeparator = "E";
                ld.firstDayOfWeek = 1;
                ld.fullDateFormat = "EEEE, MMMM d, y";
                ld.fullTimeFormat = "h:mm:ss a zzzz";
                ld.infinity = "∞";
                ld.integerPattern = "#,##0";
                ld.internationalCurrencySymbol = "USD";
                ld.longDateFormat = "MMMM d, y";
                ld.longMonthNames = new String[]{"January", "February", "March", "April", "May",
                        "June", "July", "August", "September", "October", "November", "December"};
                ld.longStandAloneMonthNames = new String[]{"January", "February", "March", "April",
                        "May", "June", "July", "August", "September", "October", "November",
                        "December"};
                ld.longStandAloneWeekdayNames = new String[]{"", "Sunday", "Monday", "Tuesday",
                        "Wednesday", "Thursday", "Friday", "Saturday"};
                ld.longTimeFormat = "h:mm:ss a z";
                ld.longWeekdayNames = new String[]{"", "Sunday", "Monday", "Tuesday", "Wednesday",
                        "Thursday", "Friday", "Saturday"};
                ld.mediumDateFormat = "MMM d, y";
                ld.mediumTimeFormat = "h:mm:ss a";
                ld.minimalDaysInFirstWeek = 1;
                ld.minusSign = "-";
                // ld.narrowAm = "a";
                // ld.narrowPm = "p";
                ld.numberPattern = "#,##0.###";
                ld.percentPattern = "#,##0%";
                ld.shortDateFormat = "M/d/yy";
                // ld.shortDateFormat4 = "M/d/y";
                ld.shortMonthNames = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                        "Aug", "Sep", "Oct", "Nov", "Dec"};
                ld.shortStandAloneMonthNames = new String[]{"Jan", "Feb", "Mar", "Apr", "May",
                        "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                ld.shortStandAloneWeekdayNames = new String[]{"", "Sun", "Mon", "Tue", "Wed", "Thu",
                        "Fri", "Sat"};
                ld.shortTimeFormat = "h:mm a";
                ld.shortWeekdayNames = new String[]{"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri",
                        "Sat"};
                //ld.timeFormat12 = "h:mm a";
                //ld.timeFormat24 = "HH:mm";
                ld.tinyMonthNames = new String[]{"J", "F", "M", "A", "M", "J", "J", "A", "S", "O",
                        "N", "D"};
                ld.tinyStandAloneMonthNames = new String[]{"J", "F", "M", "A", "M", "J", "J", "A",
                        "S", "O", "N", "D"};
                ld.tinyStandAloneWeekdayNames = new String[]{"", "S", "M", "T", "W", "T", "F", "S"};
                ld.tinyWeekdayNames = new String[]{"", "S", "M", "T", "W", "T", "F", "S"};
                ld.today = "Today";
                ld.tomorrow = "Tomorrow";
                ld.yesterday = "Yesterday";
                ld.decimalSeparator = '.';
                ld.groupingSeparator = ',';
                ld.monetarySeparator = '.';
                ld.patternSeparator = ';';
                ld.perMill = '‰';
                ld.percent = "%";
                ld.zeroDigit = '0';

                return true;
            }
        });

        Resources mockResources = mock(Resources.class);
        Configuration mockConfiguration = new Configuration();
        when(mockResources.getConfiguration()).thenReturn(mockConfiguration);

        when(mockResources
                .getString(eq(com.android.internal.R.string.elapsed_time_short_format_mm_ss)))
                .thenReturn(
                        "%1$02d:%2$02d");

        when(mockResources
                .getString(eq(com.android.internal.R.string.time_of_day)))
                .thenReturn(
                        "%H:%M:%S");
        when(mockResources
                .getString(eq(com.android.internal.R.string.month_day_year)))
                .thenReturn(
                        "%-e. %B %Y");

        when(mockResources
                .getString(eq(com.android.internal.R.string.date_and_time)))
                .thenReturn(
                        "%1$s, %2$s");

        when(mockResources
                .getString(eq(com.android.internal.R.string.elapsed_time_short_format_h_mm_ss)))
                .thenReturn("%1$d:%2$02d:%3$02d");

        when(mockResources
                .getString(eq(com.android.internal.R.string.elapsed_time_short_format_mm_ss)))
                .thenReturn("%1$02d:%2$02d");

        when(mockResources
                .getString(eq(com.android.internal.R.string.time_of_day)))
                .thenReturn("%H:%M:%S");
        when(mockResources
                .getString(eq(com.android.internal.R.string.month_day_year)))
                .thenReturn("%-e. %B %Y");
        when(mockResources
                .getString(eq(com.android.internal.R.string.date_and_time)))
                .thenReturn("%1$s, %2$s");

        mockStatic(Resources.class);
        when(Resources.getSystem()).thenReturn(mockResources);

        // after a quite complex setup we can finally call DateUtils.formatElapsedTime
        // in a real world use case we would move the setup a static method in a helper class
        assertEquals("33:20", DateUtils.formatElapsedTime(2000));
    }
}
