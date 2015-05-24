package org.zbot.accessors;

import java.math.BigInteger;

public interface IBuffer {

    byte[] getPayload();

    int getCaret();

    void writeInvertedLEInt(int i);

    void writeLE24(int i, byte b0);

    void enableEncryption(BigInteger biginteger, byte b0);
}
