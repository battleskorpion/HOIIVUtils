package com.HOIIVUtils.clauzewitz.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PDXScriptList extends ArrayList<AbstractPDX<?>> {

    public PDXScriptList(AbstractPDX<?>... scripts) {
        super(List.of(scripts));
    }

    public PDXScriptList(Collection<AbstractPDX<?>> scripts) {
        super(scripts);
    }
}
