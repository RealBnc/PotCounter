package com.potcounter.render;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class MarkerHelper {
    public static final ClickEvent POT_MARKER = new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "potcounter_marker");

    public static MutableText mark(MutableText text) {
        if (text == null) return null;
        return text.styled(style -> style.withClickEvent(POT_MARKER));
    }

    public static boolean hasMarker(Text text) {
        if (text == null) return false;
        Style style = text.getStyle();
        if (style != null && POT_MARKER.equals(style.getClickEvent())) {
            return true;
        }
        for (Text sibling : text.getSiblings()) {
            if (hasMarker(sibling)) {
                return true;
            }
        }
        return false;
    }
}
