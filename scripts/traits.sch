use useless;

trait A {
  
  a() {
    print("a");
  }
}

trait B1 {

  b1() {
    print("b1");
  }
}

trait B2 {

  b2() {
    print("b2");
  }
}

trait B with B1, B2 {

  b() {
    self.b1();
    self.b2();
  }
}

node C with A, B, Useless {

  c() {
    print("c");
  }
}