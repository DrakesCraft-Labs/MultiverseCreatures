package com.Chagui68.wiki;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

/** The shelves of the wiki, in the order the home page shows them. */
public enum WikiCategory {

    WEAPONS(Material.NETHERITE_SWORD, NamedTextColor.RED, "Weapons", "Armas",
            "Swords, axes, bows, a scythe and spellbooks.",
            "Espadas, hachas, arcos, una guadaña y grimorios."),
    ARMOR(Material.NETHERITE_CHESTPLATE, NamedTextColor.GRAY, "Armor & Off-hands", "Armaduras y mano secundaria",
            "Armor sets, the Eight-Handled Wheel and the relics held in the off-hand.",
            "Sets de armadura, la Eight-Handled Wheel y las reliquias de la mano secundaria."),
    RELICS(Material.SOUL_LANTERN, NamedTextColor.GOLD, "Relics, Tools & Music", "Reliquias, herramientas y música",
            "Crowns, claws, lanterns, mines and music discs from across the multiverse.",
            "Coronas, garras, linternas, minas y discos de música de todo el multiverso."),
    CONSUMABLES(Material.COOKIE, NamedTextColor.YELLOW, "Food & Potions", "Comida y pociones",
            "Things to eat and to drink, DIO's blood included.",
            "Cosas para comer y beber, incluida la sangre de DIO."),
    DROPS(Material.BONE, NamedTextColor.GREEN, "Creature Drops", "Botín de criaturas",
            "What each creature of the multiverse leaves behind.",
            "Lo que deja cada criatura del multiverso al morir."),
    CRAFTED(Material.CRAFTING_TABLE, NamedTextColor.AQUA, "Crafted Components", "Componentes fabricados",
            "Intermediate parts: cores, plates, molds and refined metals.",
            "Piezas intermedias: núcleos, placas, moldes y metales refinados."),
    BOSS_LOOT(Material.NETHER_STAR, NamedTextColor.LIGHT_PURPLE, "Boss Loot & Catalysts", "Botín de jefes y catalizadores",
            "Trophies of the bosses and the offerings that summon them.",
            "Trofeos de los jefes y las ofrendas que los invocan."),
    STANDS(Material.SPECTRAL_ARROW, NamedTextColor.DARK_PURPLE, "Stands", "Stands",
            "The Arrow, DIO's blood and the six Stands it can awaken.",
            "La Flecha, la sangre de DIO y los seis Stands que puede despertar."),
    BOSSES(Material.WITHER_SKELETON_SKULL, NamedTextColor.DARK_RED, "Bosses & Rituals", "Jefes y rituales",
            "Where every boss lives and how to call it.",
            "Dónde vive cada jefe y cómo invocarlo.");

    private final Material icon;
    private final NamedTextColor color;
    private final String nameEn;
    private final String nameEs;
    private final String blurbEn;
    private final String blurbEs;

    WikiCategory(Material icon, NamedTextColor color, String nameEn, String nameEs, String blurbEn, String blurbEs) {
        this.icon = icon;
        this.color = color;
        this.nameEn = nameEn;
        this.nameEs = nameEs;
        this.blurbEn = blurbEn;
        this.blurbEs = blurbEs;
    }

    public Material icon() {
        return icon;
    }

    public NamedTextColor color() {
        return color;
    }

    public String name(WikiLang lang) {
        return lang.pick(nameEn, nameEs);
    }

    public String blurb(WikiLang lang) {
        return lang.pick(blurbEn, blurbEs);
    }
}
