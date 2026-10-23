package com.potcounter.combat;

import java.util.Objects;
import java.util.UUID;

public final class CombatPair {
    private final UUID u1;
    private final UUID u2;

    public CombatPair(UUID a, UUID b) {
        if (a.compareTo(b) <= 0) {
            this.u1 = a;
            this.u2 = b;
        } else {
            this.u1 = b;
            this.u2 = a;
        }
    }

    public UUID getU1() { return u1; }
    public UUID getU2() { return u2; }

    public boolean contains(UUID uuid) {
        return u1.equals(uuid) || u2.equals(uuid);
    }

    public UUID getOther(UUID uuid) {
        if (u1.equals(uuid)) return u2;
        if (u2.equals(uuid)) return u1;
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CombatPair that = (CombatPair) o;
        return Objects.equals(u1, that.u1) && Objects.equals(u2, that.u2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(u1, u2);
    }

    @Override
    public String toString() {
        return "CombatPair{" + u1 + " <-> " + u2 + "}";
    }
}
