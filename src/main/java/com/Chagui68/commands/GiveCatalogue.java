package com.Chagui68.commands;

import com.Chagui68.items.armor.EightHandledWheel;
import com.Chagui68.items.armor.ObsidianBastion;
import com.Chagui68.items.components.ArchitectKernel;
import com.Chagui68.items.components.BoneMarrow;
import com.Chagui68.items.components.ChaosCore;
import com.Chagui68.items.components.ChaosFragment;
import com.Chagui68.items.components.ChaosOrb;
import com.Chagui68.items.components.ChaosPowder;
import com.Chagui68.items.components.CompressedGoldBlock;
import com.Chagui68.items.components.CondensedChaosOrb;
import com.Chagui68.items.components.EnderCore;
import com.Chagui68.items.components.EnderFragment;
import com.Chagui68.items.components.ExecutionerWarrant;
import com.Chagui68.items.components.FrostHeart;
import com.Chagui68.items.components.HeadSlimeHeart;
import com.Chagui68.items.components.MagmaCore;
import com.Chagui68.items.components.MilitaryComponent;
import com.Chagui68.items.components.MoltenMarrow;
import com.Chagui68.items.components.MoltenNetherite;
import com.Chagui68.items.components.MoltenWheelCore;
import com.Chagui68.items.components.MultiversalCore;
import com.Chagui68.items.components.ObsidianShard;
import com.Chagui68.items.components.OssifiedPlate;
import com.Chagui68.items.components.ReaperCore;
import com.Chagui68.items.components.ReaperEssence;
import com.Chagui68.items.components.RefinedNetherite;
import com.Chagui68.items.components.RefinedWheelCore;
import com.Chagui68.items.components.ReinforcedBone;
import com.Chagui68.items.components.ReinforcedBoneBlock;
import com.Chagui68.items.components.SentinelCore;
import com.Chagui68.items.components.ShadowCloak;
import com.Chagui68.items.components.StarCore;
import com.Chagui68.items.components.StormCrystal;
import com.Chagui68.items.components.SwordMold;
import com.Chagui68.items.components.VenomGland;
import com.Chagui68.items.components.VoidEssence;
import com.Chagui68.items.components.WheelCore;
import com.Chagui68.items.components.WheelEssence;
import com.Chagui68.items.food.HeadSlimeGelatin;
import com.Chagui68.items.food.ScoobyCookie;
import com.Chagui68.items.misc.IceCrown;
import com.Chagui68.items.misc.MantisClaws;
import com.Chagui68.items.misc.MilitaryMine;
import com.Chagui68.items.misc.WirtsLantern;
import com.Chagui68.items.misc.offhand.FrostHeartOffhand;
import com.Chagui68.items.misc.offhand.MarrowAegis;
import com.Chagui68.items.misc.offhand.VeilwalkerMantle;
import com.Chagui68.items.weapons.magic.ChaosForge;
import com.Chagui68.items.weapons.magic.SentinelGrimoire;
import com.Chagui68.items.weapons.magic.SkyfireTalisman;
import com.Chagui68.items.weapons.melee.CinderGreatsword;
import com.Chagui68.items.weapons.melee.Excalibur;
import com.Chagui68.items.weapons.melee.NullshearEdge;
import com.Chagui68.items.weapons.melee.SoulreapScythe;
import com.Chagui68.items.weapons.melee.Venomfang;
import com.Chagui68.items.weapons.ranged.AetherPullshot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Data behind {@code /msc give}: which aliases map to which custom item, plus the text of the help
 * pages.
 *
 * <p>The command used to hold a ~60 case switch, a separate 20 line tab-completion list and four
 * pages of help lines, all describing the same items. The alias table is now the single source of
 * truth for giving and for tab completion, and a unit test checks that every item named in the help
 * text actually resolves.
 */
final class GiveCatalogue {

    /**
     * One custom item.
     *
     * @param aliases  every accepted alias; the first one is the canonical name used in the help
     * @param template item factory — kept lazy so loading this catalogue never initialises item
     *                 classes (they read Bukkit's item meta) until an item is really requested
     */
    record Entry(List<String> aliases, Supplier<ItemStack> template) {}

    static final List<String> PAGE_TITLES = List.of(
            "LEGENDARY WEAPONS & MAGIC",
            "ARMOR SETS, RELICS & OFFHANDS",
            "BOSS CATALYSTS & APEX COMPONENTS",
            "CRAFTING MATERIALS & ESSENCES");

    /** Help bodies (everything after the "&e• ") for page 1; grouped lines are allowed. */
    private static final List<String> PAGE_1 = List.of(
            "excalibur &8- &6Holy Blade of Kings",
            "cindergreatsword &8- &cBlazing Heavy Greatsword",
            "nullshearedge &8- &5Void Spatial Slicer",
            "soulreapscythe &8- &8Life-draining Scythe",
            "venomfang &8- &2Poison-tipped Dagger",
            "aetherpullshot &8- &bGravitational Pull Bow",
            "chaosforge &8- &dChaos Casting Hammer",
            "skyfiretalisman &8- &6Celestial Skyfire Charm",
            "sentinelgrimoire &8- &9Guardian Spell Grimoire",
            "executionerguillotine &8- &4NIX Executioner Axe",
            "architectdeployer &8- &bJackStar Data Bow");

    private static final List<String> PAGE_2 = List.of(
            "eighthandledwheel &8- &fMahoraga's Sacred Wheel",
            "obsidianbastionhelmet &8- &8Obsidian Bastion Helmet",
            "obsidianbastionchestplate &8- &8Obsidian Bastion Chestplate",
            "obsidianbastionleggings &8- &8Obsidian Bastion Leggings",
            "obsidianbastionboots &8- &8Obsidian Bastion Boots",
            "icecrown &8- &bGlacial Monarch Crown",
            "wirtslantern &8- &6Illuminating Explorer Lantern",
            "mantisclaws &8- &aPreying Mantis Dual Claws",
            "militarymine &8- &cProximity Landmine",
            "frostheartoffhand &8- &9Cryo Shield Offhand",
            "marrowaegis &8- &fBone Marrow Aegis Shield",
            "veilwalkermantle &8- &5Shadow Veilwalker Cloak");

    private static final List<String> PAGE_3 = List.of(
            "executionerwarrant &8- &4NIX Scaffold Summon Warrant",
            "compressedgoldblock &8- &6Boss Altar Anchor Block",
            "multiversalcore &8- &dMultiverse Nexus Core",
            "wheelcore &8- &fDivergent Sila Wheel Core",
            "moltenwheelcore &8- &cMolten Infused Wheel Core",
            "refinedwheelcore &8- &bPurified Wheel Core",
            "reapercore &8- &8Soul Reaper Core",
            "sentinelcore &8- &9Ancient Sentinel Core",
            "endercore &8- &5End Void Dimensional Core",
            "starcore &8- &eCelestial Star Core",
            "chaoscore &8- &dRaw Concentrated Chaos Core",
            "scoobycookie &8- &6Mystery Scooby Snack (Food)",
            "executioneredge &8- &4NIX Axe Shard",
            "vampireblood &8/ &eunstableblood &8/ &ebearerelixir &8- &4DIO Blood & Elixir");

    private static final List<String> PAGE_4 = List.of(
            "reaperessence &8/ &evoidessence &8/ &ewheelessence &8- &7Essences",
            "stormcrystal &8/ &emagmacore &8/ &efrostheart &8- &7Elemental Cores",
            "obsidianshard &8/ &erefinednetherite &8/ &emoltennetherite &8- &7Metals",
            "headslimeheart &8/ &eheadslimegelatin &8/ &evenomgland &8- &7Organics",
            "shadowcloak &8/ &eswordmold &8/ &emilitarycomponent &8- &7Relics",
            "reinforcedbone &8/ &ereinforcedboneblock &8- &7Bone Crafting",
            "chaosorb &8/ &echaospowder &8/ &echaosfragment &8- &7Chaos Alch.",
            "condensedchaosorb &8/ &eenderfragment &8- &7Infused Catalysts");

    private static final List<List<String>> PAGES = List.of(PAGE_1, PAGE_2, PAGE_3, PAGE_4);

    private static final List<Entry> ENTRIES = List.of(
            // Food & cosmetic
            new Entry(List.of("scoobycookie", "cookie"), () -> ScoobyCookie.SCOOBY_COOKIE),
            new Entry(List.of("icecrown", "crown"), () -> IceCrown.ICE_CROWN),
            new Entry(List.of("wirtslantern", "lantern"), () -> WirtsLantern.WIRTS_LANTERN),
            new Entry(List.of("starcore", "star"), () -> StarCore.STAR_CORE),
            // Stand Arrow arc: DIO, NIX and Jack Star
            new Entry(List.of("vampireblood", "blood"),
                    () -> com.Chagui68.items.components.VampireBlood.VAMPIRE_BLOOD),
            new Entry(List.of("unstableblood"), () -> com.Chagui68.items.potions.VampirePotions.UNSTABLE_BLOOD),
            new Entry(List.of("bearerelixir", "elixir"),
                    () -> com.Chagui68.items.potions.VampirePotions.BEARER_ELIXIR),
            new Entry(List.of("executioneredge", "edge"),
                    () -> com.Chagui68.items.components.ExecutionerEdge.EXECUTIONER_EDGE),
            new Entry(List.of("executionerguillotine", "guillotine"),
                    () -> com.Chagui68.items.weapons.melee.ExecutionerGuillotine.EXECUTIONER_GUILLOTINE),
            new Entry(List.of("architectdeployer", "deployer"),
                    () -> com.Chagui68.items.weapons.ranged.ArchitectDeployer.ARCHITECT_DEPLOYER),
            // Weapons
            new Entry(List.of("excalibur", "sword"), () -> Excalibur.EXCALIBUR_SWORD),
            new Entry(List.of("aetherpullshot", "pullshot"), () -> AetherPullshot.AETHER_PULLSHOT),
            new Entry(List.of("chaosforge"), () -> ChaosForge.CHAOS_FORGE),
            new Entry(List.of("cindergreatsword", "greatsword"), () -> CinderGreatsword.CINDER_GREATSWORD),
            new Entry(List.of("nullshearedge", "nullshear"), () -> NullshearEdge.NULLSHEAR_EDGE),
            new Entry(List.of("soulreapscythe", "scythe"), () -> SoulreapScythe.SOULREAP_SCYTHE),
            new Entry(List.of("venomfang", "dagger"), () -> Venomfang.VENOMFANG),
            new Entry(List.of("skyfiretalisman", "talisman"), () -> SkyfireTalisman.SKYFIRE_TALISMAN),
            new Entry(List.of("sentinelgrimoire", "grimoire"), () -> SentinelGrimoire.GRIMOIRE),
            new Entry(List.of("swordmold", "mold"), () -> SwordMold.SWORD_MOLD),
            new Entry(List.of("mantisclaws", "claws"), () -> MantisClaws.MANTIS_CLAWS_ITEM),
            new Entry(List.of("militarymine", "mine"), () -> MilitaryMine.MILITARY_MINE),
            // Armor
            new Entry(List.of("eighthandledwheel", "wheel"), () -> EightHandledWheel.EIGHT_HANDLED_WHEEL),
            new Entry(List.of("obsidianbastionhelmet", "bastionhelmet"), () -> ObsidianBastion.HELMET),
            new Entry(List.of("obsidianbastionchestplate", "bastionchestplate"), () -> ObsidianBastion.CHESTPLATE),
            new Entry(List.of("obsidianbastionleggings", "bastionleggings"), () -> ObsidianBastion.LEGGINGS),
            new Entry(List.of("obsidianbastionboots", "bastionboots"), () -> ObsidianBastion.BOOTS),
            // Off-hand misc
            new Entry(List.of("frostheartoffhand", "frostoffhand"), () -> FrostHeartOffhand.FROST_HEART_OFFHAND),
            new Entry(List.of("marrowaegis", "aegis"), () -> MarrowAegis.MARROW_AEGIS),
            new Entry(List.of("veilwalkermantle", "mantle"), () -> VeilwalkerMantle.VEILWALKER_MANTLE),
            new Entry(List.of("militarycomponent", "component"), () -> MilitaryComponent.MILITARY_COMPONENT),
            new Entry(List.of("shadowcloak", "cloak"), () -> ShadowCloak.SHADOW_CLOAK),
            // Boss catalysts & apex components
            new Entry(List.of("executionerwarrant", "warrant", "deathwarrant"),
                    () -> ExecutionerWarrant.EXECUTIONER_WARRANT),
            new Entry(List.of("architectkernel", "kernel", "architect"), () -> ArchitectKernel.ARCHITECT_KERNEL),
            new Entry(List.of("multiversalcore", "multiverse"), () -> MultiversalCore.MULTIVERSAL_CORE),
            new Entry(List.of("compressedgoldblock", "goldblock"), () -> CompressedGoldBlock.COMPRESSED_GOLD_BLOCK),
            new Entry(List.of("wheelcore"), () -> WheelCore.WHEEL_CORE),
            new Entry(List.of("moltenwheelcore", "moltenwheel"), () -> MoltenWheelCore.MOLTEN_WHEEL_CORE),
            new Entry(List.of("refinedwheelcore", "refinedwheel"), () -> RefinedWheelCore.REFINED_WHEEL_CORE),
            new Entry(List.of("reapercore"), () -> ReaperCore.REAPER_CORE),
            new Entry(List.of("sentinelcore", "sentinel"), () -> SentinelCore.SENTINEL_CORE),
            new Entry(List.of("endercore"), () -> EnderCore.ENDER_CORE),
            new Entry(List.of("chaoscore"), () -> ChaosCore.CHAOS_CORE),
            // Crafting materials & essences
            new Entry(List.of("headslimeheart", "heart"), () -> HeadSlimeHeart.HEAD_SLIME_HEART),
            new Entry(List.of("headslimegelatin", "gelatin"), () -> HeadSlimeGelatin.HEAD_SLIME_GELATIN),
            new Entry(List.of("chaosorb"), () -> ChaosOrb.CHAOS_ORB),
            new Entry(List.of("chaospowder"), () -> ChaosPowder.CHAOS_POWDER),
            new Entry(List.of("chaosfragment"), () -> ChaosFragment.CHAOS_FRAGMENT),
            new Entry(List.of("condensedchaosorb", "condensed"), () -> CondensedChaosOrb.CONDENSED_CHAOS_ORB),
            new Entry(List.of("enderfragment", "ender"), () -> EnderFragment.ENDER_FRAGMENT),
            new Entry(List.of("frostheart", "frost"), () -> FrostHeart.FROST_HEART),
            new Entry(List.of("magmacore", "magma"), () -> MagmaCore.MAGMA_CORE),
            new Entry(List.of("obsidianshard", "shard"), () -> ObsidianShard.OBSIDIAN_SHARD),
            new Entry(List.of("reaperessence", "reaper"), () -> ReaperEssence.REAPER_ESSENCE),
            new Entry(List.of("reinforcedbone", "bone"), () -> ReinforcedBone.REINFORCED_BONE),
            new Entry(List.of("reinforcedboneblock"), () -> ReinforcedBoneBlock.REINFORCED_BONE_BLOCK),
            new Entry(List.of("bonemarrow", "marrow"), () -> BoneMarrow.BONE_MARROW),
            new Entry(List.of("ossifiedplate", "plate"), () -> OssifiedPlate.OSSIFIED_PLATE),
            new Entry(List.of("moltenmarrow"), () -> MoltenMarrow.MOLTEN_MARROW),
            new Entry(List.of("stormcrystal", "storm"), () -> StormCrystal.STORM_CRYSTAL),
            new Entry(List.of("venomgland", "venom"), () -> VenomGland.VENOM_GLAND),
            new Entry(List.of("voidessence", "void"), () -> VoidEssence.VOID_ESSENCE),
            new Entry(List.of("wheelessence", "whelessence"), () -> WheelEssence.WHEEL_ESSENCE),
            new Entry(List.of("refinednetherite"), () -> RefinedNetherite.REFINED_NETHERITE),
            new Entry(List.of("moltennetherite", "molten"), () -> MoltenNetherite.MOLTEN_NETHERITE));

    private static final Map<String, Entry> BY_ALIAS = buildAliasIndex();

    private GiveCatalogue() {}

    private static Map<String, Entry> buildAliasIndex() {
        Map<String, Entry> index = new LinkedHashMap<>();
        for (Entry entry : ENTRIES) {
            for (String alias : entry.aliases()) {
                if (alias == null || alias.isBlank()) {
                    throw new IllegalStateException("Give catalogue alias must not be blank");
                }
                Entry previous = index.put(alias.toLowerCase(), entry);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate give alias '" + alias + "'");
                }
            }
        }
        return Map.copyOf(index);
    }

    /**
     * Builds a fresh copy of the item registered under {@code alias}, or {@code null} when the
     * alias is unknown. Callers own the returned stack.
     */
    static ItemStack find(String alias) {
        if (alias == null) return null;
        Entry entry = BY_ALIAS.get(alias.toLowerCase());
        return entry == null ? null : entry.template().get().clone();
    }

    static List<Entry> entries() {
        return ENTRIES;
    }

    static int pages() {
        return PAGES.size();
    }

    static String pageTitle(int page) {
        return PAGE_TITLES.get(page - 1);
    }

    /** The rendered help bodies of one page, in declaration order. */
    static List<String> helpLines(int page) {
        return PAGES.get(page - 1);
    }

    /** Every alias of every entry, in declaration order — the source of tab completion. */
    static List<String> aliases() {
        List<String> all = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            all.addAll(entry.aliases());
        }
        return all;
    }
}
