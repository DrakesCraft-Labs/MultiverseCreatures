package com.Chagui68.wiki;

import com.Chagui68.items.armor.EightHandledWheel;
import com.Chagui68.items.armor.ObsidianBastion;
import com.Chagui68.items.components.*;
import com.Chagui68.items.food.HeadSlimeGelatin;
import com.Chagui68.items.food.ScoobyCookie;
import com.Chagui68.items.misc.IceCrown;
import com.Chagui68.items.misc.MantisClaws;
import com.Chagui68.items.misc.MilitaryMine;
import com.Chagui68.items.misc.WirtsLantern;
import com.Chagui68.items.misc.offhand.FrostHeartOffhand;
import com.Chagui68.items.misc.offhand.MarrowAegis;
import com.Chagui68.items.misc.offhand.VeilwalkerMantle;
import com.Chagui68.items.potions.VampirePotions;
import com.Chagui68.items.weapons.magic.ChaosForge;
import com.Chagui68.items.weapons.magic.SentinelGrimoire;
import com.Chagui68.items.weapons.magic.SkyfireTalisman;
import com.Chagui68.items.weapons.melee.CinderGreatsword;
import com.Chagui68.items.weapons.melee.Excalibur;
import com.Chagui68.items.weapons.melee.ExecutionerGuillotine;
import com.Chagui68.items.weapons.melee.NullshearEdge;
import com.Chagui68.items.weapons.melee.SoulreapScythe;
import com.Chagui68.items.weapons.melee.Venomfang;
import com.Chagui68.items.weapons.ranged.AetherPullshot;
import com.Chagui68.items.weapons.ranged.ArchitectDeployer;
import com.Chagui68.stand.StandType;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.Chagui68.wiki.WikiCategory.*;

/**
 * Every page of the wiki, in both languages.
 *
 * <p>Recipes are not written here: {@link WikiRecipes} reads them from the server. This class only
 * says what a recipe cannot — who drops an item, who sells it, how a boss is called — and the
 * pages that have no item at all. Built on first use, never while the class loads, because the
 * items read the server's item meta.</p>
 */
public final class WikiCatalogue {

    private static List<WikiEntry> entries;
    private static Map<String, WikiEntry> byId;

    private WikiCatalogue() {
    }

    public static synchronized List<WikiEntry> entries() {
        if (entries == null) {
            entries = Collections.unmodifiableList(build());
            Map<String, WikiEntry> index = new LinkedHashMap<>();
            for (WikiEntry entry : entries) {
                if (index.put(entry.id(), entry) != null) {
                    throw new IllegalStateException("Duplicate wiki page " + entry.id());
                }
            }
            byId = index;
        }
        return entries;
    }

    public static WikiEntry byId(String id) {
        entries();
        return id == null ? null : byId.get(id.toLowerCase(Locale.ROOT));
    }

    public static List<WikiEntry> in(WikiCategory category) {
        List<WikiEntry> list = new ArrayList<>();
        for (WikiEntry entry : entries()) {
            if (entry.category() == category) {
                list.add(entry);
            }
        }
        return list;
    }

    // ----------------------------------------------------------------- sources

    private static WikiSource mob(Material egg, String mobEn, String mobEs, String spawnEn, String spawnEs,
                                  String section, double fallback) {
        return WikiSource.drop(egg, mobEn, mobEs, spawnEn, spawnEs, "entities." + section + ".drop-chance", fallback);
    }

    private static WikiSource merchant(String priceEn, String priceEs) {
        return WikiSource.of(Material.WANDERING_TRADER_SPAWN_EGG, "Multiverse Merchant", "Multiverse Merchant",
                "Sold by the Multiverse Merchant, a Wandering Trader that sometimes arrives from another world, for "
                        + priceEn + ".",
                "Lo vende el Multiverse Merchant, un Comerciante Errante que a veces llega desde otro mundo, por "
                        + priceEs + ".");
    }

    private static WikiRecipe trade(ItemStack first, ItemStack second, ItemStack result) {
        return WikiRecipe.trade(first, second, result);
    }

    private static ItemStack of(Material material, int amount) {
        return new ItemStack(material, amount);
    }

    private static ItemStack amount(ItemStack item, int amount) {
        ItemStack copy = item.clone();
        copy.setAmount(amount);
        return copy;
    }

    private static ItemStack awkwardPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        if (potion.getItemMeta() instanceof PotionMeta meta) {
            meta.setBasePotionType(PotionType.AWKWARD);
            potion.setItemMeta(meta);
        }
        return potion;
    }

    // ------------------------------------------------------------------ pages

    private static List<WikiEntry> build() {
        List<WikiEntry> list = new ArrayList<>();
        weapons(list);
        armor(list);
        relics(list);
        consumables(list);
        drops(list);
        crafted(list);
        bossLoot(list);
        stands(list);
        bosses(list);
        return list;
    }

    private static void weapons(List<WikiEntry> list) {
        list.add(WikiEntry.item("excalibur", WEAPONS, () -> Excalibur.EXCALIBUR_SWORD)
                .source(merchant("16 Star Cores and 32 Netherite Ingots", "16 Star Core y 32 lingotes de netherita"))
                .recipe(() -> trade(amount(StarCore.STAR_CORE, 16), of(Material.NETHERITE_INGOT, 32),
                        Excalibur.EXCALIBUR_SWORD.clone()))
                .build());
        list.add(WikiEntry.item("cindergreatsword", WEAPONS, () -> CinderGreatsword.CINDER_GREATSWORD).build());
        list.add(WikiEntry.item("nullshearedge", WEAPONS, () -> NullshearEdge.NULLSHEAR_EDGE).build());
        list.add(WikiEntry.item("soulreapscythe", WEAPONS, () -> SoulreapScythe.SOULREAP_SCYTHE).build());
        list.add(WikiEntry.item("venomfang", WEAPONS, () -> Venomfang.VENOMFANG).build());
        list.add(WikiEntry.item("executionerguillotine", WEAPONS, () -> ExecutionerGuillotine.EXECUTIONER_GUILLOTINE)
                .about("NIX's own axe. Its blade comes from the Executioner's Edges he drops.",
                        "El hacha de NIX. Su hoja sale de los Executioner's Edge que él suelta.")
                .build());
        list.add(WikiEntry.item("aetherpullshot", WEAPONS, () -> AetherPullshot.AETHER_PULLSHOT).build());
        list.add(WikiEntry.item("architectdeployer", WEAPONS, () -> ArchitectDeployer.ARCHITECT_DEPLOYER)
                .about("Jack Star's legacy, built round the Architect Kernel he drops.",
                        "El legado de Jack Star, construido alrededor del Architect Kernel que suelta.")
                .build());
        list.add(WikiEntry.item("chaosforge", WEAPONS, () -> ChaosForge.CHAOS_FORGE).build());
        list.add(WikiEntry.item("skyfiretalisman", WEAPONS, () -> SkyfireTalisman.SKYFIRE_TALISMAN).build());
        list.add(WikiEntry.item("sentinelgrimoire", WEAPONS, () -> SentinelGrimoire.GRIMOIRE).build());
    }

    private static void armor(List<WikiEntry> list) {
        list.add(WikiEntry.item("eighthandledwheel", ARMOR, () -> EightHandledWheel.EIGHT_HANDLED_WHEEL)
                .about("Mahoraga's wheel. It adapts to every kind of damage you take.",
                        "La rueda de Mahoraga. Se adapta a cada tipo de daño que recibes.")
                .build());
        String bastionEn = "Wear all four pieces of the Obsidian Bastion for its set bonus.";
        String bastionEs = "Lleva las cuatro piezas del Obsidian Bastion para activar su bonificación de set.";
        list.add(WikiEntry.item("obsidianbastionhelmet", ARMOR, () -> ObsidianBastion.HELMET).about(bastionEn, bastionEs).build());
        list.add(WikiEntry.item("obsidianbastionchestplate", ARMOR, () -> ObsidianBastion.CHESTPLATE).about(bastionEn, bastionEs).build());
        list.add(WikiEntry.item("obsidianbastionleggings", ARMOR, () -> ObsidianBastion.LEGGINGS).about(bastionEn, bastionEs).build());
        list.add(WikiEntry.item("obsidianbastionboots", ARMOR, () -> ObsidianBastion.BOOTS).about(bastionEn, bastionEs).build());
        list.add(WikiEntry.item("marrowaegis", ARMOR, () -> MarrowAegis.MARROW_AEGIS).build());
        list.add(WikiEntry.item("frostheartoffhand", ARMOR, () -> FrostHeartOffhand.FROST_HEART_OFFHAND).build());
        list.add(WikiEntry.item("veilwalkermantle", ARMOR, () -> VeilwalkerMantle.VEILWALKER_MANTLE).build());
    }

    private static void relics(List<WikiEntry> list) {
        list.add(WikiEntry.item("icecrown", RELICS, () -> IceCrown.ICE_CROWN)
                .source(merchant("48 Nether Stars and 64 Blue Ice", "48 estrellas del Nether y 64 de hielo azul"))
                .recipe(() -> trade(of(Material.NETHER_STAR, 48), of(Material.BLUE_ICE, 64), IceCrown.ICE_CROWN.clone()))
                .build());
        list.add(WikiEntry.item("mantisclaws", RELICS, () -> MantisClaws.MANTIS_CLAWS_ITEM)
                .source(merchant("16 Iron Ingots and 8 String", "16 lingotes de hierro y 8 de cuerda"))
                .recipe(() -> trade(of(Material.IRON_INGOT, 16), of(Material.STRING, 8), MantisClaws.MANTIS_CLAWS_ITEM.clone()))
                .build());
        list.add(WikiEntry.item("wirtslantern", RELICS, () -> WirtsLantern.WIRTS_LANTERN)
                .source(merchant("32 Soul Sand and 16 Soul Soil", "32 de arena de almas y 16 de tierra de almas"))
                .recipe(() -> trade(of(Material.SOUL_SAND, 32), of(Material.SOUL_SOIL, 16), WirtsLantern.WIRTS_LANTERN.clone()))
                .build());
        list.add(WikiEntry.item("militarymine", RELICS, () -> MilitaryMine.MILITARY_MINE)
                .about("Place it and it takes the look of the ground around it. One step is enough.",
                        "Al colocarla toma el aspecto del suelo que la rodea. Basta con un paso.")
                .build());
        list.add(WikiEntry.info("musicdiscs", RELICS, Material.MUSIC_DISC_13, NamedTextColor.AQUA,
                        "Music Discs", "Discos de música")
                .about("Custom songs that play in any jukebox, from Megalovania to Bohemian Rhapsody.",
                        "Canciones propias que suenan en cualquier tocadiscos, de Megalovania a Bohemian Rhapsody.")
                .source(WikiSource.of(Material.VILLAGER_SPAWN_EGG, "Disc Trader", "Disc Trader",
                        "A villager that sometimes spawns as a Disc Trader sells every disc for 16 Emeralds.",
                        "Un aldeano que a veces aparece como Disc Trader vende cada disco por 16 esmeraldas."))
                .build());
    }

    private static void consumables(List<WikiEntry> list) {
        list.add(WikiEntry.item("scoobycookie", CONSUMABLES, () -> ScoobyCookie.SCOOBY_COOKIE)
                .source(merchant("20 Diamonds (5 cookies)", "20 diamantes (5 galletas)"))
                .source(WikiSource.of(Material.COMMAND_BLOCK, "Jack Star", "Jack Star",
                        "Jack Star throws debug cookies during his fight.",
                        "Jack Star lanza galletas de depuración durante su combate."))
                .recipe(() -> trade(of(Material.DIAMOND, 20), null, amount(ScoobyCookie.SCOOBY_COOKIE, 5)))
                .build());
        list.add(WikiEntry.item("headslimegelatin", CONSUMABLES, () -> HeadSlimeGelatin.HEAD_SLIME_GELATIN).build());
        list.add(WikiEntry.item("unstableblood", CONSUMABLES, () -> VampirePotions.UNSTABLE_BLOOD)
                .recipe(() -> WikiRecipe.brewing(VampireBlood.VAMPIRE_BLOOD.clone(), awkwardPotion(),
                        VampirePotions.UNSTABLE_BLOOD.clone()))
                .build());
        list.add(WikiEntry.item("bearerelixir", CONSUMABLES, () -> VampirePotions.BEARER_ELIXIR)
                .about("Drink it to become a vampire who can carry a Stand. See the Stands section.",
                        "Bébela para volverte un vampiro capaz de portar un Stand. Mira la sección de Stands.")
                .recipe(() -> WikiRecipe.brewing(new ItemStack(Material.WITHER_ROSE), VampirePotions.UNSTABLE_BLOOD.clone(),
                        VampirePotions.BEARER_ELIXIR.clone()))
                .build());
    }

    private static void drops(List<WikiEntry> list) {
        list.add(WikiEntry.item("wheelessence", DROPS, () -> WheelEssence.WHEEL_ESSENCE)
                .source(mob(Material.ZOMBIE_HEAD, "Mahoraga", "Mahoraga",
                        "A miniboss that very rarely rises in place of a Zombie.",
                        "Un minijefe que muy rara vez aparece en lugar de un Zombi.", "mahoraga", 0.75))
                .build());
        list.add(WikiEntry.item("chaosorb", DROPS, () -> ChaosOrb.CHAOS_ORB)
                .source(mob(Material.EVOKER_SPAWN_EGG, "Chaos Mage", "Chaos Mage",
                        "Takes the place of some Evokers, in mansions and in raids.",
                        "Ocupa el lugar de algunos Evocadores, en mansiones y en asaltos.", "chaos-mage", 0.6))
                .build());
        list.add(WikiEntry.item("enderfragment", DROPS, () -> EnderFragment.ENDER_FRAGMENT)
                .source(mob(Material.ENDERMAN_SPAWN_EGG, "Ender Knight", "Ender Knight",
                        "Takes the place of some Endermen.",
                        "Ocupa el lugar de algunos Endermans.", "ender-knight", 0.55))
                .build());
        list.add(WikiEntry.item("frostheart", DROPS, () -> FrostHeart.FROST_HEART)
                .source(mob(Material.IRON_GOLEM_SPAWN_EGG, "Frost Golem", "Frost Golem",
                        "Some Iron Golems, natural or built, awaken as Frost Golems.",
                        "Algunos Gólems de Hierro, naturales o construidos, despiertan como Frost Golem.",
                        "frost-golem", 0.75))
                .build());
        list.add(WikiEntry.item("magmacore", DROPS, () -> MagmaCore.MAGMA_CORE)
                .source(mob(Material.BLAZE_SPAWN_EGG, "Flame Elemental", "Flame Elemental",
                        "Takes the place of some Blazes in Nether fortresses.",
                        "Ocupa el lugar de algunos Blazes en las fortalezas del Nether.", "flame-elemental", 0.6))
                .build());
        list.add(WikiEntry.item("stormcrystal", DROPS, () -> StormCrystal.STORM_CRYSTAL)
                .source(mob(Material.WITCH_SPAWN_EGG, "Storm Caller", "Storm Caller",
                        "Takes the place of some Witches, raids included.",
                        "Ocupa el lugar de algunas Brujas, también en asaltos.", "storm-caller", 0.6))
                .build());
        list.add(WikiEntry.item("venomgland", DROPS, () -> VenomGland.VENOM_GLAND)
                .source(mob(Material.WITCH_SPAWN_EGG, "Venom Witch", "Venom Witch",
                        "Takes the place of some Witches, raids included.",
                        "Ocupa el lugar de algunas Brujas, también en asaltos.", "venom-witch", 0.6))
                .build());
        list.add(WikiEntry.item("voidessence", DROPS, () -> VoidEssence.VOID_ESSENCE)
                .source(mob(Material.SPIDER_SPAWN_EGG, "Void Crawler", "Void Crawler",
                        "Takes the place of some Spiders.",
                        "Ocupa el lugar de algunas Arañas.", "void-crawler", 0.5))
                .build());
        list.add(WikiEntry.item("reaperessence", DROPS, () -> ReaperEssence.REAPER_ESSENCE)
                .source(mob(Material.WITHER_SKELETON_SPAWN_EGG, "Soul Reaper", "Soul Reaper",
                        "Takes the place of some Wither Skeletons.",
                        "Ocupa el lugar de algunos Esqueletos Wither.", "soul-reaper", 0.6))
                .build());
        list.add(WikiEntry.item("reinforcedbone", DROPS, () -> ReinforcedBone.REINFORCED_BONE)
                .source(mob(Material.SKELETON_SPAWN_EGG, "Bone Shield", "Bone Shield",
                        "Takes the place of some Skeletons.",
                        "Ocupa el lugar de algunos Esqueletos.", "bone-shield", 0.8))
                .build());
        list.add(WikiEntry.item("shadowcloak", DROPS, () -> ShadowCloak.SHADOW_CLOAK)
                .source(mob(Material.SKELETON_SPAWN_EGG, "Shadow Rogue", "Shadow Rogue",
                        "Takes the place of some Skeletons.",
                        "Ocupa el lugar de algunos Esqueletos.", "shadow-rogue", 0.5))
                .build());
        list.add(WikiEntry.item("obsidianshard", DROPS, () -> ObsidianShard.OBSIDIAN_SHARD)
                .source(mob(Material.ZOMBIE_SPAWN_EGG, "Obsidian Guard", "Obsidian Guard",
                        "A heavy zombie that takes the place of some Zombies.",
                        "Un zombi pesado que ocupa el lugar de algunos Zombis.", "obsidian-guard", 0.85))
                .build());
        list.add(WikiEntry.item("headslimeheart", DROPS, () -> HeadSlimeHeart.HEAD_SLIME_HEART)
                .source(WikiSource.of(Material.SLIME_SPAWN_EGG, "Head Slime", "Head Slime",
                        "Always dropped by the Head Slime, which takes the place of some Slimes.",
                        "Siempre lo suelta el Head Slime, que ocupa el lugar de algunos Slimes."))
                .build());
        list.add(WikiEntry.item("militarycomponent", DROPS, () -> MilitaryComponent.MILITARY_COMPONENT)
                .source(WikiSource.drop(Material.ZOMBIE_HORSE_SPAWN_EGG, "Military Army", "Military Army",
                        "Under a full moon a zombie horse may wander in; get close and a whole army ambushes you. "
                                + "Every soldier can drop one.",
                        "Con luna llena puede aparecer un caballo zombi; si te acercas, un ejército entero te embosca. "
                                + "Cada soldado puede soltar uno.",
                        "entities.zombie-horse-trap.military-component-drop-chance", 0.3))
                .build());
    }

    private static void crafted(List<WikiEntry> list) {
        list.add(WikiEntry.item("starcore", CRAFTED, () -> StarCore.STAR_CORE).build());
        list.add(WikiEntry.item("swordmold", CRAFTED, () -> SwordMold.SWORD_MOLD).build());
        list.add(WikiEntry.item("reinforcedboneblock", CRAFTED, () -> ReinforcedBoneBlock.REINFORCED_BONE_BLOCK).build());
        list.add(WikiEntry.item("bonemarrow", CRAFTED, () -> BoneMarrow.BONE_MARROW).build());
        list.add(WikiEntry.item("ossifiedplate", CRAFTED, () -> OssifiedPlate.OSSIFIED_PLATE).build());
        list.add(WikiEntry.item("moltenmarrow", CRAFTED, () -> MoltenMarrow.MOLTEN_MARROW)
                .about("Only a Blast Furnace is hot enough to melt the plate.",
                        "Solo un Alto horno está lo bastante caliente para fundir la placa.")
                .build());
        list.add(WikiEntry.item("chaospowder", CRAFTED, () -> ChaosPowder.CHAOS_POWDER).build());
        list.add(WikiEntry.item("chaosfragment", CRAFTED, () -> ChaosFragment.CHAOS_FRAGMENT).build());
        list.add(WikiEntry.item("chaoscore", CRAFTED, () -> ChaosCore.CHAOS_CORE).build());
        list.add(WikiEntry.item("condensedchaosorb", CRAFTED, () -> CondensedChaosOrb.CONDENSED_CHAOS_ORB).build());
        list.add(WikiEntry.item("endercore", CRAFTED, () -> EnderCore.ENDER_CORE).build());
        list.add(WikiEntry.item("wheelcore", CRAFTED, () -> WheelCore.WHEEL_CORE).build());
        list.add(WikiEntry.item("moltenwheelcore", CRAFTED, () -> MoltenWheelCore.MOLTEN_WHEEL_CORE).build());
        list.add(WikiEntry.item("compressedgoldblock", CRAFTED, () -> CompressedGoldBlock.COMPRESSED_GOLD_BLOCK).build());
        list.add(WikiEntry.item("refinednetherite", CRAFTED, () -> RefinedNetherite.REFINED_NETHERITE).build());
        list.add(WikiEntry.item("moltennetherite", CRAFTED, () -> MoltenNetherite.MOLTEN_NETHERITE).build());
        list.add(WikiEntry.item("refinedwheelcore", CRAFTED, () -> RefinedWheelCore.REFINED_WHEEL_CORE).build());
        list.add(WikiEntry.item("reapercore", CRAFTED, () -> ReaperCore.REAPER_CORE).build());
        list.add(WikiEntry.item("multiversalcore", CRAFTED, () -> MultiversalCore.MULTIVERSAL_CORE).build());
    }

    private static void bossLoot(List<WikiEntry> list) {
        list.add(WikiEntry.item("sentinelcore", BOSS_LOOT, () -> SentinelCore.SENTINEL_CORE)
                .source(new WikiSource(Material.ARMOR_STAND, "The Obsidian Sentinel", "The Obsidian Sentinel",
                        "The final boss of the Boss Dimension drops it when he falls.",
                        "El jefe final de la Dimensión de Jefes lo suelta al caer.",
                        "entities.armor-stand-boss.sentinel-core-drop-chance", 100.0, true))
                .build());
        list.add(WikiEntry.item("vampireblood", BOSS_LOOT, () -> VampireBlood.VAMPIRE_BLOOD)
                .source(WikiSource.of(Material.CLOCK, "DIO", "DIO",
                        "DIO always leaves 1 to 2 Vampire Blood behind when he falls.",
                        "DIO siempre deja entre 1 y 2 Vampire Blood al caer."))
                .build());
        list.add(WikiEntry.item("executioneredge", BOSS_LOOT, () -> ExecutionerEdge.EXECUTIONER_EDGE)
                .source(new WikiSource(Material.NETHERITE_AXE, "NIX - The Executioner", "NIX - The Executioner",
                        "NIX drops one Edge, and sometimes a second one.",
                        "NIX suelta un Filo y, a veces, un segundo.",
                        "entities.nix-executioner.edge-drop-chance", 1.0, false))
                .build());
        list.add(WikiEntry.item("executionerwarrant", BOSS_LOOT, () -> ExecutionerWarrant.EXECUTIONER_WARRANT)
                .about("The offering that summons NIX. See Bosses & Rituals.",
                        "La ofrenda que invoca a NIX. Mira Jefes y rituales.")
                .build());
        list.add(WikiEntry.item("architectkernel", BOSS_LOOT, () -> ArchitectKernel.ARCHITECT_KERNEL)
                .source(WikiSource.of(Material.COMMAND_BLOCK, "JackStar", "JackStar",
                        "JackStar always drops it when his last process ends.",
                        "JackStar siempre lo suelta cuando termina su último proceso."))
                .build());
        list.add(WikiEntry.item("garoucosmiccore", BOSS_LOOT, () -> GarouCosmicCore.GAROU_COSMIC_CORE)
                .source(WikiSource.of(Material.WITHER_SKELETON_SKULL, "Garou", "Garou",
                        "Garou always drops it. He very rarely takes the place of a Wither Skeleton.",
                        "Garou siempre lo suelta. Muy rara vez aparece en lugar de un Esqueleto Wither."))
                .build());
    }

    private static void stands(List<WikiEntry> list) {
        list.add(WikiEntry.info("thearrow", STANDS, Material.SPECTRAL_ARROW, NamedTextColor.GOLD,
                        "Awakening a Stand", "Despertar un Stand")
                .about("The Arrow judges whoever it pierces. DIO's blood makes you a Stand bearer: the Arrow can no "
                                + "longer kill you and always awakens your Stand.",
                        "La Flecha juzga a quien atraviesa. La sangre de DIO te vuelve portador de Stand: la Flecha ya "
                                + "no puede matarte y siempre despierta tu Stand.")
                .source(WikiSource.of(Material.SKELETON_SKULL, "The Archer of the Arrow", "El Archer of the Arrow",
                        "A crowned skeleton that takes the place of some Skeletons. A few of its shots are the Arrow.",
                        "Un esqueleto con corona que ocupa el lugar de algunos Esqueletos. Algunos de sus disparos "
                                + "son la Flecha."))
                .source(WikiSource.of(Material.POTION, "With DIO's blood", "Con la sangre de DIO",
                        "Drink the Bearer's Elixir and you are a Stand bearer: the Arrow never kills you and always "
                                + "awakens a random Stand.",
                        "Bebe el Bearer's Elixir y serás portador de Stand: la Flecha nunca te mata y siempre despierta "
                                + "un Stand al azar."))
                .source(new WikiSource(Material.SPECTRAL_ARROW, "Without it: death chance", "Sin ella: probabilidad de morir",
                        "Without the Elixir, the Arrow kills most of those it pierces. The survivors awaken a Stand.",
                        "Sin el Elixir, la Flecha mata a la mayoría de quienes atraviesa. Los que sobreviven despiertan "
                                + "un Stand.",
                        "entities.arrow-skeleton.death-chance", 0.7, false))
                .source(WikiSource.of(Material.WITHER_SKELETON_SKULL, "Unworthy", "Indigno",
                        "Whoever the Arrow kills is marked unworthy: it gives them no Stand until they drink the "
                                + "Bearer's Elixir, which lifts the mark.",
                        "Quien muere por la Flecha queda marcado como indigno: no le dará un Stand hasta que beba el "
                                + "Bearer's Elixir, que borra la marca."))
                .source(WikiSource.of(Material.KNOWLEDGE_BOOK, "Controls", "Controles",
                        "Sneak + F summons your Stand or sends it back. F uses its ability. /stand shows yours.",
                        "Agachado + F invoca o retira tu Stand. F usa su habilidad. /stand muestra el tuyo."))
                .build());
        list.add(WikiEntry.info("vampirism", STANDS, Material.RED_DYE, NamedTextColor.DARK_RED,
                        "Vampirism", "Vampirismo")
                .about("The Bearer's Elixir also makes you a vampire: Strength and Speed at night, Night Vision and "
                                + "life steal. The sun burns you with true damage under the open sky.",
                        "El Bearer's Elixir también te convierte en vampiro: Fuerza y Velocidad de noche, Visión "
                                + "nocturna y robo de vida. El sol te quema con daño verdadero bajo el cielo abierto.")
                .build());
        stand(list, StandType.HERMIT_PURPLE,
                "Thorned purple vines coil round your arm.",
                "Enredaderas moradas con espinas rodean tu brazo.",
                "F: Spirit Photography. Every living thing nearby glows and the nearest player is pointed out.",
                "F: Fotografía espiritual. Todo ser vivo cercano brilla y se señala al jugador más próximo.");
        stand(list, StandType.MAGICIANS_RED,
                "A bird-headed warrior of fire.",
                "Un guerrero de fuego con cabeza de ave.",
                "F: Crossfire Hurricane. A fan of burning ankhs that sets its targets alight.",
                "F: Crossfire Hurricane. Un abanico de cruces de fuego que incendia a sus objetivos.");
        stand(list, StandType.CRAZY_DIAMOND,
                "A pink Stand covered in hearts, with a diamond on its chest.",
                "Un Stand rosa cubierto de corazones, con un diamante en el pecho.",
                "F: Restoration. Heals the player you look at, or you, and mends the item they hold.",
                "F: Restauración. Cura al jugador que miras, o a ti, y repara el objeto que sostiene.");
        stand(list, StandType.KILLER_QUEEN,
                "A cat-eared Stand with skulls on its belt and hand.",
                "Un Stand con orejas de gato y calaveras en el cinturón y la mano.",
                "Passive: First Bomb, every player you hit explodes. F or /stand sha: Sheer Heart Attack chases the "
                        + "player you choose.",
                "Pasiva: Primera Bomba, cada jugador que golpeas explota. F o /stand sha: Sheer Heart Attack "
                        + "persigue al jugador que elijas.");
        stand(list, StandType.STAR_PLATINUM,
                "A Stand with a black mane, a red scarf and fists of steel.",
                "Un Stand de melena negra, bufanda roja y puños de acero.",
                "F: ORA ORA barrage on whoever stands in front of you. Sneak + left-click with an empty hand: "
                        + "Star Platinum: The World stops time.",
                "F: ráfaga ORA ORA a quien tengas delante. Agachado + clic izquierdo con la mano vacía: Star "
                        + "Platinum: The World detiene el tiempo.");
        stand(list, StandType.THE_WORLD,
                "DIO's own Stand, golden with emerald hearts.",
                "El propio Stand de DIO, dorado y con corazones de esmeralda.",
                "F: MUDA MUDA barrage. Sneak + left-click with an empty hand: ZA WARUDO, a longer time stop.",
                "F: ráfaga MUDA MUDA. Agachado + clic izquierdo con la mano vacía: ZA WARUDO, una detención del "
                        + "tiempo más larga.");
    }

    private static void stand(List<WikiEntry> list, StandType type, String lookEn, String lookEs,
                              String powerEn, String powerEs) {
        list.add(WikiEntry.info(type.key(), STANDS, type.icon(), type.textColor(),
                        "「" + type.displayName() + "」", "「" + type.displayName() + "」")
                .about(lookEn, lookEs)
                .source(WikiSource.of(Material.BLAZE_POWDER, "Abilities", "Habilidades", powerEn, powerEs))
                .source(new WikiSource(Material.SPECTRAL_ARROW, "Rarity", "Rareza",
                        "How likely the Arrow is to awaken this Stand, out of every Stand.",
                        "Probabilidad de que la Flecha despierte este Stand, entre todos los Stands.",
                        "stands." + type.key() + ".weight", type.defaultWeight(), false))
                .build());
    }

    private static void bosses(List<WikiEntry> list) {
        list.add(WikiEntry.info("bossdimension", BOSSES, Material.CRYING_OBSIDIAN, NamedTextColor.DARK_PURPLE,
                        "The Boss Dimension", "La Dimensión de Jefes")
                .about("Every great boss is fought in a wasteland of its own. Build the Ritual Structure in the "
                                + "overworld to get there.",
                        "Cada gran jefe se enfrenta en un páramo propio. Construye la Estructura Ritual en el mundo "
                                + "normal para llegar.")
                .source(WikiSource.of(Material.OBSIDIAN, "The Ritual Structure", "La Estructura Ritual",
                        "A 7x7 platform of blackstone with crying obsidian and obsidian in the middle, ringed by 12 "
                                + "candles. Light them all and stand inside for five seconds.",
                        "Una plataforma de 7x7 de piedra negra con obsidiana llorosa y obsidiana en el centro, rodeada "
                                + "de 12 velas. Enciéndelas todas y quédate dentro cinco segundos."))
                .build());
        list.add(WikiEntry.info("obsidiansentinel", BOSSES, Material.ARMOR_STAND, NamedTextColor.DARK_GRAY,
                        "The Obsidian Sentinel", "The Obsidian Sentinel")
                .about("The final boss: a giant knight with a spear and a shield of obsidian.",
                        "El jefe final: un caballero gigante con lanza y escudo de obsidiana.")
                .source(WikiSource.of(Material.RED_CANDLE, "Invocation Circle", "Círculo de Invocación",
                        "In the Boss Dimension, light a 5x5 ring of 12 red candles and drop an Echo Shard in the middle.",
                        "En la Dimensión de Jefes, enciende un anillo de 5x5 con 12 velas rojas y suelta un Fragmento "
                                + "de Eco en el centro."))
                .source(WikiSource.of(Material.NETHER_STAR, "Loot", "Botín",
                        "The Sentinel Core.", "El Sentinel Core."))
                .build());
        list.add(WikiEntry.info("nix", BOSSES, Material.NETHERITE_AXE, NamedTextColor.DARK_RED,
                        "NIX - The Executioner", "NIX - The Executioner")
                .about("A hooded executioner with chains and a great axe.",
                        "Un verdugo encapuchado con cadenas y una gran hacha.")
                .source(WikiSource.of(Material.ANVIL, "The Executioner's Scaffold", "El Patíbulo del Verdugo",
                        "In the Boss Dimension: an anvil, 4 lit red candles beside it and 4 gallows in the corners "
                                + "(base, chain, skull). Right-click a candle, then drop an Execution Warrant on the anvil.",
                        "En la Dimensión de Jefes: un yunque, 4 velas rojas encendidas a su lado y 4 horcas en las "
                                + "esquinas (base, cadena, calavera). Clic derecho en una vela y suelta un Execution "
                                + "Warrant sobre el yunque."))
                .source(WikiSource.of(Material.NETHERITE_SCRAP, "Loot", "Botín",
                        "Executioner's Edges.", "Executioner's Edge."))
                .build());
        list.add(WikiEntry.info("dio", BOSSES, Material.CLOCK, NamedTextColor.GOLD,
                        "DIO", "DIO")
                .about("DIO and The World. He stops time, throws knives and drops a road roller.",
                        "DIO y The World. Detiene el tiempo, lanza cuchillos y deja caer una apisonadora.")
                .source(WikiSource.of(Material.GOLD_BLOCK, "The World's Throne", "El Trono de The World",
                        "In the Boss Dimension: a gold block, 4 lit yellow candles beside it and 4 pillars in the "
                                + "corners (gold block, emerald block, a head). Right-click a candle, then drop a Clock.",
                        "En la Dimensión de Jefes: un bloque de oro, 4 velas amarillas encendidas a su lado y 4 "
                                + "pilares en las esquinas (oro, esmeralda, una cabeza). Clic derecho en una vela y suelta "
                                + "un Reloj."))
                .source(WikiSource.of(Material.RED_DYE, "Loot", "Botín",
                        "Vampire Blood.", "Vampire Blood."))
                .build());
        list.add(WikiEntry.info("jackstar", BOSSES, Material.COMMAND_BLOCK, NamedTextColor.DARK_AQUA,
                        "JackStar - The System Architect", "JackStar - The System Architect")
                .about("The architect of the server. He revives twice and calls other bosses as subprocesses.",
                        "El arquitecto del servidor. Revive dos veces e invoca a otros jefes como subprocesos.")
                .source(WikiSource.of(Material.LODESTONE, "The Multiverse Terminal", "La Terminal del Multiverso",
                        "In the Boss Dimension: a respawn anchor or lodestone, 4 lit cyan or light blue candles beside "
                                + "it and a lightning rod on each corner pillar. Offer an Architect Kernel, a Nether Star, "
                                + "an Echo Shard, a Heart of the Sea or a Beacon.",
                        "En la Dimensión de Jefes: un nexo de reaparición o una magnetita, 4 velas cian o azul claro "
                                + "encendidas a su lado y un pararrayos en cada pilar de esquina. Ofrece un Architect "
                                + "Kernel, una Estrella del Nether, un Fragmento de Eco, un Corazón del Mar o un Faro."))
                .source(WikiSource.of(Material.NETHER_STAR, "Loot", "Botín",
                        "The Architect Kernel.", "El Architect Kernel."))
                .build());
        list.add(WikiEntry.info("mahoraga", BOSSES, Material.ZOMBIE_HEAD, NamedTextColor.WHITE,
                        "Mahoraga", "Mahoraga")
                .about("The Divine General. He adapts to whatever hurts him.",
                        "El General Divino. Se adapta a todo lo que le hace daño.")
                .source(WikiSource.of(Material.ZOMBIE_SPAWN_EGG, "Where", "Dónde",
                        "Very rarely rises in place of a Zombie in the overworld.",
                        "Muy rara vez aparece en lugar de un Zombi en el mundo normal."))
                .source(WikiSource.of(Material.NETHERITE_SCRAP, "Loot", "Botín",
                        "Wheel Essence.", "Wheel Essence."))
                .build());
        list.add(WikiEntry.info("garou", BOSSES, Material.WITHER_SKELETON_SKULL, NamedTextColor.DARK_PURPLE,
                        "Garou", "Garou")
                .about("The Hero Hunter, a martial artist who fights barehanded.",
                        "El Cazador de Héroes, un artista marcial que pelea con las manos desnudas.")
                .source(WikiSource.of(Material.WITHER_SKELETON_SPAWN_EGG, "Where", "Dónde",
                        "Very rarely takes the place of a Wither Skeleton.",
                        "Muy rara vez aparece en lugar de un Esqueleto Wither."))
                .source(WikiSource.of(Material.NETHER_STAR, "Loot", "Botín",
                        "The Garou Cosmic Core.", "El Garou Cosmic Core."))
                .build());
    }
}
