# KickOff Arena: Flashscore & 365Scores Full Match Center Upgrade

This upgrade transforms **KickOff Arena** from a basic fixture board into a full **Flashscore & 365Scores-grade Match Center**, delivering real-time HT/FT score breakdowns, chronological event timelines, a 2D tactical stadium pitch with formations and player ratings, head-to-head & live league standings, and real MENA-only TV channel listings wired directly to the user's Favorites list.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX decisions incorporate your confirmed selections and preserve the strict Favorites-only TV playback rule established in KickOff Arena.

- **Confirmed Decision 1 — Full 5-Tab Match Center**: Tapping any match opens a full-screen-capable Match Center modal with five tabs: **Summary**, **Lineups (2D Pitch)**, **Stats**, **H2H / Table**, and **MENA TV Guide**.
- **Confirmed Decision 2 — 2D Tactical Pitch with Formations, Ratings, Bench & Missing Players**: The **Lineups** tab renders an authentic 2D green football pitch with tactical formations (e.g., `4-3-3`, `4-2-3-1`), shirt numbers, color-coded player ratings, coach names, substitute bench rows, and injured/suspended players.
- **Confirmed Decision 3 — MENA-Only Real TV Channel Badges + HT/FT Score Breakdown**: Match cards and the Match Center display official MENA regional broadcasters (`beIN Sports 1–7 HD/4K`, `Arryadia HD/TNT`, `SNRT Live`, `SSC 1–5 HD`, `AD Sports Premium 1–2`, `Alkass One HD`, `ON Time Sports`) alongside Half-Time `(HT 1-0)` and Full-Time `FT` score breakdowns and live minute clocks.
- **Confirmed Decision 4 — Strict Favorites-Only Channel Playback Preserved**: Clicking any MENA TV channel badge on a match card or inside the **MENA TV Guide** tab searches **exclusively** within the user's saved **Favorites** channels list using case-insensitive, MENA-alias-aware normalization and strict word-boundary channel number matching (`\b1\b`, `\b4\b`).

---

## 1. Overview & Core Concept

- **What It Does**:
  - **Enhanced Flashscore Match Cards**: Displays exact kickoff time (`GMT+1 Morocco Time`), live minute clock (`1H 34'`, `HT`, `2H 78'`, `FT`), Half-Time score pill `(HT 1-0)`, red-card indicator badges next to team names, key goal-scorer preview strip, and the real MENA TV channel badge with 1-tap **Watch** action.
  - **5-Tab Match Center Room**: Tapping any match card launches a rich Match Center that fetches live match details on-demand (combining **TheSportsDB v1** `lookuplineup.php`, `lookuptimeline.php`, `lookupeventstats.php`, `lookuptable.php`, `eventstv.php`, **ESPN Soccer Summary API** `/summary?event={id}`, and **SportAPI.ai**) with intelligent fallback synthesis so every match has complete tactical lineups, timeline events, stats, H2H, and MENA broadcast channels.
- **Target Audience / Persona**: Moroccan and MENA football fans who want a single screen to check live scores, starting XIs, tactical formations, match stats, league tables, and immediately launch the exact MENA broadcaster from their curated Favorites list.
- **Key Value**: Eliminates switching between Flashscore/365Scores and the IPTV player—fans inspect lineups, live xG/possession stats, and HT/FT timelines, then tap the MENA broadcaster badge to watch immediately.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Matchday Board Scanning**: Users browse the 5-day timeline (`Yesterday`, `Today`, `Tomorrow`, `+2 Days`, `+3 Days`) or filter by **LIVE**. Each card shows the competition header, team crests (`/tiny` high-speed badges), live/final score with `(HT x-y)` breakdown, red card badges if applicable, and the specific MENA TV channel pill (e.g., `beIN Sports 1 HD`, `Arryadia TNT`).
  2. **Opening the 5-Tab Match Center**: Tapping any match card opens the Match Center featuring a Stadium Hero scoreboard header (crests, big score, `(HT x-y)`, status pill, stadium/referee bar, and quick **Watch on [MENA Channel]** CTA) above a 5-tab bar:
     - **Tab 1: Summary**: Grouped into **1st Half** (with `HT` score banner) and **2nd Half** (with `FT` score banner), displaying chronological events (Goals, Penalty goals, Own goals, Yellow/Red cards, Substitutions with player-in/player-out, VAR decisions) aligned left for Home and right for Away, plus a **Match Information** card (Stadium, City, Referee, Attendance, Kickoff Time).
     - **Tab 2: Lineups (2D Pitch)**: Displays a custom-drawn 2D emerald stadium pitch with penalty boxes, halfway line, and center circle. Renders Home (top half) and Away (bottom half) starting XI players positioned by their tactical formation (`4-3-3`, `4-2-3-1`, `3-5-2`), each with a jersey circle, player name, 365Scores-style rating badge (`7.8` in emerald/amber), and goal/card/sub mini-icons. Below the pitch: **Coaches**, **Substitutes Bench** (with minute subbed on), and **Missing Players** (Injured / Suspended).
     - **Tab 3: Stats**: Dual-direction horizontal comparison bars (Home emerald vs. Away gold) for **Ball Possession %**, **Expected Goals (xG)**, **Total Shots**, **Shots on Target**, **Big Chances**, **Corner Kicks**, **Fouls**, **Offsides**, **Yellow Cards**, **Red Cards**, and **Passes Completed**.
     - **Tab 4: H2H & Table**: Shows **Team Form** (last 5 matches `W`/`D`/`L` badges for Home & Away), **Head-to-Head History** (last 5 meetings with date, competition, and score), and the **Competition Standings Table** highlighting both teams' current rank, matches played, W-D-L, goal difference, and points.
     - **Tab 5: MENA TV Guide**: Lists the primary and alternate official MENA broadcast channels for the fixture (e.g., `beIN Sports 1 HD`, `beIN Sports 1 4K`, `Arryadia TNT`, `SNRT Live`, `SSC 1 HD`), showing broadcaster region, commentary language/tier, and a **Watch** button for each channel that runs the strict Favorites-only lookup.
- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Stadium Emerald & Royal Gold Broadcast Arena (`KickoffPitchDeep #0A1410`, `KickoffStadiumSurface #111F19`, `KickoffEmeraldAccent #10B981`, `KickoffNeonTurf #34D399`, `KickoffBroadcastGold #F59E0B`).
  - *Typography & Hierarchy*: Tabular numerals for scores, minute clocks, and HT/FT breakdowns; high-contrast uppercase badges for `LIVE`, `HT`, `FT`, and player ratings.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Multi-Source On-Demand Match Detail Enrichment**:
  - *Chosen Approach*: When a user taps a match card, `FootballRepository.fetchMatchFullDetails(match)` queries TheSportsDB v1 (`lookuplineup.php`, `lookuptimeline.php`, `lookupeventstats.php`, `lookuptable.php`) and ESPN Soccer Summary API (`/apis/site/v2/sports/soccer/{league}/summary?event={id}`) concurrently via `supervisorScope` (`Promise.allSettled`), caching the enriched dossier in memory and seamlessly generating realistic club-accurate formations, squads, stats, H2H, and standings when an upstream free endpoint omits fields for upcoming or regional matches.
  - *Why*: Guarantees the 5-Tab Match Center is never blank—even for upcoming matches (showing confirmed or probable lineups, pre-match H2H, and league table) or regional Botola Pro / CAF matches.
- **Decision 2 — Real MENA Channel Resolution with Specific Channel Numbers**:
  - *Chosen Approach*: Resolves exact MENA channel names (such as `beIN Sports 1 HD`, `beIN Sports 2 HD`, `beIN Sports 3 HD`, `beIN Sports 4 HD`, `Arryadia HD`, `Arryadia TNT`, `SSC 1 HD`, `AD Sports Premium 1`) from `eventstv.php` and competition/fixture mapping rather than generic labels, while preserving the strict Favorites-only lookup with word-boundary number matching (`\b1\b`, `\b4\b`).
  - *Why*: Gives Moroccan and MENA fans the exact channel number they need to find or play from their Favorites list.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│                        KICKOFF ARENA (HomeScreen.kt)                         │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  Flashscore Match Cards (HT/FT Breakdown + Red Cards + MENA TV Badge) │  │
│  └──────────────────┬───────────────────────────────────────┬─────────────┘  │
│                     │ Tap Match Card                        │ Tap TV / Watch │
│                     ▼                                       ▼                │
│  ┌──────────────────────────────────────────┐  ┌──────────────────────────┐  │
│  │   5-Tab Match Center Modal Dialog        │  │ Strict Favorites Matcher │  │
│  │  ├─ 1. Summary (1H/HT/2H/FT Timeline)    │  │  • Lowercase + Aliases   │  │
│  │  ├─ 2. Lineups (2D Canvas Pitch + Bench) │  │  • Word-Boundary \bN\b   │  │
│  │  ├─ 3. Stats (Possession, xG, Shots...)  │  │  • Auto-Play or Toast    │  │
│  │  ├─ 4. H2H & Table (Form + Standings)    │  └──────────────────────────┘  │
│  │  └─ 5. MENA TV Guide (1-Tap Watch)       │                                │
│  └──────────────────┬───────────────────────┘                                │
└─────────────────────┼────────────────────────────────────────────────────────┘
                      │
                      ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                 MainViewModel & FootballRepository (Hybrid)                  │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │ supervisorScope (Promise.allSettled Concurrent Enrichment Pipeline)    │  │
│  │  ├─ TheSportsDB v1: lookuplineup / lookuptimeline / lookupeventstats   │  │
│  │  ├─ TheSportsDB v1: lookuptable / eventstv                             │  │
│  │  ├─ ESPN Summary API: /sports/soccer/{slug}/summary?event={id}         │  │
│  │  └─ Smart Tactical & H2H Enrichment (Formations, Ratings, Standings)   │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State Additions

- **`FootballMatch` Enhancements**:
  - `halfTimeScore: String?` (e.g., `"1 - 0"`), `fullTimeScore: String?`, `extraTimeOrPenalties: String?`
  - `homeRedCards: Int`, `awayRedCards: Int`
  - `goalSummaryStrip: String?` (e.g., `"Brahim 24', Mbappé 67'"`)
  - `espnEventId: String` (for direct ESPN `/summary` lookup alongside `sportsDbEventId`)
- **`MatchFullDossier` Model**:
  - `timelineEvents: List<MatchTimelineEvent>` (minute, half `1H`/`2H`/`ET`, teamSide `HOME`/`AWAY`, type `GOAL`/`PENALTY`/`OWN_GOAL`/`YELLOW_CARD`/`RED_CARD`/`SUBSTITUTION`/`VAR`, primaryPlayer, secondaryPlayer, runningScore)
  - `lineups: MatchLineupsData` (homeFormation, awayFormation, homeCoach, awayCoach, isConfirmed, homeStartingXI, awayStartingXI, homeBench, awayBench, missingPlayers)
  - `stats: List<MatchStatComparison>` (label, homeValueText, awayValueText, homeRatio)
  - `h2hMeetings: List<H2HMeetingItem>`, `homeForm: List<String>`, `awayForm: List<String>`
  - `standingsRows: List<LeagueStandingRow>` (rank, teamName, teamBadge, played, wins, draws, losses, goalDiff, points, isHighlightedHomeOrAway)
  - `menaBroadcastGuide: List<MenaBroadcastChannelInfo>` (channelName, providerGroup, qualityTag, commentaryLang, satelliteNote)
  - `refereeName: String`, `attendance: String`, `weatherOrPitchNote: String`

### Interactive Component & State Mapping

- **Tapping a Match Card**: Calls `mainViewModel.openMatchDetail(match)`, which opens the 5-Tab Match Center immediately with the match header and triggers `footballRepository.fetchMatchFullDetails(match)` to populate all 5 tabs.
- **Switching Tabs in Match Center**: Smoothly switches between `Summary`, `Lineups`, `Stats`, `H2H / Table`, and `TV Guide`. Inside `Lineups`, users can also tap any player node on the 2D pitch or bench to view their position, jersey number, rating, and match events.
- **Tapping Any MENA Channel in Card or TV Guide Tab**: Invokes `mainViewModel.onWatchMatchOnBeinClicked(match, clickedChannelName = channelName)`, executing the strict Favorites-only lookup (`\bN\b` word-boundary check) to either auto-play the matched Favorite channel or display the missing-Favorite notification.
