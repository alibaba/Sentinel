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

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

/**
 * Encodes strings into their Beider-Morse phonetic encoding.
 * <p>
 * Beider-Morse phonetic encodings are optimised for family names. However, they may be useful for a wide range
 * of words.
 * <p>
 * This encoder is intentionally mutable to allow dynamic configuration through bean properties. As such, it
 * is mutable, and may not be thread-safe. If you require a guaranteed thread-safe encoding then use
 * {@link PhoneticEngine} directly.
 * <p>
 * <b>Encoding overview</b>
 * <p>
 * Beider-Morse phonetic encodings is a multi-step process. Firstly, a table of rules is consulted to guess what
 * language the word comes from. For example, if it ends in "<code>ault</code>" then it infers that the word is French.
 * Next, the word is translated into a phonetic representation using a language-specific phonetics table. Some
 * runs of letters can be pronounced in multiple ways, and a single run of letters may be potentially broken up
 * into phonemes at different places, so this stage results in a set of possible language-specific phonetic
 * representations. Lastly, this language-specific phonetic representation is processed by a table of rules that
 * re-writes it phonetically taking into account systematic pronunciation differences between languages, to move
 * it towards a pan-indo-european phonetic representation. Again, sometimes there are multiple ways this could be
 * done and sometimes things that can be pronounced in several ways in the source language have only one way to
 * represent them in this average phonetic language, so the result is again a set of phonetic spellings.
 * <p>
 * Some names are treated as having multiple parts. This can be due to two things. Firstly, they may be hyphenated.
 * In this case, each individual hyphenated word is encoded, and then these are combined end-to-end for the final
 * encoding. Secondly, some names have standard prefixes, for example, "<code>Mac/Mc</code>" in Scottish (English)
 * names. As sometimes it is ambiguous whether the prefix is intended or is an accident of the spelling, the word
 * is encoded once with the prefix and once without it. The resulting encoding contains one and then the other result.
 * <p>
 * <b>Encoding format</b>
 * <p>
 * Individual phonetic spellings of an input word are represented in upper- and lower-case roman characters. Where
 * there are multiple possible phonetic representations, these are joined with a pipe (<code>|</code>) character.
 * If multiple hyphenated words where found, or if the word may contain a name prefix, each encoded word is placed
 * in elipses and these blocks are then joined with hyphens. For example, "<code>d'ortley</code>" has a possible
 * prefix. The form without prefix encodes to "<code>ortlaj|ortlej</code>", while the form with prefix encodes to
 * "<code>dortlaj|dortlej</code>". Thus, the full, combined encoding is "{@code (ortlaj|ortlej)-(dortlaj|dortlej)}".
 * <p>
 * The encoded forms are often quite a bit longer than the input strings. This is because a single input may have many
 * potential phonetic interpretations. For example, "<code>Renault</code>" encodes to
 * "<code>rYnDlt|rYnalt|rYnult|rinDlt|rinalt|rinult</code>". The <code>APPROX</code> rules will tend to produce larger
 * encodings as they consider a wider range of possible, approximate phonetic interpretations of the original word.
 * Down-stream applications may wish to further process the encoding for indexing or lookup purposes, for example, by
 * splitting on pipe (<code>|</code>) and indexing under each of these alternatives.
 *
 * @since 1.6
 * @version $Id: BeiderMorseEncoder.java 1429868 2013-01-07 16:08:05Z ggregory $
 */
public class BeiderMorseEncoder implements StringEncoder {
    // Implementation note: This class is a spring-friendly facade to PhoneticEngine. It allows read/write configuration
    // of an immutable PhoneticEngine instance that will be delegated to for the actual encoding.

    // a cached object
    private PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);

    @Override
    public Object encode(final Object source) throws EncoderException {
        if (!(source instanceof String)) {
            throw new EncoderException("BeiderMorseEncoder encode parameter is not of type String");
        }
        return encode((String) source);
    }

    @Override
    public String encode(final String source) throws EncoderException {
        if (source == null) {
            return null;
        }
        return this.engine.encode(source);
    }

    /**
     * Gets the name type currently in operation.
     *
     * @return the NameType currently being used
     */
    public NameType getNameType() {
        return this.engine.getNameType();
    }

    /**
     * Gets the rule type currently in operation.
     *
     * @return the RuleType currently being used
     */
    public RuleType getRuleType() {
        return this.engine.getRuleType();
    }

    /**
     * Discovers if multiple possible encodings are concatenated.
     *
     * @return true if multiple encodings are concatenated, false if just the first one is returned
     */
    public boolean isConcat() {
        return this.engine.isConcat();
    }

    /**
     * Sets how multiple possible phonetic encodings are combined.
     *
     * @param concat
     *            true if multiple encodings are to be combined with a '|', false if just the first one is
     *            to be considered
     */
    public void setConcat(final boolean concat) {
        this.engine = new PhoneticEngine(this.engine.getNameType(),
                                         this.engine.getRuleType(),
                                         concat,
                                         this.engine.getMaxPhonemes());
    }

    /**
     * Sets the type of name. Use {@link NameType#GENERIC} unless you specifically want phonetic encodings
     * optimized for Ashkenazi or Sephardic Jewish family names.
     *
     * @param nameType
     *            the NameType in use
     */
    public void setNameType(final NameType nameType) {
        this.engine = new PhoneticEngine(nameType,
                                         this.engine.getRuleType(),
                                         this.engine.isConcat(),
                                         this.engine.getMaxPhonemes());
    }

    /**
     * Sets the rule type to apply. This will widen or narrow the range of phonetic encodings considered.
     *
     * @param ruleType
     *            {@link RuleType#APPROX} or {@link RuleType#EXACT} for approximate or exact phonetic matches
     */
    public void setRuleType(final RuleType ruleType) {
        this.engine = new PhoneticEngine(this.engine.getNameType(),
                                         ruleType,
                                         this.engine.isConcat(),
                                         this.engine.getMaxPhonemes());
    }

    /**
     * Sets the number of maximum of phonemes that shall be considered by the engine.
     *
     * @param maxPhonemes
     *            the maximum number of phonemes returned by the engine
     * @since 1.7
     */
    public void setMaxPhonemes(final int maxPhonemes) {
        this.engine = new PhoneticEngine(this.engine.getNameType(),
                                         this.engine.getRuleType(),
                                         this.engine.isConcat(),
                                         maxPhonemes);
    }

}
