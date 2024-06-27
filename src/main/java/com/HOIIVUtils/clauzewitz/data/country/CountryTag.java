package com.HOIIVUtils.clauzewitz.data.country;

import com.HOIIVUtils.clauzewitz.script.PDXScript;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CountryTag extends PDXScript<String> implements Comparable<CountryTag> {
    public static final CountryTag NULL_TAG = new CountryTag("###");
    public static final int COUNTRY_TAG_LENGTH = 3;         // standard country tag length (for a normal country tag)private final String tag;

    public CountryTag() {
        super("tag");
    }

    public CountryTag(String tag) {
        super("tag");
        set(tag);
    }

//    public CountryTag(String tag) {
//        if (tag == null || tag.isEmpty()) {
//            tag = NULL_TAG.tag;
//        }
//        this.tag = tag;
//    }
//
//
//    @Override
//    public String toString() {
//        return tag;
//    }
//
//    // todo make this adhere to equals implementation requirements
//    @Override
//    public boolean equals(Object other) {
//        if (other.getClass() == this.getClass()) {
//            return this.tag.equals(((CountryTag) other).tag);
//        } else if (other.getClass() == String.class) {
//            return this.tag.equals(other);
//        }
//
//        return false;
//    }
//
    @Override
    public int compareTo(@NotNull CountryTag o) {
        return this.get().compareTo(o.get());
    }

//
//    public String tag() {
//        return tag;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(tag);
//    }

}
