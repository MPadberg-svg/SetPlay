# SetPlay Kickoff Live Edge Proxy (Cloudflare Workers)

Zero-dependency Cloudflare Worker that isolates football API keys server-side and coalesces client live-score requests into a single upstream call per 45-second edge cache window (`caches.default` + `ETag`/`304`).

## Free-Tier Deployment (5 Steps)

1. **Authenticate Wrangler with your Cloudflare account:**
   ```bash
   cd cloudflare-worker
   npx wrangler login
   ```
2. **Store `API_FOOTBALL_KEY` as an encrypted Worker secret (optional — falls back to ESPN public live feeds if omitted):**
   ```bash
   npx wrangler secret put API_FOOTBALL_KEY
   ```
3. **Store `FOOTBALL_DATA_KEY` as an encrypted Worker secret (optional):**
   ```bash
   npx wrangler secret put FOOTBALL_DATA_KEY
   ```
4. **Deploy the Worker:**
   ```bash
   npx wrangler deploy
   ```
   Verify health at `https://setplay-kickoff-edge.<your-subdomain>.workers.dev/v1/health`.
5. **Connect the Android app in `gradle.properties`:**
   ```properties
   SETPLAY_KICKOFF_EDGE_URL=https://setplay-kickoff-edge.<your-subdomain>.workers.dev
   ```
   Leave `SETPLAY_KICKOFF_EDGE_URL=` blank to continue using the built-in static schedule and direct adapter fallback without an Edge Worker.
