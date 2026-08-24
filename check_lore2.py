with open("src/main/java/dev/elysium/item/item/ItemManager.java", "r", encoding="utf-8") as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    if "ID:" in line:
        for j in range(max(0, i-5), min(len(lines), i+2)):
            encoded = repr(lines[j]).encode("ascii", "ignore").decode("ascii")
            print(f"Line {j+1}: {encoded}")