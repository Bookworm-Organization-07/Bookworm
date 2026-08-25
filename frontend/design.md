# Design — Bookworm.com

A locked design system for this app. Every page redesign reads this file before
emitting code. Do not regenerate per page — extend or amend this file when the
system needs to grow.

## Genre
editorial

## Macrostructure family

- **Discovery** (Home): Ecosystem Index — brief positioning paragraph (no big
  centered display headline), rail-titled bands ("Browse by genre",
  "On offer", "In the lending library"), "See more →" links, dense thumbnails,
  no reveal.
- **Catalog/listing** (Products, Lending Library):
  Catalogue — uniform book-cover grid, hairline row dividers, category label
  band, brand mark + tagline only (no big display heading). Genre and
  Language are two independent `<select>` dropdowns (styled as `.field-input`,
  the same input used on every form) that combine — picking both narrows to
  their intersection. This replaced the earlier "slim hairline list" pattern
  once Language became a first-class filter alongside Genre.
- **Item detail** (Item Description): restrained content pattern — cover image
  + hairline-separated metadata list + prose description. Editorial
  magazine-detail-page voice, not a named marketing macrostructure.
- **App/utility** (Login, Register, My Shelf, My Library): typography-only, no
  enrichment. Centered forms are acceptable here — the "no centered hero" ban
  is about marketing heroes, not utility forms.

## Theme
- `--color-paper`     oklch(97% 0.012 75)
- `--color-paper-2`   oklch(94% 0.014 75)
- `--color-rule`      oklch(85% 0.02 60)
- `--color-neutral`   oklch(58% 0.02 50)
- `--color-muted`     oklch(42% 0.02 50)
- `--color-ink`       oklch(20% 0.02 40)
- `--color-accent`    oklch(48% 0.16 35)   /* terracotta — old paper & ink register */
- `--color-accent-ink` oklch(97% 0.01 35)  /* text on filled accent */
- `--color-focus`     oklch(55% 0.19 35)

Warm terracotta-on-cream anchor (hue ~35–40). Accent used only for: active nav
state, focus rings, link underlines, price emphasis, and normal-sized (never
oversized) primary buttons. Never a large filled block.

## Typography
- Display + wordmark: **Fraunces** (variable serif). Weight 600 for headings,
  600 for the wordmark. Editorial allows collapsing wordmark into the display
  family — used here.
- Body: **IBM Plex Sans**, weight 400 (350 not needed — light mode only).
- Outlier (≤ 2 slots, never a third body font): **JetBrains Mono** — used only
  for (1) price figures and (2) small-caps genre/category eyebrow labels.
- Display tracking: -0.02em. Small-caps / label tracking: 0.08em.
- Type scale anchor: `--text-display: clamp(2.25rem, 4vw + 1rem, 3.75rem)`
  (kept modest — this is a commerce/browsing app, not a poster).

## Spacing
Hallmark's 4-pt named scale, defined in `tokens.css`:
`--space-3xs` … `--space-4xl`. Use `gap` for sibling spacing; `margin` only for
optical breaks.

## Motion
- Easings: `--ease-out: cubic-bezier(0.16, 1, 0.3, 1)`.
- Reveal pattern: none — no scroll reveals anywhere.
- Hover: cards lift 1px + border darkens; links underline on hover.
- `:focus-visible` ring shows instantly (never animated), 2px solid
  `--color-focus`, ≥3:1 contrast.
- `prefers-reduced-motion: reduce` → all transitions collapse to opacity only,
  ≤150ms.

## Microinteractions stance
- Silent success — no celebratory toasts.
- Disabled "coming in Phase N" buttons: reduced-opacity, `cursor: not-allowed`,
  native `title` tooltip (no custom tooltip component needed yet).
- Loading state on form submits: button label swaps to a present-progressive
  string ("Signing on…") — no spinner icon needed at this scale.

## CTA voice
- Primary CTA: solid `--color-accent` fill, `--color-accent-ink` text, small
  radius (`--radius-input`), normal button size (never oversized/full-width
  hero buttons).
- Secondary CTA: outline (1px `--color-rule`), `--color-ink` text, same shape.

## Nav (N6 Newspaper masthead, adapted)
Centered wordmark "Bookworm.com" in Fraunces 600. Thin small-caps utility row
beneath: primary links (Home / Products / Lending Library) on the left,
auth-state actions on the right (Sign on / Register, or My Shelf / My Library /
"Hi, {name}" / Sign off). Double hairline rule below the whole block.

"Products" is a native `<details>`/`<summary>` dropdown listing every genre
plus a "See all products" link — a fast path into the catalogue from any
page, per BRD §4.1's Top Menu Bar submenu. Styled as a bordered, paper-toned
panel (`.nav-dropdown-panel`), consistent with the flat, hairline-bordered
look used everywhere else (no drop shadow — this system stays flat).

## Footer (Ft1 Mast-headed)
Wordmark + one-line tagline anchor a single band. 2–3 quiet links beside
(About · Contact · Feedback, per BRD §4.1 — not yet wired to real pages).

## Per-page allowances
- Discovery/catalog pages MAY show dense imagery (book covers) — that's
  content, not decorative enrichment.
- No page uses hero illustration, generated art, or Lottie. Enrichment tier:
  none, everywhere, on purpose.

## What pages MUST share
- The wordmark (Fraunces, "Bookworm.com").
- The accent colour (terracotta) and its restrained placement.
- The display + body fonts.
- The CTA voice (button shape, radius, padding rhythm).
- The nav (N6) and footer (Ft1).

## What pages MAY differ on
- Macrostructure within their family (see above).
- Whether the Genre/Language filter dropdowns are present (catalog pages only).

## Exports

### tokens.css
See `frontend/tokens.css` — the authoritative token file every page imports.
