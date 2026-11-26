/**
 * SetPlay Va3 — Zero-Dependency Cloudflare Worker Live Edge Proxy.
 *
 * Isolates upstream API keys server-side and coalesces up to 100k+ daily client polls
 * into a single upstream request per 45-second edge cache window via `caches.default`.
 *
 * Routes:
 *   GET /v1/health   -> Edge readiness & configured provider flags (never exposes secrets)
 *   GET /v1/live     -> 45s edge-cached canonical live football delta with ETag/304 support
 *   GET /v1/schedule -> 30m edge-cached proxy for SetPlay's static GitHub Pages schedule window
 */

export interface Env {
  API_FOOTBALL_KEY?: string;
  FOOTBALL_DATA_KEY?: string;
}

interface CanonicalLiveMatch {
  canonicalId: string;
  tournamentId: string;
  tournamentName: string;
  tournamentRegion: string;
  kickoffEpochMs: number;
  status: "UPCOMING" | "LIVE" | "HALFTIME" | "FINISHED" | "POSTPONED" | "CANCELLED";
  liveMinuteOrBadge: string;
  homeTeamId: string;
  homeTeamName: string;
  homeTeamBadge: string;
  homeScore: number | null;
  awayTeamId: string;
  awayTeamName: string;
  awayTeamBadge: string;
  awayScore: number | null;
  apiFootballFixtureId: string;
  espnEventId: string;
}

interface CompetitionDef {
  tournamentId: string;
  tournamentName: string;
  tournamentRegion: string;
  espnSlug: string;
  apiFootballIds: number[];
}

const STATIC_SCHEDULE_URL =
  "https://MPadberg-svg.github.io/SetPlay/kickoff/schedule_window.json";
const LIVE_CACHE_TTL_SECONDS = 45;
const SCHEDULE_CACHE_TTL_SECONDS = 1800;

const COMPETITIONS: CompetitionDef[] = [
  {
    tournamentId: "uefa_cl",
    tournamentName: "UEFA Champions League",
    tournamentRegion: "Europe",
    espnSlug: "uefa.champions",
    apiFootballIds: [2],
  },
  {
    tournamentId: "uefa_el",
    tournamentName: "UEFA Europa League",
    tournamentRegion: "Europe",
    espnSlug: "uefa.europa",
    apiFootballIds: [3],
  },
  {
    tournamentId: "premier_league",
    tournamentName: "Premier League",
    tournamentRegion: "England",
    espnSlug: "eng.1",
    apiFootballIds: [39],
  },
  {
    tournamentId: "laliga",
    tournamentName: "LaLiga EA Sports",
    tournamentRegion: "Spain",
    espnSlug: "esp.1",
    apiFootballIds: [140],
  },
  {
    tournamentId: "serie_a",
    tournamentName: "Serie A",
    tournamentRegion: "Italy",
    espnSlug: "ita.1",
    apiFootballIds: [135],
  },
  {
    tournamentId: "bundesliga",
    tournamentName: "Bundesliga",
    tournamentRegion: "Germany",
    espnSlug: "ger.1",
    apiFootballIds: [78],
  },
  {
    tournamentId: "ligue_1",
    tournamentName: "Ligue 1 McDonald's",
    tournamentRegion: "France",
    espnSlug: "fra.1",
    apiFootballIds: [61],
  },
  {
    tournamentId: "botola_pro",
    tournamentName: "Botola Pro Inwi",
    tournamentRegion: "Morocco",
    espnSlug: "mar.1",
    apiFootballIds: [200],
  },
  {
    tournamentId: "saudi_pro",
    tournamentName: "Roshn Saudi League",
    tournamentRegion: "Saudi Arabia",
    espnSlug: "ksa.1",
    apiFootballIds: [307],
  },
  {
    tournamentId: "caf_cl",
    tournamentName: "CAF Champions League",
    tournamentRegion: "Africa",
    espnSlug: "caf.champions",
    apiFootballIds: [12],
  },
  {
    tournamentId: "caf_confed",
    tournamentName: "CAF Confederation Cup",
    tournamentRegion: "Africa",
    espnSlug: "caf.confed",
    apiFootballIds: [20],
  },
  {
    tournamentId: "intl_official",
    tournamentName: "International Official Fixtures",
    tournamentRegion: "International",
    espnSlug: "fifa.worldq.caf",
    apiFootballIds: [1, 4, 5, 6, 7, 9, 15, 29, 30, 31, 32, 34, 36],
  },
  {
    tournamentId: "intl_friendly",
    tournamentName: "International Friendlies",
    tournamentRegion: "International",
    espnSlug: "fifa.friendly",
    apiFootballIds: [10],
  },
];

const COMP_BY_API_FOOTBALL_ID = new Map<number, CompetitionDef>();
for (const comp of COMPETITIONS) {
  for (const id of comp.apiFootballIds) {
    COMP_BY_API_FOOTBALL_ID.set(id, comp);
  }
}

const CLUB_ALIASES: Record<string, string> = {
  "al qadsiyah": "al qadsiah",
  "al qadisiya": "al qadsiah",
  qadsiyah: "al qadsiah",
  qadisiya: "al qadsiah",
  "al feiha": "al fayha",
  feiha: "al fayha",
  "al ittihad jeddah": "al ittihad",
  ittihad: "al ittihad",
  psg: "paris saint germain",
  "paris sg": "paris saint germain",
  internazionale: "inter milan",
  "internazionale milano": "inter milan",
  inter: "inter milan",
  "man city": "manchester city",
  "man united": "manchester united",
  "man utd": "manchester united",
};

const STRIP_TOKENS = new Set([
  "fc",
  "cf",
  "sc",
  "ac",
  "as",
  "ca",
  "cd",
  "ud",
  "rc",
  "rcd",
  "sd",
  "fk",
  "sk",
  "sv",
  "bsc",
  "ssc",
  "club",
  "de",
  "football",
  "futbol",
  "calcio",
]);

function isUsableSecret(raw?: string): boolean {
  if (!raw) return false;
  const val = raw.trim();
  if (!val) return false;
  const upper = val.toUpperCase();
  return upper !== "NULL" && !upper.startsWith("YOUR_") && !upper.startsWith("MY_");
}

function normalizeTeamName(raw: string): string {
  const cleaned = raw
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, " ")
    .trim();
  if (CLUB_ALIASES[cleaned]) return CLUB_ALIASES[cleaned];
  const reduced = cleaned
    .split(" ")
    .filter((tok) => tok && !STRIP_TOKENS.has(tok))
    .join(" ")
    .trim() || cleaned;
  return CLUB_ALIASES[reduced] ?? reduced;
}

function buildCanonicalId(
  tournamentId: string,
  kickoffEpochMs: number,
  homeName: string,
  awayName: string
): string {
  const isoDate =
    kickoffEpochMs > 0
      ? new Date(kickoffEpochMs).toISOString().slice(0, 10)
      : new Date().toISOString().slice(0, 10);
  return `${tournamentId.toLowerCase()}|${isoDate}|${normalizeTeamName(homeName)}|${normalizeTeamName(awayName)}`;
}

async function computeWeakEtag(body: string): Promise<string> {
  const encoded = new TextEncoder().encode(body);
  const digest = await crypto.subtle.digest("SHA-256", encoded);
  const hex = Array.from(new Uint8Array(digest))
    .slice(0, 12)
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
  return `W/"${hex}"`;
}

function mapApiFootballStatus(
  shortCode: string,
  elapsed: unknown
): { status: CanonicalLiveMatch["status"]; badge: string } {
  const s = (shortCode || "").toUpperCase();
  if (s === "HT") return { status: "HALFTIME", badge: "HT" };
  if (["1H", "2H", "ET", "P", "BT", "LIVE"].includes(s)) {
    const label = typeof elapsed === "number" && elapsed > 0 ? `${elapsed}'` : s;
    return { status: "LIVE", badge: label };
  }
  if (["FT", "AET", "PEN"].includes(s)) return { status: "FINISHED", badge: s };
  if (["PST", "SUSP", "INT"].includes(s)) return { status: "POSTPONED", badge: "PPD" };
  if (["CANC", "ABD", "AWD", "WO"].includes(s)) return { status: "CANCELLED", badge: "CANC" };
  return { status: "UPCOMING", badge: "" };
}

async function fetchLiveFromApiFootball(
  apiKey: string
): Promise<{ ok: boolean; rateLimited: boolean; matches: CanonicalLiveMatch[] }> {
  try {
    const resp = await fetch("https://v3.football.api-sports.io/fixtures?live=all", {
      headers: {
        "x-apisports-key": apiKey,
        Accept: "application/json",
      },
    });
    if (resp.status === 429) {
      return { ok: false, rateLimited: true, matches: [] };
    }
    if (!resp.ok) {
      return { ok: false, rateLimited: false, matches: [] };
    }
    const payload = (await resp.json()) as { response?: Array<Record<string, any>> };
    const items = Array.isArray(payload.response) ? payload.response : [];
    const matches: CanonicalLiveMatch[] = [];

    for (const item of items) {
      const leagueId = Number(item?.league?.id ?? 0);
      const comp = COMP_BY_API_FOOTBALL_ID.get(leagueId);
      if (!comp) continue;

      const fixture = item?.fixture ?? {};
      const teams = item?.teams ?? {};
      const goals = item?.goals ?? {};
      const home = teams?.home ?? {};
      const away = teams?.away ?? {};
      const homeName = String(home?.name ?? "").trim();
      const awayName = String(away?.name ?? "").trim();
      if (!homeName || !awayName) continue;

      const parsedDate = Date.parse(String(fixture?.date ?? ""));
      const kickoffEpochMs = Number.isFinite(parsedDate)
        ? parsedDate
        : typeof fixture?.timestamp === "number"
          ? fixture.timestamp * 1000
          : 0;
      const { status, badge } = mapApiFootballStatus(
        String(fixture?.status?.short ?? ""),
        fixture?.status?.elapsed
      );

      matches.push({
        canonicalId: buildCanonicalId(comp.tournamentId, kickoffEpochMs, homeName, awayName),
        tournamentId: comp.tournamentId,
        tournamentName: comp.tournamentName,
        tournamentRegion: comp.tournamentRegion,
        kickoffEpochMs,
        status,
        liveMinuteOrBadge: badge,
        homeTeamId: String(home?.id ?? ""),
        homeTeamName: homeName,
        homeTeamBadge: String(home?.logo ?? ""),
        homeScore: typeof goals?.home === "number" ? goals.home : null,
        awayTeamId: String(away?.id ?? ""),
        awayTeamName: awayName,
        awayTeamBadge: String(away?.logo ?? ""),
        awayScore: typeof goals?.away === "number" ? goals.away : null,
        apiFootballFixtureId: String(fixture?.id ?? ""),
        espnEventId: "",
      });
    }

    return { ok: true, rateLimited: false, matches };
  } catch {
    return { ok: false, rateLimited: false, matches: [] };
  }
}

async function fetchLiveFromEspnFallback(): Promise<CanonicalLiveMatch[]> {
  const todayCompact = new Date().toISOString().slice(0, 10).replace(/-/g, "");
  const settled = await Promise.allSettled(
    COMPETITIONS.map(async (comp): Promise<CanonicalLiveMatch[]> => {
      const url = `https://site.api.espn.com/apis/site/v2/sports/soccer/${comp.espnSlug}/scoreboard?dates=${todayCompact}`;
      const resp = await fetch(url, { headers: { Accept: "application/json" } });
      if (!resp.ok) return [];
      const payload = (await resp.json()) as { events?: Array<Record<string, any>> };
      const events = Array.isArray(payload.events) ? payload.events : [];
      const liveMatches: CanonicalLiveMatch[] = [];

      for (const ev of events) {
        const c0 = Array.isArray(ev?.competitions) ? ev.competitions[0] : null;
        if (!c0) continue;
        const statusObj = c0?.status ?? ev?.status ?? {};
        const statusType = statusObj?.type ?? {};
        const state = String(statusType?.state ?? "").toLowerCase();
        const name = String(statusType?.name ?? "").toUpperCase();
        const shortDetail = String(statusType?.shortDetail ?? "").trim();
        const displayClock = String(statusObj?.displayClock ?? "").trim();

        // Only emit in-progress or halftime matches in /v1/live
        if (state !== "in" && !name.includes("HALFTIME") && shortDetail.toUpperCase() !== "HT") {
          continue;
        }

        const competitors = Array.isArray(c0?.competitors) ? c0.competitors : [];
        const homeEntry = competitors.find((c: any) => c?.homeAway === "home");
        const awayEntry = competitors.find((c: any) => c?.homeAway === "away");
        if (!homeEntry || !awayEntry) continue;

        const homeTeam = homeEntry?.team ?? {};
        const awayTeam = awayEntry?.team ?? {};
        const homeName = String(
          homeTeam?.shortDisplayName ?? homeTeam?.displayName ?? homeTeam?.name ?? ""
        ).trim();
        const awayName = String(
          awayTeam?.shortDisplayName ?? awayTeam?.displayName ?? awayTeam?.name ?? ""
        ).trim();
        if (!homeName || !awayName) continue;

        const parsedDate = Date.parse(String(c0?.date ?? ev?.date ?? ""));
        const kickoffEpochMs = Number.isFinite(parsedDate) ? parsedDate : 0;
        const isHalftime = name.includes("HALFTIME") || shortDetail.toUpperCase() === "HT";
        const parsedHome = Number.parseInt(String(homeEntry?.score ?? ""), 10);
        const parsedAway = Number.parseInt(String(awayEntry?.score ?? ""), 10);

        liveMatches.push({
          canonicalId: buildCanonicalId(comp.tournamentId, kickoffEpochMs, homeName, awayName),
          tournamentId: comp.tournamentId,
          tournamentName: comp.tournamentName,
          tournamentRegion: comp.tournamentRegion,
          kickoffEpochMs,
          status: isHalftime ? "HALFTIME" : "LIVE",
          liveMinuteOrBadge: isHalftime ? "HT" : displayClock || shortDetail || "LIVE",
          homeTeamId: String(homeTeam?.id ?? ""),
          homeTeamName: homeName,
          homeTeamBadge: String(homeTeam?.logo ?? ""),
          homeScore: Number.isNaN(parsedHome) ? null : parsedHome,
          awayTeamId: String(awayTeam?.id ?? ""),
          awayTeamName: awayName,
          awayTeamBadge: String(awayTeam?.logo ?? ""),
          awayScore: Number.isNaN(parsedAway) ? null : parsedAway,
          apiFootballFixtureId: "",
          espnEventId: String(ev?.id ?? ""),
        });
      }
      return liveMatches;
    })
  );

  const deduped = new Map<string, CanonicalLiveMatch>();
  for (const res of settled) {
    if (res.status !== "fulfilled") continue;
    for (const match of res.value) {
      if (!deduped.has(match.canonicalId)) {
        deduped.set(match.canonicalId, match);
      }
    }
  }
  return Array.from(deduped.values());
}

async function handleLiveRequest(
  request: Request,
  env: Env,
  ctx: ExecutionContext
): Promise<Response> {
  const cache = caches.default;
  const cacheKey = new Request(new URL("/v1/live", request.url).toString(), {
    method: "GET",
  });
  const clientIfNoneMatch = request.headers.get("If-None-Match")?.trim();

  const cached = await cache.match(cacheKey);
  if (cached) {
    const cachedEtag = cached.headers.get("ETag")?.trim();
    if (clientIfNoneMatch && cachedEtag && clientIfNoneMatch === cachedEtag) {
      return new Response(null, {
        status: 304,
        headers: {
          ETag: cachedEtag,
          "Cache-Control": `public, max-age=${LIVE_CACHE_TTL_SECONDS}, s-maxage=${LIVE_CACHE_TTL_SECONDS}`,
          "X-SetPlay-Edge-Cache": "HIT-304",
        },
      });
    }
    const clonedHeaders = new Headers(cached.headers);
    clonedHeaders.set("X-SetPlay-Edge-Cache", "HIT");
    return new Response(cached.body, { status: cached.status, headers: clonedHeaders });
  }

  let sourceProvider = "espn_soccer";
  let matches: CanonicalLiveMatch[] = [];

  if (isUsableSecret(env.API_FOOTBALL_KEY)) {
    const apiResult = await fetchLiveFromApiFootball(env.API_FOOTBALL_KEY!.trim());
    if (apiResult.ok) {
      sourceProvider = "api_football";
      matches = apiResult.matches;
    } else {
      matches = await fetchLiveFromEspnFallback();
      sourceProvider = "espn_soccer";
    }
  } else {
    matches = await fetchLiveFromEspnFallback();
    sourceProvider = "espn_soccer";
  }

  const body = JSON.stringify({
    schemaVersion: 1,
    generatedAtEpochMs: Date.now(),
    sourceProvider,
    matches,
  });
  const etag = await computeWeakEtag(body);

  const responseHeaders = {
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": `public, max-age=${LIVE_CACHE_TTL_SECONDS}, s-maxage=${LIVE_CACHE_TTL_SECONDS}`,
    ETag: etag,
    "X-SetPlay-Edge-Cache": "MISS",
  };

  const responseToCache = new Response(body, { status: 200, headers: responseHeaders });
  ctx.waitUntil(cache.put(cacheKey, responseToCache.clone()));

  if (clientIfNoneMatch && clientIfNoneMatch === etag) {
    return new Response(null, {
      status: 304,
      headers: {
        ETag: etag,
        "Cache-Control": responseHeaders["Cache-Control"],
        "X-SetPlay-Edge-Cache": "MISS-304",
      },
    });
  }

  return responseToCache;
}

async function handleScheduleRequest(
  request: Request,
  ctx: ExecutionContext
): Promise<Response> {
  const cache = caches.default;
  const cacheKey = new Request(new URL("/v1/schedule", request.url).toString(), {
    method: "GET",
  });
  const clientIfNoneMatch = request.headers.get("If-None-Match")?.trim();

  const cached = await cache.match(cacheKey);
  if (cached) {
    const cachedEtag = cached.headers.get("ETag")?.trim();
    if (clientIfNoneMatch && cachedEtag && clientIfNoneMatch === cachedEtag) {
      return new Response(null, {
        status: 304,
        headers: {
          ETag: cachedEtag,
          "Cache-Control": `public, max-age=${SCHEDULE_CACHE_TTL_SECONDS}, s-maxage=${SCHEDULE_CACHE_TTL_SECONDS}`,
        },
      });
    }
    return cached;
  }

  const upstream = await fetch(STATIC_SCHEDULE_URL, {
    headers: { Accept: "application/json" },
  });
  if (!upstream.ok) {
    return new Response(
      JSON.stringify({ ok: false, error: "Upstream schedule snapshot unavailable" }),
      {
        status: upstream.status === 404 ? 404 : 502,
        headers: { "Content-Type": "application/json; charset=utf-8" },
      }
    );
  }

  const body = await upstream.text();
  const etag = upstream.headers.get("ETag")?.trim() || (await computeWeakEtag(body));
  const response = new Response(body, {
    status: 200,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": `public, max-age=${SCHEDULE_CACHE_TTL_SECONDS}, s-maxage=${SCHEDULE_CACHE_TTL_SECONDS}`,
      ETag: etag,
    },
  });
  ctx.waitUntil(cache.put(cacheKey, response.clone()));

  if (clientIfNoneMatch && clientIfNoneMatch === etag) {
    return new Response(null, {
      status: 304,
      headers: {
        ETag: etag,
        "Cache-Control": `public, max-age=${SCHEDULE_CACHE_TTL_SECONDS}, s-maxage=${SCHEDULE_CACHE_TTL_SECONDS}`,
      },
    });
  }
  return response;
}

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    if (request.method !== "GET") {
      return new Response(JSON.stringify({ ok: false, error: "Method Not Allowed" }), {
        status: 405,
        headers: {
          Allow: "GET",
          "Content-Type": "application/json; charset=utf-8",
        },
      });
    }

    const url = new URL(request.url);
    switch (url.pathname) {
      case "/v1/health":
        return new Response(
          JSON.stringify({
            ok: true,
            version: "Va3",
            providersConfigured: {
              apiFootball: isUsableSecret(env.API_FOOTBALL_KEY),
              footballData: isUsableSecret(env.FOOTBALL_DATA_KEY),
            },
            timestampMs: Date.now(),
          }),
          {
            status: 200,
            headers: {
              "Content-Type": "application/json; charset=utf-8",
              "Cache-Control": "no-store",
            },
          }
        );

      case "/v1/live":
        return handleLiveRequest(request, env, ctx);

      case "/v1/schedule":
        return handleScheduleRequest(request, ctx);

      default:
        return new Response(JSON.stringify({ ok: false, error: "Not Found" }), {
          status: 404,
          headers: { "Content-Type": "application/json; charset=utf-8" },
        });
    }
  },
};
