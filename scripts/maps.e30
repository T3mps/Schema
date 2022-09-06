
auto mp = create_map("one", 1, "two", 2, "three", 3);

print(mp.get("one")); // 1

mp.set("one", 1.1);
print(mp.get("one")); // 1.1

auto size = mp.size();
print(size); // 3

mp.put("size", size);
size = mp.size();
print(mp.get("size")); // 3
print(size); // 4

mp.remove(size - 1);
print(mp); // one: 1, two: 2, three: 3

auto mp_clone = create_map(mp);
mp.clear();
print(mp); // []

print(mp_clone.has_key("two")); // true
print(mp_clone.has_value(2)); // true