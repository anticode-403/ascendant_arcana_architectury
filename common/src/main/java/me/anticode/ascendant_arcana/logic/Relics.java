package me.anticode.ascendant_arcana.logic;

public enum Relics {
    DAMAGE(0),
    DURABILITY(1),
    PROTECTION(2),
    HASTE(3),
    ENCHANTMENT_CAPACITY(4);

    private final int value;

    Relics(int relic) {
        this.value = relic;
    }

    public static Relics fromId(int value) {
        for (Relics r : values()) {
            if (r.value == value) return r;
        }
        return null;
    }

    public static int toId(Relics relic) {
        return relic.value;
    }
}
