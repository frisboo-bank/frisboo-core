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

public enum class Countries(
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
    VE("VE", "Venezuela"),
    ;

    // ========================================
    // REGIONAL GROUPS
    // ========================================
    public companion object {
        public object AsiaPacific {
            public val AF: Countries = Countries.AF
            public val AS: Countries = Countries.AS
            public val AU: Countries = Countries.AU
            public val BD: Countries = Countries.BD
            public val BH: Countries = Countries.BH
            public val BN: Countries = Countries.BN
            public val BT: Countries = Countries.BT
            public val CC: Countries = Countries.CC
            public val CK: Countries = Countries.CK
            public val CN: Countries = Countries.CN
            public val CX: Countries = Countries.CX
            public val FJ: Countries = Countries.FJ
            public val FM: Countries = Countries.FM
            public val GU: Countries = Countries.GU
            public val HK: Countries = Countries.HK
            public val ID: Countries = Countries.ID
            public val IN: Countries = Countries.IN
            public val JP: Countries = Countries.JP
            public val KH: Countries = Countries.KH
            public val KI: Countries = Countries.KI
            public val KP: Countries = Countries.KP
            public val KR: Countries = Countries.KR
            public val KW: Countries = Countries.KW
            public val KZ: Countries = Countries.KZ
            public val LA: Countries = Countries.LA
            public val LK: Countries = Countries.LK
            public val MM: Countries = Countries.MM
            public val MN: Countries = Countries.MN
            public val MO: Countries = Countries.MO
            public val MV: Countries = Countries.MV
            public val MY: Countries = Countries.MY
            public val NP: Countries = Countries.NP
            public val NR: Countries = Countries.NR
            public val NU: Countries = Countries.NU
            public val NZ: Countries = Countries.NZ
            public val OM: Countries = Countries.OM
            public val PF: Countries = Countries.PF
            public val PG: Countries = Countries.PG
            public val PH: Countries = Countries.PH
            public val PK: Countries = Countries.PK
            public val PN: Countries = Countries.PN
            public val PW: Countries = Countries.PW
            public val QA: Countries = Countries.QA
            public val SA: Countries = Countries.SA
            public val SB: Countries = Countries.SB
            public val SG: Countries = Countries.SG
            public val TH: Countries = Countries.TH
            public val TJ: Countries = Countries.TJ
            public val TK: Countries = Countries.TK
            public val TL: Countries = Countries.TL
            public val TM: Countries = Countries.TM
            public val TV: Countries = Countries.TV
            public val TW: Countries = Countries.TW
            public val UZ: Countries = Countries.UZ
            public val VN: Countries = Countries.VN
            public val VU: Countries = Countries.VU
            public val WS: Countries = Countries.WS
        }

        public object Europe {
            public val AD: Countries = Countries.AD
            public val AL: Countries = Countries.AL
            public val AM: Countries = Countries.AM
            public val AT: Countries = Countries.AT
            public val AX: Countries = Countries.AX
            public val AZ: Countries = Countries.AZ
            public val BA: Countries = Countries.BA
            public val BE: Countries = Countries.BE
            public val BG: Countries = Countries.BG
            public val BY: Countries = Countries.BY
            public val CH: Countries = Countries.CH
            public val CY: Countries = Countries.CY
            public val CZ: Countries = Countries.CZ
            public val DE: Countries = Countries.DE
            public val DK: Countries = Countries.DK
            public val EE: Countries = Countries.EE
            public val ES: Countries = Countries.ES
            public val FI: Countries = Countries.FI
            public val FO: Countries = Countries.FO
            public val FR: Countries = Countries.FR
            public val GB: Countries = Countries.GB
            public val GE: Countries = Countries.GE
            public val GG: Countries = Countries.GG
            public val GI: Countries = Countries.GI
            public val GR: Countries = Countries.GR
            public val HR: Countries = Countries.HR
            public val HU: Countries = Countries.HU
            public val IE: Countries = Countries.IE
            public val IM: Countries = Countries.IM
            public val IS: Countries = Countries.IS
            public val IT: Countries = Countries.IT
            public val JE: Countries = Countries.JE
            public val LI: Countries = Countries.LI
            public val LT: Countries = Countries.LT
            public val LU: Countries = Countries.LU
            public val LV: Countries = Countries.LV
            public val MC: Countries = Countries.MC
            public val MD: Countries = Countries.MD
            public val ME: Countries = Countries.ME
            public val MK: Countries = Countries.MK
            public val MT: Countries = Countries.MT
            public val NL: Countries = Countries.NL
            public val NO: Countries = Countries.NO
            public val PL: Countries = Countries.PL
            public val PT: Countries = Countries.PT
            public val RO: Countries = Countries.RO
            public val RS: Countries = Countries.RS
            public val RU: Countries = Countries.RU
            public val SE: Countries = Countries.SE
            public val SI: Countries = Countries.SI
            public val SJ: Countries = Countries.SJ
            public val SK: Countries = Countries.SK
            public val SM: Countries = Countries.SM
            public val TR: Countries = Countries.TR
            public val UA: Countries = Countries.UA
            public val VA: Countries = Countries.VA
        }

        public object MiddleEast {
            public val AE: Countries = Countries.AE
            public val BH: Countries = Countries.BH
            public val CY: Countries = Countries.CY
            public val EG: Countries = Countries.EG
            public val IL: Countries = Countries.IL
            public val IQ: Countries = Countries.IQ
            public val IR: Countries = Countries.IR
            public val JO: Countries = Countries.JO
            public val KW: Countries = Countries.KW
            public val LB: Countries = Countries.LB
            public val OM: Countries = Countries.OM
            public val PS: Countries = Countries.PS
            public val SA: Countries = Countries.SA
            public val SY: Countries = Countries.SY
            public val YE: Countries = Countries.YE
        }

        public object Africa {
            public val AO: Countries = Countries.AO
            public val BF: Countries = Countries.BF
            public val BI: Countries = Countries.BI
            public val BJ: Countries = Countries.BJ
            public val BW: Countries = Countries.BW
            public val CD: Countries = Countries.CD
            public val CF: Countries = Countries.CF
            public val CG: Countries = Countries.CG
            public val CI: Countries = Countries.CI
            public val CM: Countries = Countries.CM
            public val CV: Countries = Countries.CV
            public val DJ: Countries = Countries.DJ
            public val DZ: Countries = Countries.DZ
            public val EH: Countries = Countries.EH
            public val ER: Countries = Countries.ER
            public val ET: Countries = Countries.ET
            public val GA: Countries = Countries.GA
            public val GH: Countries = Countries.GH
            public val GM: Countries = Countries.GM
            public val GN: Countries = Countries.GN
            public val GQ: Countries = Countries.GQ
            public val GW: Countries = Countries.GW
            public val KE: Countries = Countries.KE
            public val KM: Countries = Countries.KM
            public val LR: Countries = Countries.LR
            public val LS: Countries = Countries.LS
            public val LY: Countries = Countries.LY
            public val MA: Countries = Countries.MA
            public val MG: Countries = Countries.MG
            public val ML: Countries = Countries.ML
            public val MR: Countries = Countries.MR
            public val MU: Countries = Countries.MU
            public val MW: Countries = Countries.MW
            public val MZ: Countries = Countries.MZ
            public val NA: Countries = Countries.NA
            public val NE: Countries = Countries.NE
            public val NG: Countries = Countries.NG
            public val RE: Countries = Countries.RE
            public val RW: Countries = Countries.RW
            public val SC: Countries = Countries.SC
            public val SD: Countries = Countries.SD
            public val SH: Countries = Countries.SH
            public val SL: Countries = Countries.SL
            public val SN: Countries = Countries.SN
            public val SO: Countries = Countries.SO
            public val SS: Countries = Countries.SS
            public val ST: Countries = Countries.ST
            public val SZ: Countries = Countries.SZ
            public val TD: Countries = Countries.TD
            public val TG: Countries = Countries.TG
            public val TN: Countries = Countries.TN
            public val TZ: Countries = Countries.TZ
            public val UG: Countries = Countries.UG
            public val YT: Countries = Countries.YT
            public val ZA: Countries = Countries.ZA
            public val ZM: Countries = Countries.ZM
            public val ZW: Countries = Countries.ZW
        }

        public object NorthAmerica {
            public val AG: Countries = Countries.AG
            public val AI: Countries = Countries.AI
            public val AW: Countries = Countries.AW
            public val BB: Countries = Countries.BB
            public val BL: Countries = Countries.BL
            public val BM: Countries = Countries.BM
            public val BS: Countries = Countries.BS
            public val BZ: Countries = Countries.BZ
            public val CA: Countries = Countries.CA
            public val CR: Countries = Countries.CR
            public val CU: Countries = Countries.CU
            public val DM: Countries = Countries.DM
            public val DO: Countries = Countries.DO
            public val GD: Countries = Countries.GD
            public val GL: Countries = Countries.GL
            public val GP: Countries = Countries.GP
            public val GT: Countries = Countries.GT
            public val HN: Countries = Countries.HN
            public val HT: Countries = Countries.HT
            public val JM: Countries = Countries.JM
            public val KN: Countries = Countries.KN
            public val LC: Countries = Countries.LC
            public val MF: Countries = Countries.MF
            public val MQ: Countries = Countries.MQ
            public val MS: Countries = Countries.MS
            public val MX: Countries = Countries.MX
            public val NI: Countries = Countries.NI
            public val PA: Countries = Countries.PA
            public val PM: Countries = Countries.PM
            public val PR: Countries = Countries.PR
            public val SV: Countries = Countries.SV
            public val TC: Countries = Countries.TC
            public val TT: Countries = Countries.TT
            public val US: Countries = Countries.US
            public val VC: Countries = Countries.VC
            public val VG: Countries = Countries.VG
            public val VI: Countries = Countries.VI
        }

        public object SouthAmerica {
            public val AR: Countries = Countries.AR
            public val BO: Countries = Countries.BO
            public val BR: Countries = Countries.BR
            public val CL: Countries = Countries.CL
            public val CO: Countries = Countries.CO
            public val EC: Countries = Countries.EC
            public val FK: Countries = Countries.FK
            public val GF: Countries = Countries.GF
            public val GY: Countries = Countries.GY
            public val PE: Countries = Countries.PE
            public val PY: Countries = Countries.PY
            public val SR: Countries = Countries.SR
            public val UY: Countries = Countries.UY
            public val VE: Countries = Countries.VE
        }
    }

    // Helper functions
    public fun fromCode(code: String): Countries? =
        entries.find {
            it.code.equals(code, ignoreCase = true)
        }

    public fun fromName(name: String): Countries? =
        entries.find {
            it.displayName.equals(name, ignoreCase = true)
        }
}
