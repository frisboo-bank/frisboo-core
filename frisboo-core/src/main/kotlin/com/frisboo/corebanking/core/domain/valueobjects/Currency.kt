/*
 * Copyright 2025 Frisboo Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.frisboo.corebanking.core.domain.valueobjects

public enum class Currency(
    public val code: String,
    public val symbol: String,
    public val displayName: String,
) {
    // ========================================
    // ASIA PACIFIC (38 currencies)
    // ========================================
    AED("AED", "د.إ", "UAE Dirham"), AFN("AFN", "؋", "Afghan Afghani"), AUD(
        "AUD",
        "A$",
        "Australian Dollar",
    ),
    BDT("BDT", "৳", "Bangladeshi Taka"), BND("BND", "B$", "Brunei Dollar"), BTN(
        "BTN",
        "Nu.",
        "Bhutanese Ngultrum",
    ),
    CNY("CNY", "¥", "Chinese Yuan"), FJD("FJD", "$", "Fijian Dollar"), HKD("HKD", "HK$", "Hong Kong Dollar"), IDR(
        "IDR",
        "Rp",
        "Indonesian Rupiah",
    ),
    INR("INR", "₹", "Indian Rupee"), JPY("JPY", "¥", "Japanese Yen"), KHR("KHR", "៛", "Cambodian Riel"), KRW(
        "KRW",
        "₩",
        "South Korean Won",
    ),
    KWD("KWD", "د.ك", "Kuwaiti Dinar"), KYD("KYD", "$", "Cayman Islands Dollar"), LAK("LAK", "₭", "Lao Kip"), LKR(
        "LKR",
        "₨",
        "Sri Lankan Rupee",
    ),
    MNT("MNT", "₮", "Mongolian Tögrög"), MOP("MOP", "P", "Macanese Pataca"), MVR(
        "MVR",
        "ރ",
        "Maldivian Rufiyaa",
    ),
    MYR("MYR", "RM", "Malaysian Ringgit"), NPR("NPR", "₨", "Nepalese Rupee"), NZD(
        "NZD",
        "NZ$",
        "New Zealand Dollar",
    ),
    OMR("OMR", "ر.ع.", "Omani Rial"), PGK("PGK", "K", "Papua New Guinean Kina"), PHP(
        "PHP",
        "₱",
        "Philippine Peso",
    ),
    PKR("PKR", "₨", "Pakistani Rupee"), QAR("QAR", "ر.ق", "Qatari Riyal"), SAR("SAR", "ر.س", "Saudi Riyal"), SBD(
        "SBD",
        "$",
        "Solomon Islands Dollar",
    ),
    SCR("SCR", "₨", "Seychellois Rupee"), SGD("SGD", "S$", "Singapore Dollar"), THB("THB", "฿", "Thai Baht"), TJS(
        "TJS",
        "SM",
        "Tajikistani Somoni",
    ),
    TOP("TOP", "T$", "Tongan Paʻanga"), TWD("TWD", "NT$", "New Taiwan Dollar"), VUV("VUV", "Vt", "Vanuatu Vatu"),

    // ========================================
    // EUROPE (36 currencies)
    // ========================================
    ALL("ALL", "L", "Albanian Lek"), AMD("AMD", "֏", "Armenian Dram"), AZN("AZN", "₼", "Azerbaijani Manat"), BAM(
        "BAM",
        "KM",
        "Bosnia-Herzegovina Convertible Mark",
    ),
    BGN("BGN", "лв", "Bulgarian Lev"), BYN("BYN", "Br", "Belarusian Ruble"), CHF(
        "CHF",
        "CHF",
        "Swiss Franc",
    ),
    CZK("CZK", "Kč", "Czech Koruna"), DKK("DKK", "kr", "Danish Krone"), EUR("EUR", "€", "Euro"), GBP(
        "GBP",
        "£",
        "British Pound",
    ),
    GEL("GEL", "₾", "Georgian Lari"), GIP("GIP", "£", "Gibraltar Pound"), HRK("HRK", "kn", "Croatian Kuna"), HUF(
        "HUF",
        "Ft",
        "Hungarian Forint",
    ),
    ISK("ISK", "kr", "Icelandic Króna"), MDL("MDL", "L", "Moldovan Leu"), MKD(
        "MKD",
        "ден",
        "Macedonian Denar",
    ),
    NOK("NOK", "kr", "Norwegian Krone"), PLN("PLN", "zł", "Polish Złoty"), RON("RON", "lei", "Romanian Leu"), RSD(
        "RSD",
        "дин",
        "Serbian Dinar",
    ),
    RUB("RUB", "₽", "Russian Ruble"), SEK("SEK", "kr", "Swedish Krona"), TRY("TRY", "₺", "Turkish Lira"), UAH(
        "UAH",
        "₴",
        "Ukrainian Hryvnia",
    ),

    // ========================================
    // MIDDLE EAST (11 currencies)
    // ========================================
    BHD("BHD", ".د.ب", "Bahraini Dinar"), ILS("ILS", "₪", "Israeli Shekel"), IRR("IRR", "﷼", "Iranian Rial"), JOD(
        "JOD",
        "د.ا",
        "Jordanian Dinar",
    ),
    LBP("LBP", "ل.ل", "Lebanese Pound"), SYP("SYP", "£S", "Syrian Pound"), YER("YER", "﷼", "Yemeni Rial"),

    // ========================================
    // AFRICA (37 currencies)
    // ========================================
    AOA("AOA", "Kz", "Angolan Kwanza"), BIF("BIF", "Fr", "Burundian Franc"), CDF(
        "CDF",
        "Fr",
        "Congolese Franc",
    ),
    CVE("CVE", "$", "Cape Verdean Escudo"), DJF("DJF", "Fr", "Djiboutian Franc"), DZD(
        "DZD",
        "د.ج",
        "Algerian Dinar",
    ),
    ETB("ETB", "Br", "Ethiopian Birr"), GHS("GHS", "₵", "Ghanaian Cedi"), GMD("GMD", "D", "Gambian Dalasi"), GNF(
        "GNF",
        "Fr",
        "Guinean Franc",
    ),
    KES("KES", "Sh", "Kenyan Shilling"), LRD("LRD", "$", "Liberian Dollar"), LSL("LSL", "L", "Lesotho Loti"), LYD(
        "LYD",
        "ل.د",
        "Libyan Dinar",
    ),
    MAD("MAD", "د.م", "Moroccan Dirham"), MRU("MRU", "UM", "Mauritanian Ouguiya"), MUR(
        "MUR",
        "₨",
        "Mauritian Rupee",
    ),
    MWK("MWK", "MK", "Malawian Kwacha"), MZN("MZN", "MT", "Mozambican Metical"), NAD(
        "NAD",
        "$",
        "Namibian Dollar",
    ),
    NGN("NGN", "₦", "Nigerian Naira"), RWF("RWF", "Fr", "Rwandan Franc"), SDG(
        "SDG",
        "£Sd",
        "Sudanese Pound",
    ),
    SOS("SOS", "Sh", "Somali Shilling"), SSP("SSP", "£", "South Sudanese Pound"), STN(
        "STN",
        "Db",
        "São Tomé & Príncipe Dobra",
    ),
    SZL("SZL", "L", "Swazi Lilangeni"), TND("TND", "د.ت", "Tunisian Dinar"), TZS(
        "TZS",
        "Sh",
        "Tanzanian Shilling",
    ),
    UGX("UGX", "USh", "Ugandan Shilling"), XAF("XAF", "FCFA", "Central African CFA Franc"), ZAR(
        "ZAR",
        "R",
        "South African Rand",
    ),
    ZMW("ZMW", "ZK", "Zambian Kwacha"), ZWL("ZWL", "Z$", "Zimbabwean Dollar"),

    // ========================================
    // NORTH AMERICA (3 currencies)
    // ========================================
    CAD("CAD", "C$", "Canadian Dollar"), MXN("MXN", "$", "Mexican Peso"), USD("USD", "$", "US Dollar"),

    // ========================================
    // SOUTH AMERICA (13 currencies)
    // ========================================
    ARS("ARS", "$", "Argentine Peso"), BOB("BOB", "Bs", "Boliviano"), BRL("BRL", "R$", "Brazilian Real"), CLP(
        "CLP",
        "$",
        "Chilean Peso",
    ),
    COP("COP", "$", "Colombian Peso"), GTQ("GTQ", "Q", "Guatemalan Quetzal"), GYD(
        "GYD",
        "$",
        "Guyanese Dollar",
    ),
    HNL("HNL", "L", "Honduran Lempira"), NIO("NIO", "C$", "Nicaraguan Córdoba"), PAB(
        "PAB",
        "B/.",
        "Panamanian Balboa",
    ),
    PEN("PEN", "S/", "Peruvian Sol"), PYG("PYG", "₲", "Paraguayan Guarani"), UYU(
        "UYU",
        "\$U",
        "Uruguayan Peso",
    ),
    VES("VES", "Bs", "Venezuelan Bolívar"),

    // ========================================
    // MULTI-REGION / OTHER (4 currencies)
    // ========================================
    XCD("XCD", "EC$", "East Caribbean Dollar"), XDR("XDR", "SDR", "Special Drawing Rights"), XOF(
        "XOF",
        "CFA",
        "West African CFA Franc",
    ),
    XPF("XPF", "₣", "CFP Franc"),
    ;

    // ========================================
    // LOOKUP HELPERS
    // ========================================
    public companion object {
        private val byCode: Map<String, Currency> = entries.associateBy { it.code.uppercase() }

        public fun fromCode(code: String): Currency? = byCode[code.uppercase()]

        // ========================================
        // REGIONAL GROUPS
        // ========================================
        public val asiaPacific: Set<Currency> = setOf(
            AED,
            AFN,
            AUD,
            BDT,
            BND,
            BTN,
            CNY,
            FJD,
            HKD,
            IDR,
            INR,
            JPY,
            KHR,
            KRW,
            KWD,
            KYD,
            LAK,
            LKR,
            MNT,
            MOP,
            MVR,
            MYR,
            NPR,
            NZD,
            OMR,
            PGK,
            PHP,
            PKR,
            QAR,
            SAR,
            SBD,
            SCR,
            SGD,
            THB,
            TJS,
            TOP,
            TWD,
            VUV,
        )

        public val europe: Set<Currency> = setOf(
            ALL,
            AMD,
            AZN,
            BAM,
            BGN,
            BYN,
            CHF,
            CZK,
            DKK,
            EUR,
            GBP,
            GEL,
            GIP,
            HRK,
            HUF,
            ISK,
            MDL,
            MKD,
            NOK,
            PLN,
            RON,
            RSD,
            RUB,
            SEK,
            TRY,
            UAH,
            XPF,
        )

        public val middleEast: Set<Currency> = setOf(AED, BHD, ILS, IRR, JOD, KWD, LBP, SAR, SYP, YER)

        public val africa: Set<Currency> = setOf(
            AOA,
            BIF,
            CDF,
            CVE,
            DJF,
            DZD,
            ETB,
            GHS,
            GMD,
            GNF,
            KES,
            LRD,
            LSL,
            LYD,
            MAD,
            MRU,
            MUR,
            MWK,
            MZN,
            NAD,
            NGN,
            RWF,
            SDG,
            SOS,
            SSP,
            STN,
            SZL,
            TND,
            TZS,
            UGX,
            XAF,
            XOF,
            ZAR,
            ZMW,
            ZWL,
        )

        public val northAmerica: Set<Currency> = setOf(CAD, MXN, USD)

        public val southAmerica: Set<Currency> = setOf(
            ARS,
            BOB,
            BRL,
            CLP,
            COP,
            GTQ,
            GYD,
            HNL,
            NIO,
            PAB,
            PEN,
            PYG,
            UYU,
            VES,
        )
    }
}
