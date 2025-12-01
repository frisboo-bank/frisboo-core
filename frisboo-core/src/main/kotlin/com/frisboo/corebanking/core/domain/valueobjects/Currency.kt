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
    AED("AED", "د.إ", "UAE Dirham"),
    AFN("AFN", "؋", "Afghan Afghani"),
    AUD("AUD", "A$", "Australian Dollar"),
    BDT("BDT", "৳", "Bangladeshi Taka"),
    BND("BND", "B$", "Brunei Dollar"),
    BTN("BTN", "Nu.", "Bhutanese Ngultrum"),
    CNY("CNY", "¥", "Chinese Yuan"),
    FJD("FJD", "$", "Fijian Dollar"),
    HKD("HKD", "HK$", "Hong Kong Dollar"),
    IDR("IDR", "Rp", "Indonesian Rupiah"),
    INR("INR", "₹", "Indian Rupee"),
    JPY("JPY", "¥", "Japanese Yen"),
    KHR("KHR", "៛", "Cambodian Riel"),
    KRW("KRW", "₩", "South Korean Won"),
    KWD("KWD", "د.ك", "Kuwaiti Dinar"),
    KYD("KYD", "$", "Cayman Islands Dollar"),
    LAK("LAK", "₭", "Lao Kip"),
    LKR("LKR", "₨", "Sri Lankan Rupee"),
    MNT("MNT", "₮", "Mongolian Tögrög"),
    MOP("MOP", "P", "Macanese Pataca"),
    MVR("MVR", "ރ", "Maldivian Rufiyaa"),
    MYR("MYR", "RM", "Malaysian Ringgit"),
    NPR("NPR", "₨", "Nepalese Rupee"),
    NZD("NZD", "NZ$", "New Zealand Dollar"),
    OMR("OMR", "ر.ع.", "Omani Rial"),
    PGK("PGK", "K", "Papua New Guinean Kina"),
    PHP("PHP", "₱", "Philippine Peso"),
    PKR("PKR", "₨", "Pakistani Rupee"),
    QAR("QAR", "ر.ق", "Qatari Riyal"),
    SAR("SAR", "ر.س", "Saudi Riyal"),
    SBD("SBD", "$", "Solomon Islands Dollar"),
    SCR("SCR", "₨", "Seychellois Rupee"),
    SGD("SGD", "S$", "Singapore Dollar"),
    THB("THB", "฿", "Thai Baht"),
    TJS("TJS", "SM", "Tajikistani Somoni"),
    TOP("TOP", "T$", "Tongan Paʻanga"),
    TWD("TWD", "NT$", "New Taiwan Dollar"),
    VUV("VUV", "Vt", "Vanuatu Vatu"),

    // ========================================
    // EUROPE (36 currencies)
    // ========================================
    ALL("ALL", "L", "Albanian Lek"),
    AMD("AMD", "֏", "Armenian Dram"),
    AZN("AZN", "₼", "Azerbaijani Manat"),
    BAM("BAM", "KM", "Bosnia-Herzegovina Convertible Mark"),
    BGN("BGN", "лв", "Bulgarian Lev"),
    BYN("BYN", "Br", "Belarusian Ruble"),
    CHF("CHF", "CHF", "Swiss Franc"),
    CZK("CZK", "Kč", "Czech Koruna"),
    DKK("DKK", "kr", "Danish Krone"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    GEL("GEL", "₾", "Georgian Lari"),
    GIP("GIP", "£", "Gibraltar Pound"),
    HRK("HRK", "kn", "Croatian Kuna"),
    HUF("HUF", "Ft", "Hungarian Forint"),
    ISK("ISK", "kr", "Icelandic Króna"),
    MDL("MDL", "L", "Moldovan Leu"),
    MKD("MKD", "ден", "Macedonian Denar"),
    NOK("NOK", "kr", "Norwegian Krone"),
    PLN("PLN", "zł", "Polish Złoty"),
    RON("RON", "lei", "Romanian Leu"),
    RSD("RSD", "дин", "Serbian Dinar"),
    RUB("RUB", "₽", "Russian Ruble"),
    SEK("SEK", "kr", "Swedish Krona"),
    TRY("TRY", "₺", "Turkish Lira"),
    UAH("UAH", "₴", "Ukrainian Hryvnia"),

    // ========================================
    // MIDDLE EAST (11 currencies)
    // ========================================
    BHD("BHD", ".د.ب", "Bahraini Dinar"),
    ILS("ILS", "₪", "Israeli Shekel"),
    IRR("IRR", "﷼", "Iranian Rial"),
    JOD("JOD", "د.ا", "Jordanian Dinar"),
    LBP("LBP", "ل.ل", "Lebanese Pound"),
    SYP("SYP", "£S", "Syrian Pound"),
    YER("YER", "﷼", "Yemeni Rial"),

    // ========================================
    // AFRICA (37 currencies)
    // ========================================
    AOA("AOA", "Kz", "Angolan Kwanza"),
    BIF("BIF", "Fr", "Burundian Franc"),
    CDF("CDF", "Fr", "Congolese Franc"),
    CVE("CVE", "$", "Cape Verdean Escudo"),
    DJF("DJF", "Fr", "Djiboutian Franc"),
    DZD("DZD", "د.ج", "Algerian Dinar"),
    ETB("ETB", "Br", "Ethiopian Birr"),
    GHS("GHS", "₵", "Ghanaian Cedi"),
    GMD("GMD", "D", "Gambian Dalasi"),
    GNF("GNF", "Fr", "Guinean Franc"),
    KES("KES", "Sh", "Kenyan Shilling"),
    LRD("LRD", "$", "Liberian Dollar"),
    LSL("LSL", "L", "Lesotho Loti"),
    LYD("LYD", "ل.د", "Libyan Dinar"),
    MAD("MAD", "د.م", "Moroccan Dirham"),
    MRU("MRU", "UM", "Mauritanian Ouguiya"),
    MUR("MUR", "₨", "Mauritian Rupee"),
    MWK("MWK", "MK", "Malawian Kwacha"),
    MZN("MZN", "MT", "Mozambican Metical"),
    NAD("NAD", "$", "Namibian Dollar"),
    NGN("NGN", "₦", "Nigerian Naira"),
    RWF("RWF", "Fr", "Rwandan Franc"),
    SDG("SDG", "£Sd", "Sudanese Pound"),
    SOS("SOS", "Sh", "Somali Shilling"),
    SSP("SSP", "£", "South Sudanese Pound"),
    STN("STN", "Db", "São Tomé & Príncipe Dobra"),
    SZL("SZL", "L", "Swazi Lilangeni"),
    TND("TND", "د.ت", "Tunisian Dinar"),
    TZS("TZS", "Sh", "Tanzanian Shilling"),
    UGX("UGX", "USh", "Ugandan Shilling"),
    XAF("XAF", "FCFA", "Central African CFA Franc"),
    ZAR("ZAR", "R", "South African Rand"),
    ZMW("ZMW", "ZK", "Zambian Kwacha"),
    ZWL("ZWL", "Z$", "Zimbabwean Dollar"),

    // ========================================
    // NORTH AMERICA (3 currencies)
    // ========================================
    CAD("CAD", "C$", "Canadian Dollar"),
    MXN("MXN", "$", "Mexican Peso"),
    USD("USD", "$", "US Dollar"),

    // ========================================
    // SOUTH AMERICA (13 currencies)
    // ========================================
    ARS("ARS", "$", "Argentine Peso"),
    BOB("BOB", "Bs", "Boliviano"),
    BRL("BRL", "R$", "Brazilian Real"),
    CLP("CLP", "$", "Chilean Peso"),
    COP("COP", "$", "Colombian Peso"),
    GTQ("GTQ", "Q", "Guatemalan Quetzal"),
    GYD("GYD", "$", "Guyanese Dollar"),
    HNL("HNL", "L", "Honduran Lempira"),
    NIO("NIO", "C$", "Nicaraguan Córdoba"),
    PAB("PAB", "B/.", "Panamanian Balboa"),
    PEN("PEN", "S/", "Peruvian Sol"),
    PYG("PYG", "₲", "Paraguayan Guarani"),
    UYU("UYU", $$"$U", "Uruguayan Peso"),
    VES("VES", "Bs", "Venezuelan Bolívar"),

    // ========================================
    // MULTI-REGION / OTHER (4 currencies)
    // ========================================
    XCD("XCD", "EC$", "East Caribbean Dollar"),
    XDR("XDR", "SDR", "Special Drawing Rights"),
    XOF("XOF", "CFA", "West African CFA Franc"),
    XPF("XPF", "₣", "CFP Franc"),
    ;

    // ========================================
    // REGIONAL GROUPS
    // ========================================
    public companion object {
        public object AsiaPacific {
            public val AED: Currency = Currency.AED
            public val AFN: Currency = Currency.AFN
            public val AUD: Currency = Currency.AUD
            public val BDT: Currency = Currency.BDT
            public val BND: Currency = Currency.BND
            public val BTN: Currency = Currency.BTN
            public val CNY: Currency = Currency.CNY
            public val FJD: Currency = Currency.FJD
            public val HKD: Currency = Currency.HKD
            public val IDR: Currency = Currency.IDR
            public val INR: Currency = Currency.INR
            public val JPY: Currency = Currency.JPY
            public val KHR: Currency = Currency.KHR
            public val KRW: Currency = Currency.KRW
            public val KWD: Currency = Currency.KWD
            public val KYD: Currency = Currency.KYD
            public val LAK: Currency = Currency.LAK
            public val LKR: Currency = Currency.LKR
            public val MNT: Currency = Currency.MNT
            public val MOP: Currency = Currency.MOP
            public val MVR: Currency = Currency.MVR
            public val MYR: Currency = Currency.MYR
            public val NPR: Currency = Currency.NPR
            public val NZD: Currency = Currency.NZD
            public val OMR: Currency = Currency.OMR
            public val PGK: Currency = Currency.PGK
            public val PHP: Currency = Currency.PHP
            public val PKR: Currency = Currency.PKR
            public val QAR: Currency = Currency.QAR
            public val SAR: Currency = Currency.SAR
            public val SBD: Currency = Currency.SBD
            public val SCR: Currency = Currency.SCR
            public val SGD: Currency = Currency.SGD
            public val THB: Currency = Currency.THB
            public val TJS: Currency = Currency.TJS
            public val TOP: Currency = Currency.TOP
            public val TWD: Currency = Currency.TWD
            public val VUV: Currency = Currency.VUV
        }

        public object Europe {
            public val ALL: Currency = Currency.ALL
            public val AMD: Currency = Currency.AMD
            public val AZN: Currency = Currency.AZN
            public val BAM: Currency = Currency.BAM
            public val BGN: Currency = Currency.BGN
            public val BYN: Currency = Currency.BYN
            public val CHF: Currency = Currency.CHF
            public val CZK: Currency = Currency.CZK
            public val DKK: Currency = Currency.DKK
            public val EUR: Currency = Currency.EUR
            public val GBP: Currency = Currency.GBP
            public val GEL: Currency = Currency.GEL
            public val GIP: Currency = Currency.GIP
            public val HRK: Currency = Currency.HRK
            public val HUF: Currency = Currency.HUF
            public val ISK: Currency = Currency.ISK
            public val MDL: Currency = Currency.MDL
            public val MKD: Currency = Currency.MKD
            public val NOK: Currency = Currency.NOK
            public val PLN: Currency = Currency.PLN
            public val RON: Currency = Currency.RON
            public val RSD: Currency = Currency.RSD
            public val RUB: Currency = Currency.RUB
            public val SEK: Currency = Currency.SEK
            public val TRY: Currency = Currency.TRY
            public val UAH: Currency = Currency.UAH
            public val XPF: Currency = Currency.XPF

            // Country-specific accessors
            public val Eurozone: Currency = Currency.EUR
            public val UnitedKingdom: Currency = Currency.GBP
            public val Switzerland: Currency = Currency.CHF
            public val Sweden: Currency = Currency.SEK
            public val Norway: Currency = Currency.NOK
            public val Denmark: Currency = Currency.DKK
            public val Poland: Currency = Currency.PLN
            public val Turkey: Currency = Currency.TRY
            public val Russia: Currency = Currency.RUB
            public val Czech: Currency = Currency.CZK
            public val Hungary: Currency = Currency.HUF
            public val Romania: Currency = Currency.RON
        }

        public object MiddleEast {
            public val AED: Currency = Currency.AED
            public val BHD: Currency = Currency.BHD
            public val ILS: Currency = Currency.ILS
            public val IRR: Currency = Currency.IRR
            public val JOD: Currency = Currency.JOD
            public val KWD: Currency = Currency.KWD
            public val LBP: Currency = Currency.LBP
            public val SAR: Currency = Currency.SAR
            public val SYP: Currency = Currency.SYP
            public val YER: Currency = Currency.YER
        }

        public object Africa {
            public val AOA: Currency = Currency.AOA
            public val BIF: Currency = Currency.BIF
            public val CVE: Currency = Currency.CVE
            public val CDF: Currency = Currency.CDF
            public val DJF: Currency = Currency.DJF
            public val DZD: Currency = Currency.DZD
            public val ETB: Currency = Currency.ETB
            public val GHS: Currency = Currency.GHS
            public val GMD: Currency = Currency.GMD
            public val GNF: Currency = Currency.GNF
            public val KES: Currency = Currency.KES
            public val LRD: Currency = Currency.LRD
            public val LSL: Currency = Currency.LSL
            public val LYD: Currency = Currency.LYD
            public val MAD: Currency = Currency.MAD
            public val MRU: Currency = Currency.MRU
            public val MUR: Currency = Currency.MUR
            public val MWK: Currency = Currency.MWK
            public val MZN: Currency = Currency.MZN
            public val NAD: Currency = Currency.NAD
            public val NGN: Currency = Currency.NGN
            public val RWF: Currency = Currency.RWF
            public val SDG: Currency = Currency.SDG
            public val SOS: Currency = Currency.SOS
            public val SSP: Currency = Currency.SSP
            public val STN: Currency = Currency.STN
            public val SZL: Currency = Currency.SZL
            public val TND: Currency = Currency.TND
            public val TZS: Currency = Currency.TZS
            public val UGX: Currency = Currency.UGX
            public val XAF: Currency = Currency.XAF
            public val XOF: Currency = Currency.XOF
            public val ZAR: Currency = Currency.ZAR
            public val ZMW: Currency = Currency.ZMW
            public val ZWL: Currency = Currency.ZWL
        }

        public object NorthAmerica {
            public val CAD: Currency = Currency.CAD
            public val USD: Currency = Currency.USD
            public val MXN: Currency = Currency.MXN
        }

        public object SouthAmerica {
            public val ARS: Currency = Currency.ARS
            public val BOB: Currency = Currency.BOB
            public val BRL: Currency = Currency.BRL
            public val CLP: Currency = Currency.CLP
            public val COP: Currency = Currency.COP
            public val GTQ: Currency = Currency.GTQ
            public val GYD: Currency = Currency.GYD
            public val HNL: Currency = Currency.HNL
            public val NIO: Currency = Currency.NIO
            public val PAB: Currency = Currency.PAB
            public val PEN: Currency = Currency.PEN
            public val PYG: Currency = Currency.PYG
            public val UYU: Currency = Currency.UYU
            public val VES: Currency = Currency.VES
        }

        public fun fromCode(code: String): Currency? =
            entries.find {
                it.code.equals(code, ignoreCase = true)
            }

        public fun fromSymbol(symbol: String): Currency? =
            entries.find {
                it.symbol == symbol
            }
    }
}
