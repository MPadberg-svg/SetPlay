package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// SetPlay Luxury Brand DNA — Core Foundation Tokens
// =========================================================================
val DarkGraphite = Color(0xFF141619)   // Global luxury base & high-contrast dark text on bright pills
val MetalCharcoal = Color(0xFF25282E)  // Shared modal/chrome surface
val AmberGold = Color(0xFFD4A238)      // Brand DNA gold signature
val PaleGold = Color(0xFFF6E8C8)       // Luminous warm ivory-gold headline typography
val Champagne = Color(0xFFDFC69E)      // Warm champagne secondary metadata & subtle borders

// Supporting surface & status tokens harmonized with Brand DNA
val MetalCharcoalElevated = Color(0xFF2F333B)
val ChampagneSubtleBorder = Color(0x4DDFC69E) // 30% Champagne for 0.5dp-1dp luxury borders
val ChampagneMuted = Color(0xFF9E9078)
val LiveRed = Color(0xFFE5484D)
val CatchupGreen = Color(0xFF2EBD85)

// =========================================================================
// 5-Section Bespoke Brand Kit Palettes (From User's 5 Section Color Sheets)
// =========================================================================

// 1. KICKOFF ARENA PALETTE (Legacy tokens preserved for backward compatibility + Official 6-Color Architectural Palette)
val KickoffCeruleanBlue = Color(0xFF0081A7)  // #0081A7 — Legacy Primary Cerulean Ocean Blue
val KickoffTropicalTeal = Color(0xFF00AFB9)  // #00AFB9 — Legacy Vibrant Tropical Teal-Cyan
val KickoffPeachCream = Color(0xFFFED9B7)    // #FED9B7 — Legacy Warm Peach-Champagne highlight & badge text
val KickoffCoralRed = Color(0xFFF07167)      // #F07167 — Legacy Vibrant Coral-Red live & action accent

// Official Kickoff 6-Color Architectural Palette
val KickoffTerracotta = Color(0xFFD9541E)       // #D9541E — Burnt Terracotta / Signal Orange (Live badges, primary CTAs, active pillar, focus rings)
val KickoffImperialNavy = Color(0xFF0F1B3D)     // #0F1B3D — Deep Royal Imperial Navy (Hero match cards, active day capsule, competition group headers, 2D pitch)
val KickoffObsidianBlack = Color(0xFF111111)    // #111111 — Obsidian Carbon Black (Primary screen canvas, modal backdrop, recessed scorebox wells)
val KickoffAlabasterIvory = Color(0xFFF3EEE3)   // #F3EEE3 — Warm Alabaster Ivory (Primary high-contrast typography, team names, live tabular scores)
val KickoffAgedParchment = Color(0xFFD8CBB3)    // #D8CBB3 — Aged Parchment / Warm Sand (Secondary metadata, TV channel pills, kickoff times, subtle borders)
val KickoffCharcoalSlate = Color(0xFF1E1E1C)    // #1E1E1C — Warm Charcoal Slate (Match row cards, elevated containers, filter pills, bottom sheets)

// Controlled Shade & Alpha Derivations
val KickoffNavyElevated = Color(0xFF162654)     // Elevated Imperial Navy for hero gradients & pitch stripes
val KickoffCharcoalElevated = Color(0xFF272724) // Elevated Charcoal for inner pills & unselected controls
val KickoffTerracottaGlow = Color(0x33D9541E)   // 20% #D9541E for live match card border aura
val KickoffParchmentBorder = Color(0x2ED8CBB3)  // 18% #D8CBB3 for crisp 0.8dp architectural borders
val KickoffParchmentMuted = Color(0xB3D8CBB3)   // 70% #D8CBB3 for secondary captions

// Dark ocean-stadium companion surfaces mapped to Official 6-Color Architectural Palette
val KickoffOceanBase = KickoffObsidianBlack
val KickoffOceanSurface = KickoffCharcoalSlate
val KickoffOceanElevated = KickoffImperialNavy

// 2. LIVE TV PALETTE (TV-colors.jpg: #1F313B, #383852, #784259, #B94E56, #BE4039, #683536)
val TvMidnightSlate = Color(0xFF1F313B)      // #1F313B — Deep Midnight Slate-Navy background base
val TvTwilightIndigo = Color(0xFF383852)     // #383852 — Twilight Indigo-Slate elevated surface
val TvPlumMagenta = Color(0xFF784259)        // #784259 — Plum Magenta mid-surface & border accent
val TvCrimsonRose = Color(0xFFB94E56)        // #B94E56 — Crimson Rose secondary accent
val TvBroadcastScarlet = Color(0xFFBE4039)   // #BE4039 — Vibrant Broadcast Scarlet primary accent
val TvDeepMaroon = Color(0xFF683536)         // #683536 — Deep Maroon-Wine card/banner container

// 3. MOVIES PALETTE (Movies-colors.jpg: #80EE98, #46DFB1, #09D1C7, #15919B, #0C6478, #213A58)
val MoviesMintSpring = Color(0xFF80EE98)     // #80EE98 — Electric Mint-Spring highlight accent
val MoviesAquamarine = Color(0xFF46DFB1)     // #46DFB1 — Aquamarine Turquoise bright accent
val MoviesCyanTeal = Color(0xFF09D1C7)       // #09D1C7 — Vibrant Cyan-Teal primary action accent
val MoviesOceanTeal = Color(0xFF15919B)      // #15919B — Ocean Teal secondary surface & border
val MoviesAtlanticTeal = Color(0xFF0C6478)   // #0C6478 — Deep Atlantic Teal card surface
val MoviesSapphireNavy = Color(0xFF213A58)   // #213A58 — Midnight Sapphire-Navy background base

// 4. SERIES PALETTE (Sereis-colors.jpg: #113B3A, #184948, #235E5D, #317978, #599D9C, #B7E6E5)
val SeriesPineBase = Color(0xFF113B3A)       // #113B3A — Deepest Pine-Teal background base
val SeriesForestSurface = Color(0xFF184948)  // #184948 — Dark Teal-Forest surface
val SeriesCypressCard = Color(0xFF235E5D)    // #235E5D — Elevated Cypress-Teal card container
val SeriesSeaJade = Color(0xFF317978)        // #317978 — Rich Sea-Jade mid-tone accent & border
val SeriesMistTeal = Color(0xFF599D9C)       // #599D9C — Soft Mist-Teal secondary accent
val SeriesIceAqua = Color(0xFFB7E6E5)        // #B7E6E5 — Luminous Ice-Aqua highlight & primary text/pill accent

// 5. FAVORITES PALETTE (Fvoretes-colors.jpg: #051F20, #0B2B26, #163832, #235347, #8EB69B, #DAF1DE)
val FavoritesObsidianBase = Color(0xFF051F20)     // #051F20 — Obsidian Forest-Black background base
val FavoritesEvergreenSurface = Color(0xFF0B2B26) // #0B2B26 — Deep Evergreen-Pine dark surface
val FavoritesEmeraldCard = Color(0xFF163832)      // #163832 — Rich Emerald-Moss card surface
val FavoritesBotanicalBorder = Color(0xFF235347)  // #235347 — Lush Botanical Green elevated container & border
val FavoritesSagePrimary = Color(0xFF8EB69B)      // #8EB69B — Soft Sage-Celadon primary accent
val FavoritesPaleMint = Color(0xFFDAF1DE)         // #DAF1DE — Pale Mint-Sage luminous text/badge highlight

// =========================================================================
// Legacy & Harmonized Section Tokens
// =========================================================================
val LiveCrimson = Color(0xFFE63946)
val LiveCrimsonGlow = TvDeepMaroon
val LiveCoralAccent = TvCrimsonRose
val LiveBroadcastGold = AmberGold

val CinemaRuby = Color(0xFFC1121F)
val CinemaVelvetSurface = MoviesAtlanticTeal
val CinemaMarqueeGold = Color(0xFFE5A93C)
val CinemaSpotlight = MoviesMintSpring

val SeriesEmerald = Color(0xFF1B998B)
val SeriesJadeAccent = Color(0xFF2EC4B6)
val SeriesDeepVault = SeriesForestSurface
val SeriesVaultGold = SeriesIceAqua

// KickOff Arena tokens mapped to the Official 6-Color Architectural Palette
val ArenaPitchGreen = KickoffTerracotta
val ArenaTurfDark = KickoffCharcoalSlate
val ArenaTurfElevated = KickoffImperialNavy
val ArenaFloodlightLime = KickoffAlabasterIvory
val ArenaTrophyGold = KickoffAgedParchment

// MENA Sports Broadcaster Accents
val MenaBeinPurple = Color(0xFF5C2D91)
val MenaBeinDark = Color(0xFF39195C)
val MenaBeinLavender = Color(0xFFE6D4FF)
val MenaArryadiaRed = Color(0xFFC1272D)
val MenaArryadiaDark = Color(0xFF3D1014)
val MenaSscTeal = Color(0xFF0D9488)
val MenaSscDark = Color(0xFF0F2E2B)
val MenaAdSportsBlue = Color(0xFF2563EB)
val MenaAdSportsDark = Color(0xFF132347)

// Compatibility aliases used across secondary screens so the entire app shares the SetPlay DNA
val CinemaBlack = DarkGraphite
val CinemaDarkBackground = DarkGraphite
val CinemaSurface = MetalCharcoal
val CinemaSurfaceVariant = MetalCharcoal
val CinemaSurfaceElevated = MetalCharcoalElevated
val CinemaBorder = Champagne

val NeonCyan = AmberGold
val NeonCyanVariant = AmberGold
val ElectricBlue = Champagne
val NeonAmber = PaleGold

val TextPrimary = PaleGold
val TextSecondary = Champagne
val TextMuted = ChampagneMuted
