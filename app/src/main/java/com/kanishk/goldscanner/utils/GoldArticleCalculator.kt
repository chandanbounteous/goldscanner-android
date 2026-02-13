package com.kanishk.goldscanner.utils

import com.kanishk.goldscanner.data.model.GoldArticleCalculation
import com.kanishk.goldscanner.data.model.LookupEntryForWastageAndMakingCharge

object GoldArticleCalculator {
    
    private const val LUXURY_TAX_PERCENT = 0.02
    private const val ONE_TOLA_IN_GMS = 11.664
    private const val MAX_ARTICLE_WEIGHT = 999.0
    private const val KARAT_24 = 24
    private const val KARAT_22 = 22
    private const val PURITY_FACTOR_24_KARAT = 1.0
    private const val PURITY_FACTOR_22_KARAT = 0.92

    private val lookupTable = listOf(
        LookupEntryForWastageAndMakingCharge(0.0, 1.0, 0, { 0.39 }, { 1200.0 }),
        LookupEntryForWastageAndMakingCharge(1.0, 2.0, 0, { 0.65 }, { 1500.0 }),
        LookupEntryForWastageAndMakingCharge(2.0, 3.0, 0, { 0.70 }, { 1700.0 }),
        LookupEntryForWastageAndMakingCharge(3.0, 4.0, 0, { 0.75 }, { 1800.0 }),
        LookupEntryForWastageAndMakingCharge(4.0, 6.0, 0, { 0.95 }, { 2200.0 }),
        LookupEntryForWastageAndMakingCharge(6.0, 7.0, 0, { 1.00 }, { 3500.0 }),
        LookupEntryForWastageAndMakingCharge(7.0, MAX_ARTICLE_WEIGHT, KARAT_24, { it * 0.07 }, { it * 0.01 }),
        LookupEntryForWastageAndMakingCharge(7.0, ONE_TOLA_IN_GMS, KARAT_22, { it * 0.07 }, { it * 0.01 }),
        LookupEntryForWastageAndMakingCharge(
            ONE_TOLA_IN_GMS,
            MAX_ARTICLE_WEIGHT, 
            KARAT_22, 
            { it * 0.09 }, 
            { it * 0.01 }
        )
    )

    /**
     * Calculate all gold article costs based on current gold rate and article properties
     */
    fun calculateArticleCost(
        currentGoldRate24KPerTola: Double,
        netWeight: Double,
        karat: Int,
        addOnCost: Double,
        discount: Double = 0.0
    ): GoldArticleCalculation {
        
        // Step 1: Calculate wastage
        val wastage = calculateWastage(netWeight, karat)
        
        // Step 2: Calculate total weight (net weight + wastage)
        val totalWeight = netWeight + wastage
        
        // Step 3: Calculate cost as per weight, rate and karat
        val articleCostAsPerWeightRateAndKarat = calculateArticleCostAsPerWeightRateAndKarat(
            totalWeight, karat, currentGoldRate24KPerTola
        )
        
        // Step 4: Calculate making charge (needs total taxable amount for some entries)
        val makingCharge = calculateMakingCharge(netWeight, karat, articleCostAsPerWeightRateAndKarat)
        
        // Step 5: Calculate total cost before tax
        val totalCostBeforeTax = calculateTotalCostBeforeTax(
            articleCostAsPerWeightRateAndKarat,
            makingCharge, 
            discount
        )
        
        // Step 6: Calculate luxury tax
        val luxuryTax = calculateLuxuryTax(totalCostBeforeTax)
        
        // Step 7: Calculate total cost after tax
        val totalCostAfterTax = calculateTotalCostAfterTax(totalCostBeforeTax, luxuryTax)
        
        // Step 8: Calculate final estimated cost
        val finalEstimatedCost = totalCostAfterTax + addOnCost
        
        return GoldArticleCalculation(
            wastage = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(wastage, 2),
            articleCostAsPerWeightRateAndKarat = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(articleCostAsPerWeightRateAndKarat, 2),
            makingCharge = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(makingCharge, 2),
            totalCostBeforeTax = totalCostBeforeTax,
            luxuryTax = luxuryTax,
            totalCostAfterTax = totalCostAfterTax,
            finalEstimatedCost = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(finalEstimatedCost, 2)
        )
    }

    fun calculateWastage(netWeight: Double, karat: Int): Double {
        val matchedEntry = lookupTable.firstOrNull { entry ->
            netWeight >= entry.minNetWeight &&
                    netWeight < entry.maxNetWeight &&
                    (karat == entry.karat || entry.karat == 0)
        }
        return matchedEntry?.wastage?.invoke(netWeight) ?: 0.0
    }

    fun calculateArticleCostAsPerWeightRateAndKarat(
        totalWeight: Double,
        karat: Int,
        goldRatePerTola: Double
    ): Double {
        val weightInTolas = totalWeight / ONE_TOLA_IN_GMS
        val purityFactor = if (karat == KARAT_24) PURITY_FACTOR_24_KARAT else PURITY_FACTOR_22_KARAT
        val cost = weightInTolas * purityFactor * goldRatePerTola
        return cost
    }

    fun calculateMakingCharge(netWeight: Double, karat: Int, totalTaxableAmount: Double): Double {
        val matchedEntry = lookupTable.firstOrNull { entry ->
            netWeight >= entry.minNetWeight &&
                    netWeight < entry.maxNetWeight &&
                    (karat == entry.karat || entry.karat == 0)
        }
        return matchedEntry?.makingCharge?.invoke(totalTaxableAmount) ?: 0.0
    }

    fun calculateTotalCostBeforeTax(
        costAsPerGoldRateAndKarat: Double,
        makingCharge: Double,
        discount: Double
    ): Double {
        val totalTaxableAmount = costAsPerGoldRateAndKarat + makingCharge - discount
        return com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(totalTaxableAmount, 2)
    }

    fun calculateLuxuryTax(totalCostBeforeTax: Double): Double =
        com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(LUXURY_TAX_PERCENT * totalCostBeforeTax, 2)

    fun calculateTotalCostAfterTax(
        totalCostBeforeTax: Double,
        luxuryTaxAmount: Double
    ): Double = com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(totalCostBeforeTax + luxuryTaxAmount, 2)


    /**
     * Calculate total weight from net weight and wastage
     */
    fun calculateTotalWeight(netWeight: Double, wastage: Double): Double {
        return com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(netWeight + wastage, 2)
    }

    /**
     * Calculate final estimated cost
     */
    fun calculateFinalEstimatedCost(articleCostAfterTax: Double, addOnCost: Double): Double {
        return com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(articleCostAfterTax + addOnCost, 2)
    }
    
    /**
     * Calculate updated pre-tax basket amount
     * Formula: original pre-tax amount + old gold item cost - extra discount
     */
    fun calculatePreTaxBasketAmount(
        originalPreTaxAmount: Double,
        oldGoldItemCost: Double,
        extraDiscount: Double
    ): Double {
        return com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(
            originalPreTaxAmount + oldGoldItemCost - extraDiscount, 2
        )
    }
    
    /**
     * Calculate basket luxury tax
     */
    fun calculateBasketLuxuryTax(preTaxBasketAmount: Double): Double {
        return calculateLuxuryTax(preTaxBasketAmount)
    }
    
    /**
     * Calculate post-tax basket amount
     */
    fun calculatePostTaxBasketAmount(
        preTaxBasketAmount: Double,
        luxuryTax: Double
    ): Double {
        return calculateTotalCostAfterTax(preTaxBasketAmount, luxuryTax)
    }
    
    /**
     * Calculate total basket amount
     * Formula: totalBasketAmount = postTaxBasketAmount + totalAddOnCost
     */
    fun calculateTotalBasketAmount(
        postTaxBasketAmount: Double,
        totalAddOnCost: Double
    ): Double {
        return com.kanishk.goldscanner.utils.Utils.roundToTwoDecimalPlaces(
            postTaxBasketAmount + totalAddOnCost, 2
        )
    }
}