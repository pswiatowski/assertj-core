/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2016 the original author or authors.
 */
package org.assertj.core.api;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.util.Arrays.array;
import static org.assertj.core.util.DateUtil.parseDatetime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.api.test.ComparableExample;
import org.assertj.core.data.MapEntry;
import org.assertj.core.test.CartoonCharacter;
import org.assertj.core.test.Maps;
import org.assertj.core.test.Name;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for <code>{@link SoftAssertions}</code>.
 *
 * @author Brian Laframboise
 */
public class SoftAssertionsTest extends BaseAssertionsTest {

  private SoftAssertions softly;

  private CartoonCharacter homer;
  private CartoonCharacter fred;

  @Before
  public void setup() {
    softly = new SoftAssertions();

    CartoonCharacter bart = new CartoonCharacter("Bart Simpson");
    CartoonCharacter lisa = new CartoonCharacter("Lisa Simpson");
    CartoonCharacter maggie = new CartoonCharacter("Maggie Simpson");

    homer = new CartoonCharacter("Homer Simpson");
    homer.getChildren().add(bart);
    homer.getChildren().add(lisa);
    homer.getChildren().add(maggie);

    CartoonCharacter pebbles = new CartoonCharacter("Pebbles Flintstone");
    fred = new CartoonCharacter("Fred Flintstone");
    fred.getChildren().add(pebbles);
  }

  @Test
  public void all_assertions_should_pass() {
    softly.assertThat(1).isEqualTo(1);
    softly.assertThat(Lists.newArrayList(1, 2)).containsOnly(1, 2);
    softly.assertAll();
  }

  @Test
  public void should_return_success_of_last_assertion() {
    try {
      softly.assertThat(true).isFalse();
    } catch (AssertionError ignore) {}
    softly.assertThat(true).isEqualTo(true);
    assertThat(softly.wasSuccess()).isTrue();
  }

  @Test
  public void should_return_success_of_last_assertion_with_nested_calls() {
    try {
      softly.assertThat(true).isFalse();
    } catch (AssertionError ignore) {}
    softly.assertThat(true).isTrue(); // isTrue() calls isEqualTo(true)
    assertThat(softly.wasSuccess()).isTrue();
  }

  @Test
  public void should_return_failure_of_last_assertion() {
    softly.assertThat(true).isTrue();
    softly.assertThat(true).isEqualTo(false);
    assertThat(softly.wasSuccess()).isFalse();
  }

  @Test
  public void should_return_failure_of_last_assertion_with_nested_calls() {
    softly.assertThat(true).isTrue();
    softly.assertThat(true).isFalse(); // isFalse() calls isEqualTo(false)
    assertThat(softly.wasSuccess()).isFalse();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void should_be_able_to_catch_exceptions_thrown_by_map_assertions() {
    try {
      softly.assertThat(Maps.mapOf(MapEntry.entry("54", "55"))).contains(MapEntry.entry("1", "2"));
      softly.assertAll();
      fail("Should not reach here");
    } catch (SoftAssertionError e) {
      List<String> errors = e.getErrors();
      assertThat(errors).contains(String.format("%nExpecting:%n"
                                                + " <{\"54\"=\"55\"}>%n"
                                                + "to contain:%n"
                                                + " <[MapEntry[key=\"1\", value=\"2\"]]>%n"
                                                + "but could not find:%n"
                                                + " <[MapEntry[key=\"1\", value=\"2\"]]>%n"));

    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void should_be_able_to_catch_exceptions_thrown_by_all_proxied_methods() throws URISyntaxException {
    try {
      softly.assertThat(BigDecimal.ZERO).isEqualTo(BigDecimal.ONE);

      softly.assertThat(Boolean.FALSE).isTrue();
      softly.assertThat(false).isTrue();
      softly.assertThat(new boolean[] { false }).isEqualTo(new boolean[] { true });

      softly.assertThat(new Byte((byte) 0)).isEqualTo((byte) 1);
      softly.assertThat((byte) 2).inHexadecimal().isEqualTo((byte) 3);
      softly.assertThat(new byte[] { 4 }).isEqualTo(new byte[] { 5 });

      softly.assertThat(new Character((char) 65)).isEqualTo(new Character((char) 66));
      softly.assertThat((char) 67).isEqualTo((char) 68);
      softly.assertThat(new char[] { 69 }).isEqualTo(new char[] { 70 });

      softly.assertThat(new StringBuilder("a")).isEqualTo(new StringBuilder("b"));

      softly.assertThat(Object.class).isEqualTo(String.class);

      softly.assertThat(parseDatetime("1999-12-31T23:59:59")).isEqualTo(parseDatetime("2000-01-01T00:00:01"));

      softly.assertThat(new Double(6.0d)).isEqualTo(new Double(7.0d));
      softly.assertThat(8.0d).isEqualTo(9.0d);
      softly.assertThat(new double[] { 10.0d }).isEqualTo(new double[] { 11.0d });

      softly.assertThat(new File("a"))
            .overridingErrorMessage("expected:<File(b)> but was:<File(a)>")
            .isEqualTo(new File("b"));

      softly.assertThat(new Float(12f)).isEqualTo(new Float(13f));
      softly.assertThat(14f).isEqualTo(15f);
      softly.assertThat(new float[] { 16f }).isEqualTo(new float[] { 17f });

      softly.assertThat(new ByteArrayInputStream(new byte[] { (byte) 65 }))
            .hasSameContentAs(new ByteArrayInputStream(new byte[] { (byte) 66 }));

      softly.assertThat(new Integer(20)).isEqualTo(new Integer(21));
      softly.assertThat(22).isEqualTo(23);
      softly.assertThat(new int[] { 24 }).isEqualTo(new int[] { 25 });

      softly.assertThat((Iterable<String>) Lists.newArrayList("26")).isEqualTo(Lists.newArrayList("27"));
      softly.assertThat(Lists.newArrayList("28").iterator()).contains("29");
      softly.assertThat(Lists.newArrayList("30")).isEqualTo(Lists.newArrayList("31"));

      softly.assertThat(new Long(32L)).isEqualTo(new Long(33L));
      softly.assertThat(34L).isEqualTo(35L);
      softly.assertThat(new long[] { 36L }).isEqualTo(new long[] { 37L });

      softly.assertThat(Maps.mapOf(MapEntry.entry("38", "39"))).isEqualTo(Maps.mapOf(MapEntry.entry("40", "41")));

      softly.assertThat(new Short((short) 42)).isEqualTo(new Short((short) 43));
      softly.assertThat((short) 44).isEqualTo((short) 45);
      softly.assertThat(new short[] { (short) 46 }).isEqualTo(new short[] { (short) 47 });

      softly.assertThat("48").isEqualTo("49");

      softly.assertThat(new Object() {
        @Override
        public String toString() {
          return "50";
        }
      }).isEqualTo(new Object() {
        @Override
        public String toString() {
          return "51";
        }
      });

      softly.assertThat(new Object[] { new Object() {
        @Override
        public String toString() {
          return "52";
        }
      } }).isEqualTo(new Object[] { new Object() {
        @Override
        public String toString() {
          return "53";
        }
      } });

      final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("IllegalArgumentException message");
      softly.assertThat(illegalArgumentException).hasMessage("NullPointerException message");
      softly.assertThatThrownBy(new ThrowingCallable() {

        @Override
        public void call() throws Exception {
          throw new Exception("something was wrong");
        }

      }).hasMessage("something was good");
      softly.assertThat(Maps.mapOf(MapEntry.entry("54", "55"))).contains(MapEntry.entry("1", "2"));
      softly.assertThat(new URI("http://assertj.org")).hasPort(8888);

      softly.assertAll();
      fail("Should not reach here");
    } catch (SoftAssertionError e) {
      List<String> errors = e.getErrors();
      assertThat(errors).hasSize(41);
      assertThat(errors.get(0)).isEqualTo("expected:<[1]> but was:<[0]>");

      assertThat(errors.get(1)).isEqualTo("expected:<[tru]e> but was:<[fals]e>");
      assertThat(errors.get(2)).isEqualTo("expected:<[tru]e> but was:<[fals]e>");
      assertThat(errors.get(3)).isEqualTo("expected:<[[tru]e]> but was:<[[fals]e]>");

      assertThat(errors.get(4)).isEqualTo("expected:<[1]> but was:<[0]>");
      assertThat(errors.get(5)).isEqualTo("expected:<0x0[3]> but was:<0x0[2]>");
      assertThat(errors.get(6)).isEqualTo("expected:<[[5]]> but was:<[[4]]>");

      assertThat(errors.get(7)).isEqualTo("expected:<'[B]'> but was:<'[A]'>");
      assertThat(errors.get(8)).isEqualTo("expected:<'[D]'> but was:<'[C]'>");
      assertThat(errors.get(9)).isEqualTo("expected:<['[F]']> but was:<['[E]']>");

      assertThat(errors.get(10)).isEqualTo("expected:<[b]> but was:<[a]>");

      assertThat(errors.get(11)).isEqualTo("expected:<java.lang.[String]> but was:<java.lang.[Object]>");

      assertThat(errors.get(12)).isEqualTo("expected:<[2000-01-01T00:00:01].000> but was:<[1999-12-31T23:59:59].000>");

      assertThat(errors.get(13)).isEqualTo("expected:<[7].0> but was:<[6].0>");
      assertThat(errors.get(14)).isEqualTo("expected:<[9].0> but was:<[8].0>");
      assertThat(errors.get(15)).isEqualTo("expected:<[1[1].0]> but was:<[1[0].0]>");

      assertThat(errors.get(16)).isEqualTo("expected:<File(b)> but was:<File(a)>");

      assertThat(errors.get(17)).isEqualTo("expected:<1[3].0f> but was:<1[2].0f>");
      assertThat(errors.get(18)).isEqualTo("expected:<1[5].0f> but was:<1[4].0f>");
      assertThat(errors.get(19)).isEqualTo("expected:<[1[7].0f]> but was:<[1[6].0f]>");

      assertThat(errors.get(20)).isEqualTo(String.format("%nInputStreams do not have same content:%n%n"
                                                         + "Changed content at line 1:%n"
                                                         + "expecting:%n"
                                                         + "  [\"B\"]%n"
                                                         + "but was:%n"
                                                         + "  [\"A\"]%n"));

      assertThat(errors.get(21)).isEqualTo("expected:<2[1]> but was:<2[0]>");
      assertThat(errors.get(22)).isEqualTo("expected:<2[3]> but was:<2[2]>");
      assertThat(errors.get(23)).isEqualTo("expected:<[2[5]]> but was:<[2[4]]>");

      assertThat(errors.get(24)).isEqualTo("expected:<[\"2[7]\"]> but was:<[\"2[6]\"]>");
      assertThat(errors.get(25)).isEqualTo(String.format("%nExpecting:%n" +
                                                         " <[\"28\"]>%n" +
                                                         "to contain:%n" +
                                                         " <[\"29\"]>%n" +
                                                         "but could not find:%n" +
                                                         " <[\"29\"]>%n"));
      assertThat(errors.get(26)).isEqualTo("expected:<[\"3[1]\"]> but was:<[\"3[0]\"]>");

      assertThat(errors.get(27)).isEqualTo("expected:<3[3]L> but was:<3[2]L>");
      assertThat(errors.get(28)).isEqualTo("expected:<3[5]L> but was:<3[4]L>");
      assertThat(errors.get(29)).isEqualTo("expected:<[3[7]L]> but was:<[3[6]L]>");

      assertThat(errors.get(30)).isEqualTo("expected:<{\"[40\"=\"41]\"}> but was:<{\"[38\"=\"39]\"}>");

      assertThat(errors.get(31)).isEqualTo("expected:<4[3]> but was:<4[2]>");
      assertThat(errors.get(32)).isEqualTo("expected:<4[5]> but was:<4[4]>");
      assertThat(errors.get(33)).isEqualTo("expected:<[4[7]]> but was:<[4[6]]>");

      assertThat(errors.get(34)).isEqualTo("expected:<\"4[9]\"> but was:<\"4[8]\">");

      assertThat(errors.get(35)).isEqualTo("expected:<5[1]> but was:<5[0]>");
      assertThat(errors.get(36)).isEqualTo("expected:<[5[3]]> but was:<[5[2]]>");
      assertThat(errors.get(37)).isEqualTo(String.format("%nExpecting message:%n"
                                                         + " <\"NullPointerException message\">%n"
                                                         + "but was:%n"
                                                         + " <\"IllegalArgumentException message\">"));
      assertThat(errors.get(38)).isEqualTo(String.format("%nExpecting message:%n"
                                                         + " <\"something was good\">%n"
                                                         + "but was:%n"
                                                         + " <\"something was wrong\">"));
      assertThat(errors.get(39)).isEqualTo(String.format("%nExpecting:%n"
                                                         + " <{\"54\"=\"55\"}>%n"
                                                         + "to contain:%n"
                                                         + " <[MapEntry[key=\"1\", value=\"2\"]]>%n"
                                                         + "but could not find:%n"
                                                         + " <[MapEntry[key=\"1\", value=\"2\"]]>%n"));
      assertThat(errors.get(40)).contains("Expecting port of");
    }
  }

  @Test
  public void should_pass_when_using_extracting_with_list() {

    List<Name> names = asList(name("John", "Doe"), name("Jane", "Doe"));

    softly.assertThat(names)
          .extracting("first")
          .as("using extracting()")
          .contains("John")
          .contains("Jane");

    softly.assertThat(names)
          .extracting(new Extractor<Name, String>() {
            @Override
            public String extract(Name input) {
              return input.getFirst();
            }
          })
          .as("using extracting(Extractor)")
          .contains("John")
          .contains("Jane");

    softly.assertThat(names)
          .extracting("first", String.class)
          .as("using extracting(..., Class)")
          .contains("John")
          .contains("Jane");

    softly.assertThat(names)
          .extracting("first", "last")
          .as("using extracting(...)")
          .contains(tuple("John", "Doe"))
          .contains(tuple("Jane", "Doe"));

    softly.assertThat(names)
          .extractingResultOf("getFirst", String.class)
          .as("using extractingResultOf(method, Class)")
          .contains("John")
          .contains("Jane");

    softly.assertThat(names)
          .extractingResultOf("getFirst")
          .as("using extractingResultOf(method)")
          .contains("John")
          .contains("Jane");

    softly.assertAll();
  }

  @Test
  public void should_pass_when_using_extracting_with_iterable() {

    Iterable<Name> names = asList(name("John", "Doe"), name("Jane", "Doe"));

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(names)
            .extracting("first")
            .as("using extracting()")
            .contains("John")
            .contains("Jane");

      softly.assertThat(names)
            .extracting(new Extractor<Name, String>() {
              @Override
              public String extract(Name input) {
                return input.getFirst();
              }
            })
            .as("using extracting(Extractor)")
            .contains("John")
            .contains("Jane");

      softly.assertThat(names)
            .extracting("first", String.class)
            .as("using extracting(..., Class)")
            .contains("John")
            .contains("Jane");

      softly.assertThat(names)
            .extracting("first", "last")
            .as("using extracting(...)")
            .contains(tuple("John", "Doe"))
            .contains(tuple("Jane", "Doe"));

      softly.assertThat(names)
            .extractingResultOf("getFirst", String.class)
            .as("using extractingResultOf(method, Class)")
            .contains("John")
            .contains("Jane");

      softly.assertThat(names)
            .extractingResultOf("getFirst")
            .as("using extractingResultOf(method)")
            .contains("John")
            .contains("Jane");
    }
  }

  @Test
  public void should_pass_when_using_extracting_with_array() {

    Name[] namesAsArray = new Name[] { name("John", "Doe"), name("Jane", "Doe") };

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(namesAsArray)
            .extracting("first")
            .as("using extracting()")
            .contains("John")
            .contains("Jane");

      softly.assertThat(namesAsArray)
            .extracting(new Extractor<Name, String>() {
              @Override
              public String extract(Name input) {
                return input.getFirst();
              }
            })
            .as("using extracting(Extractor)")
            .contains("John")
            .contains("Jane");

      softly.assertThat(namesAsArray)
            .extracting("first", String.class)
            .as("using extracting(..., Class)")
            .contains("John")
            .contains("Jane");

      softly.assertThat(namesAsArray)
            .extracting("first", "last")
            .as("using extracting(...)")
            .contains(tuple("John", "Doe"))
            .contains(tuple("Jane", "Doe"));

      softly.assertThat(namesAsArray)
            .extractingResultOf("getFirst", String.class)
            .as("using extractingResultOf(method, Class)")
            .contains("John")
            .contains("Jane");

      softly.assertThat(namesAsArray)
            .extractingResultOf("getFirst")
            .as("using extractingResultOf(method)")
            .contains("John")
            .contains("Jane");
    }
  }

  @Test
  public void should_pass_when_using_extracting_with_iterator() {

    Iterator<Name> names = asList(name("John", "Doe"), name("Jane", "Doe")).iterator();

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(names)
            .extracting("first")
            .as("using extracting()")
            .contains("John")
            .contains("Jane");
    }
  }

  @Test
  public void should_pass_when_using_flat_extracting() {

    List<CartoonCharacter> characters = asList(homer, fred);

    softly.assertThat(characters)
          .flatExtracting(children())
          .as("using flatExtracting on Iterable")
          .hasSize(4);

    CartoonCharacter[] charactersAsArray = characters.toArray(new CartoonCharacter[characters.size()]);

    softly.assertThat(charactersAsArray)
          .flatExtracting(children())
          .as("using flatExtracting on array")
          .hasSize(4);

    softly.assertAll();
  }

  @Test
  public void should_collect_all_errors_when_using_extracting() {

    List<Name> names = asList(name("John", "Doe"), name("Jane", "Doe"));

    softly.assertThat(names)
          .extracting("first")
          .overridingErrorMessage("error 1")
          .contains("gandalf")
          .overridingErrorMessage("error 2")
          .contains("frodo");

    softly.assertThat(names)
          .extracting("last")
          .overridingErrorMessage("error 3")
          .isEmpty();

    try {
      softly.assertAll();
      shouldHaveThrown(SoftAssertionError.class);
    } catch (SoftAssertionError e) {
      assertThat(e.getErrors()).containsExactly("error 1", "error 2", "error 3");
    }
  }

  @Test
  public void should_collect_all_errors_when_using_flat_extracting() throws Exception {

    List<CartoonCharacter> characters = asList(homer, fred);

    softly.assertThat(characters)
          .flatExtracting(children())
          .overridingErrorMessage("error 1")
          .hasSize(0)
          .overridingErrorMessage("error 2")
          .isEmpty();

    try {
      softly.assertAll();
      shouldHaveThrown(SoftAssertionError.class);
    } catch (SoftAssertionError e) {
      assertThat(e.getErrors()).containsExactly("error 1", "error 2");
    }
  }

  @Test
  public void should_collect_all_errors_when_using_filtering() throws Exception {

    softly.assertThat(asList(homer, fred))
          .filteredOn("name", "Homer Simpson")
          .hasSize(10)
          .isEmpty();

    try {
      softly.assertAll();
      shouldHaveThrown(SoftAssertionError.class);
    } catch (SoftAssertionError e) {
      assertThat(e.getErrors()).containsOnly(format("%nExpected size:<10> but was:<1> in:%n<[CartoonCharacter [name=Homer Simpson]]>"),
                                             format("%nExpecting empty but was:<[CartoonCharacter [name=Homer Simpson]]>"));
    }
  }

  @Test
  public void should_work_with_comparable() throws Exception {
    ComparableExample example1 = new ComparableExample(0);
    ComparableExample example2 = new ComparableExample(0);
    softly.assertThat(example1).isEqualByComparingTo(example2);
    softly.assertAll();
  }

  @Test
  public void should_have_the_same_methods_as_in_bdd_soft_assertions() {
    Method[] assertThatMethods = findMethodsWithName(AbstractStandardSoftAssertions.class, "assertThat");
    Method[] thenMethods = findMethodsWithName(AbstractBDDSoftAssertions.class, "then");

    assertThat(assertThatMethods).usingElementComparator(IGNORING_DECLARING_CLASS_AND_METHOD_NAME)
                                 .containsExactlyInAnyOrder(thenMethods);
  }

  @Test
  public void should_propagate_AssertionError_from_nested_proxied_calls() {
    // the nested proxied call to isNotEmpty() throw an Assertion error that must be propagated to the caller.
    softly.assertThat(asList()).first();
    // it must be caught by softly.assertAll()
    assertThat(softly.errorsCollected()).hasSize(1);
  }

  private static Name name(String first, String last) {
    return new Name(first, last);
  }

  private static ChildrenExtractor children() {
    return new ChildrenExtractor();
  }

  private static class ChildrenExtractor implements Extractor<CartoonCharacter, Collection<CartoonCharacter>> {
    @Override
    public Collection<CartoonCharacter> extract(CartoonCharacter input) {
      return input.getChildren();
    }
  }

  // bug #447

  public class TolkienCharacter {
    String name;
    int age;
  }

  @Test
  public void check_477_bugfix() {
    // GIVEN
    TolkienCharacter frodo = new TolkienCharacter();
    TolkienCharacter samnullGamgee = null;
    TolkienSoftAssertions softly = new TolkienSoftAssertions();
    // WHEN
    softly.assertThat(frodo).hasAge(10); // Expect failure - age will be 0 due to not being initialized.
    softly.assertThat(samnullGamgee).hasAge(11); // Expect failure - argument is null.
    // THEN
    assertThat(softly.errorsCollected()).hasSize(2);
  }

  public static class TolkienCharacterAssert extends AbstractAssert<TolkienCharacterAssert, TolkienCharacter> {

    public TolkienCharacterAssert(TolkienCharacter actual) {
      super(actual, TolkienCharacterAssert.class);
    }

    public static TolkienCharacterAssert assertThat(TolkienCharacter actual) {
      return new TolkienCharacterAssert(actual);
    }

    // 4 - a specific assertion !
    public TolkienCharacterAssert hasName(String name) {
      // check that actual TolkienCharacter we want to make assertions on is not null.
      isNotNull();

      // check condition
      if (!Objects.equals(actual.name, name)) {
        failWithMessage("Expected character's name to be <%s> but was <%s>", name, actual.name);
      }

      // return the current assertion for method chaining
      return this;
    }

    // 4 - another specific assertion !
    public TolkienCharacterAssert hasAge(int age) {
      // check that actual TolkienCharacter we want to make assertions on is not null.
      isNotNull();

      // check condition
      if (actual.age != age) {
        failWithMessage("Expected character's age to be <%s> but was <%s>", age, actual.age);
      }

      // return the current assertion for method chaining
      return this;
    }
  }
  public static class TolkienSoftAssertions extends SoftAssertions {

    public TolkienCharacterAssert assertThat(TolkienCharacter actual) {
      return proxy(TolkienCharacterAssert.class, TolkienCharacter.class, actual);
    }
  }

  @Test
  public void should_return_failure_after_fail() {
    String failureMessage = "Should not reach here";
    softly.fail(failureMessage);
    assertThat(softly.wasSuccess()).isFalse();
    assertThat(softly.errorsCollected()).hasSize(1);
    assertThat(softly.errorsCollected().get(0).getMessage()).isEqualTo(failureMessage);
  }

  @Test
  public void should_return_failure_after_fail_with_parameters() {
    String failureMessage = "Should not reach %s or %s";
    softly.fail(failureMessage, "here", "here");
    assertThat(softly.wasSuccess()).isFalse();
    assertThat(softly.errorsCollected()).hasSize(1);
    assertThat(softly.errorsCollected().get(0).getMessage()).isEqualTo("Should not reach here or here");
  }
  
  @Test
  public void should_return_failure_after_fail_with_throwable() {
    String failureMessage = "Should not reach here";
    IllegalStateException realCause = new IllegalStateException();
    softly.fail(failureMessage, realCause);
    assertThat(softly.wasSuccess()).isFalse();
    assertThat(softly.errorsCollected()).hasSize(1);
    assertThat(softly.errorsCollected().get(0).getMessage()).isEqualTo(failureMessage);
    assertThat(softly.errorsCollected().get(0).getCause()).isEqualTo(realCause);
  }

  @Test
  public void should_return_failure_after_shouldHaveThrown() {
    softly.shouldHaveThrown(IllegalArgumentException.class);
    assertThat(softly.wasSuccess()).isFalse();
    assertThat(softly.errorsCollected()).hasSize(1);
    assertThat(softly.errorsCollected().get(0).getMessage()).isEqualTo("IllegalArgumentException should have been thrown");
  }

  @Test
  public void should_work_with_atomic() throws Exception {
    // simple atomic value
    softly.assertThat(new AtomicBoolean(true)).isTrue();
    softly.assertThat(new AtomicInteger(1)).isGreaterThan(0);
    softly.assertThat(new AtomicLong(1l)).isGreaterThan(0l);
    softly.assertThat(new AtomicReference<String>("abc")).isEqualTo("abc");
    // atomic array value
    softly.assertThat(new AtomicIntegerArray(new int[] { 1, 2, 3 })).containsExactly(1, 2, 3);
    softly.assertThat(new AtomicLongArray(new long[] { 1l, 2l, 3l })).containsExactly(1l, 2l, 3l);
    softly.assertThat(new AtomicReferenceArray<>(array("a", "b", "c"))).containsExactly("a", "b", "c");
    softly.assertAll();
  }
  
}
