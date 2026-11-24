package com.example.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

enum class EditCategory(
    val title: String,
    val shortLabel: String,
    val description: String
) {
    BACKGROUND(
        title = "AI Background",
        shortLabel = "Background",
        description = "Describe any scene, studio, or atmosphere to place behind your subject"
    ),
    CLOTHES(
        title = "AI Wardrobe",
        shortLabel = "Clothes",
        description = "Swap outfits, jackets, gowns, or textures while keeping your look natural"
    ),
    HAIRSTYLE(
        title = "AI Hairstyle",
        shortLabel = "Hairstyle",
        description = "Try new haircuts, waves, curls, or studio hair colors effortlessly"
    )
}

enum class OutputQuality(
    val label: String,
    val badge: String
) {
    STANDARD_1K("Standard 1K", "1K"),
    HD_2K("Studio HD 2K", "2K HD")
}

enum class AspectPreset(val label: String) {
    AUTO("Original"),
    PORTRAIT_3_4("3:4"),
    SQUARE_1_1("1:1"),
    LANDSCAPE_16_9("16:9"),
    STORY_9_16("9:16")
}

enum class BokehLevel(val label: String, val promptDirective: String) {
    NATURAL("Natural Depth", "natural optical depth and crisp subject edge separation"),
    SHALLOW("f/1.8 Bokeh", "cinematic shallow depth of field with creamy f/1.8 background bokeh"),
    SHARP("Deep Focus", "sharp architectural detail across both subject and background")
}

enum class CompareMode {
    EDITED,
    ORIGINAL,
    SPLIT
}

@Immutable
data class StylePreset(
    val id: String,
    val category: EditCategory,
    val title: String,
    val subtitle: String,
    val prompt: String
)

@Immutable
data class EditStep(
    val id: String,
    val bitmap: Bitmap,
    val category: EditCategory?,
    val summary: String,
    val quality: OutputQuality,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Immutable
data class SavedCreation(
    val id: String,
    val filePath: String,
    val mediaStoreUri: String?,
    val title: String,
    val categoryLabel: String,
    val width: Int,
    val height: Int,
    val createdAtMillis: Long
)

object StudioPresets {
    val backgroundPresets = listOf(
        StylePreset(
            id = "bg_golden_coast",
            category = EditCategory.BACKGROUND,
            title = "Golden Coast",
            subtitle = "Warm sunset cliffs & ocean breeze",
            prompt = "Sunlit Mediterranean coastal cliffside at golden hour, warm amber sunlight, sparkling turquoise sea in soft distance, natural sea breeze ambiance"
        ),
        StylePreset(
            id = "bg_tokyo_neon",
            category = EditCategory.BACKGROUND,
            title = "Neon Tokyo",
            subtitle = "Rain-slicked cyberpunk bokeh",
            prompt = "Atmospheric rainy Tokyo night street with glowing electric cyan and magenta neon reflections on wet pavement, cinematic shallow bokeh"
        ),
        StylePreset(
            id = "bg_paris_cafe",
            category = EditCategory.BACKGROUND,
            title = "Paris Café",
            subtitle = "Sun-dappled European terrace",
            prompt = "Charming sunlit Parisian sidewalk café terrace with warm limestone facades, bistro chairs, soft morning sunlight filtering through trees"
        ),
        StylePreset(
            id = "bg_editorial_studio",
            category = EditCategory.BACKGROUND,
            title = "Editorial Studio",
            subtitle = "Sculpted luxury key lighting",
            prompt = "High-fashion minimalist photography cyclorama studio with warm taupe gradient backdrop, soft sculpted rim lighting and clean floor shadow"
        ),
        StylePreset(
            id = "bg_alpine_peaks",
            category = EditCategory.BACKGROUND,
            title = "Alpine Mist",
            subtitle = "Snowy mountain dawn vista",
            prompt = "Majestic snow-dusted Swiss alpine mountain valley at crisp blue-hour sunrise with low drifting mist and dramatic horizon glow"
        ),
        StylePreset(
            id = "bg_botanical",
            category = EditCategory.BACKGROUND,
            title = "Glass Conservatory",
            subtitle = "Lush sunlit botanical atrium",
            prompt = "Victorian glass botanical conservatory filled with lush monstera and ferns, soft diffused daylight streaming through glass panes"
        ),
        StylePreset(
            id = "bg_penthouse",
            category = EditCategory.BACKGROUND,
            title = "Skyline Loft",
            subtitle = "Dusk city skyline panorama",
            prompt = "Modern luxury glass penthouse terrace overlooking a twilight city skyline with warm architectural interior accents"
        )
    )

    val clothesPresets = listOf(
        StylePreset(
            id = "cl_blazer",
            category = EditCategory.CLOTHES,
            title = "Tailored Blazer",
            subtitle = "Charcoal wool & crisp tee",
            prompt = "Tailored charcoal Italian wool blazer layered over a crisp heavyweight ivory crew-neck shirt, clean modern editorial fit"
        ),
        StylePreset(
            id = "cl_linen",
            category = EditCategory.CLOTHES,
            title = "Resort Linen",
            subtitle = "Relaxed sage button-down",
            prompt = "Breezy relaxed-fit sage green European linen button-down shirt with softly rolled sleeves and natural fabric drape"
        ),
        StylePreset(
            id = "cl_leather",
            category = EditCategory.CLOTHES,
            title = "Moto Leather",
            subtitle = "Matte black biker jacket",
            prompt = "Structured matte black lambskin leather moto jacket with subtle brushed silver hardware over a fitted dark knit top"
        ),
        StylePreset(
            id = "cl_evening",
            category = EditCategory.CLOTHES,
            title = "Silk Evening",
            subtitle = "Emerald satin drape",
            prompt = "Elegant emerald green silk satin evening attire with luminous sheen, refined tailoring and graceful drape"
        ),
        StylePreset(
            id = "cl_street",
            category = EditCategory.CLOTHES,
            title = "Tech Streetwear",
            subtitle = "Layered utility shell",
            prompt = "Modern monochrome technical streetwear utility jacket with clean matte seams layered over a minimalist charcoal hoodie"
        ),
        StylePreset(
            id = "cl_knit",
            category = EditCategory.CLOTHES,
            title = "Cashmere Knit",
            subtitle = "Warm camel turtleneck",
            prompt = "Plush ribbed camel cashmere turtleneck sweater with rich tactile knit texture and timeless silhouette"
        )
    )

    val hairstylePresets = listOf(
        StylePreset(
            id = "hr_french_bob",
            category = EditCategory.HAIRSTYLE,
            title = "Textured Bob",
            subtitle = "Effortless chin-length layers",
            prompt = "Chic effortless French textured chin-length bob with airy face-framing layers and natural movement"
        ),
        StylePreset(
            id = "hr_waves",
            category = EditCategory.HAIRSTYLE,
            title = "Studio Waves",
            subtitle = "Voluminous glossy waves",
            prompt = "Soft voluminous glossy waves with natural shine and effortless editorial volume"
        ),
        StylePreset(
            id = "hr_bun",
            category = EditCategory.HAIRSTYLE,
            title = "Sleek High Bun",
            subtitle = "Sculpted clean updo",
            prompt = "Polished sleek high bun updo with clean middle part and delicate wispy temple strands"
        ),
        StylePreset(
            id = "hr_curtain",
            category = EditCategory.HAIRSTYLE,
            title = "Curtain Bangs",
            subtitle = "Layered brunette shimmer",
            prompt = "Long layered hair with soft wispy curtain bangs framing the cheekbones and rich dimensional gloss"
        ),
        StylePreset(
            id = "hr_copper",
            category = EditCategory.HAIRSTYLE,
            title = "Copper Auburn",
            subtitle = "Warm dimensional ginger",
            prompt = "Luminous rich copper auburn hair color with natural sunlit highlights and soft layered texture"
        ),
        StylePreset(
            id = "hr_taper",
            category = EditCategory.HAIRSTYLE,
            title = "Textured Taper",
            subtitle = "Modern cropped fade",
            prompt = "Clean modern textured crop on top with neat tapered sides and natural matte styling"
        )
    )

    fun presetsFor(category: EditCategory): List<StylePreset> = when (category) {
        EditCategory.BACKGROUND -> backgroundPresets
        EditCategory.CLOTHES -> clothesPresets
        EditCategory.HAIRSTYLE -> hairstylePresets
    }
}
