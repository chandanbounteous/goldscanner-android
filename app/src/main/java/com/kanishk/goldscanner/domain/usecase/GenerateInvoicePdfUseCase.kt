package com.kanishk.goldscanner.domain.usecase

import com.kanishk.goldscanner.domain.repository.InvoiceRepository

class GenerateInvoicePdfUseCase(
    private val invoiceRepository: InvoiceRepository
) {
    suspend operator fun invoke(basketId: String): ByteArray {
        return invoiceRepository.generateInvoicePdf(basketId)
    }
}