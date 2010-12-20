/*
 * Created on Dec 14, 2010
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright @2010 the original author or authors.
 */
package org.fest.assertions.internal;

import static org.fest.assertions.error.DoesNotContain.doesNotContain;
import static org.fest.assertions.test.ExpectedException.none;
import static org.fest.assertions.test.FailureMessages.*;
import static org.fest.assertions.util.ArrayWrapperList.wrap;
import static org.fest.util.Collections.set;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import org.fest.assertions.core.AssertionInfo;
import org.fest.assertions.core.WritableAssertionInfo;
import org.fest.assertions.test.ArrayFactory;
import org.fest.assertions.test.ExpectedException;
import org.junit.*;

/**
 * Tests for <code>{@link ByteArrays#assertContains(AssertionInfo, byte[], byte[])}</code>.
 *
 * @author Alex Ruiz
 */
public class ByteArrays_assertContains_Test {

  private static WritableAssertionInfo info;

  @Rule public ExpectedException thrown = none();

  private Failures failures;
  private byte[] actual;
  private ByteArrays arrays;

  @BeforeClass public static void setUpOnce() {
    info = new WritableAssertionInfo();
  }

  @Before public void setUp() {
    failures = spy(Failures.instance());
    actual = ArrayFactory.arrayOfBytes(6, 8, 10);
    arrays = new ByteArrays(failures);
  }

  @Test public void should_pass_if_actual_contains_given_values() {
    arrays.assertContains(info, actual, ArrayFactory.arrayOfBytes(6));
  }

  @Test public void should_pass_if_actual_contains_given_values_in_different_order() {
    arrays.assertContains(info, actual, ArrayFactory.arrayOfBytes(8, 10));
  }

  @Test public void should_pass_if_actual_contains_all_given_values() {
    arrays.assertContains(info, actual, ArrayFactory.arrayOfBytes(6, 8, 10));
  }

  @Test public void should_pass_if_actual_contains_given_values_more_than_once() {
    actual = ArrayFactory.arrayOfBytes(6, 8, 10, 10, 8);
    arrays.assertContains(info, actual, ArrayFactory.arrayOfBytes(8));
  }

  @Test public void should_pass_if_actual_contains_given_values_even_if_duplicated() {
    arrays.assertContains(info, actual, ArrayFactory.arrayOfBytes(6, 6));
  }

  @Test public void should_throw_error_if_array_of_values_to_look_for_is_empty() {
    thrown.expectIllegalArgumentException(arrayToLookForIsEmpty());
    arrays.assertContains(info, actual, new byte[0]);
  }

  @Test public void should_throw_error_if_array_of_values_to_look_for_is_null() {
    thrown.expectNullPointerException(arrayToLookForIsNull());
    arrays.assertContains(info, actual, null);
  }

  @Test public void should_fail_if_actual_is_null() {
    thrown.expectAssertionError(unexpectedNull());
    arrays.assertContains(info, null, ArrayFactory.arrayOfBytes(8));
  }

  @Test public void should_fail_if_actual_does_not_contain_values() {
    byte[] expected = { 6, 8, 9 };
    try {
      arrays.assertContains(info, actual, expected);
      fail();
    } catch (AssertionError e) {}
    verify(failures).failure(info, doesNotContain(wrap(actual), wrap(expected), set((byte)9)));
  }
}
