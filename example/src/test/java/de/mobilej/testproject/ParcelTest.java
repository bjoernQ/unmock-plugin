package de.mobilej.testproject;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Shows how you can test Parcel
 * IMPROVE rewrite to mock de.mobilej.ABridge's methods
 * <p>
 * Created by bjoern on 25.12.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Parcel.class)
public class ParcelTest {
    private Parcel parcel;

    @Before
    public void setup() {
        Parcel mockParcel = fakeParcel();
        mockStatic(Parcel.class);
        when(Parcel.obtain()).thenReturn(mockParcel);

        parcel = Parcel.obtain();
    }

    @Test
    public void testObtain() {
        assertThat(parcel, notNullValue());
    }

    @Test
    public void testReadIntWhenEmpty() {
        assertThat(parcel.readInt(), equalTo(0));
    }

    @Test
    public void testReadLongWhenEmpty() {
        assertThat(parcel.readLong(), equalTo(0L));
    }

    @Test
    public void testReadStringWhenEmpty() {
        assertThat(parcel.readString(), nullValue());
    }

    @Test
    public void testReadWriteSingleString() {
        String val = "test";
        parcel.writeString(val);
        assertThat(parcel.readString(), equalTo(val));
    }

    @Test
    public void testWriteNullString() {
        parcel.writeString(null);
        assertThat(parcel.readString(), nullValue());
    }

    @Test
    public void testReadWriteMultipleStrings() {
        for (int i = 0; i < 10; ++i) {
            parcel.writeString(Integer.toString(i));
        }
        for (int i = 0; i < 10; ++i) {
            assertThat(parcel.readString(), equalTo(Integer.toString(i)));
        }
        // now try to read past the number of items written and see what happens
        assertThat(parcel.readString(), nullValue());
    }

    @Test
    public void testReadWriteSingleInt() {
        int val = 5;
        parcel.writeInt(val);
        assertThat(parcel.readInt(), equalTo(val));
    }

    @Test
    public void testReadWriteMultipleInts() {
        for (int i = 0; i < 10; ++i) {
            parcel.writeInt(i);
        }
        for (int i = 0; i < 10; ++i) {
            assertThat(parcel.readInt(), equalTo(i));
        }
        // now try to read past the number of items written and see what happens
        assertThat(parcel.readInt(), equalTo(0));
    }

    @Test
    public void testReadWriteStringInt() {
        for (int i = 0; i < 10; ++i) {
            parcel.writeString(Integer.toString(i));
            parcel.writeInt(i);
        }
        for (int i = 0; i < 10; ++i) {
            assertThat(parcel.readString(), equalTo(Integer.toString(i)));
            assertThat(parcel.readInt(), equalTo(i));
        }
        // now try to read past the number of items written and see what happens
        assertThat(parcel.readString(), nullValue());
        assertThat(parcel.readInt(), equalTo(0));
    }

    @Test(expected = ClassCastException.class)
    public void testWriteStringReadInt() {
        String val = "test";
        parcel.writeString(val);
        parcel.readInt();
    }

    @Test(expected = ClassCastException.class)
    public void testWriteIntReadString() {
        int val = 9;
        parcel.writeInt(val);
        parcel.readString();
    }

    @Test
    public void testReadWriteSingleLong() {
        long val = 5;
        parcel.writeLong(val);
        assertThat(parcel.readLong(), equalTo(val));
    }

    @Test
    public void testReadWriteMultipleLongs() {
        for (long i = 0; i < 10; ++i) {
            parcel.writeLong(i);
        }
        for (long i = 0; i < 10; ++i) {
            assertThat(parcel.readLong(), equalTo(i));
        }
        // now try to read past the number of items written and see what happens
        assertThat(parcel.readLong(), equalTo(0L));
    }

    @Test
    public void testReadWriteStringLong() {
        for (long i = 0; i < 10; ++i) {
            parcel.writeString(Long.toString(i));
            parcel.writeLong(i);
        }
        for (long i = 0; i < 10; ++i) {
            assertThat(parcel.readString(), equalTo(Long.toString(i)));
            assertThat(parcel.readLong(), equalTo(i));
        }
        // now try to read past the number of items written and see what happens
        assertThat(parcel.readString(), nullValue());
        assertThat(parcel.readLong(), equalTo(0L));
    }

    @Test(expected = ClassCastException.class)
    public void testWriteStringReadLong() {
        String val = "test";
        parcel.writeString(val);
        parcel.readLong();
    }

    @Test(expected = ClassCastException.class)
    public void testWriteLongReadString() {
        long val = 9;
        parcel.writeLong(val);
        parcel.readString();
    }

    public static Parcel fakeParcel() {
        final ArrayList<Object> data = new ArrayList<>();
        final int[] indexHolder = new int[]{0};

        Parcel parcel = mock(Parcel.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String method = invocation.getMethod().getName();

                if (method.startsWith("write")) {
                    data.add(invocation.getArguments()[0]);
                } else if (method.startsWith("read")) {
                    if (data.size() > indexHolder[0]) {
                        return data.get(indexHolder[0]++);
                    } else {
                        return null;
                    }
                }

                return null;
            }
        });

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return data.get(indexHolder[0]++);
            }
        }).when(parcel).createByteArray();
        return parcel;
    }

}
