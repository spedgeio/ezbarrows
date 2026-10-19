package com.ezbarrows;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Prayer;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarbitID;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum BarrowsBrother {
    AHRIM(ObjectID.BARROW_AHRIM_SARCOPHAGUS, Prayer.PROTECT_FROM_MAGIC, SpriteID.Prayeron.PROTECT_FROM_MAGIC, VarbitID.BARROWS_KILLED_AHRIM),
    DHAROK(ObjectID.BARROW_DHAROK_SARCOPHAGUS, Prayer.PROTECT_FROM_MELEE, SpriteID.Prayeron.PROTECT_FROM_MELEE, VarbitID.BARROWS_KILLED_DHAROK),
    GUTHAN(ObjectID.BARROW_GUTHAN_SARCOPHAGUS, Prayer.PROTECT_FROM_MELEE, SpriteID.Prayeron.PROTECT_FROM_MELEE, VarbitID.BARROWS_KILLED_GUTHAN),
    KARIL(ObjectID.BARROW_KARIL_SARCOPHAGUS, Prayer.PROTECT_FROM_MISSILES, SpriteID.Prayeron.PROTECT_FROM_MISSILES, VarbitID.BARROWS_KILLED_KARIL),
    TORAG(ObjectID.BARROW_TORAG_SARCOPHAGUS, Prayer.PROTECT_FROM_MELEE, SpriteID.Prayeron.PROTECT_FROM_MELEE, VarbitID.BARROWS_KILLED_TORAG),
    VERAC(ObjectID.BARROW_VERAC_SARCOPHAGUS, Prayer.PROTECT_FROM_MELEE, SpriteID.Prayeron.PROTECT_FROM_MELEE, VarbitID.BARROWS_KILLED_VERAC);

    private static final Map<Integer, BarrowsBrother> BY_SARCOPHAGUS = ImmutableMap.copyOf(
            Arrays.stream(values()).collect(Collectors.toMap(BarrowsBrother::getSarcophagusId, Function.identity())));

    private final int sarcophagusId;
    private final Prayer prayer;
    private final int prayerSpriteId;
    private final int killedVarbit;

    static BarrowsBrother fromSarcophagus(int objectId) {
        return BY_SARCOPHAGUS.get(objectId);
    }
}
