use traits;

node D : C {

    d() {
        self.a();
        self.b();
        self.c();
        print("d");
    }
}

auto d = D().d;

d();