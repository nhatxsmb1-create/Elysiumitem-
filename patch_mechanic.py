import re

with open(r'src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'r', encoding='utf-8') as f:
    text = f.read()

# Blood Domain damage
old_bd = '''                    target.damage(100.0 * marks, player); // Heavy damage'''
new_bd = '''                    double dmg = 100.0 * marks;
                    try { dmg *= dev.elysium.core.api.CoreAPI.getMetaMultiplier("BLOOD"); } catch(Exception ex) {}
                    target.damage(dmg, player); // Heavy damage'''
text = text.replace(old_bd, new_bd)

# Frost Nova damage
old_fn = '''                target.damage(50.0, player); // Flat frost damage'''
new_fn = '''                double dmg = 50.0;
                try { dmg *= dev.elysium.core.api.CoreAPI.getMetaMultiplier("FROST"); } catch(Exception ex) {}
                target.damage(dmg, player); // Flat frost damage'''
text = text.replace(old_fn, new_fn)

# DAMAGE_PER_BLOOD_MARK
old_dpbm = '''                            double extra = e.getDamage() * (percent * marks);
                            e.setDamage(e.getDamage() + extra);'''
new_dpbm = '''                            double extra = e.getDamage() * (percent * marks);
                            try { extra *= dev.elysium.core.api.CoreAPI.getMetaMultiplier("BLOOD"); } catch(Exception ex) {}
                            e.setDamage(e.getDamage() + extra);'''
text = text.replace(old_dpbm, new_dpbm)

# CONSUME_BLOOD_MARK_ON_DASH
old_cbd = '''                            double boom = 50.0 * marks;
                            e.setDamage(e.getDamage() + boom);'''
new_cbd = '''                            double boom = 50.0 * marks;
                            try { boom *= dev.elysium.core.api.CoreAPI.getMetaMultiplier("BLOOD"); } catch(Exception ex) {}
                            e.setDamage(e.getDamage() + boom);'''
text = text.replace(old_cbd, new_cbd)


with open(r'src/main/java/dev/elysium/item/mechanic/MechanicEngine.java', 'w', encoding='utf-8') as f:
    f.write(text)

print("MechanicEngine patched with Meta Rotation!")