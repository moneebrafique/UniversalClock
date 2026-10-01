package com.skyclock.app.data

import android.content.Context
import java.time.ZoneId

data class City(
    val name: String,
    val country: String,
    val zone: String,
    val lat: Double,
    val lon: Double,
) {
    val id: String get() = "$name, $country"
}

object Cities {
    private fun c(name: String, country: String, zone: String, lat: Double, lon: Double) =
        City(name, country, zone, lat, lon)

    private val raw = listOf(
        // Pakistan
        c("Lahore", "Pakistan", "Asia/Karachi", 31.5204, 74.3587),
        c("Karachi", "Pakistan", "Asia/Karachi", 24.8607, 67.0011),
        c("Islamabad", "Pakistan", "Asia/Karachi", 33.6844, 73.0479),
        c("Peshawar", "Pakistan", "Asia/Karachi", 34.0151, 71.5249),
        c("Quetta", "Pakistan", "Asia/Karachi", 30.1798, 66.9750),
        c("Multan", "Pakistan", "Asia/Karachi", 30.1575, 71.5249),
        c("Faisalabad", "Pakistan", "Asia/Karachi", 31.4504, 73.1350),
        // Middle East
        c("Dubai", "UAE", "Asia/Dubai", 25.2048, 55.2708),
        c("Abu Dhabi", "UAE", "Asia/Dubai", 24.4539, 54.3773),
        c("Riyadh", "Saudi Arabia", "Asia/Riyadh", 24.7136, 46.6753),
        c("Makkah", "Saudi Arabia", "Asia/Riyadh", 21.3891, 39.8579),
        c("Madinah", "Saudi Arabia", "Asia/Riyadh", 24.5247, 39.5692),
        c("Jeddah", "Saudi Arabia", "Asia/Riyadh", 21.4858, 39.1925),
        c("Doha", "Qatar", "Asia/Qatar", 25.2854, 51.5310),
        c("Kuwait City", "Kuwait", "Asia/Kuwait", 29.3759, 47.9774),
        c("Muscat", "Oman", "Asia/Muscat", 23.5880, 58.3829),
        c("Manama", "Bahrain", "Asia/Bahrain", 26.2285, 50.5860),
        c("Tehran", "Iran", "Asia/Tehran", 35.6892, 51.3890),
        c("Baghdad", "Iraq", "Asia/Baghdad", 33.3152, 44.3661),
        c("Istanbul", "Türkiye", "Europe/Istanbul", 41.0082, 28.9784),
        c("Ankara", "Türkiye", "Europe/Istanbul", 39.9334, 32.8597),
        c("Amman", "Jordan", "Asia/Amman", 31.9454, 35.9284),
        c("Beirut", "Lebanon", "Asia/Beirut", 33.8938, 35.5018),
        c("Cairo", "Egypt", "Africa/Cairo", 30.0444, 31.2357),
        // South & Central Asia
        c("Kabul", "Afghanistan", "Asia/Kabul", 34.5553, 69.2075),
        c("New Delhi", "India", "Asia/Kolkata", 28.6139, 77.2090),
        c("Mumbai", "India", "Asia/Kolkata", 19.0760, 72.8777),
        c("Kolkata", "India", "Asia/Kolkata", 22.5726, 88.3639),
        c("Bengaluru", "India", "Asia/Kolkata", 12.9716, 77.5946),
        c("Chennai", "India", "Asia/Kolkata", 13.0827, 80.2707),
        c("Kathmandu", "Nepal", "Asia/Kathmandu", 27.7172, 85.3240),
        c("Dhaka", "Bangladesh", "Asia/Dhaka", 23.8103, 90.4125),
        c("Colombo", "Sri Lanka", "Asia/Colombo", 6.9271, 79.8612),
        c("Malé", "Maldives", "Indian/Maldives", 4.1755, 73.5093),
        c("Tashkent", "Uzbekistan", "Asia/Tashkent", 41.2995, 69.2401),
        c("Almaty", "Kazakhstan", "Asia/Almaty", 43.2220, 76.8512),
        c("Baku", "Azerbaijan", "Asia/Baku", 40.4093, 49.8671),
        c("Tbilisi", "Georgia", "Asia/Tbilisi", 41.7151, 44.8271),
        // East & South-East Asia
        c("Beijing", "China", "Asia/Shanghai", 39.9042, 116.4074),
        c("Shanghai", "China", "Asia/Shanghai", 31.2304, 121.4737),
        c("Hong Kong", "China", "Asia/Hong_Kong", 22.3193, 114.1694),
        c("Taipei", "Taiwan", "Asia/Taipei", 25.0330, 121.5654),
        c("Tokyo", "Japan", "Asia/Tokyo", 35.6762, 139.6503),
        c("Osaka", "Japan", "Asia/Tokyo", 34.6937, 135.5023),
        c("Seoul", "South Korea", "Asia/Seoul", 37.5665, 126.9780),
        c("Singapore", "Singapore", "Asia/Singapore", 1.3521, 103.8198),
        c("Kuala Lumpur", "Malaysia", "Asia/Kuala_Lumpur", 3.1390, 101.6869),
        c("Jakarta", "Indonesia", "Asia/Jakarta", -6.2088, 106.8456),
        c("Bangkok", "Thailand", "Asia/Bangkok", 13.7563, 100.5018),
        c("Manila", "Philippines", "Asia/Manila", 14.5995, 120.9842),
        c("Hanoi", "Vietnam", "Asia/Ho_Chi_Minh", 21.0278, 105.8342),
        c("Ho Chi Minh City", "Vietnam", "Asia/Ho_Chi_Minh", 10.8231, 106.6297),
        // Oceania
        c("Sydney", "Australia", "Australia/Sydney", -33.8688, 151.2093),
        c("Melbourne", "Australia", "Australia/Melbourne", -37.8136, 144.9631),
        c("Brisbane", "Australia", "Australia/Brisbane", -27.4698, 153.0251),
        c("Perth", "Australia", "Australia/Perth", -31.9505, 115.8605),
        c("Adelaide", "Australia", "Australia/Adelaide", -34.9285, 138.6007),
        c("Auckland", "New Zealand", "Pacific/Auckland", -36.8485, 174.7633),
        // Europe
        c("London", "United Kingdom", "Europe/London", 51.5074, -0.1278),
        c("Manchester", "United Kingdom", "Europe/London", 53.4808, -2.2426),
        c("Birmingham", "United Kingdom", "Europe/London", 52.4862, -1.8904),
        c("Edinburgh", "United Kingdom", "Europe/London", 55.9533, -3.1883),
        c("Dublin", "Ireland", "Europe/Dublin", 53.3498, -6.2603),
        c("Paris", "France", "Europe/Paris", 48.8566, 2.3522),
        c("Berlin", "Germany", "Europe/Berlin", 52.5200, 13.4050),
        c("Munich", "Germany", "Europe/Berlin", 48.1351, 11.5820),
        c("Frankfurt", "Germany", "Europe/Berlin", 50.1109, 8.6821),
        c("Madrid", "Spain", "Europe/Madrid", 40.4168, -3.7038),
        c("Barcelona", "Spain", "Europe/Madrid", 41.3851, 2.1734),
        c("Rome", "Italy", "Europe/Rome", 41.9028, 12.4964),
        c("Milan", "Italy", "Europe/Rome", 45.4642, 9.1900),
        c("Amsterdam", "Netherlands", "Europe/Amsterdam", 52.3676, 4.9041),
        c("Brussels", "Belgium", "Europe/Brussels", 50.8503, 4.3517),
        c("Vienna", "Austria", "Europe/Vienna", 48.2082, 16.3738),
        c("Zurich", "Switzerland", "Europe/Zurich", 47.3769, 8.5417),
        c("Geneva", "Switzerland", "Europe/Zurich", 46.2044, 6.1432),
        c("Stockholm", "Sweden", "Europe/Stockholm", 59.3293, 18.0686),
        c("Oslo", "Norway", "Europe/Oslo", 59.9139, 10.7522),
        c("Copenhagen", "Denmark", "Europe/Copenhagen", 55.6761, 12.5683),
        c("Helsinki", "Finland", "Europe/Helsinki", 60.1699, 24.9384),
        c("Reykjavik", "Iceland", "Atlantic/Reykjavik", 64.1466, -21.9426),
        c("Warsaw", "Poland", "Europe/Warsaw", 52.2297, 21.0122),
        c("Prague", "Czechia", "Europe/Prague", 50.0755, 14.4378),
        c("Budapest", "Hungary", "Europe/Budapest", 47.4979, 19.0402),
        c("Athens", "Greece", "Europe/Athens", 37.9838, 23.7275),
        c("Lisbon", "Portugal", "Europe/Lisbon", 38.7223, -9.1393),
        c("Bucharest", "Romania", "Europe/Bucharest", 44.4268, 26.1025),
        c("Kyiv", "Ukraine", "Europe/Kiev", 50.4501, 30.5234),
        c("Moscow", "Russia", "Europe/Moscow", 55.7558, 37.6173),
        // Africa
        c("Lagos", "Nigeria", "Africa/Lagos", 6.5244, 3.3792),
        c("Nairobi", "Kenya", "Africa/Nairobi", -1.2921, 36.8219),
        c("Johannesburg", "South Africa", "Africa/Johannesburg", -26.2041, 28.0473),
        c("Cape Town", "South Africa", "Africa/Johannesburg", -33.9249, 18.4241),
        c("Casablanca", "Morocco", "Africa/Casablanca", 33.5731, -7.5898),
        c("Addis Ababa", "Ethiopia", "Africa/Addis_Ababa", 9.0300, 38.7400),
        c("Accra", "Ghana", "Africa/Accra", 5.6037, -0.1870),
        c("Algiers", "Algeria", "Africa/Algiers", 36.7538, 3.0588),
        c("Tunis", "Tunisia", "Africa/Tunis", 36.8065, 10.1815),
        c("Dar es Salaam", "Tanzania", "Africa/Dar_es_Salaam", -6.7924, 39.2083),
        // North America
        c("New York", "USA", "America/New_York", 40.7128, -74.0060),
        c("Washington DC", "USA", "America/New_York", 38.9072, -77.0369),
        c("Boston", "USA", "America/New_York", 42.3601, -71.0589),
        c("Miami", "USA", "America/New_York", 25.7617, -80.1918),
        c("Atlanta", "USA", "America/New_York", 33.7490, -84.3880),
        c("Chicago", "USA", "America/Chicago", 41.8781, -87.6298),
        c("Houston", "USA", "America/Chicago", 29.7604, -95.3698),
        c("Dallas", "USA", "America/Chicago", 32.7767, -96.7970),
        c("Denver", "USA", "America/Denver", 39.7392, -104.9903),
        c("Phoenix", "USA", "America/Phoenix", 33.4484, -112.0740),
        c("Las Vegas", "USA", "America/Los_Angeles", 36.1699, -115.1398),
        c("Los Angeles", "USA", "America/Los_Angeles", 34.0522, -118.2437),
        c("San Francisco", "USA", "America/Los_Angeles", 37.7749, -122.4194),
        c("Seattle", "USA", "America/Los_Angeles", 47.6062, -122.3321),
        c("Anchorage", "USA", "America/Anchorage", 61.2181, -149.9003),
        c("Honolulu", "USA", "Pacific/Honolulu", 21.3069, -157.8583),
        c("Toronto", "Canada", "America/Toronto", 43.6532, -79.3832),
        c("Montreal", "Canada", "America/Toronto", 45.5017, -73.5673),
        c("Vancouver", "Canada", "America/Vancouver", 49.2827, -123.1207),
        c("Calgary", "Canada", "America/Edmonton", 51.0447, -114.0719),
        c("Mexico City", "Mexico", "America/Mexico_City", 19.4326, -99.1332),
        c("Havana", "Cuba", "America/Havana", 23.1136, -82.3666),
        c("Panama City", "Panama", "America/Panama", 8.9824, -79.5199),
        // South America
        c("Bogotá", "Colombia", "America/Bogota", 4.7110, -74.0721),
        c("Lima", "Peru", "America/Lima", -12.0464, -77.0428),
        c("Caracas", "Venezuela", "America/Caracas", 10.4806, -66.9036),
        c("Santiago", "Chile", "America/Santiago", -33.4489, -70.6693),
        c("Buenos Aires", "Argentina", "America/Argentina/Buenos_Aires", -34.6037, -58.3816),
        c("São Paulo", "Brazil", "America/Sao_Paulo", -23.5505, -46.6333),
        c("Rio de Janeiro", "Brazil", "America/Sao_Paulo", -22.9068, -43.1729),
    )

    /** Only cities whose time zone this phone knows (all of them on any normal device). */
    val all: List<City> = raw.filter { runCatching { ZoneId.of(it.zone) }.isSuccess }
        .sortedBy { it.name }

    val byId: Map<String, City> = all.associateBy { it.id }

    val defaults: List<City> = listOf("Lahore, Pakistan", "New York, USA", "London, United Kingdom", "Berlin, Germany")
        .mapNotNull { byId[it] }
}

/** Saves the user's chosen clocks (order matters). */
class CityStore(context: Context) {
    private val prefs = context.getSharedPreferences("clocks", Context.MODE_PRIVATE)

    fun load(): List<City> {
        val raw = prefs.getString("ids", null) ?: return Cities.defaults
        if (raw.isEmpty()) return emptyList()
        return raw.split("\n").mapNotNull { Cities.byId[it] }
    }

    fun save(list: List<City>) {
        prefs.edit().putString("ids", list.joinToString("\n") { it.id }).apply()
    }

    var gridLayout: Boolean
        get() = prefs.getBoolean("grid", false)
        set(value) { prefs.edit().putBoolean("grid", value).apply() }
}

const val MAX_CLOCKS = 8
