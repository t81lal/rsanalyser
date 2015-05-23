package org.zbot.accessors;

import java.math.BigInteger;

public interface IBuffer {

    int getCaret();

    byte[] getPayload();

    void enableEncryption(BigInteger biginteger, int i);
}
