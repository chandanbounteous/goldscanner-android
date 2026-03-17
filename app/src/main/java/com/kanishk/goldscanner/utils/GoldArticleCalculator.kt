package com.kanishk.goldscanner.utils

import android.util.Log
import com.kanishk.goldscanner.data.model.GoldArticleCalculation
import com.kanishk.goldscanner.data.model.LookupEntryForWastageAndMakingCharge

object GoldArticleCalculator {
    
    // Flag to enable/disable logging
    private const val ENABLE_LOGGING = true
    private const val TAG = "GoldArticleCalculator"
    
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
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCost() called with: currentGoldRate24KPerTola=$currentGoldRate24KPerTola, netWeight=$netWeight, karat=$karat, addOnCost=$addOnCost, discount=$discount")
        }
        
        // Step 1: Calculate wastage
        val wastage = calculateWastage(netWeight, karat)
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCost() wastage calculated: $wastage")
        }
        
        // Step 2: Calculate total weight (net weight + wastage)
        val totalWeight = netWeight + wastage
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCost() totalWeight calculated: $totalWeight")
        }
        
        // Step 3: Calculate cost as per weight, rate and karat
        val articleCostAsPerWeightRateAndKarat = calculateArticleCostAsPerWeightRateAndKarat(
            totalWeight, karat, currentGoldRate24KPerTola
        )
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCost() articleCostAsPerWeightRateAndKarat calculated: $articleCostAsPerWeightRateAndKarat")
        }
        
        // Step 4: Calculate making charge (needs total taxable amount for some entries)
        val makingCharge = calculateMakingCharge(netWeight, karat, articleCostAsPerWeightRateAndKarat)
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCost() makingCharge calculated: $makingCharge")
        }
        
        // Step 5: Calculate total cost before tax
        val totalCostBeforeTax = calculateTotalCostBeforeTax(
            articleCostAsPerWeightRateAndKarat,
            makingCharge, 
            discount
        )
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCost() totalCostBeforeTax calculated: $totalCostBeforeTax")
        }
        
        // Step 6: Calculate luxury tax
        val luxuryTax = calculateLuxuryTax(totalCostBeforeTax)
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCost() luxuryTax calculated: $luxuryTax")
        }
        
        // Step 7: Calculate total cost after tax
        val totalCostAfterTax = calculateTotalCostAfterTax(totalCostBeforeTax, luxuryTax)
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCost() totalCostAfterTax calculated: $totalCostAfterTax")
        }
        
        // Step 8: Calculate final estimated cost
        val finalEstimatedCost = totalCostAfterTax + addOnCost
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCost() finalEstimatedCost calculated: $finalEstimatedCost")
        }
        
        val result = GoldArticleCalculation(
            wastage = com.kanishk.goldscanner.utils.Utils.roundToDecimalPlaces(wastage, 12),
            articleCostAsPerWeightRateAndKarat = com.kanishk.goldscanner.utils.Utils.roundToDecimalPlaces(articleCostAsPerWeightRateAndKarat, 2),
            makingCharge = com.kanishk.goldscanner.utils.Utils.roundToDecimalPlaces(makingCharge, 2),
            totalCostBeforeTax = totalCostBeforeTax,
            luxuryTax = luxuryTax,
            totalCostAfterTax = totalCostAfterTax,
            finalEstimatedCost = com.kanishk.goldscanner.utils.Utils.roundToDecimalPlaces(finalEstimatedCost, 2)
        )
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCost() returning: $result")
        }
        return result
    }

    fun calculateWastage(netWeight: Double, karat: Int): Double {
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateWastage() called with: netWeight=$netWeight, karat=$karat")
        }
        
        val matchedEntry = lookupTable.firstOrNull { entry ->
            netWeight >= entry.minNetWeight &&
                    netWeight < entry.maxNetWeight &&
                    (karat == entry.karat || entry.karat == 0)
        }
        
        val result = matchedEntry?.wastage?.invoke(netWeight) ?: 0.0
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateWastage() returning: $result (matchedEntry found: ${matchedEntry != null})")
        }
        return result
    }

    fun calculateArticleCostAsPerWeightRateAndKarat(
        totalWeight: Double,
        karat: Int,
        goldRatePerTola: Double
    ): Double {
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCostAsPerWeightRateAndKarat() called with: totalWeight=$totalWeight, karat=$karat, goldRatePerTola=$goldRatePerTola")
        }
        
        val weightInTolas = totalWeight / ONE_TOLA_IN_GMS
        val purityFactor = if (karat == KARAT_24) PURITY_FACTOR_24_KARAT else PURITY_FACTOR_22_KARAT
        val cost = weightInTolas * purityFactor * goldRatePerTola
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateArticleCostAsPerWeightRateAndKarat() weightInTolas=$weightInTolas, purityFactor=$purityFactor, returning: $cost")
        }
        return cost
    }

    fun calculateMakingCharge(netWeight: Double, karat: Int, totalTaxableAmount: Double): Double {
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateMakingCharge() called with: netWeight=$netWeight, karat=$karat, totalTaxableAmount=$totalTaxableAmount")
        }
        
        val matchedEntry = lookupTable.firstOrNull { entry ->
            netWeight >= entry.minNetWeight &&
                    netWeight < entry.maxNetWeight &&
                    (karat == entry.karat || entry.karat == 0)
        }
        
        val result = matchedEntry?.makingCharge?.invoke(totalTaxableAmount) ?: 0.0
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateMakingCharge() returning: $result (matchedEntry found: ${matchedEntry != null})")
        }
        return result
    }

    fun calculateTotalCostBeforeTax(
        costAsPerGoldRateAndKarat: Double,
        makingCharge: Double,
        discount: Double
    ): Double {
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateTotalCostBeforeTax() called with: costAsPerGoldRateAndKarat=$costAsPerGoldRateAndKarat, makingCharge=$makingCharge, discount=$discount")
        }
        
        val totalTaxableAmount = costAsPerGoldRateAndKarat + makingCharge - discount
        val result = com.kanishk.goldscanner.utils.Utils.roundToDecimalPlaces(totalTaxableAmount, 2)
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateTotalCostBeforeTax() totalTaxableAmount=$totalTaxableAmount, returning: $result")
        }
        return result
    }

    fun calculateLuxuryTax(totalCostBeforeTax: Double): Double {
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateLuxuryTax() called with: totalCostBeforeTax=$totalCostBeforeTax")
        }
        
        val result = com.kanishk.goldscanner.utils.Utils.roundToDecimalPlaces(LUXURY_TAX_PERCENT * totalCostBeforeTax, 2)
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateLuxuryTax() returning: $result (LUXURY_TAX_PERCENT=$LUXURY_TAX_PERCENT)")
        }
        return result
    }

    fun calculateTotalCostAfterTax(
        totalCostBeforeTax: Double,
        luxuryTaxAmount: Double
    ): Double {
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateTotalCostAfterTax() called with: totalCostBeforeTax=$totalCostBeforeTax, luxuryTaxAmount=$luxuryTaxAmount")
        }
        
        val result = com.kanishk.goldscanner.utils.Utils.roundToDecimalPlaces(totalCostBeforeTax + luxuryTaxAmount, 2)
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateTotalCostAfterTax() returning: $result")
        }
        return result
    }


    /**
     * Calculate total weight from net weight and wastage
     */
    fun calculateTotalWeight(netWeight: Double, wastage: Double): Double {
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateTotalWeight() called with: netWeight=$netWeight, wastage=$wastage")
        }
        
        val result = com.kanishk.goldscanner.utils.Utils.roundToDecimalPlaces(netWeight + wastage, 12)
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateTotalWeight() returning: $result")
        }
        return result
    }

    /**
     * Calculate final estimated cost
     */
    fun calculateFinalEstimatedCost(articleCostAfterTax: Double, addOnCost: Double): Double {
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateFinalEstimatedCost() called with: articleCostAfterTax=$articleCostAfterTax, addOnCost=$addOnCost")
        }
        
        val result = com.kanishk.goldscanner.utils.Utils.roundToDecimalPlaces(articleCostAfterTax + addOnCost, 2)
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateFinalEstimatedCost() returning: $result")
        }
        return result
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
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculatePreTaxBasketAmount() called with: originalPreTaxAmount=$originalPreTaxAmount, oldGoldItemCost=$oldGoldItemCost, extraDiscount=$extraDiscount")
        }
        
        val result = com.kanishk.goldscanner.utils.Utils.roundToDecimalPlaces(
            originalPreTaxAmount - (oldGoldItemCost + extraDiscount), 2
        )
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculatePreTaxBasketAmount() returning: $result")
        }
        return result
    }
    
    /**
     * Calculate basket luxury tax
     */
    fun calculateBasketLuxuryTax(preTaxBasketAmount: Double): Double {
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateBasketLuxuryTax() called with: preTaxBasketAmount=$preTaxBasketAmount")
        }
        
        val result = calculateLuxuryTax(preTaxBasketAmount)
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateBasketLuxuryTax() returning: $result")
        }
        return result
    }
    
    /**
     * Calculate post-tax basket amount
     */
    fun calculatePostTaxBasketAmount(
        preTaxBasketAmount: Double,
        luxuryTax: Double
    ): Double {
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculatePostTaxBasketAmount() called with: preTaxBasketAmount=$preTaxBasketAmount, luxuryTax=$luxuryTax")
        }
        
        val result = calculateTotalCostAfterTax(preTaxBasketAmount, luxuryTax)
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculatePostTaxBasketAmount() returning: $result")
        }
        return result
    }
    
    /**
     * Calculate total basket amount
     * Formula: totalBasketAmount = postTaxBasketAmount + totalAddOnCost
     */
    fun calculateTotalBasketAmount(
        postTaxBasketAmount: Double,
        totalAddOnCost: Double
    ): Double {
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateTotalBasketAmount() called with: postTaxBasketAmount=$postTaxBasketAmount, totalAddOnCost=$totalAddOnCost")
        }
        
        val result = com.kanishk.goldscanner.utils.Utils.roundToDecimalPlaces(
            postTaxBasketAmount + totalAddOnCost, 2
        )
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "calculateTotalBasketAmount() returning: $result")
        }
        return result
    }
}