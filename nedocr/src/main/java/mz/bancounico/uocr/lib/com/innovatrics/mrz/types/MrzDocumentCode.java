/**
 * Java parser for the MRZ records, as specified by the ICAO organization.
 * Copyright (C) 2011 Innovatrics s.r.o.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package mz.bancounico.uocr.lib.com.innovatrics.mrz.types;

import mz.bancounico.uocr.lib.com.innovatrics.mrz.MrzParseException;
import mz.bancounico.uocr.lib.com.innovatrics.mrz.MrzRange;

/**
 * Lists all supported MRZ record types (a.k.a. document codes).
 * @author Martin Vysny
 */
public enum MrzDocumentCode {

    /**
     * A passport, P or IP.
     * ... maybe Travel Document that is very similar to the passport.
     */
    Passport,
    /**
     * General I type (besides IP).
     */
    TypeI,
    /**
     * General A type (besides AC).
     */
    TypeA,
    /**
     * Crew member (AC).
     */
    CrewMember,
    /**
     * General type C.
     */
    TypeC, 
    /**
     * Type V (Visa).
     */
    TypeV,
    /**
     *
     */
    Migrant;

    /**
     * @author Zsombor
     * turning to switch statement due to lots of types
     *
     * @param mrz
     * @return
     */
    public static MrzDocumentCode parse(String mrz) {
        final String code = mrz.substring(0, 2);

        // 2-letter checks
        // TODO why?
        return switch (code) {
            case "IV" ->
                    throw new MrzParseException("IV document code is not allowed", mrz, new MrzRange(0, 2, 0), null); // TODO why?
            case "AC" -> CrewMember;
            case "ME" -> Migrant;
            case "TD" -> Migrant; // travel document
            case "IP" -> Passport;
            default ->

                // 1-letter checks
                    switch (code.charAt(0)) {
                        case 'T', 'P' -> Passport;  // T usually Travel Document
                        case 'A' -> TypeA;
                        case 'C' -> TypeC;
                        case 'V' -> TypeV;
                        case 'I' -> TypeI; // identity card or residence permit
                        case 'R' -> Migrant;// swedish '51 Convention Travel Document
                        default ->
                                throw new MrzParseException("Unsupported document code: " + code, mrz, new MrzRange(0, 2, 0), null);
                    };
        };


    }
}
