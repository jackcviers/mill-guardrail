package com.jackcviers.mill.guardrail

class MySuite extends munit.FunSuite {
  test("hello") {
    assertEquals(Hello.msg, "Hello World!")
  }
}

