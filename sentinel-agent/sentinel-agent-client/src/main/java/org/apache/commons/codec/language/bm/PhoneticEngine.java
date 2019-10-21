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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.language.bm.Languages.LanguageSet;
import org.apache.commons.codec.language.bm.Rule.Phoneme;

/**
 * Converts words into potential phonetic representations.
 * <p>
 * This is a two-stage process. Firstly, the word is converted into a phonetic representation that takes
 * into account the likely source language. Next, this phonetic representation is converted into a
 * pan-European 'average' representation, allowing comparison between different versions of essentially
 * the same word from different languages.
 * <p>
 * This class is intentionally immutable and thread-safe.
 * If you wish to alter the settings for a PhoneticEngine, you
 * must make a new one with the updated settings.
 * <p>
 * Ported from phoneticengine.php
 *
 * @since 1.6
 * @version $Id: PhoneticEngine.java 1549875 2013-12-10 15:54:15Z ggregory $
 */
public class PhoneticEngine {

    /**
     * Utility for manipulating a set of phonemes as they are being built up. Not intended for use outside
     * this package, and probably not outside the {@link PhoneticEngine} class.
     *
     * @since 1.6
     */
    static final class PhonemeBuilder {

        /**
         * An empty builder where all phonemes must come from some set of languages. This will contain a single
         * phoneme of zero characters. This can then be appended to. This should be the only way to create a new
         * phoneme from scratch.
         *
         * @param languages the set of languages
         * @return  a new, empty phoneme builder
         */
        public static PhonemeBuilder empty(final Languages.LanguageSet languages) {
            return new PhonemeBuilder(new Rule.Phoneme("", languages));
        }

        private final Set<Rule.Phoneme> phonemes;

        private PhonemeBuilder(final Rule.Phoneme phoneme) {
            this.phonemes = new LinkedHashSet<Rule.Phoneme>();
            this.phonemes.add(phoneme);
        }

        private PhonemeBuilder(final Set<Rule.Phoneme> phonemes) {
            this.phonemes = phonemes;
        }

        /**
         * Creates a new phoneme builder containing all phonemes in this one extended by <code>str</code>.
         *
         * @param str   the characters to append to the phonemes
         */
        public void append(final CharSequence str) {
            for (final Rule.Phoneme ph : this.phonemes) {
                ph.append(str);
            }
        }

        /**
         * Applies the given phoneme expression to all phonemes in this phoneme builder.
         * <p>
         * This will lengthen phonemes that have compatible language sets to the expression, and drop those that are
         * incompatible.
         *
         * @param phonemeExpr   the expression to apply
         * @param maxPhonemes   the maximum number of phonemes to build up
         */
        public void apply(final Rule.PhonemeExpr phonemeExpr, final int maxPhonemes) {
            final Set<Rule.Phoneme> newPhonemes = new LinkedHashSet<Rule.Phoneme>(maxPhonemes);

            EXPR: for (final Rule.Phoneme left : this.phonemes) {
                for (final Rule.Phoneme right : phonemeExpr.getPhonemes()) {
                    final LanguageSet languages = left.getLanguages().restrictTo(right.getLanguages());
                    if (!languages.isEmpty()) {
                        final Rule.Phoneme join = new Phoneme(left, right, languages);
                        if (newPhonemes.size() < maxPhonemes) {
                            newPhonemes.add(join);
                            if (newPhonemes.size() >= maxPhonemes) {
                                break EXPR;
                            }
                        }
                    }
                }
            }

            this.phonemes.clear();
            this.phonemes.addAll(newPhonemes);
        }

        /**
         * Gets underlying phoneme set. Please don't mutate.
         *
         * @return  the phoneme set
         */
        public Set<Rule.Phoneme> getPhonemes() {
            return this.phonemes;
        }

        /**
         * Stringifies the phoneme set. This produces a single string of the strings of each phoneme,
         * joined with a pipe. This is explicitly provided in place of toString as it is a potentially
         * expensive operation, which should be avoided when debugging.
         *
         * @return  the stringified phoneme set
         */
        public String makeString() {
            final StringBuilder sb = new StringBuilder();

            for (final Rule.Phoneme ph : this.phonemes) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(ph.getPhonemeText());
            }

            return sb.toString();
        }
    }

    /**
     * A function closure capturing the application of a list of rules to an input sequence at a particular offset.
     * After invocation, the values <code>i</code> and <code>found</code> are updated. <code>i</code> points to the
     * index of the next char in <code>input</code> that must be processed next (the input up to that index having been
     * processed already), and <code>found</code> indicates if a matching rule was found or not. In the case where a
     * matching rule was found, <code>phonemeBuilder</code> is replaced with a new builder containing the phonemes
     * updated by the matching rule.
     *
     * Although this class is not thread-safe (it has mutable unprotected fields), it is not shared between threads
     * as it is constructed as needed by the calling methods.
     * @since 1.6
     */
    private static final class RulesApplication {
        private final Map<String, List<Rule>> finalRules;
        private final CharSequence input;

        private PhonemeBuilder phonemeBuilder;
        private int i;
        private final int maxPhonemes;
        private boolean found;

        public RulesApplication(final Map<String, List<Rule>> finalRules, final CharSequence input,
                                final PhonemeBuilder phonemeBuilder, final int i, final int maxPhonemes) {
            if (finalRules == null) {
                throw new NullPointerException("The finalRules argument must not be null");
            }
            this.finalRules = finalRules;
            this.phonemeBuilder = phonemeBuilder;
            this.input = input;
            this.i = i;
            this.maxPhonemes = maxPhonemes;
        }

        public int getI() {
            return this.i;
        }

        public PhonemeBuilder getPhonemeBuilder() {
            return this.phonemeBuilder;
        }

        /**
         * Invokes the rules. Loops over the rules list, stopping at the first one that has a matching context
         * and pattern. Then applies this rule to the phoneme builder to produce updated phonemes. If there was no
         * match, <code>i</code> is advanced one and the character is silently dropped from the phonetic spelling.
         *
         * @return <code>this</code>
         */
        public RulesApplication invoke() {
            this.found = false;
            int patternLength = 1;
            final List<Rule> rules = this.finalRules.get(input.subSequence(i, i+patternLength));
            if (rules != null) {
                for (final Rule rule : rules) {
                    final String pattern = rule.getPattern();
                    patternLength = pattern.length();
                    if (rule.patternAndContextMatches(this.input, this.i)) {
                        this.phonemeBuilder.apply(rule.getPhoneme(), maxPhonemes);
                        this.found = true;
                        break;
                    }
                }
            }

            if (!this.found) {
                patternLength = 1;
            }

            this.i += patternLength;
            return this;
        }

        public boolean isFound() {
            return this.found;
        }
    }

    private static final Map<NameType, Set<String>> NAME_PREFIXES = new EnumMap<NameType, Set<String>>(NameType.class);

    static {
        NAME_PREFIXES.put(NameType.ASHKENAZI,
                Collections.unmodifiableSet(
                        new HashSet<String>(Arrays.asList("bar", "ben", "da", "de", "van", "von"))));
        NAME_PREFIXES.put(NameType.SEPHARDIC,
                Collections.unmodifiableSet(
                        new HashSet<String>(Arrays.asList("al", "el", "da", "dal", "de", "del", "dela", "de la",
                                                          "della", "des", "di", "do", "dos", "du", "van", "von"))));
        NAME_PREFIXES.put(NameType.GENERIC,
                Collections.unmodifiableSet(
                        new HashSet<String>(Arrays.asList("da", "dal", "de", "del", "dela", "de la", "della",
                                                          "des", "di", "do", "dos", "du", "van", "von"))));
    }

    /**
     * Joins some strings with an internal separator.
     * @param strings   Strings to join
     * @param sep       String to separate them with
     * @return a single String consisting of each element of <code>strings</code> interleaved by <code>sep</code>
     */
    private static String join(final Iterable<String> strings, final String sep) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<String> si = strings.iterator();
        if (si.hasNext()) {
            sb.append(si.next());
        }
        while (si.hasNext()) {
            sb.append(sep).append(si.next());
        }

        return sb.toString();
    }

    private static final int DEFAULT_MAX_PHONEMES = 20;

    private final Lang lang;

    private final NameType nameType;

    private final RuleType ruleType;

    private final boolean concat;

    private final int maxPhonemes;

    /**
     * Generates a new, fully-configured phonetic engine.
     *
     * @param nameType
     *            the type of names it will use
     * @param ruleType
     *            the type of rules it will apply
     * @param concat
     *            if it will concatenate multiple encodings
     */
    public PhoneticEngine(final NameType nameType, final RuleType ruleType, final boolean concat) {
        this(nameType, ruleType, concat, DEFAULT_MAX_PHONEMES);
    }

    /**
     * Generates a new, fully-configured phonetic engine.
     *
     * @param nameType
     *            the type of names it will use
     * @param ruleType
     *            the type of rules it will apply
     * @param concat
     *            if it will concatenate multiple encodings
     * @param maxPhonemes
     *            the maximum number of phonemes that will be handled
     * @since 1.7
     */
    public PhoneticEngine(final NameType nameType, final RuleType ruleType, final boolean concat,
                          final int maxPhonemes) {
        if (ruleType == RuleType.RULES) {
            throw new IllegalArgumentException("ruleType must not be " + RuleType.RULES);
        }
        this.nameType = nameType;
        this.ruleType = ruleType;
        this.concat = concat;
        this.lang = Lang.instance(nameType);
        this.maxPhonemes = maxPhonemes;
    }

    /**
     * Applies the final rules to convert from a language-specific phonetic representation to a
     * language-independent representation.
     *
     * @param phonemeBuilder the current phonemes
     * @param finalRules the final rules to apply
     * @return the resulting phonemes
     */
    private PhonemeBuilder applyFinalRules(final PhonemeBuilder phonemeBuilder,
                                           final Map<String, List<Rule>> finalRules) {
        if (finalRules == null) {
            throw new NullPointerException("finalRules can not be null");
        }
        if (finalRules.isEmpty()) {
            return phonemeBuilder;
        }

        final Set<Rule.Phoneme> phonemes = new TreeSet<Rule.Phoneme>(Rule.Phoneme.COMPARATOR);

        for (final Rule.Phoneme phoneme : phonemeBuilder.getPhonemes()) {
            PhonemeBuilder subBuilder = PhonemeBuilder.empty(phoneme.getLanguages());
            final String phonemeText = phoneme.getPhonemeText().toString();

            for (int i = 0; i < phonemeText.length();) {
                final RulesApplication rulesApplication =
                        new RulesApplication(finalRules, phonemeText, subBuilder, i, maxPhonemes).invoke();
                final boolean found = rulesApplication.isFound();
                subBuilder = rulesApplication.getPhonemeBuilder();

                if (!found) {
                    // not found, appending as-is
                    subBuilder.append(phonemeText.subSequence(i, i + 1));
                }

                i = rulesApplication.getI();
            }

            phonemes.addAll(subBuilder.getPhonemes());
        }

        return new PhonemeBuilder(phonemes);
    }

    /**
     * Encodes a string to its phonetic representation.
     *
     * @param input
     *            the String to encode
     * @return the encoding of the input
     */
    public String encode(final String input) {
        final Languages.LanguageSet languageSet = this.lang.guessLanguages(input);
        return encode(input, languageSet);
    }

    /**
     * Encodes an input string into an output phonetic representation, given a set of possible origin languages.
     *
     * @param input
     *            String to phoneticise; a String with dashes or spaces separating each word
     * @param languageSet
     * @return a phonetic representation of the input; a String containing '-'-separated phonetic representations
     *   of the input
     */
    public String encode(String input, final Languages.LanguageSet languageSet) {
        final Map<String, List<Rule>> rules = Rule.getInstanceMap(this.nameType, RuleType.RULES, languageSet);
        // rules common across many (all) languages
        final Map<String, List<Rule>> finalRules1 = Rule.getInstanceMap(this.nameType, this.ruleType, "common");
        // rules that apply to a specific language that may be ambiguous or wrong if applied to other languages
        final Map<String, List<Rule>> finalRules2 = Rule.getInstanceMap(this.nameType, this.ruleType, languageSet);

        // tidy the input
        // lower case is a locale-dependent operation
        input = input.toLowerCase(Locale.ENGLISH).replace('-', ' ').trim();

        if (this.nameType == NameType.GENERIC) {
            if (input.length() >= 2 && input.substring(0, 2).equals("d'")) { // check for d'
                final String remainder = input.substring(2);
                final String combined = "d" + remainder;
                return "(" + encode(remainder) + ")-(" + encode(combined) + ")";
            }
            for (final String l : NAME_PREFIXES.get(this.nameType)) {
                // handle generic prefixes
                if (input.startsWith(l + " ")) {
                    // check for any prefix in the words list
                    final String remainder = input.substring(l.length() + 1); // input without the prefix
                    final String combined = l + remainder; // input with prefix without space
                    return "(" + encode(remainder) + ")-(" + encode(combined) + ")";
                }
            }
        }

        final List<String> words = Arrays.asList(input.split("\\s+"));
        final List<String> words2 = new ArrayList<String>();

        // special-case handling of word prefixes based upon the name type
        switch (this.nameType) {
        case SEPHARDIC:
            for (final String aWord : words) {
                final String[] parts = aWord.split("'");
                final String lastPart = parts[parts.length - 1];
                words2.add(lastPart);
            }
            words2.removeAll(NAME_PREFIXES.get(this.nameType));
            break;
        case ASHKENAZI:
            words2.addAll(words);
            words2.removeAll(NAME_PREFIXES.get(this.nameType));
            break;
        case GENERIC:
            words2.addAll(words);
            break;
        default:
            throw new IllegalStateException("Unreachable case: " + this.nameType);
        }

        if (this.concat) {
            // concat mode enabled
            input = join(words2, " ");
        } else if (words2.size() == 1) {
            // not a multi-word name
            input = words.iterator().next();
        } else {
            // encode each word in a multi-word name separately (normally used for approx matches)
            final StringBuilder result = new StringBuilder();
            for (final String word : words2) {
                result.append("-").append(encode(word));
            }
            // return the result without the leading "-"
            return result.substring(1);
        }

        PhonemeBuilder phonemeBuilder = PhonemeBuilder.empty(languageSet);

        // loop over each char in the input - we will handle the increment manually
        for (int i = 0; i < input.length();) {
            final RulesApplication rulesApplication =
                    new RulesApplication(rules, input, phonemeBuilder, i, maxPhonemes).invoke();
            i = rulesApplication.getI();
            phonemeBuilder = rulesApplication.getPhonemeBuilder();
        }

        // Apply the general rules
        phonemeBuilder = applyFinalRules(phonemeBuilder, finalRules1);
        // Apply the language-specific rules
        phonemeBuilder = applyFinalRules(phonemeBuilder, finalRules2);

        return phonemeBuilder.makeString();
    }

    /**
     * Gets the Lang language guessing rules being used.
     *
     * @return the Lang in use
     */
    public Lang getLang() {
        return this.lang;
    }

    /**
     * Gets the NameType being used.
     *
     * @return the NameType in use
     */
    public NameType getNameType() {
        return this.nameType;
    }

    /**
     * Gets the RuleType being used.
     *
     * @return the RuleType in use
     */
    public RuleType getRuleType() {
        return this.ruleType;
    }

    /**
     * Gets if multiple phonetic encodings are concatenated or if just the first one is kept.
     *
     * @return true if multiple phonetic encodings are returned, false if just the first is
     */
    public boolean isConcat() {
        return this.concat;
    }

    /**
     * Gets the maximum number of phonemes the engine will calculate for a given input.
     *
     * @return the maximum number of phonemes
     * @since 1.7
     */
    public int getMaxPhonemes() {
        return this.maxPhonemes;
    }
}
