package org.zbot.accessors.collections;

public interface INode {

    INode getPrevious();

    INode getNext();

    long getKey();
}
