package com.hoi4utils.clausewitz.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PDXScriptList extends ArrayList<PDXScript<?>> {

    public PDXScriptList(PDXScript<?>... scripts) {
        super(List.of(scripts));
    }

    public PDXScriptList(Collection<PDXScript<?>> scripts) {
        super(scripts);
    }
}
