package clausewitz_coding.country;

public final class CountryTag {
    public static final CountryTag NULL_TAG = new CountryTag("###");
    private final String tag;

    public CountryTag(String tag) {
        if (tag == null || tag.equals("")) {
            tag = NULL_TAG.tag;
        }

        this.tag = tag;
    }

    public String toString() {
        return tag;
    }

    public boolean equals(Object other) {
        if (other.getClass() == this.getClass()) {
            return this.tag.equals(((CountryTag) other).tag);
        }

        return false;
    }
}
