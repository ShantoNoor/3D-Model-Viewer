package com.modelviewer.Tests.NuklearTest;

import org.lwjgl.nuklear.NkContext;

public abstract class NuklearLayer {
    protected NkContext ctx;

    public NuklearLayer(NkContext ctx){
        this.ctx = ctx;
    }

    abstract public void layout(int x, int y);
}
