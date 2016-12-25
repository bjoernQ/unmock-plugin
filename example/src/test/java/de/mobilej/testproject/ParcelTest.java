package de.mobilej.testproject;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import de.mobilej.ABridge;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Shows how you can test Parcel
 * <p>
 * Created by bjoern on 25.12.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ABridge.class)
public class ParcelTest {
    private Parcel parcel;

    @Before
    public void setup() {
        MockParcel();

        parcel = Parcel.obtain();
    }

    @Test
    public void testTwoParcels() {
        Parcel p1 = Parcel.obtain();
        Parcel p2 = Parcel.obtain();

        p1.writeInt(42);
        p2.writeInt(23);

        assertThat(p2.readInt(), equalTo(23));
        assertThat(p1.readInt(), equalTo(42));
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

    /**
     * Uses PowerMock to setup mocking/fakeing of android.os.Parcel
     * <p>
     * Incomplete implementation only supports read/write int,long,String just to show how it can be done.
     */
    public static void MockParcel() {
        final HashMap<Long, ByteBufferWrapper> data = new HashMap<>();
        final long fakeNativePtr[] = new long[]{1};

        final Answer defaultAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String method = (String) invocation.getArguments()[0];
                Object[] realParams = (Object[]) invocation.getArguments()[2];

                if (method.startsWith("android.os.Parcel.nativeWrite")) {
                    ByteBufferWrapper buffer = data.get(realParams[0]);
                    if (buffer == null) {
                        buffer = new ByteBufferWrapper();
                        data.put((Long) realParams[0], buffer);
                    }

                    if (method.contains("Int")) {
                        buffer.putInt((Integer) realParams[1]);
                    } else if (method.contains("String")) {
                        buffer.putString((String) realParams[1]);
                    } else if (method.contains("Long")) {
                        buffer.putLong((Long) realParams[1]);
                    }
                } else if (method.startsWith("android.os.Parcel.nativeRead")) {
                    ByteBufferWrapper buffer = data.get(realParams[0]);
                    if (buffer != null) {
                        if (method.contains("Int")) {
                            return buffer.getInt();
                        } else if (method.contains("String")) {
                            return buffer.getString();
                        } else if (method.contains("Long")) {
                            return buffer.getLong();
                        }
                    }
                } else if (method.startsWith("android.os.Parcel.nativeCreate()")) {
                    return fakeNativePtr[0]++;
                }

                return null;
            }
        };

        mockStatic(ABridge.class, defaultAnswer);
    }

    public static class ByteBufferWrapper {
        private static final int TYPE_INT = 1;
        private static final int TYPE_STRING = 2;
        private static final int TYPE_LONG = 3;

        private ByteBuffer buffer = ByteBuffer.allocate(500000);
        private ByteBuffer readBuffer = ByteBuffer.wrap(buffer.array());

        public int getInt() {
            if (checkType(TYPE_INT)) return 0;
            return readBuffer.getInt();
        }

        public void putInt(int v) {
            buffer.putInt(TYPE_INT);
            buffer.putInt(v);
        }

        public long getLong() {
            if (checkType(TYPE_LONG)) return 0;
            return readBuffer.getLong();
        }

        public void putLong(long v) {
            buffer.putInt(TYPE_LONG);
            buffer.putLong(v);
        }

        public String getString() {
            if (checkType(TYPE_STRING)) return null;
            int l = readBuffer.getInt();
            if (l == -1) {
                return null;
            }
            byte[] bytes = new byte[l];
            readBuffer.get(bytes);
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public void putString(String v) {
            buffer.putInt(TYPE_STRING);
            if (v == null) {
                buffer.putInt(-1);
                return;
            }
            byte[] bytes = new byte[0];
            try {
                bytes = v.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }

        private boolean checkType(int type) {
            int t = readBuffer.getInt();
            if (t == 0) {
                return true;
            }
            if (t != type) {
                throw new ClassCastException();
            }
            return false;
        }
    }
}