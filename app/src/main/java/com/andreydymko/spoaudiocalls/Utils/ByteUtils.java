package com.andreydymko.spoaudiocalls.Utils;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

public class ByteUtils {
    public final static int INT_BYTES = Integer.SIZE/Byte.SIZE;
    public final static int LONG_BYTES = Long.SIZE/Byte.SIZE;
    public final static int UUID_BYTES = LONG_BYTES * 2;

    public static byte getBoolBytes(boolean bool) {
        return (byte) (bool ? 1 : 0);
    }

    public static int getInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte[] getIntBytes(int integer) {
        return ByteBuffer.wrap(new byte[INT_BYTES]).putInt(integer).array();
    }

    public static UUID getUUID(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    public static byte[] getUUIDBytes(UUID uuid) {
        return ByteBuffer.wrap(new byte[UUID_BYTES])
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }

    public static byte[] getUUIDsBytes(Collection<UUID> uuids) {
        byte[] res = new byte[uuids.size() * ByteUtils.UUID_BYTES];
        Iterator<UUID> uuidsIterator = uuids.iterator();
        for (int i = 0; i < res.length && uuidsIterator.hasNext(); i += ByteUtils.UUID_BYTES) {
            System.arraycopy(ByteUtils.getUUIDBytes(uuidsIterator.next()), 0, res, i, ByteUtils.UUID_BYTES);
        }
        return res;
    }

    public static byte[] concatAll(byte b, byte[]... bytes) {
        int totalLength = 1;
        for (byte[] array : bytes) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];
        result[0] = b;

        int offset = 1;
        for (byte[] array : bytes) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
