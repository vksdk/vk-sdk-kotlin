/*
 * Copyright (C) 2018 Touchlab, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.petersamokhin.vksdk.internal.co.touchlab.stately.concurrency

/**
 * Multiplatform AtomicInt implementation
 */
expect class AtomicInt(initialValue: Int) {
  fun get(): Int
  fun set(newValue: Int)
  fun incrementAndGet(): Int
  fun decrementAndGet(): Int

  fun addAndGet(delta: Int): Int
  fun compareAndSet(expected: Int, new: Int): Boolean
}

var AtomicInt.value
  get() = get()
  set(value) {
    set(value)
  }