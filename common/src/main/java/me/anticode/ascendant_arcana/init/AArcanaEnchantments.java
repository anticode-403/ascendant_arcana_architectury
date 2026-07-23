package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.enchantment.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;

public class AArcanaEnchantments {
    private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.ENCHANTMENT);

    public static RegistrySupplier<Enchantment> AMBUSH = register(new Ambush(), "ambush");
    public static RegistrySupplier<Enchantment> ARCHERS_GAMBIT = register(new ArchersGambit(), "archers_gambit");
    public static RegistrySupplier<Enchantment> BLAZEBOLT = register(new Blazebolt(), "blazebolt");
    public static RegistrySupplier<Enchantment> CLEANSE = register(new Cleanse(), "cleanse");
    public static RegistrySupplier<Enchantment> CROSS_COUNTER = register(new CrossCounter(), "cross_counter");
    public static RegistrySupplier<Enchantment> CUSHIONING = register(new Cushioning(), "cushioning");
    public static RegistrySupplier<Enchantment> DEBILITATING_CHAIN = register(new DebilitatingChain(), "debilitating_chain");
    public static RegistrySupplier<Enchantment> DEFLECT = register(new Deflect(), "deflect");
    public static RegistrySupplier<Enchantment> EVOKERS_WRATH = register(new EvokersWrath(), "evokers_wrath");
    public static RegistrySupplier<Enchantment> HELLWALKER = register(new HellWalker(), "hellwalker");
    public static RegistrySupplier<Enchantment> HOBBLING_SHOT = register(new HobblingShot(), "hobbling_shot");
    public static RegistrySupplier<Enchantment> LIFETIDE = register(new Lifetide(), "lifetide");
    public static RegistrySupplier<Enchantment> MIASMA = register(new Miasma(), "miasma");
    public static RegistrySupplier<Enchantment> PINCUSHION = register(new Pincushion(), "pincushion");
    public static RegistrySupplier<Enchantment> PROTECTIVE_ECHO = register(new ProtectiveEcho(), "protective_echo");
    public static RegistrySupplier<Enchantment> REJUVENATING_SHOT = register(new RejuvenatingShot(), "rejuvenating_shot");
    public static RegistrySupplier<Enchantment> REPEATING = register(new Repeating(), "repeating");
    public static RegistrySupplier<Enchantment> RICOCHET = register(new Ricochet(), "ricochet");
    public static RegistrySupplier<Enchantment> ROCKETRY = register(new Rocketry(), "rocketry");
    public static RegistrySupplier<Enchantment> SALVO = register(new Salvo(), "salvo");
    public static RegistrySupplier<Enchantment> SHATTERSHOT = register(new Shattershot(), "shattershot");
    public static RegistrySupplier<Enchantment> SINGULARITY = register(new Singularity(), "singularity");
    public static RegistrySupplier<Enchantment> SMELTING = register(new Smelting(), "smelting");
    public static RegistrySupplier<Enchantment> SONIC_BLAST = register(new SonicBlast(), "sonic_blast");
    public static RegistrySupplier<Enchantment> SOUL_BURST = register(new SoulBurst(), "soul_burst");
    public static RegistrySupplier<Enchantment> STRAFE = register(new Strafe(), "strafe");
    public static RegistrySupplier<Enchantment> SUNDERING = register(new Sundering(), "sundering");
    public static RegistrySupplier<Enchantment> SUREFOOT = register(new Surefoot(), "surefoot");

    public static RegistrySupplier<Enchantment> ALCHEMISTS_HEART = register(new HeartEnchantment(), "alchemists_heart");
    public static RegistrySupplier<Enchantment> NETHER_HEART = register(new HeartEnchantment(), "heart_of_the_nether");
    public static RegistrySupplier<Enchantment> COLDHEART = register(new HeartEnchantment(), "coldheart");
    public static RegistrySupplier<Enchantment> STORM_HEART = register(new HeartEnchantment(), "heart_of_the_storm");
    public static RegistrySupplier<Enchantment> BLADEHEART =  register(new HeartEnchantment(), "bladeheart");
    public static RegistrySupplier<Enchantment> WITCH_HEART = register(new HeartEnchantment(), "witch_heart");
    public static RegistrySupplier<Enchantment> TURTLE_HEART = register(new TurtleHeart(), "heart_of_the_turtle");

    public static RegistrySupplier<Enchantment> DEPTHS_CURSE = register(new DepthsCurse(), "depths_curse");
    public static RegistrySupplier<Enchantment> ENFEEBLEMENT_CURSE = register(new EnfeeblementCurse(), "enfeeblement_curse");
    public static RegistrySupplier<Enchantment> INACCURACY_CURSE = register(new InaccuracyCurse(), "inaccuracy_curse");


    public static RegistrySupplier<Enchantment> register(Enchantment enchantment, String id) {
        return ENCHANTMENTS.register(id, () -> enchantment);
    }

    public enum IndirectHeartDamageTypes {
        NETHER,
        COLD,
        STORM
    }

    public static void initialize() {
        ENCHANTMENTS.register();
    }
}
