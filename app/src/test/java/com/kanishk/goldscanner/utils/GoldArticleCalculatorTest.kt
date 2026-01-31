package com.kanishk.goldscanner.utils

import org.junit.Test
import org.junit.Assert.*

class GoldArticleCalculatorTest {

    @Test
    fun `test calculation for small article under 1 gram`() {
        val result = GoldArticleCalculator.calculateArticleCosts(
            currentGoldRate24KPerTola = 150000.0, // NPR per tola
            netWeight = 0.5, // grams
            karat = 24,
            addOnCost = 0.0,
            discount = 0.0
        )
        
        // Expected: wastage = 0.39, total weight = 0.5 + 0.39 = 0.89
        // Weight in tolas = 0.89 / 11.664 = 0.0763 tolas
        // Cost = 0.0763 * 1.0 * 150000 = 11445 NPR
        
        assertEquals(0.39, result.wastage, 0.01)
        assertTrue("Article cost should be positive", result.articleCostAsPerWeightRateAndKarat > 0)
        assertTrue("Total cost should include making charge", result.totalCostAfterTax > result.articleCostAsPerWeightRateAndKarat)
    }

    @Test
    fun `test calculation for 22K gold article`() {
        val result = GoldArticleCalculator.calculateArticleCosts(
            currentGoldRate24KPerTola = 150000.0,
            netWeight = 15.0, // grams (above tola limit)
            karat = 22,
            addOnCost = 500.0,
            discount = 100.0
        )
        
        // For 22K gold above tola weight, wastage = 15.0 * 0.09 = 1.35
        // Total weight = 15.0 + 1.35 = 16.35 grams
        // Weight in tolas = 16.35 / 11.664 = 1.402 tolas
        // Purity factor for 22K = 0.92
        // Cost = 1.402 * 0.92 * 150000 = 193476 NPR
        
        assertEquals(1.35, result.wastage, 0.01)
        assertTrue("Making charge should be calculated as percentage", result.makingCharge > 0)
        assertTrue("Luxury tax should be 2%", result.luxuryTax > 0)
        assertTrue("Total cost should be properly calculated", result.totalCostAfterTax > result.totalCostBeforeTax)
    }

    @Test
    fun `test calculation with discount`() {
        val result = GoldArticleCalculator.calculateArticleCosts(
            currentGoldRate24KPerTola = 150000.0,
            netWeight = 5.0,
            karat = 24,
            addOnCost = 1000.0,
            discount = 500.0
        )
        
        assertTrue("Discount should reduce total cost", result.totalCostBeforeTax > 0)
        assertTrue("Luxury tax should be calculated on discounted amount", result.luxuryTax > 0)
        assertTrue("Total cost should account for discount", result.totalCostAfterTax > result.articleCostAsPerWeightRateAndKarat)
    }

    @Test
    fun `test luxury tax calculation`() {
        val result = GoldArticleCalculator.calculateArticleCosts(
            currentGoldRate24KPerTola = 100000.0,
            netWeight = 2.5,
            karat = 24,
            addOnCost = 0.0,
            discount = 0.0
        )
        
        // Luxury tax should be 2% of total cost before tax
        val expectedLuxuryTax = result.totalCostBeforeTax * 0.02
        assertEquals(expectedLuxuryTax, result.luxuryTax, 0.01)
        
        val expectedTotalAfterTax = result.totalCostBeforeTax + result.luxuryTax
        assertEquals(expectedTotalAfterTax, result.totalCostAfterTax, 0.01)
    }

    @Test
    fun `test fixed making charge for small articles`() {
        val result = GoldArticleCalculator.calculateArticleCosts(
            currentGoldRate24KPerTola = 150000.0,
            netWeight = 1.5, // Should get fixed making charge of 1500
            karat = 24,
            addOnCost = 0.0,
            discount = 0.0
        )
        
        // For 1.0-2.0 gram range, making charge should be 1500
        assertEquals(1500.0, result.makingCharge, 0.01)
    }

    @Test
    fun `test percentage-based making charge for large articles`() {
        val result = GoldArticleCalculator.calculateArticleCosts(
            currentGoldRate24KPerTola = 150000.0,
            netWeight = 50.0, // Large article, should get percentage-based making charge
            karat = 24,
            addOnCost = 2000.0,
            discount = 0.0
        )
        
        // For large articles (>7g, 24K), making charge should be 1% of total taxable amount
        assertTrue("Making charge should be percentage-based for large articles", result.makingCharge > 1500)
    }
}