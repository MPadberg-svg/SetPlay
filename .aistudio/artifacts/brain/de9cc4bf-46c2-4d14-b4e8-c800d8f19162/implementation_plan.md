# KickOff Arena & Top Bar UI Upgrade Plan

## 1. Restore Top Section Name Badge to Its Previous Look (`MainActivity.kt`)
- **Keep `Set ▶ Play` Brand Logo Separate**: Leave the app logo and tri-color underline bar untouched.
- **Revert Section Badge in `TopStreamingBar`**: Restore the previous compact single-line rounded pill badge (`RoundedCornerShape(50)`) next to the logo with:
  - Subtle tinted surface (`sectionPrimaryAccent.copy(alpha = 0.14f)`) and border (`0.8.dp`, `sectionPrimaryAccent.copy(alpha = 0.55f)`).
  - `5.5.dp` colored status dot (`sectionPrimaryAccent`) + uppercase `sectionBadgeTitle` (`KICKOFF ARENA`, `LIVE TV`, `MOVIES`, `SERIES`, `FAVORITES`) in crisp `9.5.sp` `FontWeight.ExtraBold` white text.
- **Preserve Clean Section Details Cards**: Keep the streamlined section details cards below the top bar free of decorative color bars and `Link IPTV` buttons.

## 2. Remove `'FEATURED CLASH'` Spotlight Card (`HomeScreen.kt`)
- **Direct Flow into Games List**: Remove the `SpotlightMatchOfTheNightCard` (`FEATURED CLASH` / `LIVE SPOTLIGHT` banner) from the KickOff Arena feed so the sticky date & tournament bar flows directly into the tournament groups and match list without visual redundancy.

## 3. Compact Animated Dates Line (`FiveDayMatchTimelineRibbon` in `HomeScreen.kt`)
- **Compact Horizontal Pill Strip**:
  - Replace the tall 3-tier vertical cards (`106.dp` wide, ~`74.dp` tall) with a sleek, low-profile horizontal date strip (`~40.dp` height) inside a recessed glass dock.
  - Each date pill displays the compact day label (`YESTERDAY`, `TODAY`, `TOMORROW`, or short weekday/date `04 Nov`) alongside a compact match-count badge (`16`, `5`, `9`) on a single streamlined row or tight two-line micro-pill.
- **Spring Animations**:
  - Add a spring-animated morphing/sliding active pill background (`Spring.DampingRatioLowBouncy`, `Spring.StiffnessMediumLow`), smooth scale-and-glow transition on selection, and a pulsing live crimson dot on `TODAY` when live matches are in progress.

## 4. Real Tournament Logos in Tournament Filter Line (`TournamentFilterRow` in `HomeScreen.kt`)
- **Official Tournament Crest Medallions**:
  - Replace the plain colored dot inside each tournament filter chip with a **real tournament logo** (`UEFA Champions League`, `La Liga`, `Premier League`, `Ligue 1`, `Bundesliga`, `Serie A`, `CAF Champions League`, `Botola Pro`, `UEFA Europa League`, `Saudi Pro League`) rendered via Coil `AsyncImage` inside a `22.dp` circular crest medallion with a subtle illuminated ring and custom vector fallback.
- **Animated Selection & Live Count Badge**:
  - Animate the crest medallion scale and border glow when a tournament chip is selected, and display a compact live/game count indicator when matches are active in that competition.

## 5. Upgraded Games List & Tournament Section Headers (`HomeScreen.kt`)
- **Tournament Section Headers (`FlashscoreTournamentHeader`)**:
  - Upgrade the generic trophy icon box to use the **real tournament logo** inside a circular crest medallion paired with the country/region subtitle, tournament title, pin button, live match indicator, and animated collapse chevron.
- **Sleeker Compact Match Cards (`FlashscoreMatchCard`)**:
  - Reduce vertical bulk and tighten padding so more fixtures are visible at a glance.
  - **Animated Live Minute Pulse**: Add a breathing/pulsing live clock pod and smooth live progress hairline for active matches.
  - **Cleaner Scoreboard Rows**: Refine the team crest containers (`26.dp` crisp circular/shield framing), bold winner/live typography, recessed digital score boxes with subtle glow for live goals, and a streamlined footer combining stadium info with the single-tap broadcaster watch capsule.

## Verification Plan
- Run `compile_applet` to verify all Kotlin, Compose, and Coil image-loading code compiles cleanly with zero errors.
- Verify all `testTag` identifiers (`top_streaming_bar`, `tournament_filter_row`, `filter_chip_*`, `tournament_header_*`, `match_card_*`, `watch_bein_btn_*`) remain intact.
