package id.antasari.trackingmoney.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatRupiah(amount: Long): String {
        // Use Locale("id", "ID") for Indonesian Rupiah formatting
        val localeID = Locale("id", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        // This will typically output something like "Rp1.000.000,00"
        // Let's add a space after Rp to match "Rp 1.000.000,00" if desired
        var formatted = numberFormat.format(amount)
        if (formatted.startsWith("Rp") && !formatted.startsWith("Rp ")) {
            formatted = formatted.replace("Rp", "Rp ")
        } else if (formatted.startsWith("-Rp") && !formatted.startsWith("-Rp ")) {
            formatted = formatted.replace("-Rp", "-Rp ")
        }
        return formatted
    }
}
