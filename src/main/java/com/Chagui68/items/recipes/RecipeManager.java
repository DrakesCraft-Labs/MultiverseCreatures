package com.Chagui68.items.recipes;

import com.Chagui68.items.armor.EightHandledWheel;
import com.Chagui68.items.armor.ObsidianBastion;
import com.Chagui68.items.components.BoneMarrow;
import com.Chagui68.items.components.ChaosCore;
import com.Chagui68.items.components.ChaosFragment;
import com.Chagui68.items.components.ChaosOrb;
import com.Chagui68.items.components.ChaosPowder;
import com.Chagui68.items.components.CompressedGoldBlock;
import com.Chagui68.items.components.CondensedChaosOrb;
import com.Chagui68.items.components.EnderCore;
import com.Chagui68.items.components.EnderFragment;
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
import com.Chagui68.items.components.RefinedWheelCore;
import com.Chagui68.items.components.SentinelCore;
import com.Chagui68.items.components.RefinedNetherite;
import com.Chagui68.items.components.ReinforcedBone;
import com.Chagui68.items.components.ReinforcedBoneBlock;
import com.Chagui68.items.components.ShadowCloak;
import com.Chagui68.items.components.StarCore;
import com.Chagui68.items.components.StormCrystal;
import com.Chagui68.items.components.SwordMold;
import com.Chagui68.items.components.VenomGland;
import com.Chagui68.items.components.VoidEssence;
import com.Chagui68.items.components.WheelCore;
import com.Chagui68.items.components.WheelEssence;
import com.Chagui68.items.food.HeadSlimeGelatin;
import com.Chagui68.items.misc.MilitaryMine;
import com.Chagui68.items.misc.offhand.FrostHeartOffhand;
import com.Chagui68.items.misc.offhand.MarrowAegis;
import com.Chagui68.items.misc.offhand.VeilwalkerMantle;
import com.Chagui68.items.weapons.magic.ChaosForge;
import com.Chagui68.items.weapons.magic.SentinelGrimoire;
import com.Chagui68.items.weapons.magic.SkyfireTalisman;
import com.Chagui68.items.weapons.melee.CinderGreatsword;
import com.Chagui68.items.weapons.melee.NullshearEdge;
import com.Chagui68.items.weapons.melee.SoulreapScythe;
import com.Chagui68.items.weapons.melee.Venomfang;
import com.Chagui68.items.weapons.ranged.AetherPullshot;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

public class RecipeManager {

    private RecipeManager() {
    }

    public static void registerRecipes() {
        registerStarCore();
        registerSwordMold();
        registerReinforcedBoneBlock();
        registerEnderCore();
        registerChaosCompressionChain();
        registerMilitaryMine();
        registerHeadSlimeGelatin();
        registerVenomfang();
        registerFrostHeartOffhand();
        registerSkyfireTalisman();
        registerMarrowAegis();
        registerMarrowChain();
        registerEightHandledWheel();
        registerSoulreapScythe();
        registerChaosForge();
        registerAetherPullshot();
        registerNullshearEdge();
        registerVeilwalkerMantle();
        registerCinderGreatsword();
        registerObsidianBastion();
        registerCompressedGoldBlock();
        registerMultiversalCore();
        registerSentinelGrimoire();
    }

    private static NamespacedKey key(String name) {
        return new NamespacedKey("multiversecreatures", name);
    }

    private static void registerStarCore() {
        ShapedRecipe recipe = new ShapedRecipe(key("star_core"), StarCore.STAR_CORE.clone());
        recipe.shape("NBN", "BSB", "NBN");
        recipe.setIngredient('N', Material.NETHERITE_BLOCK);
        recipe.setIngredient('B', Material.DIAMOND_BLOCK);
        recipe.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(recipe);
    }

    private static void registerSwordMold() {
        ShapedRecipe recipe = new ShapedRecipe(key("sword_mold"), SwordMold.SWORD_MOLD.clone());
        recipe.shape("IAI", "AIA", "IAI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('A', Material.IRON_BLOCK);
        Bukkit.addRecipe(recipe);
    }

    private static void registerReinforcedBoneBlock() {
        ShapedRecipe recipe = new ShapedRecipe(key("reinforced_bone_block"), ReinforcedBoneBlock.REINFORCED_BONE_BLOCK.clone());
        recipe.shape("RRR", "RRR", "RRR");
        recipe.setIngredient('R', new RecipeChoice.ExactChoice(ReinforcedBone.REINFORCED_BONE.clone()));
        Bukkit.addRecipe(recipe);
    }

    private static void registerEnderCore() {
        ShapedRecipe recipe = new ShapedRecipe(key("ender_core"), EnderCore.ENDER_CORE.clone());
        recipe.shape("DFD", "FNF", "DFD");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('F', new RecipeChoice.ExactChoice(EnderFragment.ENDER_FRAGMENT.clone()));
        recipe.setIngredient('N', Material.NETHER_STAR);
        Bukkit.addRecipe(recipe);
    }

    // Chaos compression chain: Powder -> Fragment -> Core -> Condensed Chaos Orb
    private static void registerChaosCompressionChain() {
        ShapedRecipe powder = new ShapedRecipe(key("chaos_powder"), ChaosPowder.CHAOS_POWDER.clone());
        powder.shape(".G.", "GOG", ".G.");
        powder.setIngredient('G', Material.GLOWSTONE_DUST);
        powder.setIngredient('O', new RecipeChoice.ExactChoice(ChaosOrb.CHAOS_ORB.clone()));
        Bukkit.addRecipe(powder);

        ShapedRecipe fragment = new ShapedRecipe(key("chaos_fragment"), ChaosFragment.CHAOS_FRAGMENT.clone());
        fragment.shape("PPP", "POP", "PPP");
        fragment.setIngredient('P', new RecipeChoice.ExactChoice(ChaosPowder.CHAOS_POWDER.clone()));
        fragment.setIngredient('O', new RecipeChoice.ExactChoice(ChaosOrb.CHAOS_ORB.clone()));
        Bukkit.addRecipe(fragment);

        ShapedRecipe core = new ShapedRecipe(key("chaos_core"), ChaosCore.CHAOS_CORE.clone());
        core.shape("FOF", "OSO", "FOF");
        core.setIngredient('F', new RecipeChoice.ExactChoice(ChaosFragment.CHAOS_FRAGMENT.clone()));
        core.setIngredient('O', new RecipeChoice.ExactChoice(ChaosOrb.CHAOS_ORB.clone()));
        core.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(core);

        ShapedRecipe condensed = new ShapedRecipe(key("condensed_chaos_orb"), CondensedChaosOrb.CONDENSED_CHAOS_ORB.clone());
        condensed.shape("COC", "OSO", "COC");
        condensed.setIngredient('C', new RecipeChoice.ExactChoice(ChaosCore.CHAOS_CORE.clone()));
        condensed.setIngredient('O', new RecipeChoice.ExactChoice(ChaosOrb.CHAOS_ORB.clone()));
        condensed.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(condensed);
    }

    private static void registerMilitaryMine() {
        ShapedRecipe recipe = new ShapedRecipe(key("military_mine"), MilitaryMine.MILITARY_MINE.clone());
        recipe.shape("IMI", "MTM", "IMI");
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setIngredient('T', Material.TNT);
        recipe.setIngredient('M', new RecipeChoice.ExactChoice(MilitaryComponent.MILITARY_COMPONENT.clone()));
        Bukkit.addRecipe(recipe);
    }

    private static void registerHeadSlimeGelatin() {
        ShapedRecipe recipe = new ShapedRecipe(key("head_slime_gelatin"), HeadSlimeGelatin.HEAD_SLIME_GELATIN.clone());
        recipe.shape("ASA", "SHS", "ASA");
        recipe.setIngredient('A', Material.APPLE);
        recipe.setIngredient('S', Material.SLIME_BALL);
        recipe.setIngredient('H', new RecipeChoice.ExactChoice(HeadSlimeHeart.HEAD_SLIME_HEART.clone()));
        Bukkit.addRecipe(recipe);
    }

    // Venomfang (dagger, low tier)
    private static void registerVenomfang() {
        ShapedRecipe recipe = new ShapedRecipe(key("venomfang"), Venomfang.VENOMFANG.clone());
        recipe.shape("GVG", "VMV", "VSV");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('V', new RecipeChoice.ExactChoice(VenomGland.VENOM_GLAND.clone()));
        recipe.setIngredient('M', new RecipeChoice.ExactChoice(SwordMold.SWORD_MOLD.clone()));
        recipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(recipe);
    }

    // Frost Heart off-hand (low tier)
    private static void registerFrostHeartOffhand() {
        ShapedRecipe recipe = new ShapedRecipe(key("frost_heart_offhand"), FrostHeartOffhand.FROST_HEART_OFFHAND.clone());
        recipe.shape("IBI", "BHB", "IBI");
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setIngredient('B', Material.BLUE_ICE);
        recipe.setIngredient('H', new RecipeChoice.ExactChoice(FrostHeart.FROST_HEART.clone()));
        Bukkit.addRecipe(recipe);
    }

    // Skyfire Talisman (mid tier)
    private static void registerSkyfireTalisman() {
        ShapedRecipe recipe = new ShapedRecipe(key("skyfire_talisman"), SkyfireTalisman.SKYFIRE_TALISMAN.clone());
        recipe.shape("SGS", "GQG", "SGS");
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(StormCrystal.STORM_CRYSTAL.clone()));
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('Q', Material.QUARTZ);
        Bukkit.addRecipe(recipe);
    }

    // Bone Marrow -> Ossified Plate -> [blast furnace only] Molten Marrow -> Marrow Aegis (high tier)
    private static void registerMarrowChain() {
        ShapedRecipe marrowRecipe = new ShapedRecipe(key("bone_marrow"), BoneMarrow.BONE_MARROW.clone());
        marrowRecipe.shape("BRB", "BWB", "BRB");
        marrowRecipe.setIngredient('B', new RecipeChoice.ExactChoice(ReinforcedBone.REINFORCED_BONE.clone()));
        marrowRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
        marrowRecipe.setIngredient('W', Material.NETHER_WART);
        Bukkit.addRecipe(marrowRecipe);

        ShapedRecipe plateRecipe = new ShapedRecipe(key("ossified_plate"), OssifiedPlate.OSSIFIED_PLATE.clone());
        plateRecipe.shape("CMC", "MDM", "CMC");
        plateRecipe.setIngredient('C', Material.CALCITE);
        plateRecipe.setIngredient('M', new RecipeChoice.ExactChoice(BoneMarrow.BONE_MARROW.clone()));
        plateRecipe.setIngredient('D', Material.DIAMOND);
        Bukkit.addRecipe(plateRecipe);

        BlastingRecipe moltenMarrow = new BlastingRecipe(key("molten_marrow_blast"), MoltenMarrow.MOLTEN_MARROW.clone(),
                new RecipeChoice.ExactChoice(OssifiedPlate.OSSIFIED_PLATE.clone()), 0.5f, 100);
        Bukkit.addRecipe(moltenMarrow);
    }

    // Marrow Aegis (high tier)
    private static void registerMarrowAegis() {
        ShapedRecipe recipe = new ShapedRecipe(key("marrow_aegis"), MarrowAegis.MARROW_AEGIS.clone());
        recipe.shape("DPD", "PMP", "DPD");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('P', new RecipeChoice.ExactChoice(OssifiedPlate.OSSIFIED_PLATE.clone()));
        recipe.setIngredient('M', new RecipeChoice.ExactChoice(MoltenMarrow.MOLTEN_MARROW.clone()));
        Bukkit.addRecipe(recipe);
    }

    // Wheel Core -> Molten (blast furnace) -> Refined Wheel Core -> Eight-Handled Wheel (very high tier)
    private static void registerEightHandledWheel() {
        ShapedRecipe coreRecipe = new ShapedRecipe(key("wheel_core"), WheelCore.WHEEL_CORE.clone());
        coreRecipe.shape("WDW", "DED", "WDW");
        coreRecipe.setIngredient('W', new RecipeChoice.ExactChoice(WheelEssence.WHEEL_ESSENCE.clone()));
        coreRecipe.setIngredient('D', Material.DIAMOND_BLOCK);
        coreRecipe.setIngredient('E', Material.NETHER_STAR);
        Bukkit.addRecipe(coreRecipe);

        RecipeChoice wheelCoreChoice = new RecipeChoice.MaterialChoice(WheelCore.WHEEL_CORE.getType());

        BlastingRecipe moltenCore = new BlastingRecipe(key("molten_wheel_core_blast"), MoltenWheelCore.MOLTEN_WHEEL_CORE.clone(),
                wheelCoreChoice, 0.5f, 100);
        Bukkit.addRecipe(moltenCore);

        FurnaceRecipe moltenCoreFurnace = new FurnaceRecipe(key("molten_wheel_core_furnace"), MoltenWheelCore.MOLTEN_WHEEL_CORE.clone(),
                wheelCoreChoice, 0.5f, 100);
        Bukkit.addRecipe(moltenCoreFurnace);

        RecipeChoice netheriteChoice = new RecipeChoice.MaterialChoice(RefinedNetherite.REFINED_NETHERITE.getType());

        BlastingRecipe moltenNetherite = new BlastingRecipe(key("molten_netherite_blast"), MoltenNetherite.MOLTEN_NETHERITE.clone(),
                netheriteChoice, 0.5f, 100);
        Bukkit.addRecipe(moltenNetherite);

        FurnaceRecipe moltenNetheriteFurnace = new FurnaceRecipe(key("molten_netherite_furnace"), MoltenNetherite.MOLTEN_NETHERITE.clone(),
                netheriteChoice, 0.5f, 100);
        Bukkit.addRecipe(moltenNetheriteFurnace);

        ShapedRecipe refinedMix = new ShapedRecipe(key("refined_wheel_core"), RefinedWheelCore.REFINED_WHEEL_CORE.clone());
        refinedMix.shape("AB");
        refinedMix.setIngredient('A', new RecipeChoice.ExactChoice(MoltenWheelCore.MOLTEN_WHEEL_CORE.clone()));
        refinedMix.setIngredient('B', new RecipeChoice.ExactChoice(MoltenNetherite.MOLTEN_NETHERITE.clone()));
        Bukkit.addRecipe(refinedMix);

        ShapedRecipe recipe = new ShapedRecipe(key("eight_handled_wheel"), EightHandledWheel.EIGHT_HANDLED_WHEEL.clone());
        recipe.shape(".N.", "NWN", ".N.");
        recipe.setIngredient('N', Material.NETHERITE_BLOCK);
        recipe.setIngredient('W', new RecipeChoice.ExactChoice(RefinedWheelCore.REFINED_WHEEL_CORE.clone()));
        Bukkit.addRecipe(recipe);
    }

    // Reaper Core (intermediate component) -> Soulreap Scythe (high tier)
    private static void registerSoulreapScythe() {
        ShapedRecipe coreRecipe = new ShapedRecipe(key("reaper_core"), ReaperCore.REAPER_CORE.clone());
        coreRecipe.shape("RNR", "NSN", "RNR");
        coreRecipe.setIngredient('R', new RecipeChoice.ExactChoice(ReaperEssence.REAPER_ESSENCE.clone()));
        coreRecipe.setIngredient('N', Material.SOUL_SAND);
        coreRecipe.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe(coreRecipe);

        ShapedRecipe recipe = new ShapedRecipe(key("soulreap_scythe"), SoulreapScythe.SOULREAP_SCYTHE.clone());
        recipe.shape(" R ", "CR ", "NS ");
        recipe.setIngredient('R', new RecipeChoice.ExactChoice(ReaperCore.REAPER_CORE.clone()));
        recipe.setIngredient('C', Material.SOUL_SAND);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(recipe);
    }

    // Chaos Forge (high tier)
    private static void registerChaosForge() {
        ShapedRecipe recipe = new ShapedRecipe(key("chaos_forge"), ChaosForge.CHAOS_FORGE.clone());
        recipe.shape("CCC", "ONO", "ONO");
        recipe.setIngredient('C', new RecipeChoice.ExactChoice(ChaosOrb.CHAOS_ORB.clone()));
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        Bukkit.addRecipe(recipe);
    }

    // Aether Pullshot (high tier)
    private static void registerAetherPullshot() {
        ShapedRecipe recipe = new ShapedRecipe(key("aether_pullshot"), AetherPullshot.AETHER_PULLSHOT.clone());
        recipe.shape("DFD", "EFE", "DSD");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('F', Material.END_CRYSTAL);
        recipe.setIngredient('E', new RecipeChoice.ExactChoice(EnderFragment.ENDER_FRAGMENT.clone()));
        recipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(recipe);
    }

    // Nullshear Edge (high tier)
    private static void registerNullshearEdge() {
        ShapedRecipe recipe = new ShapedRecipe(key("nullshear_edge"), NullshearEdge.NULLSHEAR_EDGE.clone());
        recipe.shape("VVV", "VEV", "NMN");
        recipe.setIngredient('V', new RecipeChoice.ExactChoice(VoidEssence.VOID_ESSENCE.clone()));
        recipe.setIngredient('E', new RecipeChoice.ExactChoice(EnderCore.ENDER_CORE.clone()));
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('M', new RecipeChoice.ExactChoice(SwordMold.SWORD_MOLD.clone()));
        Bukkit.addRecipe(recipe);
    }

    // Veilwalker Mantle (high tier)
    private static void registerVeilwalkerMantle() {
        ShapedRecipe recipe = new ShapedRecipe(key("veilwalker_mantle"), VeilwalkerMantle.VEILWALKER_MANTLE.clone());
        recipe.shape("SGS", "GNG", "SGS");
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(ShadowCloak.SHADOW_CLOAK.clone()));
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('N', Material.NETHER_STAR);
        Bukkit.addRecipe(recipe);
    }

    // Cinder Greatsword (very high tier)
    private static void registerCinderGreatsword() {
        ShapedRecipe recipe = new ShapedRecipe(key("cinder_greatsword"), CinderGreatsword.CINDER_GREATSWORD.clone());
        recipe.shape("MMM", "MCM", "N N");
        recipe.setIngredient('M', new RecipeChoice.ExactChoice(MagmaCore.MAGMA_CORE.clone()));
        recipe.setIngredient('C', Material.COAL_BLOCK);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        Bukkit.addRecipe(recipe);
    }

    // Compressed Gold Block (intermediate component) -> Refined Netherite
    private static void registerCompressedGoldBlock() {
        ShapedRecipe recipe = new ShapedRecipe(key("compressed_gold_block"), CompressedGoldBlock.COMPRESSED_GOLD_BLOCK.clone());
        recipe.shape("GGG", "GGG", "GGG");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        Bukkit.addRecipe(recipe);
    }

    // Refined Netherite (intermediate component) -> Obsidian Bastion set (very high tier)
    private static void registerObsidianBastion() {
        ShapedRecipe refinedRecipe = new ShapedRecipe(key("refined_netherite"), RefinedNetherite.REFINED_NETHERITE.clone());
        refinedRecipe.shape("SNS", "NGN", "SNS");
        refinedRecipe.setIngredient('S', new RecipeChoice.ExactChoice(StarCore.STAR_CORE.clone()));
        refinedRecipe.setIngredient('N', Material.NETHERITE_SCRAP);
        refinedRecipe.setIngredient('G', new RecipeChoice.ExactChoice(CompressedGoldBlock.COMPRESSED_GOLD_BLOCK.clone()));
        Bukkit.addRecipe(refinedRecipe);

        RecipeChoice refinedChoice = new RecipeChoice.ExactChoice(RefinedNetherite.REFINED_NETHERITE.clone());

        ShapedRecipe helm = new ShapedRecipe(key("obsidian_bastion_helmet"), ObsidianBastion.HELMET.clone());
        helm.shape("ONO", "O O");
        helm.setIngredient('O', new RecipeChoice.ExactChoice(ObsidianShard.OBSIDIAN_SHARD.clone()));
        helm.setIngredient('N', refinedChoice);
        Bukkit.addRecipe(helm);

        ShapedRecipe chest = new ShapedRecipe(key("obsidian_bastion_chestplate"), ObsidianBastion.CHESTPLATE.clone());
        chest.shape("ONO", "OBO", "OOO");
        chest.setIngredient('O', new RecipeChoice.ExactChoice(ObsidianShard.OBSIDIAN_SHARD.clone()));
        chest.setIngredient('N', refinedChoice);
        chest.setIngredient('B', Material.DIAMOND_BLOCK);
        Bukkit.addRecipe(chest);

        ShapedRecipe legs = new ShapedRecipe(key("obsidian_bastion_leggings"), ObsidianBastion.LEGGINGS.clone());
        legs.shape("ONO", "O O", "O O");
        legs.setIngredient('O', new RecipeChoice.ExactChoice(ObsidianShard.OBSIDIAN_SHARD.clone()));
        legs.setIngredient('N', refinedChoice);
        Bukkit.addRecipe(legs);

        ShapedRecipe boots = new ShapedRecipe(key("obsidian_bastion_boots"), ObsidianBastion.BOOTS.clone());
        boots.shape("O O", "ONO");
        boots.setIngredient('O', new RecipeChoice.ExactChoice(ObsidianShard.OBSIDIAN_SHARD.clone()));
        boots.setIngredient('N', refinedChoice);
        Bukkit.addRecipe(boots);
    }

    // Sentinel Core (boss drop) -> Multiversal Core (apex component)
    private static void registerMultiversalCore() {
        ShapedRecipe recipe = new ShapedRecipe(key("multiversal_core"), MultiversalCore.MULTIVERSAL_CORE.clone());
        recipe.shape("NSN", "SRS", "NSN");
        recipe.setIngredient('N', new RecipeChoice.ExactChoice(RefinedNetherite.REFINED_NETHERITE.clone()));
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(StarCore.STAR_CORE.clone()));
        recipe.setIngredient('R', new RecipeChoice.ExactChoice(SentinelCore.SENTINEL_CORE.clone()));
        Bukkit.addRecipe(recipe);
    }

    // Sentinel Grimoire (apex weapon): Multiversal Core + Sentinel Core + books
    private static void registerSentinelGrimoire() {
        ShapedRecipe recipe = new ShapedRecipe(key("sentinel_grimoire"), SentinelGrimoire.GRIMOIRE.clone());
        recipe.shape(".B.", "MSM", ".B.");
        recipe.setIngredient('B', Material.BOOK);
        recipe.setIngredient('M', new RecipeChoice.ExactChoice(MultiversalCore.MULTIVERSAL_CORE.clone()));
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(SentinelCore.SENTINEL_CORE.clone()));
        Bukkit.addRecipe(recipe);
    }
}
