package com.google.inject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.internal.Messages;
import com.google.inject.spi.Message;

import java.util.Collection;

public final class ProvisionException extends RuntimeException {
   private final ImmutableSet<Message> messages;
   private static final long serialVersionUID = 0L;

   public ProvisionException(Iterable<Message> messages) {
      this.messages = ImmutableSet.copyOf(messages);
      Preconditions.checkArgument(!this.messages.isEmpty());
      this.initCause(Messages.getOnlyCause(this.messages));
   }

   public ProvisionException(String message, Throwable cause) {
      super(cause);
      this.messages = ImmutableSet.of(new Message(message, cause));
   }

   public ProvisionException(String message) {
      this.messages = ImmutableSet.of(new Message(message));
   }

   public Collection<Message> getErrorMessages() {
      return this.messages;
   }

   public String getMessage() {
      return Messages.formatMessages("Unable to provision, see the following errors", this.messages);
   }
}
