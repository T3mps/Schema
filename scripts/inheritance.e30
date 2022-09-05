
node Doughnut {
  define() {
    self.tastyness = 10;
  }

  cook() {
    print("cooking");
  }
  eat() {
    print("eating... yum!");
  }
}

node BostonCream : Doughnut {
  action() {
    parent.cook();
    parent.eat();
    print("This is a " + self.tastyness + " out of 10!");
  }
}

BostonCream().action();