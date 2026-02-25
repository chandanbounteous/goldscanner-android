package com.kanishk.goldscanner.domain.repository

interface InvoiceRepository {
    suspend fun generateInvoicePdf(basketId: String): ByteArray
}