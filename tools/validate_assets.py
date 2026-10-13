import json
import os
import re
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
MAIN = os.path.join(ROOT, "src", "main", "resources", "assets", "cultivar")
GEN = os.path.join(ROOT, "src", "generated", "resources", "assets", "cultivar")
JAVA = os.path.join(ROOT, "src", "main", "java", "com", "cultivar")

problems = []


def exists(kind, rel):
    return any(os.path.exists(os.path.join(base, kind, rel)) for base in (MAIN, GEN))


def resolve_model(ref):
    ns, path = ref.split(":", 1) if ":" in ref else ("minecraft", ref)
    return ns, path


models = {}
for base in (MAIN, GEN):
    folder = os.path.join(base, "models")
    for dirpath, _, files in os.walk(folder):
        for f in files:
            if f.endswith(".json"):
                rel = os.path.relpath(os.path.join(dirpath, f), folder).replace("\\", "/")[:-5]
                with open(os.path.join(dirpath, f), encoding="utf-8") as fh:
                    models["cultivar:" + rel] = json.load(fh)

for name, model in models.items():
    for slot, tex in model.get("textures", {}).items():
        if isinstance(tex, dict):
            tex = tex.get("sprite", "")
        if tex.startswith("#"):
            continue
        ns, path = resolve_model(tex)
        if ns == "cultivar" and not exists("textures", path + ".png"):
            problems.append(f"model {name}: missing texture {tex}")
    parent = model.get("parent")
    if parent:
        ns, path = resolve_model(parent)
        if ns == "cultivar" and f"cultivar:{path}" not in models:
            problems.append(f"model {name}: missing parent {parent}")

for base in (GEN, MAIN):
    folder = os.path.join(base, "blockstates")
    if not os.path.isdir(folder):
        continue
    for f in os.listdir(folder):
        text = open(os.path.join(folder, f), encoding="utf-8").read()
        for ref in re.findall(r'"model":\s*"([^"]+)"', text):
            ns, path = resolve_model(ref)
            if ns == "cultivar" and f"cultivar:{path}" not in models:
                problems.append(f"blockstate {f}: missing model {ref}")

items_dir = os.path.join(GEN, "items")
for f in os.listdir(items_dir):
    text = open(os.path.join(items_dir, f), encoding="utf-8").read()
    for ref in re.findall(r'"model":\s*"([^"]+)"', text):
        ns, path = resolve_model(ref)
        if ns == "cultivar" and f"cultivar:{path}" not in models:
            problems.append(f"item {f}: missing model {ref}")

particles_dir = os.path.join(GEN, "particles")
for f in os.listdir(particles_dir):
    data = json.load(open(os.path.join(particles_dir, f), encoding="utf-8"))
    for tex in data.get("textures", []):
        ns, path = resolve_model(tex)
        if ns == "cultivar" and not exists("textures", "particle/" + path + ".png"):
            problems.append(f"particle {f}: missing sprite {tex}")

for java in ("client/render/MachineEntityRenderer.java", "client/ClientRegistration.java", "client/render/WaterJetRenderer.java"):
    text = open(os.path.join(JAVA, java), encoding="utf-8").read()
    for name in re.findall(r'texture\("([a-z_]+)"\)', text) + re.findall(r'textures/entity/([a-z_]+)\.png', text):
        if not exists("textures", "entity/" + name + ".png"):
            problems.append(f"{java}: missing entity texture {name}")

lang = json.load(open(os.path.join(GEN, "lang", "en_us.json"), encoding="utf-8"))
registered = []
for java in ("registry/ModBlocks.java", "registry/ModItems.java", "registry/ModEntities.java"):
    text = open(os.path.join(JAVA, java), encoding="utf-8").read()
    kind = "block" if "Blocks" in java else "item" if "Items" in java else "entity"
    for name in re.findall(r'register(?:Block|Item|EntityType|SimpleItem)\(\s*"([a-z_]+)"', text):
        registered.append((kind, name))
for kind, name in registered:
    if f"{kind}.cultivar.{name}" not in lang:
        problems.append(f"lang: missing {kind}.cultivar.{name}")

for f in os.listdir(os.path.join(GEN, "blockstates")):
    name = f[:-5]
    if f"block.cultivar.{name}" not in lang:
        problems.append(f"lang: missing block.cultivar.{name}")
for f in os.listdir(items_dir):
    name = f[:-5]
    if f"item.cultivar.{name}" not in lang and f"block.cultivar.{name}" not in lang:
        problems.append(f"lang: missing item.cultivar.{name}")

if problems:
    print("\n".join(problems))
    print(f"{len(problems)} problem(s)")
    sys.exit(1)
print(f"OK: {len(models)} models, {len(os.listdir(items_dir))} item definitions, {len(lang)} translations, no missing assets")
