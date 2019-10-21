package com.google.inject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.internal.Messages;
import com.google.inject.spi.Message;
import java.util.Collection;

public class CreationException extends RuntimeException {
   private final ImmutableSet<Message> messages;
   private static final long serialVersionUID = 0L;

   public CreationException(Collection<Message> messages) {
      this.messages = ImmutableSet.copyOf(messages);
      Preconditions.checkArgument(!this.messages.isEmpty());
      this.initCause(Messages.getOnlyCause(this.messages));
   }

   public Collection<Message> getErrorMessages() {
      return this.messages;
   }

   public String getMessage() {
      return Messages.formatMessages("Unable to create injector, see the following errors", this.messages);
   }
}
