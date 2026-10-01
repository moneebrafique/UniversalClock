# SkyClock

A beautiful world clock for Android. Up to 8 cities, each with a live sky that matches the real
position of the sun there: starry night with the actual moon phase, dawn, sunrise, bright day,
golden hour, sunset and dusk.

## Features
- Up to 8 clocks, 130+ cities (Pakistan, Gulf, Europe, Americas, Asia, Africa, Oceania)
- Digital time in 12-hour AM/PM format; optional analog clock in List layout (Settings)
- 7 background themes: Aurora (default), Graphite, Ember, Forest, Rose, Pure Black, Daylight
- Real sky per city computed from its coordinates (sun elevation) – not a guess from the hour
- Twinkling stars, moon with real phase, glowing sun, layered hills
- Day phase label: Night, Dawn, Sunrise, Morning, Midday, Afternoon, Golden hour, Sunset, Dusk
- Today / Tomorrow / Yesterday and time difference from you (e.g. "+9h")
- Tap a clock: big sky view, smooth second hand, sun path arc, sunrise, sunset, day length,
  solar noon, time zone, daylight saving, "Sunset in 2h 10m"
- Time travel (header button): move all clocks ±12 hours; a gold banner shows while you're not seeing the current time
- Two layouts: wide list, or 2×4 grid that fits all 8 clocks on one screen
- Hold and drag any clock to reorder (works in both layouts)

## Works offline
Time comes from the phone's own clock and Android's built-in time-zone database (including
daylight saving rules). Sunrise, sunset and sky colours are calculated on the phone.
No internet needed.

## Build with GitHub
Same as SmartScan: upload this folder to a new GitHub repo, open Actions, download the
**SkyClock-apk** artifact. For installable updates, run "Create signing key (run once)" and add
the secrets KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS (`skyclock`), KEY_PASSWORD in this
repo's settings.
