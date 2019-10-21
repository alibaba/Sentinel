package com.taobao.csp.ahas.module.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class StringUtil {
   public static final String EMPTY = "";

   public static String toJSONString(Collection<String> collection) {
      if (collection != null && collection.size() >= 1) {
         StringBuilder jsonBuilder = new StringBuilder();
         jsonBuilder.append("[");
         List<String> itemList = Collections.list(Collections.enumeration(collection));

         for(int index = 0; index < itemList.size(); ++index) {
            jsonBuilder.append("\"").append((String)itemList.get(index)).append("\"");
            if (index != itemList.size() - 1) {
               jsonBuilder.append(",");
            }
         }

         jsonBuilder.append("]");
         return jsonBuilder.toString();
      } else {
         return "[]";
      }
   }

   public static boolean equalsIgnoreCase(CharSequence str1, CharSequence str2) {
      if (str1 != null && str2 != null) {
         if (str1 == str2) {
            return true;
         } else {
            return str1.length() != str2.length() ? false : regionMatches(str1, true, 0, str2, 0, str1.length());
         }
      } else {
         return str1 == str2;
      }
   }

   public static boolean equals(String str1, String str2) {
      return str1 == null ? str2 == null : str1.equals(str2);
   }

   public static String capitalize(String str) {
      return changeFirstCharacterCase(str, true);
   }

   public static String uncapitalize(String str) {
      return changeFirstCharacterCase(str, false);
   }

   private static String changeFirstCharacterCase(String str, boolean capitalize) {
      if (str != null && str.length() != 0) {
         StringBuffer buf = new StringBuffer(str.length());
         if (capitalize) {
            buf.append(Character.toUpperCase(str.charAt(0)));
         } else {
            buf.append(Character.toLowerCase(str.charAt(0)));
         }

         buf.append(str.substring(1));
         return buf.toString();
      } else {
         return str;
      }
   }

   public static boolean isBlank(String str) {
      int strLen;
      if (str != null && (strLen = str.length()) != 0) {
         for(int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static boolean isNotBlank(String str) {
      return !isBlank(str);
   }

   public static boolean isEmpty(String str) {
      return str == null || str.length() == 0;
   }

   public static boolean isNotEmpty(String str) {
      return !isEmpty(str);
   }

   public static String trimToEmpty(String str) {
      return str == null ? "" : str.trim();
   }

   public static String trim(String str) {
      return str == null ? null : str.trim();
   }

   private static boolean regionMatches(CharSequence cs, boolean ignoreCase, int thisStart, CharSequence substring, int start, int length) {
      if (cs instanceof String && substring instanceof String) {
         return ((String)cs).regionMatches(ignoreCase, thisStart, (String)substring, start, length);
      } else {
         int index1 = thisStart;
         int index2 = start;
         int tmpLen = length;
         int srcLen = cs.length() - thisStart;
         int otherLen = substring.length() - start;
         if (thisStart >= 0 && start >= 0 && length >= 0) {
            if (srcLen >= length && otherLen >= length) {
               while(tmpLen-- > 0) {
                  char c1 = cs.charAt(index1++);
                  char c2 = substring.charAt(index2++);
                  if (c1 != c2) {
                     if (!ignoreCase) {
                        return false;
                     }

                     if (Character.toUpperCase(c1) != Character.toUpperCase(c2) && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                        return false;
                     }
                  }
               }

               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public static boolean containsNone(CharSequence cs, String invalidChars) {
      return cs != null && invalidChars != null ? containsNone(cs, invalidChars.toCharArray()) : true;
   }

   public static boolean containsNone(CharSequence cs, char... searchChars) {
      if (cs != null && searchChars != null) {
         int csLen = cs.length();
         int csLast = csLen - 1;
         int searchLen = searchChars.length;
         int searchLast = searchLen - 1;

         for(int i = 0; i < csLen; ++i) {
            char ch = cs.charAt(i);

            for(int j = 0; j < searchLen; ++j) {
               if (searchChars[j] == ch) {
                  if (!Character.isHighSurrogate(ch)) {
                     return false;
                  }

                  if (j == searchLast) {
                     return false;
                  }

                  if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                     return false;
                  }
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static String stripComments(String src, String stringOpens, String stringCloses, boolean slashStarComments, boolean slashSlashComments, boolean hashComments, boolean dashDashComments) {
      if (src == null) {
         return null;
      } else {
         StringBuffer buf = new StringBuffer(src.length());
         StringReader sourceReader = new StringReader(src);
         int contextMarker = 0;
         boolean escaped = false;
         int markerTypeFound = -1;
         int ind = 0;
         boolean var13 = false;

         int currentChar;
         try {
            label141:
            while((currentChar = sourceReader.read()) != -1) {
               if (markerTypeFound != -1 && currentChar == stringCloses.charAt(markerTypeFound) && !escaped) {
                  contextMarker = 0;
                  markerTypeFound = -1;
               } else {

                  if ((ind = stringOpens.indexOf(currentChar)) != -1 && !escaped && contextMarker == 0) {
                     markerTypeFound = ind;
                     contextMarker = currentChar;
                  }
               }

               if (contextMarker == 0 && currentChar == 47 && (slashSlashComments || slashStarComments)) {
                  currentChar = sourceReader.read();
                  if (currentChar == 42 && slashStarComments) {
                     int prevChar = 0;

                     while(true) {
                        if ((currentChar = sourceReader.read()) == 47 && prevChar == 42) {
                           continue label141;
                        }

                        if (currentChar == 13) {
                           currentChar = sourceReader.read();
                           if (currentChar == 10) {
                              currentChar = sourceReader.read();
                           }
                        } else if (currentChar == 10) {
                           currentChar = sourceReader.read();
                        }

                        if (currentChar < 0) {
                           continue label141;
                        }

                        prevChar = currentChar;
                     }
                  }

                  if (currentChar == 47 && slashSlashComments) {
                     while((currentChar = sourceReader.read()) != 10 && currentChar != 13 && currentChar >= 0) {
                     }
                  }
               } else if (contextMarker == 0 && currentChar == 35 && hashComments) {
                  while((currentChar = sourceReader.read()) != 10 && currentChar != 13 && currentChar >= 0) {
                  }
               } else if (contextMarker == 0 && currentChar == 45 && dashDashComments) {
                  label156: {
                     currentChar = sourceReader.read();
                     if (currentChar != -1 && currentChar == 45) {
                        while(true) {
                           if ((currentChar = sourceReader.read()) == 10 || currentChar == 13 || currentChar < 0) {
                              break label156;
                           }
                        }
                     }

                     buf.append('-');
                     if (currentChar != -1) {
                        buf.append(currentChar);
                     }
                     continue;
                  }
               }

               if (currentChar != -1) {
                  buf.append((char)currentChar);
               }
            }
         } catch (IOException var15) {
         }

         return buf.toString();
      }
   }

   public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor) {
      return startsWithIgnoreCaseAndWs(searchIn, searchFor, 0);
   }

   public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor, int beginPos) {
      if (searchIn == null) {
         return searchFor == null;
      } else {
         for(int inLength = searchIn.length(); beginPos < inLength && Character.isWhitespace(searchIn.charAt(beginPos)); ++beginPos) {
         }

         return startsWithIgnoreCase(searchIn, beginPos, searchFor);
      }
   }

   public static boolean startsWithIgnoreCase(String searchIn, int startAt, String searchFor) {
      return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor.length());
   }

   public static String join(byte[] array, char separator) {
      return array == null ? null : join(array, separator, 0, array.length);
   }

   public static String join(byte[] array, char separator, int startIndex, int endIndex) {
      if (array == null) {
         return null;
      } else {
         int noOfItems = endIndex - startIndex;
         if (noOfItems <= 0) {
            return "";
         } else {
            StringBuilder buf = new StringBuilder(noOfItems * 16);

            for(int i = startIndex; i < endIndex; ++i) {
               if (i > startIndex) {
                  buf.append(separator);
               }

               buf.append(array[i]);
            }

            return buf.toString();
         }
      }
   }

   public static String join(Object[] array, String separator) {
      return array == null ? null : join(array, separator, 0, array.length);
   }

   public static String join(Object[] array, String separator, int startIndex, int endIndex) {
      if (array == null) {
         return null;
      } else {
         if (separator == null) {
            separator = "";
         }

         int noOfItems = endIndex - startIndex;
         if (noOfItems <= 0) {
            return "";
         } else {
            StringBuilder buf = new StringBuilder(noOfItems * 16);

            for(int i = startIndex; i < endIndex; ++i) {
               if (i > startIndex) {
                  buf.append(separator);
               }

               if (array[i] != null) {
                  buf.append(array[i]);
               }
            }

            return buf.toString();
         }
      }
   }
}
