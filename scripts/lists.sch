
auto ls = create_list("Hello", "World", "Ethan", 1, 2, 3, 4, 5);

print(ls.get(1)); // World

ls.set(1, "Hello");
print(ls.get(1)); // Hello

auto size = ls.size();
print(size); // 8

ls.add(size);
size = ls.size();
print(ls.get(8)); // 8
print(size); // 9

ls.remove(size - 1);
print(ls); // Hello, World, Ethan, 1, 2, 3, 4, 5

auto ls_clone = create_list(ls);
ls.clear();
print(ls); // []

print(ls_clone.has("Ethan")); // true

auto slc = ls_clone.slice(2, 5);
print(slc); // Ethan, 1, 2

for (auto i = 0; i < slc.size(); i = i + 1) {
    print(slc.get(i));
}

auto list_user = create_list(scan());
print(list_user); // ...