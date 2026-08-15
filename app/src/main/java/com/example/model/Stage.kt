package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TapoutBrightRed
import com.example.ui.theme.TapoutCrimson
import com.example.ui.theme.TapoutGold
import com.example.ui.theme.TapoutNeonBlue
import com.example.ui.theme.TapoutNeonPurple
import com.example.ui.theme.TapoutOrange

data class Stage(
    val id: String,
    val stageNumber: Int,
    val name: String,
    val subtitle: String,
    val era: String,
    val skyTop: Color,
    val skyBottom: Color,
    val floorColor: Color,
    val floorAccent: Color,
    val ambientGlow: Color,
    val lore: String
)

object StageRoster {
    val stages: List<Stage> = listOf(
        Stage(
            id = "stage1",
            stageNumber = 1,
            name = "KYOTO CHERRY SHADOWS",
            subtitle = "Moonlit Pagoda Courtyard",
            era = "1580 FEUDAL JAPAN",
            skyTop = Color(0xFF0F0022),
            skyBottom = Color(0xFF280B3B),
            floorColor = Color(0xFF1E0A28),
            floorAccent = Color(0xFFFF70A0),
            ambientGlow = Color(0x40FF69B4),
            lore = "Under blood moons and drifting sakura petals, ancient ninja clans fought for regional dominance."
        ),
        Stage(
            id = "stage2",
            stageNumber = 2,
            name = "NEO TOKYO SKYLINE",
            subtitle = "Megatower Helipad 99",
            era = "2099 CYBERPUNK ERA",
            skyTop = Color(0xFF02091A),
            skyBottom = Color(0xFF0D1F38),
            floorColor = Color(0xFF0A1524),
            floorAccent = TapoutNeonBlue,
            ambientGlow = Color(0x4000E5FF),
            lore = "Hovering 300 stories above neon-drenched megacity skyscrapers with holographic advertisements."
        ),
        Stage(
            id = "stage3",
            stageNumber = 3,
            name = "BROOKLYN BRICK ALLEY",
            subtitle = "Underground Boxing Ring",
            era = "1974 GOLDEN AGE",
            skyTop = Color(0xFF1A0A00),
            skyBottom = Color(0xFF331400),
            floorColor = Color(0xFF221105),
            floorAccent = TapoutOrange,
            ambientGlow = Color(0x40FF9900),
            lore = "Dim street lamps, steam vents, and rain-slicked asphalt where bare-knuckle prize fighters forged their legend."
        ),
        Stage(
            id = "stage4",
            stageNumber = 4,
            name = "IMPERIAL COLISEUM",
            subtitle = "Sands of the Caesar",
            era = "79 AD ANCIENT ROME",
            skyTop = Color(0xFF1F0008),
            skyBottom = Color(0xFF400A18),
            floorColor = Color(0xFF2B0E14),
            floorAccent = TapoutGold,
            ambientGlow = Color(0x40FFD700),
            lore = "Massive stone columns and roaring crowds cheering gladiators fighting under golden torchlight."
        ),
        Stage(
            id = "stage5",
            stageNumber = 5,
            name = "QUANTUM NEXUS VOID",
            subtitle = "Chrono Time Rift",
            era = "END OF TIME",
            skyTop = Color(0xFF000511),
            skyBottom = Color(0xFF0B1229),
            floorColor = Color(0xFF070B1A),
            floorAccent = TapoutNeonPurple,
            ambientGlow = Color(0x40B300FF),
            lore = "The convergence point of all broken timelines, where spacetime shards drift across a zero-gravity horizon."
        ),
        Stage(
            id = "stage6",
            stageNumber = 6,
            name = "CRIMSON TORII SHRINE",
            subtitle = "Sacred Mountain Gateways",
            era = "ETERNAL SPIRIT REALM",
            skyTop = Color(0xFF24000E),
            skyBottom = Color(0xFF4D001C),
            floorColor = Color(0xFF24040F),
            floorAccent = TapoutBrightRed,
            ambientGlow = Color(0x40FF2A55),
            lore = "Endless red timber torii gates winding up misty peaks guarding ancient guardian spirits."
        ),
        Stage(
            id = "stage7",
            stageNumber = 7,
            name = "HEX CONTAINMENT LAB",
            subtitle = "Cybernetic Synthesis Matrix",
            era = "2140 SYNTHETIC WAR",
            skyTop = Color(0xFF00141A),
            skyBottom = Color(0xFF002B33),
            floorColor = Color(0xFF001B20),
            floorAccent = Color(0xFF00FFCC),
            ambientGlow = Color(0x4000FFCC),
            lore = "Automated defense lasers and pulsating server racks where synthetic combat bio-drones are weaponized."
        ),
        Stage(
            id = "stage8",
            stageNumber = 8,
            name = "VOLCANIC CRUCIBLE",
            subtitle = "Molten Obsidian Core",
            era = "PREHISTORIC CATACLYSM",
            skyTop = Color(0xFF2B0500),
            skyBottom = Color(0xFF5E1100),
            floorColor = Color(0xFF2E0900),
            floorAccent = Color(0xFFFF4500),
            ambientGlow = Color(0x40FF4500),
            lore = "Cascading lava rivers and geothermal geysers testing the endurance of warlords in extreme heat."
        ),
        Stage(
            id = "stage9",
            stageNumber = 9,
            name = "CELESTIAL PANTHEON",
            subtitle = "Summit of Immortals",
            era = "GODS & MYTHOLOGY",
            skyTop = Color(0xFF140026),
            skyBottom = Color(0xFF380066),
            floorColor = Color(0xFF1C0633),
            floorAccent = Color(0xFFFFD700),
            ambientGlow = Color(0x60FFE066),
            lore = "The final championship arena resting above golden cloud banks where only true time champions survive."
        )
    )

    fun getStageByNumber(num: Int): Stage =
        stages.find { it.stageNumber == num } ?: stages.first()

    fun getStageById(id: String): Stage =
        stages.find { it.id == id } ?: stages.first()
}
