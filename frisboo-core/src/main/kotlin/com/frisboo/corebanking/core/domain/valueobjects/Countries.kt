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

public enum class Country(
    public val code: String,
    public val displayName: String,
) {
    // ========================================
    // ASIA PACIFIC (45 countries)
    // ========================================
    AF("AF", "Afghanistan"),
    AS("AS", "American Samoa"),
    AU("AU", "Australia"),
    BD("BD", "Bangladesh"),
    BN("BN", "Brunei"),
    BT("BT", "Bhutan"),
    CC("CC", "Cocos Islands"),
    CK("CK", "Cook Islands"),
    CN("CN", "China"),
    CX("CX", "Christmas Island"),
    FJ("FJ", "Fiji"),
    FM("FM", "Micronesia"),
    GU("GU", "Guam"),
    HK("HK", "Hong Kong"),
    ID("ID", "Indonesia"),
    IN("IN", "India"),
    JP("JP", "Japan"),
    KH("KH", "Cambodia"),
    KI("KI", "Kiribati"),
    KP("KP", "North Korea"),
    KR("KR", "South Korea"),
    KZ("KZ", "Kazakhstan"),
    LA("LA", "Laos"),
    LK("LK", "Sri Lanka"),
    MM("MM", "Myanmar"),
    MN("MN", "Mongolia"),
    MO("MO", "Macao"),
    MV("MV", "Maldives"),
    MY("MY", "Malaysia"),
    NP("NP", "Nepal"),
    NR("NR", "Nauru"),
    NU("NU", "Niue"),
    NZ("NZ", "New Zealand"),
    PF("PF", "French Polynesia"),
    PG("PG", "Papua New Guinea"),
    PH("PH", "Philippines"),
    PK("PK", "Pakistan"),
    PN("PN", "Pitcairn"),
    PW("PW", "Palau"),
    QA("QA", "Qatar"),
    SB("SB", "Solomon Islands"),
    SG("SG", "Singapore"),
    TH("TH", "Thailand"),
    TJ("TJ", "Tajikistan"),
    TK("TK", "Tokelau"),
    TL("TL", "Timor-Leste"),
    TM("TM", "Turkmenistan"),
    TV("TV", "Tuvalu"),
    TW("TW", "Taiwan"),
    UZ("UZ", "Uzbekistan"),
    VN("VN", "Vietnam"),
    VU("VU", "Vanuatu"),
    WS("WS", "Samoa"),

    // ========================================
    // EUROPE (49 countries)
    // ========================================
    AD("AD", "Andorra"),
    AL("AL", "Albania"),
    AM("AM", "Armenia"),
    AT("AT", "Austria"),
    AX("AX", "Åland Islands"),
    AZ("AZ", "Azerbaijan"),
    BA("BA", "Bosnia and Herzegovina"),
    BE("BE", "Belgium"),
    BG("BG", "Bulgaria"),
    BY("BY", "Belarus"),
    CH("CH", "Switzerland"),
    CZ("CZ", "Czech Republic"),
    DE("DE", "Germany"),
    DK("DK", "Denmark"),
    EE("EE", "Estonia"),
    ES("ES", "Spain"),
    FI("FI", "Finland"),
    FO("FO", "Faroe Islands"),
    FR("FR", "France"),
    GB("GB", "United Kingdom"),
    GE("GE", "Georgia"),
    GG("GG", "Guernsey"),
    GI("GI", "Gibraltar"),
    GR("GR", "Greece"),
    HR("HR", "Croatia"),
    HU("HU", "Hungary"),
    IE("IE", "Ireland"),
    IM("IM", "Isle of Man"),
    IS("IS", "Iceland"),
    IT("IT", "Italy"),
    JE("JE", "Jersey"),
    LI("LI", "Liechtenstein"),
    LT("LT", "Lithuania"),
    LU("LU", "Luxembourg"),
    LV("LV", "Latvia"),
    MC("MC", "Monaco"),
    MD("MD", "Moldova"),
    ME("ME", "Montenegro"),
    MK("MK", "North Macedonia"),
    MT("MT", "Malta"),
    NL("NL", "Netherlands"),
    NO("NO", "Norway"),
    PL("PL", "Poland"),
    PT("PT", "Portugal"),
    RO("RO", "Romania"),
    RS("RS", "Serbia"),
    RU("RU", "Russia"),
    SE("SE", "Sweden"),
    SI("SI", "Slovenia"),
    SJ("SJ", "Svalbard and Jan Mayen"),
    SK("SK", "Slovakia"),
    SM("SM", "San Marino"),
    TR("TR", "Turkey"),
    UA("UA", "Ukraine"),
    VA("VA", "Vatican City"),

    // ========================================
    // MIDDLE EAST (14 countries)
    // ========================================
    AE("AE", "United Arab Emirates"),
    BH("BH", "Bahrain"),
    CY("CY", "Cyprus"),
    EG("EG", "Egypt"),
    IL("IL", "Israel"),
    IQ("IQ", "Iraq"),
    IR("IR", "Iran"),
    JO("JO", "Jordan"),
    KW("KW", "Kuwait"),
    LB("LB", "Lebanon"),
    OM("OM", "Oman"),
    PS("PS", "Palestine"),
    SA("SA", "Saudi Arabia"),
    SY("SY", "Syria"),
    YE("YE", "Yemen"),

    // ========================================
    // AFRICA (54 countries)
    // ========================================
    AO("AO", "Angola"),
    BF("BF", "Burkina Faso"),
    BI("BI", "Burundi"),
    BJ("BJ", "Benin"),
    BW("BW", "Botswana"),
    CD("CD", "Democratic Republic of the Congo"),
    CF("CF", "Central African Republic"),
    CG("CG", "Republic of the Congo"),
    CI("CI", "Ivory Coast"),
    CM("CM", "Cameroon"),
    CV("CV", "Cape Verde"),
    DJ("DJ", "Djibouti"),
    DZ("DZ", "Algeria"),
    EH("EH", "Western Sahara"),
    ER("ER", "Eritrea"),
    ET("ET", "Ethiopia"),
    GA("GA", "Gabon"),
    GH("GH", "Ghana"),
    GM("GM", "Gambia"),
    GN("GN", "Guinea"),
    GQ("GQ", "Equatorial Guinea"),
    GW("GW", "Guinea-Bissau"),
    KE("KE", "Kenya"),
    KM("KM", "Comoros"),
    LR("LR", "Liberia"),
    LS("LS", "Lesotho"),
    LY("LY", "Libya"),
    MA("MA", "Morocco"),
    MG("MG", "Madagascar"),
    ML("ML", "Mali"),
    MR("MR", "Mauritania"),
    MU("MU", "Mauritius"),
    MW("MW", "Malawi"),
    MZ("MZ", "Mozambique"),
    NA("NA", "Namibia"),
    NE("NE", "Niger"),
    NG("NG", "Nigeria"),
    RE("RE", "Réunion"),
    RW("RW", "Rwanda"),
    SC("SC", "Seychelles"),
    SD("SD", "Sudan"),
    SH("SH", "Saint Helena"),
    SL("SL", "Sierra Leone"),
    SN("SN", "Senegal"),
    SO("SO", "Somalia"),
    SS("SS", "South Sudan"),
    ST("ST", "São Tomé and Príncipe"),
    SZ("SZ", "Eswatini"),
    TD("TD", "Chad"),
    TG("TG", "Togo"),
    TN("TN", "Tunisia"),
    TZ("TZ", "Tanzania"),
    UG("UG", "Uganda"),
    YT("YT", "Mayotte"),
    ZA("ZA", "South Africa"),
    ZM("ZM", "Zambia"),
    ZW("ZW", "Zimbabwe"),

    // ========================================
    // NORTH AMERICA (33 countries)
    // ========================================
    AG("AG", "Antigua and Barbuda"),
    AI("AI", "Anguilla"),
    AW("AW", "Aruba"),
    BB("BB", "Barbados"),
    BL("BL", "Saint Barthélemy"),
    BM("BM", "Bermuda"),
    BS("BS", "Bahamas"),
    BZ("BZ", "Belize"),
    CA("CA", "Canada"),
    CR("CR", "Costa Rica"),
    CU("CU", "Cuba"),
    DM("DM", "Dominica"),
    DO("DO", "Dominican Republic"),
    GD("GD", "Grenada"),
    GL("GL", "Greenland"),
    GP("GP", "Guadeloupe"),
    GT("GT", "Guatemala"),
    HN("HN", "Honduras"),
    HT("HT", "Haiti"),
    JM("JM", "Jamaica"),
    KN("KN", "Saint Kitts and Nevis"),
    LC("LC", "Saint Lucia"),
    MF("MF", "Saint Martin"),
    MQ("MQ", "Martinique"),
    MS("MS", "Montserrat"),
    MX("MX", "Mexico"),
    NI("NI", "Nicaragua"),
    PA("PA", "Panama"),
    PM("PM", "Saint Pierre and Miquelon"),
    PR("PR", "Puerto Rico"),
    SV("SV", "El Salvador"),
    TC("TC", "Turks and Caicos Islands"),
    TT("TT", "Trinidad and Tobago"),
    US("US", "United States"),
    VC("VC", "Saint Vincent and the Grenadines"),
    VG("VG", "British Virgin Islands"),
    VI("VI", "U.S. Virgin Islands"),

    // ========================================
    // SOUTH AMERICA (12 countries)
    // ========================================
    AR("AR", "Argentina"),
    BO("BO", "Bolivia"),
    BR("BR", "Brazil"),
    CL("CL", "Chile"),
    CO("CO", "Colombia"),
    EC("EC", "Ecuador"),
    FK("FK", "Falkland Islands"),
    GF("GF", "French Guiana"),
    GY("GY", "Guyana"),
    PE("PE", "Peru"),
    PY("PY", "Paraguay"),
    SR("SR", "Suriname"),
    UY("UY", "Uruguay"),
    VE("VE", "Venezuela");

    // ========================================
    // REGIONAL GROUPS
    // ========================================
    public companion object {

        public object AsiaPacific {
            public val AF: Country = Country.AF
            public val AS: Country = Country.AS
            public val AU: Country = Country.AU
            public val BD: Country = Country.BD
            public val BH: Country = Country.BH
            public val BN: Country = Country.BN
            public val BT: Country = Country.BT
            public val CC: Country = Country.CC
            public val CK: Country = Country.CK
            public val CN: Country = Country.CN
            public val CX: Country = Country.CX
            public val FJ: Country = Country.FJ
            public val FM: Country = Country.FM
            public val GU: Country = Country.GU
            public val HK: Country = Country.HK
            public val ID: Country = Country.ID
            public val IN: Country = Country.IN
            public val JP: Country = Country.JP
            public val KH: Country = Country.KH
            public val KI: Country = Country.KI
            public val KP: Country = Country.KP
            public val KR: Country = Country.KR
            public val KW: Country = Country.KW
            public val KZ: Country = Country.KZ
            public val LA: Country = Country.LA
            public val LK: Country = Country.LK
            public val MM: Country = Country.MM
            public val MN: Country = Country.MN
            public val MO: Country = Country.MO
            public val MV: Country = Country.MV
            public val MY: Country = Country.MY
            public val NP: Country = Country.NP
            public val NR: Country = Country.NR
            public val NU: Country = Country.NU
            public val NZ: Country = Country.NZ
            public val OM: Country = Country.OM
            public val PF: Country = Country.PF
            public val PG: Country = Country.PG
            public val PH: Country = Country.PH
            public val PK: Country = Country.PK
            public val PN: Country = Country.PN
            public val PW: Country = Country.PW
            public val QA: Country = Country.QA
            public val SA: Country = Country.SA
            public val SB: Country = Country.SB
            public val SG: Country = Country.SG
            public val TH: Country = Country.TH
            public val TJ: Country = Country.TJ
            public val TK: Country = Country.TK
            public val TL: Country = Country.TL
            public val TM: Country = Country.TM
            public val TV: Country = Country.TV
            public val TW: Country = Country.TW
            public val UZ: Country = Country.UZ
            public val VN: Country = Country.VN
            public val VU: Country = Country.VU
            public val WS: Country = Country.WS
        }

        public object Europe {
            public val AD: Country = Country.AD
            public val AL: Country = Country.AL
            public val AM: Country = Country.AM
            public val AT: Country = Country.AT
            public val AX: Country = Country.AX
            public val AZ: Country = Country.AZ
            public val BA: Country = Country.BA
            public val BE: Country = Country.BE
            public val BG: Country = Country.BG
            public val BY: Country = Country.BY
            public val CH: Country = Country.CH
            public val CY: Country = Country.CY
            public val CZ: Country = Country.CZ
            public val DE: Country = Country.DE
            public val DK: Country = Country.DK
            public val EE: Country = Country.EE
            public val ES: Country = Country.ES
            public val FI: Country = Country.FI
            public val FO: Country = Country.FO
            public val FR: Country = Country.FR
            public val GB: Country = Country.GB
            public val GE: Country = Country.GE
            public val GG: Country = Country.GG
            public val GI: Country = Country.GI
            public val GR: Country = Country.GR
            public val HR: Country = Country.HR
            public val HU: Country = Country.HU
            public val IE: Country = Country.IE
            public val IM: Country = Country.IM
            public val IS: Country = Country.IS
            public val IT: Country = Country.IT
            public val JE: Country = Country.JE
            public val LI: Country = Country.LI
            public val LT: Country = Country.LT
            public val LU: Country = Country.LU
            public val LV: Country = Country.LV
            public val MC: Country = Country.MC
            public val MD: Country = Country.MD
            public val ME: Country = Country.ME
            public val MK: Country = Country.MK
            public val MT: Country = Country.MT
            public val NL: Country = Country.NL
            public val NO: Country = Country.NO
            public val PL: Country = Country.PL
            public val PT: Country = Country.PT
            public val RO: Country = Country.RO
            public val RS: Country = Country.RS
            public val RU: Country = Country.RU
            public val SE: Country = Country.SE
            public val SI: Country = Country.SI
            public val SJ: Country = Country.SJ
            public val SK: Country = Country.SK
            public val SM: Country = Country.SM
            public val TR: Country = Country.TR
            public val UA: Country = Country.UA
            public val VA: Country = Country.VA
        }

        public object MiddleEast {
            public val AE: Country = Country.AE
            public val BH: Country = Country.BH
            public val CY: Country = Country.CY
            public val EG: Country = Country.EG
            public val IL: Country = Country.IL
            public val IQ: Country = Country.IQ
            public val IR: Country = Country.IR
            public val JO: Country = Country.JO
            public val KW: Country = Country.KW
            public val LB: Country = Country.LB
            public val OM: Country = Country.OM
            public val PS: Country = Country.PS
            public val SA: Country = Country.SA
            public val SY: Country = Country.SY
            public val YE: Country = Country.YE
        }

        public object Africa {
            public val AO: Country = Country.AO
            public val BF: Country = Country.BF
            public val BI: Country = Country.BI
            public val BJ: Country = Country.BJ
            public val BW: Country = Country.BW
            public val CD: Country = Country.CD
            public val CF: Country = Country.CF
            public val CG: Country = Country.CG
            public val CI: Country = Country.CI
            public val CM: Country = Country.CM
            public val CV: Country = Country.CV
            public val DJ: Country = Country.DJ
            public val DZ: Country = Country.DZ
            public val EH: Country = Country.EH
            public val ER: Country = Country.ER
            public val ET: Country = Country.ET
            public val GA: Country = Country.GA
            public val GH: Country = Country.GH
            public val GM: Country = Country.GM
            public val GN: Country = Country.GN
            public val GQ: Country = Country.GQ
            public val GW: Country = Country.GW
            public val KE: Country = Country.KE
            public val KM: Country = Country.KM
            public val LR: Country = Country.LR
            public val LS: Country = Country.LS
            public val LY: Country = Country.LY
            public val MA: Country = Country.MA
            public val MG: Country = Country.MG
            public val ML: Country = Country.ML
            public val MR: Country = Country.MR
            public val MU: Country = Country.MU
            public val MW: Country = Country.MW
            public val MZ: Country = Country.MZ
            public val NA: Country = Country.NA
            public val NE: Country = Country.NE
            public val NG: Country = Country.NG
            public val RE: Country = Country.RE
            public val RW: Country = Country.RW
            public val SC: Country = Country.SC
            public val SD: Country = Country.SD
            public val SH: Country = Country.SH
            public val SL: Country = Country.SL
            public val SN: Country = Country.SN
            public val SO: Country = Country.SO
            public val SS: Country = Country.SS
            public val ST: Country = Country.ST
            public val SZ: Country = Country.SZ
            public val TD: Country = Country.TD
            public val TG: Country = Country.TG
            public val TN: Country = Country.TN
            public val TZ: Country = Country.TZ
            public val UG: Country = Country.UG
            public val YT: Country = Country.YT
            public val ZA: Country = Country.ZA
            public val ZM: Country = Country.ZM
            public val ZW: Country = Country.ZW
        }

        public object NorthAmerica {
            public val AG: Country = Country.AG
            public val AI: Country = Country.AI
            public val AW: Country = Country.AW
            public val BB: Country = Country.BB
            public val BL: Country = Country.BL
            public val BM: Country = Country.BM
            public val BS: Country = Country.BS
            public val BZ: Country = Country.BZ
            public val CA: Country = Country.CA
            public val CR: Country = Country.CR
            public val CU: Country = Country.CU
            public val DM: Country = Country.DM
            public val DO: Country = Country.DO
            public val GD: Country = Country.GD
            public val GL: Country = Country.GL
            public val GP: Country = Country.GP
            public val GT: Country = Country.GT
            public val HN: Country = Country.HN
            public val HT: Country = Country.HT
            public val JM: Country = Country.JM
            public val KN: Country = Country.KN
            public val LC: Country = Country.LC
            public val MF: Country = Country.MF
            public val MQ: Country = Country.MQ
            public val MS: Country = Country.MS
            public val MX: Country = Country.MX
            public val NI: Country = Country.NI
            public val PA: Country = Country.PA
            public val PM: Country = Country.PM
            public val PR: Country = Country.PR
            public val SV: Country = Country.SV
            public val TC: Country = Country.TC
            public val TT: Country = Country.TT
            public val US: Country = Country.US
            public val VC: Country = Country.VC
            public val VG: Country = Country.VG
            public val VI: Country = Country.VI
        }

        public object SouthAmerica {
            public val AR: Country = Country.AR
            public val BO: Country = Country.BO
            public val BR: Country = Country.BR
            public val CL: Country = Country.CL
            public val CO: Country = Country.CO
            public val EC: Country = Country.EC
            public val FK: Country = Country.FK
            public val GF: Country = Country.GF
            public val GY: Country = Country.GY
            public val PE: Country = Country.PE
            public val PY: Country = Country.PY
            public val SR: Country = Country.SR
            public val UY: Country = Country.UY
            public val VE: Country = Country.VE
        }
    }

    // Helper functions
    public fun fromCode(code: String): Country? = entries.find {
        it.code.equals(code, ignoreCase = true)
    }

    public fun fromName(name: String): Country? = entries.find {
        it.displayName.equals(name, ignoreCase = true)
    }
}
