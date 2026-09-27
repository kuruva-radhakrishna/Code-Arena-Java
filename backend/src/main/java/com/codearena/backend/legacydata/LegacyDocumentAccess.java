package com.codearena.backend.legacydata;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Small read helpers shared by the legacy-document converters: each tries the
 * current field name first, then whatever name/casing the pre-rewrite Node
 * app used for the same data, so a converter body reads as "give me X" rather
 * than repeating the old/new fallback everywhere.
 */
final class LegacyDocumentAccess {

    private LegacyDocumentAccess() {
    }

    static String string(Document doc, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }

    static int intValue(Document doc, int fallback, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value instanceof Number n) {
                return n.intValue();
            }
        }
        return fallback;
    }

    static Long longValue(Document doc, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value instanceof Number n) {
                return n.longValue();
            }
        }
        return null;
    }

    static List<String> stringList(Document doc, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value instanceof List<?> list) {
                List<String> result = new ArrayList<>();
                for (Object item : list) {
                    if (item != null) {
                        result.add(String.valueOf(item));
                    }
                }
                return result;
            }
        }
        return new ArrayList<>();
    }

    static List<Document> docList(Document doc, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value instanceof List<?> list) {
                List<Document> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Document d) {
                        result.add(d);
                    }
                }
                return result;
            }
        }
        return new ArrayList<>();
    }

    /** The old app referenced users/problems/contests by a raw ObjectId; this app stores hex id strings. */
    static String idString(Document doc, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value instanceof ObjectId oid) {
                return oid.toHexString();
            }
            if (value instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }

    static Instant instant(Document doc, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value instanceof Date d) {
                return d.toInstant();
            }
            if (value instanceof Instant i) {
                return i;
            }
        }
        return null;
    }

    static List<Integer> intList(Document doc, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value instanceof List<?> list) {
                List<Integer> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Number n) {
                        result.add(n.intValue());
                    }
                }
                return result;
            }
        }
        return new ArrayList<>();
    }

    static List<Instant> instantList(Document doc, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value instanceof List<?> list) {
                List<Instant> result = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Date d) {
                        result.add(d.toInstant());
                    } else if (item instanceof Instant i) {
                        result.add(i);
                    } else {
                        result.add(null);
                    }
                }
                return result;
            }
        }
        return new ArrayList<>();
    }
}
