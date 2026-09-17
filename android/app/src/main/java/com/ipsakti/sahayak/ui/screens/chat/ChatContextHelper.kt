package com.ipsakti.sahayak.ui.screens.chat

/**
 * Centralized utility for constructing context-aware queries for AyurBot.
 * Used when users tap "Ask AyurBot" across evidence cards, risk flags,
 * compliance roadmap steps, formulation novelty cards, and persona dashboards.
 */
object ChatContextHelper {

    fun forEvidence(sourceTitle: String, citationRef: String, excerpt: String): String {
        val cleanExcerpt = excerpt.take(180).trim().replace("\n", " ")
        return "Explain how the statutory evidence '$sourceTitle' (Ref: $citationRef) impacts patentability and regulatory compliance under Indian IP law. Relevant excerpt: \"$cleanExcerpt\""
    }

    fun forRiskFlag(riskText: String, contextQuery: String? = null): String {
        return if (!contextQuery.isNullOrBlank()) {
            "Explain the following risk flag identified for '$contextQuery': \"$riskText\". What statutory provisions (e.g. Patent Act Section 3(p)/3(e), Biological Diversity Act, or AYUSH Rule 158B) apply, and what specific steps can mitigate this objection?"
        } else {
            "Explain the regulatory risk flag: \"$riskText\". How does this affect AYUSH product approval or patent eligibility, and what evidence is required to overcome it?"
        }
    }

    fun forInvestigationResult(query: String, status: String, topRisk: String? = null): String {
        val riskPart = if (!topRisk.isNullOrBlank()) " Key hurdle noted: \"$topRisk\"." else ""
        return "Deep-dive into the IP investigation report for \"$query\" (Status: $status).$riskPart What are the priority legal, empirical, and regulatory actions needed to secure IP or commercial approval in India?"
    }

    fun forComplianceStep(stepTitle: String, stepDescription: String): String {
        val cleanDesc = stepDescription.take(140).trim().replace("\n", " ")
        return "Provide detailed procedural guidance for the compliance step: '$stepTitle'. Details: \"$cleanDesc\". What statutory forms, supporting affidavits, and regulatory portals (e.g. e-Aushadhi, Indian Patent Office, NBA) are required?"
    }

    fun forBotanicalSafety(herbName: String, botanicalName: String, reason: String): String {
        return "Under Drugs & Cosmetics Rules Schedule E(1) and AYUSH pharmacopoeial standards, what are the safety precautions, permissible toxicity limits, and classical Shodhana (purification) mandates for $herbName ($botanicalName)? Context: $reason."
    }

    fun forClassicalText(textName: String, topic: String): String {
        return "Search authoritative classical references in $textName regarding $topic. What are the classical therapeutic indications (Rogaghnata), processing methods (Sanskara), and dosage forms cited?"
    }

    fun forAbsBenefitSharing(activity: String, commercialContext: String): String {
        return "Explain the Access & Benefit Sharing (ABS) compliance process under the Biological Diversity Act 2002 for $activity ($commercialContext). What are the exact fee regulations, exemptions for Indian entities/cultivators, and SBB intimation timelines?"
    }

    fun forPatentTriage(ingredients: String, formulationType: String): String {
        return "Evaluate patent eligibility under Indian Patent Act Section 3(p) and Section 3(e) for a $formulationType containing: $ingredients. Does it constitute known traditional knowledge or an aggregation of properties? How can synergy (CI < 1) be established?"
    }

    fun forRule158B(formulationType: String): String {
        return "Explain the licensing requirements under AYUSH Rule 158B for a $formulationType formulation. What specific safety, pilot clinical, or stability data is required by the State Licensing Authority (SLA) for Form 25D vs Form 25E?"
    }
}
