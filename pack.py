
# -*- coding: utf-8 -*-

import os, re
import codecs

src_dir = './src'
output_file = 'output.java'

files = [f for f in os.listdir(src_dir)]
files = [f for f in files if '.java' in f]
files = [os.path.join(src_dir, f) for f in files]
files = [f for f in files if os.path.isfile(f)]

result = ''
import_lines = []
for file in files:
    print('Processing %s' % file)
    with codecs.open(file, 'r', 'utf-8') as f:
        for line in f.readlines():
            if 'import' in line:
                import_lines.append(line)
            else:
                result += line
        result += '\n'

result = ''.join(import_lines) + result
result = re.sub(r'public (class|interface|enum)', r'\1', result)
result = re.sub(r'class ChainReaction', r'public class ChainReaction', result)

print('Writing result')
with codecs.open(output_file, 'w', 'utf-8') as f:
    f.write(result)

print('Done')

