package strikt.samples

import org.junit.jupiter.api.Test
import strikt.api.Assertion
import strikt.api.expect
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import strikt.assertions.isUpperCase
import java.time.LocalDate
import java.time.temporal.ChronoUnit.YEARS

internal object AssertionMethods {
  @Test
  fun assert() {
    val subject = Person(
      name = "David",
      birthDate = LocalDate.of(1947, 1, 8)
    )

    // use assert in a test for a one-off assertion
    expect(subject)
      .assert("is older than %d", 21) {
        if (subject.birthDate < LocalDate.now().minus(21, YEARS)) {
          pass()
        } else {
          fail()
        }
      }

    // alternatively define a reusable assertion function
    fun Assertion<Person>.isOlderThan(age: Int) =
      assert("is older than %d", age) {
        if (subject.birthDate < LocalDate.now().minus(age.toLong(), YEARS)) {
          pass()
        } else {
          fail()
        }
      }

    // which can then be used just like standard assertions
    expect(subject).isOlderThan(21)
  }

  @Test
  fun map() {
    val subject = Person(
      name = "David",
      birthDate = LocalDate.of(1947, 1, 8)
    )

    expect(subject) {
      // map based on properties...
      map { name }
        .isEqualTo("David")
        // ... or methods
        .map { toUpperCase() }
        .isEqualTo("DAVID")
      map { birthDate }
        .map { year }
        .isEqualTo(1947)
    }

    expect(subject)
      // map using a property reference
      .map(Person::name)
      .isEqualTo("David")
  }

  @Test
  fun not() {
    val subject: String? = "fnord"
    expect(subject)
      .not()
      .isNull()
      .isUpperCase()
  }
}
