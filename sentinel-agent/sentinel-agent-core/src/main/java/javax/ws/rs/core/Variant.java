package javax.ws.rs.core;

import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.ext.RuntimeDelegate;

public class Variant {
   private Locale language;
   private MediaType mediaType;
   private String encoding;

   public Variant(MediaType mediaType, String language, String encoding) {
      if (mediaType == null && language == null && encoding == null) {
         throw new IllegalArgumentException("mediaType, language, encoding all null");
      } else {
         this.encoding = encoding;
         this.language = language == null ? null : new Locale(language);
         this.mediaType = mediaType;
      }
   }

   public Variant(MediaType mediaType, String language, String country, String encoding) {
      if (mediaType == null && language == null && encoding == null) {
         throw new IllegalArgumentException("mediaType, language, encoding all null");
      } else {
         this.encoding = encoding;
         this.language = language == null ? null : new Locale(language, country);
         this.mediaType = mediaType;
      }
   }

   public Variant(MediaType mediaType, String language, String country, String languageVariant, String encoding) {
      if (mediaType == null && language == null && encoding == null) {
         throw new IllegalArgumentException("mediaType, language, encoding all null");
      } else {
         this.encoding = encoding;
         this.language = language == null ? null : new Locale(language, country, languageVariant);
         this.mediaType = mediaType;
      }
   }

   public Variant(MediaType mediaType, Locale language, String encoding) {
      if (mediaType == null && language == null && encoding == null) {
         throw new IllegalArgumentException("mediaType, language, encoding all null");
      } else {
         this.encoding = encoding;
         this.language = language;
         this.mediaType = mediaType;
      }
   }

   public Locale getLanguage() {
      return this.language;
   }

   public String getLanguageString() {
      return this.language == null ? null : this.language.toString();
   }

   public MediaType getMediaType() {
      return this.mediaType;
   }

   public String getEncoding() {
      return this.encoding;
   }

   public static VariantListBuilder mediaTypes(MediaType... mediaTypes) {
      VariantListBuilder b = VariantListBuilder.newInstance();
      b.mediaTypes(mediaTypes);
      return b;
   }

   public static VariantListBuilder languages(Locale... languages) {
      VariantListBuilder b = VariantListBuilder.newInstance();
      b.languages(languages);
      return b;
   }

   public static VariantListBuilder encodings(String... encodings) {
      VariantListBuilder b = VariantListBuilder.newInstance();
      b.encodings(encodings);
      return b;
   }

   public int hashCode() {
      int hash = 7;
      hash = 29 * hash + (this.language != null ? this.language.hashCode() : 0);
      hash = 29 * hash + (this.mediaType != null ? this.mediaType.hashCode() : 0);
      hash = 29 * hash + (this.encoding != null ? this.encoding.hashCode() : 0);
      return hash;
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         Variant other = (Variant)obj;
         if (this.language != other.language && (this.language == null || !this.language.equals(other.language))) {
            return false;
         } else if (this.mediaType == other.mediaType || this.mediaType != null && this.mediaType.equals(other.mediaType)) {
            return this.encoding == other.encoding || this.encoding != null && this.encoding.equals(other.encoding);
         } else {
            return false;
         }
      }
   }

   public String toString() {
      StringWriter w = new StringWriter();
      w.append("Variant[mediaType=");
      w.append(this.mediaType == null ? "null" : this.mediaType.toString());
      w.append(", language=");
      w.append(this.language == null ? "null" : this.language.toString());
      w.append(", encoding=");
      w.append(this.encoding == null ? "null" : this.encoding);
      w.append("]");
      return w.toString();
   }

   public abstract static class VariantListBuilder {
      protected VariantListBuilder() {
      }

      public static VariantListBuilder newInstance() {
         return RuntimeDelegate.getInstance().createVariantListBuilder();
      }

      public abstract List<Variant> build();

      public abstract VariantListBuilder add();

      public abstract VariantListBuilder languages(Locale... var1);

      public abstract VariantListBuilder encodings(String... var1);

      public abstract VariantListBuilder mediaTypes(MediaType... var1);
   }
}
