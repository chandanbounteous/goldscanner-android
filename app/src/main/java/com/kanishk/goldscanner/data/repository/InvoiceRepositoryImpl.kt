package com.kanishk.goldscanner.data.repository

import com.kanishk.goldscanner.data.network.service.InvoiceApiService
import com.kanishk.goldscanner.domain.repository.InvoiceRepository

class InvoiceRepositoryImpl(
    private val invoiceApiService: InvoiceApiService
) : InvoiceRepository {
    
    override suspend fun generateInvoicePdf(basketId: String): ByteArray {
        return invoiceApiService.generateInvoicePdf(basketId)
    }
}