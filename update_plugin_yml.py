import re

with open(r'src/main/resources/plugin.yml', 'r', encoding='utf-8') as f:
    text = f.read()

new_command = '''  trangbi:
    description: Mo GUI trang bi phu kien
    usage: /trangbi
    permission: elysium.item.use

  loren:
    description: Mo GUI Lo Ren (Kham Ngoc, Nang Cap)
    usage: /loren
    permission: elysium.item.use'''

text = text.replace('''  trangbi:
    description: Mo GUI trang bi phu kien
    usage: /trangbi
    permission: elysium.item.use''', new_command)

with open(r'src/main/resources/plugin.yml', 'w', encoding='utf-8') as f:
    f.write(text)