package com.google.inject.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.inject.*;
import com.google.inject.internal.CycleDetectingLock.CycleDetectingLockFactory;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.Message;

import java.util.Formatter;
import java.util.List;

/**
 * One instance per {@link Injector}. Also see {@code @}{@link Singleton}.
 *
 * <p>Introduction from the author: Implementation of this class seems unreasonably complicated at
 * the first sight. I fully agree with you, that the beast below is very complex and it's hard to
 * reason on how does it work or not. Still I want to assure you that hundreds(?) of hours were
 * thrown into making this code simple, while still maintaining Singleton contract.
 *
 * <p>Anyway, why is it so complex? Singleton scope does not seem to be that unique. 1) Guice has
 * never truly expected to be used in multi threading environment with many Injectors working
 * alongside each other. There is almost no code with Guice that propagates state between threads.
 * And Singleton scope is The exception. 2) Guice supports circular dependencies and thus manages
 * proxy objects. There is no interface that allows user defined Scopes to create proxies, it is
 * expected to be done by Guice. Singleton scope needs to be able to detect circular dependencies
 * spanning several threads, therefore Singleton scope needs to be able to create these proxies. 3)
 * To make things worse, Guice has a very tricky definition for a binding resolution when Injectors
 * are in in a parent/child relationship. And Scope does not have access to this information by
 * design, the only real action that Scope can do is to call or not to call a creator. 4) There is
 * no readily available code in Guice that can detect a potential deadlock, and no code for handling
 * dependency cycles spanning several threads. This is significantly harder as all the dependencies
 * in a thread at runtime can be represented with a list, where in a multi threaded environment we
 * have more complex dependency trees. 5) Guice has a pretty strong contract regarding Garbage
 * Collection, which often prevents us from linking objects directly. So simple domain specific code
 * can not be written and intermediary id objects need to be managed. 6) Guice is relatively fast
 * and we should not make things worse. We're trying our best to optimize synchronization for speed
 * and memory. Happy path should be almost as fast as in a single threaded solution and should not
 * take much more memory. 7) Error message generation in Guice was not meant to be used like this
 * and to work around its APIs we need a lot of code. Additional complexity comes from inherent data
 * races as message is only generated when failure occurs on proxy object generation. Things get
 * ugly pretty fast.
 *
 * @see #scope(Key, Provider)
 * @see CycleDetectingLock
 * @author timofeyb (Timothy Basanov)
 */
public class SingletonScope implements Scope {

   /** A sentinel value representing null. */
   private static final Object NULL = new Object();

   /**
    * Allows us to detect when circular proxies are necessary. It's only used during singleton
    * instance initialization, after initialization direct access through volatile field is used.
    *
    * <p>NB: Factory uses {@link Key}s as a user locks ids, different injectors can share them.
    * Cycles are detected properly as cycle detection does not rely on user locks ids, but error
    * message generated could be less than ideal.
    */
   // TODO(user): we may use one factory per injector tree for optimization reasons
   private static final CycleDetectingLockFactory<Key<?>> cycleDetectingLockFactory =
           new CycleDetectingLockFactory<Key<?>>();

   /**
    * Provides singleton scope with the following properties: - creates no more than one instance per
    * Key as a creator is used no more than once, - result is cached and returned quickly on
    * subsequent calls, - exception in a creator is not treated as instance creation and is not
    * cached, - creates singletons in parallel whenever possible, - waits for dependent singletons to
    * be created even across threads and when dependencies are shared as long as no circular
    * dependencies are detected, - returns circular proxy only when circular dependencies are
    * detected, - aside from that, blocking synchronization is only used for proxy creation and
    * initialization,
    *
    * @see CycleDetectingLockFactory
    */
   @Override
   public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
      /**
       * Locking strategy: - volatile instance: double-checked locking for quick exit when scope is
       * initialized, - constructionContext: manipulations with proxies list or instance
       * initialization - creationLock: singleton instance creation, -- allows to guarantee only one
       * instance per singleton, -- special type of a lock, that prevents potential deadlocks, --
       * guards constructionContext for all operations except proxy creation
       */
      return new Provider<T>() {
         /**
          * The lazily initialized singleton instance. Once set, this will either have type T or will
          * be equal to NULL. Would never be reset to null.
          */
         volatile Object instance;

         /**
          * Circular proxies are used when potential deadlocks are detected. Guarded by itself.
          * ConstructionContext is not thread-safe, so each call should be synchronized.
          */
         final ConstructionContext<T> constructionContext = new ConstructionContext<>();

         /** For each binding there is a separate lock that we hold during object creation. */
         final CycleDetectingLock<Key<?>> creationLock = cycleDetectingLockFactory.create(key);

         /**
          * The singleton provider needs a reference back to the injector, in order to get ahold of
          * InternalContext during instantiation.
          */
         final /* @Nullable */ InjectorImpl injector;

         {
            // If we are getting called by Scoping
            if (creator instanceof ProviderToInternalFactoryAdapter) {
               injector = ((ProviderToInternalFactoryAdapter) creator).getInjector();
            } else {
               injector = null;
            }
         }

         @SuppressWarnings("DoubleCheckedLocking")
         @Override
         public T get() {
            // cache volatile variable for the usual case of already initialized object
            final Object initialInstance = instance;
            if (initialInstance == null) {
               // instance is not initialized yet

               // first, store the current InternalContext in a map, so that if there is a circular
               // dependency error, we can use the InternalContext objects to create a complete
               // error message.
               // Handle injector being null, which can happen when users call Scoping.scope themselves
               final InternalContext context = injector == null ? null : injector.getLocalContext();
               // acquire lock for current binding to initialize an instance
               final ListMultimap<Thread, Key<?>> locksCycle =
                       creationLock.lockOrDetectPotentialLocksCycle();

               if (locksCycle.isEmpty()) {
                  // this thread now owns creation of an instance
                  try {
                     // intentionally reread volatile variable to prevent double initialization
                     if (instance == null) {
                        // creator throwing an exception can cause circular proxies created in
                        // different thread to never be resolved, just a warning
                        T provided = creator.get();
                        Object providedNotNull = provided == null ? NULL : provided;

                        // scope called recursively can initialize instance as a side effect
                        if (instance == null) {
                           // instance is still not initialized, so we can proceed

                           // don't remember proxies created by Guice on circular dependency
                           // detection within the same thread; they are not real instances to cache
                           if (Scopes.isCircularProxy(provided)) {
                              return provided;
                           }

                           synchronized (constructionContext) {
                              // guarantee thread-safety for instance and proxies initialization
                              instance = providedNotNull;
                              constructionContext.setProxyDelegates(provided);
                           }
                        } else {
                           // safety assert in case instance was initialized
                           Preconditions.checkState(
                                   instance == providedNotNull,
                                   "Singleton is called recursively returning different results");
                        }
                     }
                  } catch (RuntimeException e) {
                     // something went wrong, be sure to clean a construction context
                     // this helps to prevent potential memory leaks in circular proxies list
                     synchronized (constructionContext) {
                        constructionContext.finishConstruction();
                     }
                     throw e;
                  } finally {
                     // always release our creation lock, even on failures
                     creationLock.unlock();
                  }
               } else {
                  if (context == null) {
                     throw new ProvisionException(
                             ImmutableList.of(createCycleDependenciesMessage(locksCycle, null)));
                  }
                  // potential deadlock detected, creation lock is not taken by this thread
                  synchronized (constructionContext) {
                     // guarantee thread-safety for instance and proxies initialization
                     if (instance == null) {
                        // creating a proxy to satisfy circular dependency across several threads
                        Dependency<?> dependency =
                                Preconditions.checkNotNull(
                                        context.getDependency(), "internalContext.getDependency()");
                        Class<?> rawType = dependency.getKey().getTypeLiteral().getRawType();

                        try {
                           @SuppressWarnings("unchecked")
                           T proxy =
                                   (T) constructionContext.createProxy(context.getInjectorOptions(), rawType);
                           return proxy;
                        } catch (InternalProvisionException e) {
                           // best effort to create a rich error message
                           Message proxyCreationError = Iterables.getOnlyElement(e.getErrors());
                           Message cycleDependenciesMessage =
                                   createCycleDependenciesMessage(locksCycle, proxyCreationError);
                           // adding stack trace generated by us in addition to a standard one
                           throw new ProvisionException(
                                   ImmutableList.of(cycleDependenciesMessage, proxyCreationError));
                        }
                     }
                  }
               }

               // at this point we're sure that singleton was initialized,
               // reread volatile variable to catch all corner cases

               // caching volatile variable to minimize number of reads performed
               final Object initializedInstance = instance;
               Preconditions.checkState(
                       initializedInstance != null,
                       "Internal error: Singleton is not initialized contrary to our expectations");
               @SuppressWarnings("unchecked")
               T initializedTypedInstance = (T) initializedInstance;
               return initializedInstance == NULL ? null : initializedTypedInstance;
            } else {
               // singleton is already initialized and local cache can be used
               @SuppressWarnings("unchecked")
               T typedInitialIntance = (T) initialInstance;
               return initialInstance == NULL ? null : typedInitialIntance;
            }
         }

         /**
          * Helper method to create beautiful and rich error descriptions. Best effort and slow. Tries
          * its best to provide dependency information from injectors currently available in a global
          * internal context.
          *
          * <p>The main thing being done is creating a list of Dependencies involved into lock cycle
          * across all the threads involved. This is a structure we're creating:
          *
          * <pre>
          * { Current Thread, C.class, B.class, Other Thread, B.class, C.class, Current Thread }
          * To be inserted in the beginning by Guice: { A.class, B.class, C.class }
          * </pre>
          *
          * When we're calling Guice to create A and it fails in the deadlock while trying to create C,
          * which is being created by another thread, which waits for B. List would be reversed before
          * printing it to the end user.
          */
         private Message createCycleDependenciesMessage(
                 ListMultimap<Thread, Key<?>> locksCycle, /* @Nullable */ Message proxyCreationError) {
            // this is the main thing that we'll show in an error message,
            // current thread is populate by Guice
            StringBuilder sb = new StringBuilder();
            Formatter fmt = new Formatter(sb);
            fmt.format("Encountered circular dependency spanning several threads.");
            if (proxyCreationError != null) {
               fmt.format(" %s", proxyCreationError.getMessage());
            }
            fmt.format("%n");
            for (Thread lockedThread : locksCycle.keySet()) {
               List<Key<?>> lockedKeys = locksCycle.get(lockedThread);
               fmt.format("%s is holding locks the following singletons in the cycle:%n", lockedThread);
               for (Key<?> lockedKey : lockedKeys) {
                  fmt.format("%s%n", Errors.convert(lockedKey));
               }
               for (StackTraceElement traceElement : lockedThread.getStackTrace()) {
                  fmt.format("\tat %s%n", traceElement);
               }
            }
            fmt.close();
            return new Message(Thread.currentThread(), sb.toString());
         }

         @Override
         public String toString() {
            return String.format("%s[%s]", creator, Scopes.SINGLETON);
         }
      };
   }

   @Override
   public String toString() {
      return "Scopes.SINGLETON";
   }
}
