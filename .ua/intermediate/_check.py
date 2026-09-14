import json, glob, sys

base = '/home/app/workspaces/frisboo-bank/frisboo-core/.ua/intermediate'
d = json.load(open(base + '/batch-3.json'))
allids = {n['id'] for n in d['nodes']}
others = {}
for f in sorted(glob.glob(base + '/batch-*.json')):
    if f.endswith('batch-3.json'):
        continue
    dd = json.load(open(f))
    ids = {n['id'] for n in dd['nodes']}
    others[f] = ids
missing = []
for nid in sorted(allids):
    found = [f for f, ids in others.items() if nid in ids]
    if not found:
        missing.append(nid)
print('nodes in batch-3 not found elsewhere:', len(missing))
for m in missing:
    print('  ', m)

# Also check which files in batch-3.json are NOT in the parent's 25-file list
parent_files = [
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/domain/valueobjects/CustomerGuid.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/domain/valueobjects/DeviceFingerprint.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/domain/valueobjects/IdempotencyKey.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/domain/valueobjects/PrincipalId.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/domain/valueobjects/PrincipalType.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/domain/valueobjects/TransactionId.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/domain/valueobjects/Username.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/domain/valueobjects/ValueObject.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/serialization/CustomSerializers.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/serialization/JsonSerializer.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/serialization/Serializers.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/serialization/ValueObjectSerializer.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/contracts/ValidationRule.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/contracts/Validator.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/errors/ValidationError.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/model/ValidationResult.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/rules/AccountTypeRule.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/rules/CurrencyRule.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/rules/CustomerIdRule.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/rules/IdempotencyKeyRule.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/rules/PrincipalIdRule.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/rules/PrincipalTypeRule.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/rules/TransactionIdRule.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/rules/UsernameRule.kt',
    'frisboo-core/src/main/kotlin/com/frisboo/corebanking/core/validation/rules/ValueObjectRule.kt',
]
b3files = set()
for n in d['nodes']:
    if n['type'] == 'file':
        b3files.add(n['filePath'])
print()
print('parent files present in batch-3.json:', len([p for p in parent_files if p in b3files]))
print('parent files MISSING from batch-3.json:', [p for p in parent_files if p not in b3files])
print('batch-3.json files NOT in parent list:', sorted(b3files - set(parent_files)))