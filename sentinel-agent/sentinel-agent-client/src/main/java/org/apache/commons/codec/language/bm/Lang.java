/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.codec.language.bm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Language guessing utility.
 * <p>
 * This class encapsulates rules used to guess the possible languages that a word originates from. This is
 * done by reference to a whole series of rules distributed in resource files.
 * <p>
 * Instances of this class are typically managed through the static factory method instance().
 * Unless you are developing your own language guessing rules, you will not need to interact with this class directly.
 * <p>
 * This class is intended to be immutable and thread-safe.
 * <p>
 * <b>Lang resources</b>
 * <p>
 * Language guessing rules are typically loaded from resource files. These are UTF-8 encoded text files.
 * They are systematically named following the pattern:
 * <blockquote>org/apache/commons/codec/language/bm/lang.txt</blockquote>
 * The format of these resources is the following:
 * <ul>
 * <li><b>Rules:</b> whitespace separated strings.
 * There should be 3 columns to each row, and these will be interpreted as:
 * <ol>
 * <li>pattern: a regular expression.</li>
 * <li>languages: a '+'-separated list of languages.</li>
 * <li>acceptOnMatch: 'true' or 'false' indicating if a match rules in or rules out the language.</li>
 * </ol>
 * </li>
 * <li><b>End-of-line comments:</b> Any occurrence of '//' will cause all text following on that line to be
 * discarded as a comment.</li>
 * <li><b>Multi-line comments:</b> Any line starting with '/*' will start multi-line commenting mode.
 * This will skip all content until a line ending in '*' and '/' is found.</li>
 * <li><b>Blank lines:</b> All blank lines will be skipped.</li>
 * </ul>
 * <p>
 * Port of lang.php
 *
 * @since 1.6
 * @version $Id: Lang.java 1542813 2013-11-17 20:52:32Z tn $
 */
public class Lang {
    // Implementation note: This class is divided into two sections. The first part is a static factory interface that
    // exposes the LANGUAGE_RULES_RN resource as a Lang instance. The second part is the Lang instance methods that
    // encapsulate a particular language-guessing rule table and the language guessing itself.
    //
    // It may make sense in the future to expose the private constructor to allow power users to build custom language-
    // guessing rules, perhaps by marking it protected and allowing sub-classing. However, the vast majority of users
    // should be strongly encouraged to use the static factory <code>instance</code> method to get their Lang instances.

    private static final class LangRule {
        private final boolean acceptOnMatch;
        private final Set<String> languages;
        private final Pattern pattern;

        private LangRule(final Pattern pattern, final Set<String> languages, final boolean acceptOnMatch) {
            this.pattern = pattern;
            this.languages = languages;
            this.acceptOnMatch = acceptOnMatch;
        }

        public boolean matches(final String txt) {
            return this.pattern.matcher(txt).find();
        }
    }

    private static final Map<NameType, Lang> Langs = new EnumMap<NameType, Lang>(NameType.class);

    private static final String LANGUAGE_RULES_RN = "org/apache/commons/codec/language/bm/lang.txt";

    static {
        for (final NameType s : NameType.values()) {
            Langs.put(s, loadFromResource(LANGUAGE_RULES_RN, Languages.getInstance(s)));
        }
    }

    /**
     * Gets a Lang instance for one of the supported NameTypes.
     *
     * @param nameType
     *            the NameType to look up
     * @return a Lang encapsulating the language guessing rules for that name type
     */
    public static Lang instance(final NameType nameType) {
        return Langs.get(nameType);
    }

    /**
     * Loads language rules from a resource.
     * <p>
     * In normal use, you will obtain instances of Lang through the {@link #instance(NameType)} method.
     * You will only need to call this yourself if you are developing custom language mapping rules.
     *
     * @param languageRulesResourceName
     *            the fully-qualified resource name to load
     * @param languages
     *            the languages that these rules will support
     * @return a Lang encapsulating the loaded language-guessing rules.
     */
    public static Lang loadFromResource(final String languageRulesResourceName, final Languages languages) {
        final List<LangRule> rules = new ArrayList<LangRule>();
        final InputStream lRulesIS = Lang.class.getClassLoader().getResourceAsStream(languageRulesResourceName);

        if (lRulesIS == null) {
            throw new IllegalStateException("Unable to resolve required resource:" + LANGUAGE_RULES_RN);
        }

        final Scanner scanner = new Scanner(lRulesIS, ResourceConstants.ENCODING);
        try {
            boolean inExtendedComment = false;
            while (scanner.hasNextLine()) {
                final String rawLine = scanner.nextLine();
                String line = rawLine;
                if (inExtendedComment) {
                    // check for closing comment marker, otherwise discard doc comment line
                    if (line.endsWith(ResourceConstants.EXT_CMT_END)) {
                        inExtendedComment = false;
                    }
                } else {
                    if (line.startsWith(ResourceConstants.EXT_CMT_START)) {
                        inExtendedComment = true;
                    } else {
                        // discard comments
                        final int cmtI = line.indexOf(ResourceConstants.CMT);
                        if (cmtI >= 0) {
                            line = line.substring(0, cmtI);
                        }

                        // trim leading-trailing whitespace
                        line = line.trim();

                        if (line.length() == 0) {
                            continue; // empty lines can be safely skipped
                        }

                        // split it up
                        final String[] parts = line.split("\\s+");

                        if (parts.length != 3) {
                            throw new IllegalArgumentException("Malformed line '" + rawLine +
                                    "' in language resource '" + languageRulesResourceName + "'");
                        }

                        final Pattern pattern = Pattern.compile(parts[0]);
                        final String[] langs = parts[1].split("\\+");
                        final boolean accept = parts[2].equals("true");

                        rules.add(new LangRule(pattern, new HashSet<String>(Arrays.asList(langs)), accept));
                    }
                }
            }
        } finally {
            scanner.close();
        }
        return new Lang(rules, languages);
    }

    private final Languages languages;
    private final List<LangRule> rules;

    private Lang(final List<LangRule> rules, final Languages languages) {
        this.rules = Collections.unmodifiableList(rules);
        this.languages = languages;
    }

    /**
     * Guesses the language of a word.
     *
     * @param text
     *            the word
     * @return the language that the word originates from or {@link Languages#ANY} if there was no unique match
     */
    public String guessLanguage(final String text) {
        final Languages.LanguageSet ls = guessLanguages(text);
        return ls.isSingleton() ? ls.getAny() : Languages.ANY;
    }

    /**
     * Guesses the languages of a word.
     *
     * @param input
     *            the word
     * @return a Set of Strings of language names that are potential matches for the input word
     */
    public Languages.LanguageSet guessLanguages(final String input) {
        final String text = input.toLowerCase(Locale.ENGLISH);

        final Set<String> langs = new HashSet<String>(this.languages.getLanguages());
        for (final LangRule rule : this.rules) {
            if (rule.matches(text)) {
                if (rule.acceptOnMatch) {
                    langs.retainAll(rule.languages);
                } else {
                    langs.removeAll(rule.languages);
                }
            }
        }

        final Languages.LanguageSet ls = Languages.LanguageSet.from(langs);
        return ls.equals(Languages.NO_LANGUAGES) ? Languages.ANY_LANGUAGE : ls;
    }
}
