# SBP integracija: MongoDB analitika za prijave neželjenih efekata

Primena gradiva sa predmeta **Sistemi baza podataka** (MongoDB deo: modelovanje DODB,
agregacije, indeksi, analiza performansi, Metabase) u postojeći IIS projekat.

## Arhitektura

- **PostgreSQL** ostaje primarna (transakciona) baza — ništa u postojećem toku nije menjano.
- **MongoDB** (`iis_drug_crm_analytics`) je analitička baza: svaka prijava se pri
  kreiranju/izmeni/promeni statusa/dodavanju beleške automatski preslika u Mongo dokument.
- Ako Mongo nije dostupan, aplikacija radi normalno (sync se samo preskoči uz WARN log).

## Modelovanje (prezentacija 14 — DODB)

Jedna prijava = jedan dokument u kolekciji `adverse_effect_reports`. Odluka **embedding
vs referencing**: istorija statusa, verzije sadržaja i beleške analitičara su **ugnježdene**
(uvek se čitaju zajedno sa prijavom, nikad samostalno). Polje `effectLabels` je
denormalizovan niz simptoma/efekata pripremljen za `$unwind` agregacije.

## Nove komponente (paket `com.example.iisdrugcrm.mongo`)

| Klasa | Uloga |
|---|---|
| `AdverseEffectReportDocument` | Dokument model sa ugnježdenim nizovima |
| `AdverseEffectMongoSyncService` | Preslikavanje Postgres → Mongo (upsert po id-u) |
| `AdverseEffectMongoAnalyticsService` | `$facet` agregacije, indeksi, explain, seed |
| `AdverseEffectMongoController` | REST endpointi (rola FARMAKOVIGILANT) |

## REST endpointi (svi traže FARMAKOVIGILANT token)

| Metoda i putanja | Šta radi |
|---|---|
| `POST /api/adverse-effects/mongo/sync` | Backfill svih prijava iz Postgresa u Mongo |
| `GET /api/adverse-effects/mongo/analytics/summary?from&to` | Kompletna statistika kroz JEDAN `$facet` pipeline (`$match`, `$group`, `$unwind`, `$cond`, `$dateToString`) |
| `GET /api/adverse-effects/mongo/analytics/explain?medicationName&status&from&to` | Plan izvršavanja upita (ekvivalent Oracle EXPLAIN PLAN sa vežbi) |
| `GET /api/adverse-effects/mongo/indexes` | Lista indeksa |
| `POST /api/adverse-effects/mongo/indexes` | Kreira indekse (kompozitni + multikey + prosti) |
| `DELETE /api/adverse-effects/mongo/indexes` | Briše indekse (za "pre" demonstraciju) |
| `POST /api/adverse-effects/mongo/seed?count=200000` | Ubacuje sintetičke dokumente (`seeded: true`) |
| `DELETE /api/adverse-effects/mongo/seed` | Briše sve sintetičke dokumente |

## Indeksi (prezentacija 12)

- `idx_med_status_created` — **kompozitni** `{medicationName: 1, status: 1, createdAt: -1}`
  (jednakost + jednakost + opseg, redosled kolona po selektivnosti kao na vežbama)
- `idx_effect_labels` — **multikey** indeks nad nizom `effectLabels`
- `idx_created_at` — prosti indeks za vremenske opsege analitike

## Demo tok za odbranu (izmereno na 100.000 dokumenata)

1. `POST /mongo/seed?count=100000`
2. `GET /mongo/analytics/explain?medicationName=Brufen&status=SUBMITTED`
   → `COLLSCAN`, totalDocsExamined: **100.001**, executionTimeMillis: **96 ms**
3. `POST /mongo/indexes`
4. Isti explain ponovo
   → `IXSCAN (idx_med_status_created)` + `FETCH`, totalDocsExamined: **1.550**,
   executionTimeMillis: **13 ms** (~7x brže, ~65x manje pregledanih dokumenata)
5. `GET /mongo/analytics/summary` — statistika izračunata u bazi umesto u Javi
6. Metabase (http://localhost:3000) — dashboard nad istom kolekcijom

## Mongo Express (web pregled baze)

http://localhost:8081 — login `admin` / `admin`. Web interfejs za MongoDB (kao pgAdmin
za Postgres): baza `iis_drug_crm_analytics` → kolekcija `adverse_effect_reports` →
pregled dokumenata i indeksa.

## Metabase

Servis `metabase` je dodat u `docker-compose.yml` (port 3000). Pri prvom pokretanju:
Admin setup → Add database → MongoDB → host `mongodb`, port `27017`,
database `iis_drug_crm_analytics`, bez autentifikacije.
