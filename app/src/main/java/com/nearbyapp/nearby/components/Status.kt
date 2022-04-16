package com.nearbyapp.nearby.components

import com.nearbyapp.nearby.R

enum class Status(val text: String, val image: Int) {
    READY("", -1),
    SERVICE("Errore server", R.drawable.outline_dns_24),
    GENERIC("Errore sconosciuto", R.drawable.outline_error_outline_24),
    INTERNET("Attivare la connessione a internet", R.drawable.outline_signal_cellular_connected_no_internet_0_bar_24),
    LOCATION ("Attivare la localizzazione", R.drawable.outline_location_disabled_24);
}