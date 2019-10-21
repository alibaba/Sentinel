package com.google.inject.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simplified version of {@link Lock} that is special due to how it handles deadlocks detection.
 *
 * <p>Is an inherent part of {@link SingletonScope}, moved into a upper level class due to its size
 * and complexity.
 *
 * @param <ID> Lock identification provided by the client, is returned unmodified to the client when
 *     lock cycle is detected to identify it. Only toString() needs to be implemented. Lock
 *     references this object internally, for the purposes of Garbage Collection you should not use
 *     heavy IDs. Lock is referenced by a lock factory as long as it's owned by a thread.
 * @see SingletonScope
 * @see CycleDetectingLockFactory
 * @author timofeyb (Timothy Basanov)
 */
interface CycleDetectingLock<ID> {

   /**
    * Takes a lock in a blocking fashion in case no potential deadlocks are detected. If the lock was
    * successfully owned, returns an empty map indicating no detected potential deadlocks.
    *
    * <p>Otherwise, a map indicating threads involved in a potential deadlock are returned. Map is
    * ordered by dependency cycle and lists locks for each thread that are part of the loop in order,
    * the last lock in the list is the one that the thread is currently waiting for. Returned map is
    * created atomically.
    *
    * <p>In case no cycle is detected performance is O(threads creating singletons), in case cycle is
    * detected performance is O(singleton locks).
    */
   ListMultimap<Thread, ID> lockOrDetectPotentialLocksCycle();

   /** Unlocks previously locked lock. */
   void unlock();

   /**
    * Wraps locks so they would never cause a deadlock. On each {@link
    * CycleDetectingLock#lockOrDetectPotentialLocksCycle} we check for dependency cycles within locks
    * created by the same factory. Either we detect a cycle and return it or take it atomically.
    *
    * <p>Important to note that we do not prevent deadlocks in the client code. As an example: Thread
    * A takes lock L and creates singleton class CA depending on the singleton class CB. Meanwhile
    * thread B is creating class CB and is waiting on the lock L. Issue happens due to client code
    * creating interdependent classes and using locks, where no guarantees on the creation order from
    * Guice are provided.
    *
    * <p>Instances of these locks are not intended to be exposed outside of {@link SingletonScope}.
    */
   class CycleDetectingLockFactory<ID> {

      /**
       * Specifies lock that thread is currently waiting on to own it. Used only for purposes of locks
       * cycle detection.
       *
       * <ul>
       * <li>Key: thread
       * <li> Value: lock that is being waited on
       * </ul>
       *
       * <p>Element is added inside {@link #lockOrDetectPotentialLocksCycle()} before {@link
       * Lock#lock} is called. Element is removed inside {@link #lockOrDetectPotentialLocksCycle()}
       * after {@link Lock#lock} and synchronously with adding it to {@link #locksOwnedByThread}.
       *
       * <p>Same lock can be added for several threads in case all of them are trying to take it.
       *
       * <p>Guarded by {@code CycleDetectingLockFactory.class}.
       */
      private static Map<Thread, ReentrantCycleDetectingLock<?>> lockThreadIsWaitingOn =
              Maps.newHashMap();

      /**
       * Lists locks that thread owns. Used only to populate locks in a potential cycle when it is
       * detected.
       *
       * <ul>
       * <li>Key: thread
       * <li>Value: stack of locks that were owned.
       * </ul>
       *
       * <p>Element is added inside {@link #lockOrDetectPotentialLocksCycle()} after {@link Lock#lock}
       * is called. Element is removed inside {@link #unlock()} synchronously with {@link
       * Lock#unlock()} call.
       *
       * <p>Same lock can only be present several times for the same thread as locks are reentrant.
       * Lock can not be owned by several different threads as the same time.
       *
       * <p>Guarded by {@code CycleDetectingLockFactory.class}.
       */
      private static final Multimap<Thread, ReentrantCycleDetectingLock<?>> locksOwnedByThread =
              LinkedHashMultimap.create();

      /**
       * Creates new lock within this factory context. We can guarantee that locks created by the same
       * factory would not deadlock.
       *
       * @param userLockId lock id that would be used to report lock cycles if detected
       */
      CycleDetectingLock<ID> create(ID userLockId) {
         return new ReentrantCycleDetectingLock<ID>(this, userLockId, new ReentrantLock());
      }

      /** The implementation for {@link CycleDetectingLock}. */
      static class ReentrantCycleDetectingLock<ID> implements CycleDetectingLock<ID> {

         /** Underlying lock used for actual waiting when no potential deadlocks are detected. */
         private final Lock lockImplementation;
         /** User id for this lock. */
         private final ID userLockId;
         /** Factory that was used to create this lock. */
         private final CycleDetectingLockFactory<ID> lockFactory;
         /**
          * Thread that owns this lock. Nullable. Guarded by {@code CycleDetectingLockFactory.this}.
          */
         private Thread lockOwnerThread = null;

         /**
          * Number of times that thread owned this lock. Guarded by {@code
          * CycleDetectingLockFactory.this}.
          */
         private int lockReentranceCount = 0;

         ReentrantCycleDetectingLock(
                 CycleDetectingLockFactory<ID> lockFactory, ID userLockId, Lock lockImplementation) {
            this.lockFactory = lockFactory;
            this.userLockId = Preconditions.checkNotNull(userLockId, "userLockId");
            this.lockImplementation =
                    Preconditions.checkNotNull(lockImplementation, "lockImplementation");
         }

         @Override
         public ListMultimap<Thread, ID> lockOrDetectPotentialLocksCycle() {
            final Thread currentThread = Thread.currentThread();
            synchronized (CycleDetectingLockFactory.class) {
               checkState();
               // Add this lock to the waiting map to ensure it is included in any reported lock cycle.
               lockThreadIsWaitingOn.put(currentThread, this);
               ListMultimap<Thread, ID> locksInCycle = detectPotentialLocksCycle();
               if (!locksInCycle.isEmpty()) {
                  // We aren't actually going to wait for this lock, so remove it from the map.
                  lockThreadIsWaitingOn.remove(currentThread);
                  // potential deadlock is found, we don't try to take this lock
                  return locksInCycle;
               }

            }

            // this may be blocking, but we don't expect it to cause a deadlock
            lockImplementation.lock();

            synchronized (CycleDetectingLockFactory.class) {
               // current thread is no longer waiting on this lock
               lockThreadIsWaitingOn.remove(currentThread);
               checkState();

               // mark it as owned by us
               lockOwnerThread = currentThread;
               lockReentranceCount++;
               // add this lock to the list of locks owned by a current thread
               locksOwnedByThread.put(currentThread, this);
            }
            // no deadlock is found, locking successful
            return ImmutableListMultimap.of();
         }

         @Override
         public void unlock() {
            final Thread currentThread = Thread.currentThread();
            synchronized (CycleDetectingLockFactory.class) {
               checkState();
               Preconditions.checkState(
                       lockOwnerThread != null, "Thread is trying to unlock a lock that is not locked");
               Preconditions.checkState(
                       lockOwnerThread == currentThread,
                       "Thread is trying to unlock a lock owned by another thread");

               // releasing underlying lock
               lockImplementation.unlock();

               // be sure to release the lock synchronously with updating internal state
               lockReentranceCount--;
               if (lockReentranceCount == 0) {
                  // we no longer own this lock
                  lockOwnerThread = null;
                  Preconditions.checkState(
                          locksOwnedByThread.remove(currentThread, this),
                          "Internal error: Can not find this lock in locks owned by a current thread");
                  if (locksOwnedByThread.get(currentThread).isEmpty()) {
                     // clearing memory
                     locksOwnedByThread.removeAll(currentThread);
                  }
               }
            }
         }

         /** Check consistency of an internal state. */
         void checkState() throws IllegalStateException {
            final Thread currentThread = Thread.currentThread();
            Preconditions.checkState(
                    !lockThreadIsWaitingOn.containsKey(currentThread),
                    "Internal error: Thread should not be in a waiting thread on a lock now");
            if (lockOwnerThread != null) {
               // check state of a locked lock
               Preconditions.checkState(
                       lockReentranceCount >= 0,
                       "Internal error: Lock ownership and reentrance count internal states do not match");
               Preconditions.checkState(
                       locksOwnedByThread.get(lockOwnerThread).contains(this),
                       "Internal error: Set of locks owned by a current thread and lock "
                               + "ownership status do not match");
            } else {
               // check state of a non locked lock
               Preconditions.checkState(
                       lockReentranceCount == 0,
                       "Internal error: Reentrance count of a non locked lock is expect to be zero");
               Preconditions.checkState(
                       !locksOwnedByThread.values().contains(this),
                       "Internal error: Non locked lock should not be owned by any thread");
            }
         }

         /**
          * Algorithm to detect a potential lock cycle.
          *
          * <p>For lock's thread owner check which lock is it trying to take. Repeat recursively. When
          * current thread is found a potential cycle is detected.
          *
          * @see CycleDetectingLock#lockOrDetectPotentialLocksCycle()
          */
         private ListMultimap<Thread, ID> detectPotentialLocksCycle() {
            final Thread currentThread = Thread.currentThread();
            if (lockOwnerThread == null || lockOwnerThread == currentThread) {
               // if nobody owns this lock, lock cycle is impossible
               // if a current thread owns this lock, we let Guice to handle it
               return ImmutableListMultimap.of();
            }

            ListMultimap<Thread, ID> potentialLocksCycle =
                    Multimaps.newListMultimap(
                            new LinkedHashMap<Thread, Collection<ID>>(),
                            new Supplier<List<ID>>() {
                               @Override
                               public List<ID> get() {
                                  return Lists.newArrayList();
                               }
                            });
            // lock that is a part of a potential locks cycle, starts with current lock
            ReentrantCycleDetectingLock<?> lockOwnerWaitingOn = this;
            // try to find a dependency path between lock's owner thread and a current thread
            while (lockOwnerWaitingOn != null && lockOwnerWaitingOn.lockOwnerThread != null) {
               Thread threadOwnerThreadWaits = lockOwnerWaitingOn.lockOwnerThread;
               // in case locks cycle exists lock we're waiting for is part of it
               lockOwnerWaitingOn =
                       addAllLockIdsAfter(threadOwnerThreadWaits, lockOwnerWaitingOn, potentialLocksCycle);
               if (threadOwnerThreadWaits == currentThread) {
                  // owner thread depends on current thread, cycle detected
                  return potentialLocksCycle;
               }
            }
            // no dependency path from an owner thread to a current thread
            return ImmutableListMultimap.of();
         }

         /**
          * Adds all locks held by the given thread that are after the given lock and then returns the
          * lock the thread is currently waiting on, if any
          */
         private ReentrantCycleDetectingLock<?> addAllLockIdsAfter(
                 Thread thread,
                 ReentrantCycleDetectingLock<?> lock,
                 ListMultimap<Thread, ID> potentialLocksCycle) {
            boolean found = false;
            Collection<ReentrantCycleDetectingLock<?>> ownedLocks = locksOwnedByThread.get(thread);
            Preconditions.checkNotNull(
                    ownedLocks, "Internal error: No locks were found taken by a thread");
            for (ReentrantCycleDetectingLock<?> ownedLock : ownedLocks) {
               if (ownedLock == lock) {
                  found = true;
               }
               if (found && ownedLock.lockFactory == this.lockFactory) {
                  // All locks are stored in a shared map therefore there is no way to
                  // enforce type safety. We know that our cast is valid as we check for a lock's
                  // factory. If the lock was generated by the
                  // same factory it has to have same type as the current lock.
                  @SuppressWarnings("unchecked")
                  ID userLockId = (ID) ownedLock.userLockId;
                  potentialLocksCycle.put(thread, userLockId);
               }
            }
            Preconditions.checkState(
                    found,
                    "Internal error: We can not find locks that created a cycle that we detected");
            ReentrantCycleDetectingLock<?> unownedLock = lockThreadIsWaitingOn.get(thread);
            // If this thread is waiting for a lock add it to the cycle and return it
            if (unownedLock != null && unownedLock.lockFactory == this.lockFactory) {
               @SuppressWarnings("unchecked")
               ID typed = (ID) unownedLock.userLockId;
               potentialLocksCycle.put(thread, typed);
            }
            return unownedLock;
         }

         @Override
         public String toString() {
            // copy is made to prevent a data race
            // no synchronization is used, potentially stale data, should be good enough
            Thread thread = this.lockOwnerThread;
            if (thread != null) {
               return String.format("%s[%s][locked by %s]", super.toString(), userLockId, thread);
            } else {
               return String.format("%s[%s][unlocked]", super.toString(), userLockId);
            }
         }
      }
   }
}
