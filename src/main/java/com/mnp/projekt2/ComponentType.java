package com.mnp.projekt2;

/**
 * Enumeriert die Basisteile (BBx) und elektronischen Bauteile (EBx).
 */
public enum ComponentType {
    EB1, EB2, EB3, EB4, EB5,
    BB1, BB2, BB3, BB4;

    /**
     * Prüft, ob es sich um ein Elektronisches Bauteil handelt (EBx).
     */
    public boolean isElectronic() {
        return this.name().startsWith("EB");
    }

    /**
     * Prüft, ob es sich um einen Basisbaustein handelt (BBx).
     */
    public boolean isBasePart() {
        return this.name().startsWith("BB");
    }
}