package hoi4_localization.country;

public final class CountryTag {
    private final String tag;

    public CountryTag(String tag) {
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
