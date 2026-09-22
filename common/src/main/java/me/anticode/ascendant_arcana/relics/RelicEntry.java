package me.anticode.ascendant_arcana.relics;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class RelicEntry {
    private final ResourceLocation type;

    private final Operation operation;

    private final List<Float> strengths;

    private final Target target;

    public RelicEntry(ResourceLocation type, Operation operation, List<Float> strengths, Target target) {
        this.type = type;
        this.operation = operation;
        this.strengths = strengths;
        this.target = target;
    }

    public ResourceLocation getType() {
        return type;
    }

    public Operation getOperation() {
        return operation;
    }

    public Float getStrength(int strength) {
        return strengths.get(strength - 1);
    }

    public Target getTarget() {
        return target;
    }

    public double applyOperation(double input, int strength) {
        return (operation == Operation.addition) ? input + strengths.get(strength - 1) : input * (1 + strengths.get(strength - 1));
    }

    public static RelicEntry fromJson(JsonReader reader) {
        AbstractedRelicEntry abstractedRelicEntry = new Gson().fromJson(reader, AbstractedRelicEntry.class);
        return new RelicEntry(ResourceLocation.tryParse(abstractedRelicEntry.type()), abstractedRelicEntry.operation(), abstractedRelicEntry.strengths(), abstractedRelicEntry.target());
    }

    public static RelicEntry fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readResourceLocation();
        Operation operation = buf.readEnum(Operation.class);
        Target target = buf.readEnum(Target.class);
        List<Float> strengths = Lists.newArrayList(
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat()
        );
        return new RelicEntry(type, operation, strengths, target);
    }

    public static void toNetwork(FriendlyByteBuf buf, RelicEntry entry) {
        buf.writeResourceLocation(entry.type);
        buf.writeEnum(entry.operation);
        buf.writeEnum(entry.target);
        buf.writeFloat(entry.strengths.get(0));
        buf.writeFloat(entry.strengths.get(1));
        buf.writeFloat(entry.strengths.get(2));
        buf.writeFloat(entry.strengths.get(3));
        buf.writeFloat(entry.strengths.get(4));
    }

    public void toNetwork(FriendlyByteBuf buf) {
        toNetwork(buf, this);
    }

    public enum Operation {
        addition,
        multiply_total
    }

    public enum Target {
        armor,
        tool,
        durability,
        enchantable
    }
}
