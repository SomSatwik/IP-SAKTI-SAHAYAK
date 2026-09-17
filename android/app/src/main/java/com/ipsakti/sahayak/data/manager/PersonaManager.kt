package com.ipsakti.sahayak.data.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PersonaType(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val tag: String,
    val description: String,
    val focusArea: String,
    val sourceBiasing: List<String>,
    val suggestedPrompts: List<String>
) {
    PRACTITIONER(
        id = "practitioner",
        title = "Ayurvedic Practitioner",
        subtitle = "Vaidya & Clinician",
        iconEmoji = "🩺",
        tag = "Clinical & Formulations",
        description = "Focused on clinical indications, classical text grounding, dosage forms, and Schedule T GMP safety.",
        focusArea = "Clinical Efficacy, Patient Safety, Pharmacopoeia Standards",
        sourceBiasing = listOf("Ayurvedic Pharmacopoeia of India (API)", "Classical Samhitas", "Drugs & Cosmetics Act Schedule T"),
        suggestedPrompts = listOf(
            "Classical dosage forms under Schedule T GMP",
            "Standardizing polyherbal Kwatha decoctions",
            "Permissible heavy metal & microbial limits in ASU"
        )
    ),
    RESEARCHER(
        id = "researcher",
        title = "Academic / Researcher",
        subtitle = "R&D Scientist",
        iconEmoji = "🔬",
        tag = "Novelty & Pharmacology",
        description = "Focused on prior art novelty, Section 3(p) objections, synergistic extraction, and clinical trial methodology.",
        focusArea = "Novelty Discovery, Synergistic Efficacy, TKDL Prior Art",
        sourceBiasing = listOf("TKDL Database", "Indian Patent Guidelines s.3(p)", "AYUSH Research Portal", "PCT Citations"),
        suggestedPrompts = listOf(
            "Proving synergistic combination index (CI < 1)",
            "Overcoming Section 3(p) prior art objections",
            "Extraction protocols for novel standardized markers"
        )
    ),
    AYUSH_STARTUP(
        id = "startup",
        title = "AYUSH Startup",
        subtitle = "Founder & Entrepreneur",
        iconEmoji = "🚀",
        tag = "Commercialization & Speed",
        description = "Focused on rapid commercialization, Form 18A expedited examination, DPIIT incentives, and seed capital.",
        focusArea = "Expedited Patent Filing, DPIIT Startups Scheme, Fast-track NBA",
        sourceBiasing = listOf("Patent Rules Form 18A", "Startup India Scheme", "NBA 90-Day Clearance", "BIRAC Guidelines"),
        suggestedPrompts = listOf(
            "Form 18A expedited patent examination for startups",
            "90-day fast-track NBA approval for patent filing",
            "DPIIT recognition & seed funding pathways for AYUSH"
        )
    ),
    MSME(
        id = "msme",
        title = "MSME Manufacturer",
        subtitle = "Enterprise & Production",
        iconEmoji = "🏭",
        tag = "Subsidies & Licensing",
        description = "Focused on 80% patent fee concessions, Udyam benefits, SLA manufacturing licenses, and AYUSH Premium Mark.",
        focusArea = "Fee Waivers (Form 28), Manufacturing Licenses, Quality Certification",
        sourceBiasing = listOf("MSME Development Act", "Patent Fee Schedule Form 28", "AYUSH Premium Mark Standard", "State SLA Rules"),
        suggestedPrompts = listOf(
            "80% rebate on official patent fees via Form 28",
            "AYUSH Premium Mark export certification steps",
            "State Licensing Authority (SLA) Rule 158B checklist"
        )
    ),
    CULTIVATOR(
        id = "cultivator",
        title = "Herbal Cultivator",
        subtitle = "FPO & Producer",
        iconEmoji = "🌱",
        tag = "Biodiversity & ABS",
        description = "Focused on Biological Diversity Act compliance, Access and Benefit Sharing (ABS), and fair producer pricing.",
        focusArea = "State Biodiversity Board Intimation, Fair ABS, BMC Agreements",
        sourceBiasing = listOf("Biological Diversity Act 2002", "NBA ABS Regulations 2014", "Normally Traded Commodities (NTC) List", "PBR Register"),
        suggestedPrompts = listOf(
            "SBB intimation process for commercial plant harvesting",
            "Exemptions under Normally Traded Commodities (NTC)",
            "Fair Access & Benefit Sharing (ABS) fee structures"
        )
    );

    companion object {
        fun fromId(id: String?): PersonaType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: AYUSH_STARTUP
        }
    }
}

object PersonaManager {
    private const val PREFS_NAME = "ipsakti_persona_prefs"
    private const val KEY_PERSONA = "selected_persona_id"

    private var prefs: SharedPreferences? = null
    private val _currentPersona = MutableStateFlow(PersonaType.AYUSH_STARTUP)
    val currentPersona: StateFlow<PersonaType> = _currentPersona.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedId = prefs?.getString(KEY_PERSONA, PersonaType.AYUSH_STARTUP.id)
            _currentPersona.value = PersonaType.fromId(savedId)
        }
    }

    fun getPersona(): PersonaType = _currentPersona.value

    fun setPersona(persona: PersonaType) {
        _currentPersona.value = persona
        prefs?.edit()?.putString(KEY_PERSONA, persona.id)?.apply()
    }
}
