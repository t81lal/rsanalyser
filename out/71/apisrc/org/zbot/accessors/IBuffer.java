package org.zbot.accessors;

import java.math.BigInteger;

public interface IBuffer {

    byte[] getPayload();

    int getCaret();

    void enableEncryption(BigInteger biginteger, int i);
}
