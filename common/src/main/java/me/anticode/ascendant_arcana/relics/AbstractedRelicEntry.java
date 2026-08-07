package me.anticode.ascendant_arcana.relics;

import java.util.List;

public record AbstractedRelicEntry(String type, RelicEntry.Operation operation, List<Float> strengths,
                                   RelicEntry.Target target) {
}
