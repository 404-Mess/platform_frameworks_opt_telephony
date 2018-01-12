/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony.uicc.euicc.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.Parcel;
import android.service.carrier.CarrierIdentifier;
import android.service.euicc.EuiccProfileInfo;
import android.telephony.euicc.EuiccRat;

import org.junit.Test;

public class EuiccRatTest {
    @Test
    public void testFindIndex() {
        CarrierIdentifier opA = new CarrierIdentifier(new byte[] {0x21, 0x63, 0x54}, null, "4");
        CarrierIdentifier opB = new CarrierIdentifier(new byte[] {0x21, 0x69, 0x54}, "4", null);
        EuiccRat rat =
                new EuiccRat.Builder(4)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                // Matches none
                                new CarrierIdentifier[] {},
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE,
                                new CarrierIdentifier[] {
                                        // Matches none
                                        new CarrierIdentifier(
                                                new byte[] {0x21, (byte) 0xF3, 0x54},
                                                "4",
                                                null),
                                        // Matches opA
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        // Matches none
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "5"),
                                        // Matches opA and opB
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, null)
                                },
                                0)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        // Matches none
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x69, 0x54}, "5", null),
                                        // Matches opB
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, "4", null),
                                        // Matches opA
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .build();

        assertEquals(1, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE, opA));
        assertEquals(3, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE, opB));
        assertEquals(2, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE, opA));
        assertEquals(2, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE, opB));
        assertTrue(rat.hasPolicyRuleFlag(1, EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED));
        assertFalse(rat.hasPolicyRuleFlag(2, EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED));
        assertTrue(rat.hasPolicyRuleFlag(3, EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED));
    }

    @Test
    public void testFindIndex_AllowAllWithUserConsent() {
        CarrierIdentifier opA = new CarrierIdentifier(new byte[] {0x21, 0x63, 0x54}, null, "4");
        CarrierIdentifier opB = new CarrierIdentifier(
                new byte[] {0x78, (byte) 0xF4, 0x25}, "4", null);
        EuiccRat rat =
                new EuiccRat.Builder(1)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        // Matches none
                                        new CarrierIdentifier(
                                                new byte[] {(byte) 0xEE, (byte) 0xEE, (byte) 0xEE},
                                                null,
                                                null)
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .build();
        assertEquals(0, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE, opA));
        assertEquals(0, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE, opB));
        assertEquals(0, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE, opA));
        assertEquals(0, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE, opB));
    }

    @Test
    public void testFindIndex_DisallowAll() {
        CarrierIdentifier opA = new CarrierIdentifier(new byte[] {0x21, 0x63, 0x54}, null, "4");
        CarrierIdentifier opB = new CarrierIdentifier(
                new byte[] {0x78, (byte) 0xF4, 0x25}, "4", null);
        EuiccRat rat = new EuiccRat.Builder(1).add(0, new CarrierIdentifier[] {}, 0).build();
        assertEquals(-1, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE, opA));
        assertEquals(-1, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE, opB));
        assertEquals(-1, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE, opA));
        assertEquals(-1, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE, opB));
    }

    @Test
    public void testFindIndex_DisallowAllWithEmptyRules() {
        CarrierIdentifier opA = new CarrierIdentifier(new byte[] {0x21, 0x63, 0x54}, null, "4");
        CarrierIdentifier opB = new CarrierIdentifier(new byte[] {0x78, 0x34, 0x25}, "4", null);
        EuiccRat rat = new EuiccRat.Builder(0).build();
        assertEquals(-1, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE, opA));
        assertEquals(-1, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE, opB));
        assertEquals(-1, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE, opA));
        assertEquals(-1, rat.findIndex(EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE, opB));
    }

    @Test(expected = IllegalStateException.class)
    public void testBuild_NotEnoughRules() {
        new EuiccRat.Builder(1).build();
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testBuild_TooManyRules() {
        new EuiccRat.Builder(0).add(0, new CarrierIdentifier[] {}, 0).build();
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testHasPolicyRuleFlag_OutOfBounds() {
        EuiccRat rat = new EuiccRat.Builder(1).add(0, new CarrierIdentifier[] {}, 0).build();
        rat.hasPolicyRuleFlag(1, EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED);
    }

    @Test
    public void testMatch() {
        assertTrue(EuiccRat.match("12", "12"));
        assertTrue(EuiccRat.match("1E", "12"));
        assertTrue(EuiccRat.match("12E", "12"));
        assertTrue(EuiccRat.match("EEE", "12"));
        assertTrue(EuiccRat.match("120", "120"));
        assertTrue(EuiccRat.match("12E", "120"));
        assertTrue(EuiccRat.match("EEE", "120"));

        assertFalse(EuiccRat.match("13", "12"));
        assertFalse(EuiccRat.match("2E", "12"));
        assertFalse(EuiccRat.match("123", "120"));
        assertFalse(EuiccRat.match("1E", "120"));
        assertFalse(EuiccRat.match("EE", "120"));
    }

    @Test
    public void testWriteToParcel() {
        EuiccRat rat =
                new EuiccRat.Builder(4)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {},
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, (byte) 0xF3, 0x54},
                                                "4",
                                                null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "5"),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, null)
                                },
                                0)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x69, 0x54}, "5", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, "4", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .build();

        Parcel parcel = Parcel.obtain();
        assertTrue(parcel != null);
        rat.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        EuiccRat fromParcel = EuiccRat.CREATOR.createFromParcel(parcel);

        assertEquals(rat, fromParcel);

        // Empty rules.
        rat = new EuiccRat.Builder(0).build();
        parcel = Parcel.obtain();
        rat.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        fromParcel = EuiccRat.CREATOR.createFromParcel(parcel);

        assertEquals(rat, fromParcel);

        // Null carrier identifier.
        rat =
                new EuiccRat.Builder(1)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                null,
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .build();
        parcel = Parcel.obtain();
        rat.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        fromParcel = EuiccRat.CREATOR.createFromParcel(parcel);

        assertEquals(rat, fromParcel);
    }

    @Test
    public void testEquals() {
        EuiccRat rat =
                new EuiccRat.Builder(4)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {},
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, (byte) 0xF3, 0x54},
                                                "4",
                                                null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "5"),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, null)
                                },
                                0)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x69, 0x54}, "5", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, "4", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .build();

        // Same object.
        EuiccRat that = rat;
        assertTrue(rat.equals(that));

        // Same values with rat.
        that =
                new EuiccRat.Builder(4)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {},
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, (byte) 0xF3, 0x54},
                                                "4",
                                                null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "5"),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, null)
                                },
                                0)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x69, 0x54}, "5", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, "4", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .build();
        assertTrue(rat.equals(that));

        // Null object.
        that = null;
        assertFalse(rat.equals(that));

        that =
                new EuiccRat.Builder(3)
                        // One less RAT.
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {},
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, (byte) 0xF3, 0x54},
                                                "4",
                                                null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "5"),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, null)
                                },
                                0)
                        .build();
        assertFalse(rat.equals(that));

        that =
                new EuiccRat.Builder(4)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {},
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE,
                                new CarrierIdentifier[] {
                                        // Only one item
                                        new CarrierIdentifier(
                                                new byte[] {0x21, (byte) 0xF3, 0x54},
                                                "4",
                                                null)
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "5"),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, null)
                                },
                                0)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x69, 0x54}, "5", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, "4", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .build();
        assertFalse(rat.equals(that));

        that =
                new EuiccRat.Builder(4)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {},
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE,
                                new CarrierIdentifier[] {
                                        // Different value from rat
                                        new CarrierIdentifier(
                                                new byte[] {0x22, (byte) 0xF3, 0x54},
                                                "4",
                                                null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "5"),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, null)
                                },
                                0)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x69, 0x54}, "5", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, "4", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .build();
        assertFalse(rat.equals(that));

        that =
                new EuiccRat.Builder(4)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                // Null here.
                                null,
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, (byte) 0xF3, 0x54},
                                                "4",
                                                null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "5"),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, null)
                                },
                                0)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x69, 0x54}, "5", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, "4", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .build();
        assertFalse(rat.equals(that));

        that =
                new EuiccRat.Builder(4)
                        .add(
                                // Different policy rules
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE,
                                new CarrierIdentifier[] {},
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, (byte) 0xF3, 0x54},
                                                "4",
                                                null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x63, 0x54}, null, "5"),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, null)
                                },
                                0)
                        .add(
                                EuiccProfileInfo.POLICY_RULE_DO_NOT_DELETE
                                        | EuiccProfileInfo.POLICY_RULE_DO_NOT_DISABLE,
                                new CarrierIdentifier[] {
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x69, 0x54}, "5", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, "4", null),
                                        new CarrierIdentifier(
                                                new byte[] {0x21, 0x6E, 0x54}, null, "4")
                                },
                                EuiccRat.POLICY_RULE_FLAG_CONSENT_REQUIRED)
                        .build();
        assertFalse(rat.equals(that));
    }
}
