package com.mnp.projekt2;


/**
 * Stellt die Baupläne der elektronischen Bauteile zur Verfügung.
 */
public class ComponentBuilder {

    /**
     * Gibt die beiden Unterkomponenten zurück, aus denen das angegebene elektronische
     * Bauteil besteht. Für Basisbausteine wird ein leeres Array zurückgegeben.
     *
     * @param type Der Typ des elektronischen Bauteils oder Basisbausteins.
     * @return Ein Array der beiden Komponenten, aus denen das Teil besteht.
     */
    public static ComponentType[] getComponents(ComponentType type) {
        switch (type) {
            case EB1:
                return new ComponentType[]{ComponentType.BB1, ComponentType.BB2};
            case EB2:
                return new ComponentType[]{ComponentType.EB1, ComponentType.BB2};
            case EB3:
                return new ComponentType[]{ComponentType.BB3, ComponentType.EB2};
            case EB4:
                return new ComponentType[]{ComponentType.EB1, ComponentType.EB3};
            case EB5:
                return new ComponentType[]{ComponentType.EB4, ComponentType.EB1};
            default:
                // Basisbausteine haben keine weiteren Komponenten
                return new ComponentType[]{};
        }
    }
}
